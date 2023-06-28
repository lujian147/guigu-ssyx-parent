package com.atguigu.ssyx.activity.service.impl;


import com.atguigu.ssyx.activity.mapper.CouponInfoMapper;
import com.atguigu.ssyx.activity.mapper.CouponRangeMapper;
import com.atguigu.ssyx.activity.mapper.CouponUseMapper;
import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.activity.service.CouponRangeService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.enums.CouponRangeType;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.activity.CouponRange;
import com.atguigu.ssyx.model.activity.CouponUse;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券信息 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-16
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponRangeMapper couponRangeMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CouponUseMapper couponUseMapper;


    //2.根据skuId和userId获取优惠卷信息
    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId, Long userId) {
        //1.根据skuId获取到sku信息  远程调用
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        //2.根据条件查询 skuId，分类id,userId
        List<CouponInfo> couponInfoList = baseMapper.findCouponInfoList(skuId,skuInfo.getCategoryId(),userId);
        return couponInfoList;
    }

    //3.获取购物车里面可用的优惠卷列表
    @Override
    public List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId) {
        //1.根据userId获取用户全部优惠卷
        // coupon_user coupon_info
        List<CouponInfo> userAllCouponInfoList = baseMapper.selectCartCouponInfoList(userId);
        //2.从第一步返回list集合中，获取所有优惠卷id列表
        if (CollectionUtils.isEmpty(userAllCouponInfoList)){
            return new ArrayList<CouponInfo>();
        }
        List<Long> couponIdList = userAllCouponInfoList.stream()
                .map(couponInfo -> couponInfo.getId())
                .collect(Collectors.toList());
        //3.查询优惠卷对应的范围 coupon_range
        //couponRangeList
        LambdaQueryWrapper<CouponRange> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CouponRange::getCouponId,couponIdList);
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(queryWrapper);
        //4.获取优惠卷id对应的skuId列表
        //优惠卷id进行分组,得到map集合
        //Map<Long,List<Long>>
        Map<Long,List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList,couponRangeList);

        //5.遍历全部优惠卷集合，判断优惠卷类型
        //全场通用，sku和分类
        BigDecimal reduceAmount = new BigDecimal(0);
        CouponInfo optimalCouponInfo = null;
        for (CouponInfo couponInfo:userAllCouponInfoList) {
            //全场通用
            if (CouponRangeType.ALL == couponInfo.getRangeType()){
                //全场通用
                //判断是否满足优惠使用门槛
                //计算购物车商品的总价
                BigDecimal totalAmount = computeTotalAmount(cartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }else {
                List<Long> skuIdList = couponIdToSkuIdMap.get(couponInfo.getId());
                //满足符合适用范围的购物项
                List<CartInfo> currentCartInfoList = cartInfoList.stream()
                        .filter(cartInfo -> skuIdList.contains(cartInfo.getSkuId()))
                        .collect(Collectors.toList());
                BigDecimal totalAmount = computeTotalAmount(cartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }
            if (couponInfo.getIsSelect().intValue() == 1 && couponInfo.getAmount().subtract(reduceAmount).doubleValue() > 0) {
                reduceAmount = couponInfo.getAmount();
                optimalCouponInfo = couponInfo;
            }
        }
        if(null != optimalCouponInfo) {
            optimalCouponInfo.setIsOptimal(1);
        }
        //6.返回List<CouponInfo>
        return userAllCouponInfoList;
    }


    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }
    //4.获取优惠卷id对应的skuId列表
    //优惠卷id进行分组,得到map集合

    private Map<Long, List<Long>> findCouponIdToSkuIdMap(List<CartInfo> cartInfoList, List<CouponRange> couponRangeList) {
        Map<Long, List<Long>> couponIdToSkuIdMap = new HashMap<>();
         //couponRangeList数据处理，根据优惠卷id分组
        Map<Long, List<CouponRange>> couponRangeToRangeListMap = couponRangeList.stream().
                collect(Collectors.groupingBy(couponRange -> couponRange.getCouponId()));
        //遍历map集合
        Iterator<Map.Entry<Long, List<CouponRange>>> iterator = couponRangeToRangeListMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Long, List<CouponRange>> entry = iterator.next();
            Long couponId = entry.getKey();
            List<CouponRange> rangeList = entry.getValue();

            //创建集合set
            Set<Long> skuIdSet = new HashSet<>();
            for (CartInfo cartInfo:cartInfoList) {
                for (CouponRange couponRange:rangeList) {
                    //判断
                    if (couponRange.getRangeType() == CouponRangeType.SKU
                    && couponRange.getRangeId().longValue() == cartInfo.getSkuId()){
                        skuIdSet.add(cartInfo.getSkuId());
                    }else if (couponRange.getRangeType() == CouponRangeType.CATEGORY
                    && couponRange.getRangeId().longValue() == cartInfo.getCategoryId()){
                        skuIdSet.add(cartInfo.getSkuId());
                    }
                }
            }
            couponIdToSkuIdMap.put(couponId,new ArrayList<>(skuIdSet));
        }
        return couponIdToSkuIdMap;
    }

    @Override
    public IPage<CouponInfo> selectPageCouponInfo(Page<CouponInfo> couponInfoPage) {
//        //  构造排序条件
//        LambdaQueryWrapper<CouponInfo> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.orderByDesc("id");
        Page<CouponInfo> infoPage = baseMapper.selectPage(couponInfoPage, null);
        List<CouponInfo> couponInfoList = infoPage.getRecords();
        couponInfoList.stream().forEach(item ->{
            item.setCouponTypeString(item.getCouponType().getComment());
            if (item.getRangeType() != null){
                item.setRangeTypeString(item.getRangeType().getComment());
            }
        });
        return infoPage;
    }

