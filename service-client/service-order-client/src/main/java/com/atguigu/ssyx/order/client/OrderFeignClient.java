package com.atguigu.ssyx.order.client;

import com.atguigu.ssyx.model.order.OrderInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-order")
public interface OrderFeignClient {


    //根据orderNo查询订单信息
    @ApiOperation("根据orderNo查询订单信息")
    @GetMapping("/api/order/inner/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable("orderNo") String orderNo);
}
