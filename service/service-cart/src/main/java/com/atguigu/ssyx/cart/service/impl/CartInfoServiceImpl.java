package com.atguigu.ssyx.cart.service.impl;

import com.atguigu.ssyx.cart.service.CartInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.constant.RedisConst;
import com.atguigu.ssyx.common.exception.SsyxException;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.enums.SkuType;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//添加商品到购物车
//添加内容：当前登录用户id,商品id,商品数量num
@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    //返回购物车在redis中的key
    private String getCartKey(Long userId){
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
    @Override
    public void addToCart(Long userId, Long skuId, Integer skuNum) {
        //1.因为购物车数据存储到redis里面
        //从redis里面根据key获取数据，key里面包含userId
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String, CartInfo> hashOperations =
                redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = null;
        //2.根据查询出来的接口进行判断：得到的是skuId和skuNum
        //判断是否是第一次添加到购物车中
        if (hashOperations.hasKey(skuId.toString())){
            //3.如果结果中包含skuId,则不是第一次添加
            //3.1根据skuId获取商品数量，进行更新
            cartInfo = hashOperations.get(skuId.toString());
            //把购物车存在之前数量进行更新
            Integer currentSkuNum = cartInfo.getSkuNum() + skuNum;
            if (currentSkuNum < 1){
                return;
            }
            //更新cartInfo对象
            cartInfo.setSkuNum(currentSkuNum);
            cartInfo.setCurrentBuyNum(currentSkuNum);
            //判断购买商品数量不能大于限购数量
            Integer perLimit = cartInfo.getPerLimit();
            if (currentSkuNum > perLimit){
                throw new SsyxException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            //更新其他的值
            cartInfo.setIsChecked(1);//默认选中
            cartInfo.setUpdateTime(new Date());
        }else {
            //4.如果不包含skuId，就不是第一次添加
            //4.1.直接进行添加
            skuNum = 1;
            //通过远程调用，获取sku信息
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            //封装cartInfo对象
            cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }
        //5.更新redis里面的缓存
        hashOperations.put(skuId.toString(),cartInfo);
        //6.设置有效时间
        setCartKeyExpire(cartKey);
    }

    //根据skuId删除购物车
    @Override
    public void deleteCart(Long skuId, Long userId) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        if (hashOperations.hasKey(skuId.toString())){
            hashOperations.delete(skuId.toString());
        }
    }

    //清空购物车
    @Override
    public void deleteAllCart(Long userId) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        List<CartInfo> cartInfoList = hashOperations.values();
        for (CartInfo cartInfo:cartInfoList) {
            hashOperations.delete(cartInfo.getSkuId().toString());
        }
    }

    //批量删除购物车 skuId
    @Override
    public void batchDeleteCart(Long userId, List<Long> skuIdList) {
        BoundHashOperations hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        for (Long skuId:skuIdList) {
            hashOperations.delete(skuId.toString());
        }
    }

    //购物车列表
    @Override
    public List<CartInfo> getCartList(Long userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(userId)){
            return cartInfoList;
        }
        //从redis中获取数据
        BoundHashOperations hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        cartInfoList = hashOperations.values();
        if (!CollectionUtils.isEmpty(cartInfoList)){
            //根据商品添加时间进行降序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getCreateTime().compareTo(o2.getCreateTime());
                }
            });
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(Long userId, Long skuId, Integer isChecked) {
        //获取redis中的key
        String cartKey = this.getCartKey(userId);
        //根据cartKey获得key-value
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        //根据field(skuId)获取value(cartInfo)
        CartInfo cartInfo = boundHashOperations.get(skuId.toString());
        if (cartInfo != null){
            cartInfo.setIsChecked(isChecked);
            //更新
            boundHashOperations.put(skuId.toString(),cartInfo);
            //设置key过期时间
            this.setCartKeyExpire(cartKey);
        }
    }

    //全选
    @Override
    public void checkAllCart(Long userId, Integer isChecked) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = boundHashOperations.values();
        cartInfoList.forEach(cartInfo -> {
            cartInfo.setIsChecked(isChecked);
            boundHashOperations.put(cartInfo.getSkuId().toString(),cartInfo);
        });
        this.setCartKeyExpire(cartKey);
    }

    //批量选中
    @Override
    public void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            CartInfo cartInfo = boundHashOperations.get(skuId.toString());
            cartInfo.setIsChecked(isChecked);
            boundHashOperations.put(cartInfo.getSkuId().toString(),cartInfo);
        });
        this.setCartKeyExpire(cartKey);
    }

    //获取当前用户购物车选中购物项
    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = boundHashOperations.values();
        //遍历
        List<CartInfo> cartInfoListNew = cartInfoList.stream().filter(cartInfo -> {
            return cartInfo.getIsChecked().intValue() == 1;
        }).collect(Collectors.toList());

        return cartInfoListNew;
    }


    //根据userId删除选中购物车记录
    @Override
    public void deleteCartChecked(Long userId) {
        //根据userId查询选中购物车记录
        List<CartInfo> cartCheckedList = this.getCartCheckedList(userId);
        //查询list集合遍历，得到skuIdList
        List<Long> skuIdList = cartCheckedList.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
        //构建redis得key值  hash类型
        String cartKey = this.getCartKey(userId);
        //根据key查询filed-value结构
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        //根据filed值删除redis中得记录
        skuIdList.forEach(skuId ->{
            boundHashOperations.delete(skuId.toString());
        });

    }

    //设置key过期时间
    private void setCartKeyExpire(String key){
        redisTemplate.expire(key,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }


}