//    获取优惠卷信息
    @Override
    public CouponInfo getCouponInfo(Long id) {
        CouponInfo couponInfo = baseMapper.selectById(id);
        couponInfo.setCouponTypeString(couponInfo.getCouponType().getComment());
        if (couponInfo.getRangeType() != null){
            couponInfo.setRangeTypeString(couponInfo.getRangeType().getComment());
        }
        return couponInfo;
    }

    @Override
    public Map<String, Object> findCouponRuleList(Long id) {
        //第一步 根据优惠卷id查询优惠卷基本信息，coupon_info表
        CouponInfo couponInfo = baseMapper.selectById(id);
        //第二步 根据优惠卷id查询coupon_range 查询里面的range_id
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(
                new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, id)
        );
        //couponRangeList获取所有range_id
        List<Long> rangeIdList = couponRangeList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());
        Map<String,Object> result = new HashMap<>();
        //第三步 分类判断封装不同数据
        if (!CollectionUtils.isEmpty(couponRangeList)){
            if (couponInfo.getRangeType() == CouponRangeType.SKU){
                //如果规则类型为SKU  range_id就是skuId值,远程调用根据多个skuId值获取对应sku信息
                List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(rangeIdList);
                result.put("skuInfoList",skuInfoList);
            }else if (couponInfo.getRangeType() == CouponRangeType.CATEGORY){
                //如果规则类型为CATEGORY range_id就是分类id,远程调用根据多个分类id获取对应的分类信息
                List<Category> categorylList= productFeignClient.findCategoryList(rangeIdList);
                result.put("categoryList",categorylList);
            }
        }


        return result;
    }

    @Override
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
        //根据优惠卷id删除之前的优惠卷数据
        Long couponId = couponRuleVo.getCouponId();
        //删除CouponRange表里面的数据
        couponRangeMapper.deleteById(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId,couponId));
        //删除CouponUse表里面的数据
//        couponUseMapper.deleteById(new LambdaQueryWrapper<CouponUse>().eq(CouponUse::getCouponId,couponId));

        //更新
        CouponInfo couponInfo = baseMapper.selectById(couponId);
        couponInfo.setRangeType(couponRuleVo.getRangeType());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setAmount(couponRuleVo.getAmount());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());
        baseMapper.updateById(couponInfo);

        //获取规则范围数据进行添加
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange:couponRangeList) {
            couponRange.setCouponId(couponId);
//            couponRange.setRangeType(couponInfo.getRangeType());
            couponRangeMapper.insert(couponRange);
        }


    }

}
