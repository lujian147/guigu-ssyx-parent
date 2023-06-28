package com.atguigu.ssyx.cart.service;

import com.atguigu.ssyx.model.order.CartInfo;

import java.util.List;

public interface CartInfoService {
    //添加商品到购物车
    //添加内容：当前登录用户id,商品id,商品数量num
    void addToCart(Long userId, Long skuId, Integer skuNum);

    //根据skuId删除购物车
    void deleteCart(Long skuId, Long userId);

    //清空购物车
    void deleteAllCart(Long userId);

    //批量删除购物车 skuId
    void batchDeleteCart(Long userId, List<Long> skuIdList);


    //购物车列表
    List<CartInfo> getCartList(Long userId);

    //根据skuId选中
    void checkCart(Long userId, Long skuId, Integer isChecked);

    //全选
    void checkAllCart(Long userId, Integer isChecked);

    //批量选中
    void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked);
}
