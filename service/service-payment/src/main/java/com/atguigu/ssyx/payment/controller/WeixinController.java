package com.atguigu.ssyx.payment.controller;

import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.enums.PaymentType;
import com.atguigu.ssyx.payment.service.PaymentInfoService;
import com.atguigu.ssyx.payment.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <p>
 * 微信支付 API
 * </p>
 */
@Api(tags = "微信支付接口")
@RestController
@RequestMapping("/api/payment/weixin")
@Slf4j
public class WeixinController {

    @Autowired
    private WeixinService weixinPayService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    //调用微信支付系统生成预支付单
    @ApiOperation(value = "下单 小程序支付")
    @GetMapping("/createJsapi/{orderNo}")
    public Result createJsapi(@PathVariable String orderNo) {
       Map<String,String> map = weixinPayService.createJsapi(orderNo);
        return Result.ok(map);
    }

    //查询订单支付状态
    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderNo}")
    public Result queryPayStatus(
            @ApiParam(name = "orderNo", value = "订单No", required = true)
            @PathVariable("orderNo") String orderNo){
        //1.调用微信支付系统接口，查询支付状态
        Map<String,String> resultMap= weixinPayService.queryPayStatus(orderNo);
        //2.微信支付系统返回值为空，支付失败
        if (resultMap == null){
            return Result.build(null,ResultCodeEnum.PAYMENT_FAIL);
        }
        //3.如果微信系统返回值，判断支付成功
        //3.1支付成功，修改支付记录表状态，已支付
        //3.2支付成功，修改订单记录已经支付，扣减库存
        if ("SUCCESS".equals(resultMap.get("trade_state"))){
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(out_trade_no, resultMap);
            return Result.ok(null);
        }
        //支付中，等待
        return Result.build(null,ResultCodeEnum.PAYMENT_WAITING);
    }

}
