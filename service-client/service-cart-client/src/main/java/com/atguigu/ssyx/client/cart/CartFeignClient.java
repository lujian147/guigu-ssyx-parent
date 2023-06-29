package com.atguigu.ssyx.client.cart;

import com.atguigu.ssyx.model.order.CartInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("service-cart")
public interface CartFeignClient {


    //获取当前用户购物车选中购物项
    @ApiOperation("获取当前用户购物车选中购物项")
    @GetMapping("/api/cart/inner/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId);
}
