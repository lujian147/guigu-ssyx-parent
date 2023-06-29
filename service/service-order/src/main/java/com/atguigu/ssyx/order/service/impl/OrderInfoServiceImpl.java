package com.atguigu.ssyx.order.service.impl;

import com.atguigu.ssyx.client.activity.ActivityFeignClient;
import com.atguigu.ssyx.client.cart.CartFeignClient;
import com.atguigu.ssyx.client.user.UserFeignClient;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.constant.RedisConst;
import com.atguigu.ssyx.common.exception.SsyxException;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.enums.SkuType;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.order.OrderInfo;
import com.atguigu.ssyx.order.mapper.OrderInfoMapper;
import com.atguigu.ssyx.order.service.OrderInfoService;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import com.atguigu.ssyx.vo.order.OrderSubmitVo;
import com.atguigu.ssyx.vo.product.SkuStockLockVo;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.j2objc.annotations.AutoreleasePool;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-29
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ActivityFeignClient activityFeignClient;
//    确认订单
    @Override
    public OrderConfirmVo confirmOrder() {
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId();
        //获取用户对应得团长信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        //获取购物车里面选中得商品
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        //唯一标识订单
        String orderNo = System.currentTimeMillis() + "";
        redisTemplate.opsForValue().set(RedisConst.ORDER_REPEAT + orderNo,orderNo,24, TimeUnit.HOURS);
        //获取购物车满足条件活动和优惠卷信息
        OrderConfirmVo orderConfirmVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        //封装
        orderConfirmVo.setLeaderAddressVo(leaderAddressVo);
        orderConfirmVo.setOrderNo(orderNo);
        return orderConfirmVo;
    }

//    生成订单
    @Override
    public Long submitOrder(OrderSubmitVo orderParamVo) {
        //1. 设置给那个用户生成订单 设置orderParamVo得userId值
        Long userId = AuthContextHolder.getUserId();
        orderParamVo.setUserId(userId);
        //. 订单不能重复提交，重复提交判断
        //3. 通过redis + lua脚本判断
        ////lua脚本 保证原子性操作
        //3.1 获取传递过来得订单 orderNo
        String orderNo = orderParamVo.getOrderNo();
        if (StringUtils.isEmpty(orderNo)){
            throw new SsyxException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        //3.2 拿着orderNo到redis进行查询
        String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        //3.3 如果redis有相同得orderNo,表示正常提交 ，把redis中得orderNo删除
        Boolean flag = (Boolean) redisTemplate.execute(new DefaultRedisScript(script, Boolean.class),
                Arrays.asList(RedisConst.ORDER_REPEAT + orderNo), orderNo);
        //3.4 如果redis没有相同得orderNo，表示重复提交了，不能再往后进行
        if (!flag){
            throw new SsyxException(ResultCodeEnum.REPEAT_SUBMIT);
        }
        //4 验证库存 并且锁定库存
        // 验证库存，查询仓库里面是否有充足得西红柿
        // 库存充足，库存锁定（目前并没有减库存）
        //4.1 获取当前用户购物车商品（选中得购物项) 远程调用service-cart模块
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        //4.2 购物车有很多商品，商品不同类型，重点处理普通类型商品
        List<CartInfo> commmonSkuList = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getSkuType() == SkuType.COMMON.getCode())
                .collect(Collectors.toList());
        //4.3 把获取购物车里面普通类型商品list集合，转换为List<SkuStockLockVo>
        if (!CollectionUtils.isEmpty(commmonSkuList)){
            List<SkuStockLockVo> commonStockLockVoList = commmonSkuList.stream().map(item -> {
                SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
                skuStockLockVo.setSkuId(item.getSkuId());
                skuStockLockVo.setSkuNum(item.getSkuNum());
                return skuStockLockVo;
            }).collect(Collectors.toList());
        }
        //4.4 远程调用service-product模块实现商品锁定
        ////验证库存并锁定库存，保证具备原子性


        //5. 下单过程
        //5.1 向两张表中添加数据 order_info 和order_item

        //6. 返回订单id
        return null;
    }

//    获取订单详情
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        return null;
    }
}
