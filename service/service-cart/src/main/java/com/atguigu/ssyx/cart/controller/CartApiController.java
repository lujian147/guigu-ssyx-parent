package com.atguigu.ssyx.cart.controller;

import com.atguigu.ssyx.cart.service.CartInfoService;
import com.atguigu.ssyx.client.activity.ActivityFeignClient;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(value = "购物车接口")
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartInfoService cartInfoService;

    @Autowired
    private ActivityFeignClient activityFeignClient;


    //根据skuId选中
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable(value = "skuId") Long skuId,
                            @PathVariable Integer isChecked){
        //获取用户id
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.checkCart(userId,skuId,isChecked);
        return Result.ok(null);
    }
    //全选
    @GetMapping("checkAllCart/{isChecked}")
    public Result checkAllCart(@PathVariable(value = "isChecked") Integer isChecked) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        // 调用更新方法
        cartInfoService.checkAllCart(userId, isChecked);
        return Result.ok(null);
    }
    //批量选中
    @ApiOperation(value="批量选择购物车")
    @PostMapping("batchCheckCart/{isChecked}")
    public Result batchCheckCart(@RequestBody List<Long> skuIdList,
                                 @PathVariable(value = "isChecked") Integer isChecked){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchCheckCart(skuIdList, userId, isChecked);
        return Result.ok(null);
    }
    //添加商品到购物车
    //添加内容：当前登录用户id,商品id,商品数量num
    @ApiOperation("添加商品到购物车")
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum){
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.addToCart(userId,skuId,skuNum);
        return Result.ok(null);
    }


    //根据skuId删除购物车
    @ApiOperation("根据skuId删除购物车")
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteCart(skuId,userId);
        return Result.ok(null);
    }

    //清空购物车
    @ApiOperation("清空购物车")
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok(null);
    }
    //批量删除购物车 skuId
    @ApiOperation("批量删除购物车")
    @DeleteMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchDeleteCart(userId,skuIdList);
        return Result.ok(null);
    }


    //购物车列表
    @ApiOperation("购物车列表")
    @GetMapping("cartList")
    public Result getCartList(){
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        return Result.ok(cartInfoList);
    }

    //带优惠卷的购物车
    @ApiOperation("带优惠卷的购物车")
    @GetMapping("activityCartList")
    public Result activityCartList() {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);

        OrderConfirmVo orderTradeVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        return Result.ok(orderTradeVo);
    }


    //获取当前用户购物车选中购物项
    @ApiOperation("获取当前用户购物车选中购物项")
    @GetMapping("inner/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId){
        List<CartInfo> cartInfoList = cartInfoService.getCartCheckedList(userId);
        return cartInfoList;
    }
}
