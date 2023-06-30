package com.atguigu.ssyx.payment.service;

import com.atguigu.ssyx.enums.PaymentType;
import com.atguigu.ssyx.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface PaymentInfoService extends IService<PaymentInfo> {
    PaymentInfo getPaymentInfoByOrderNo(String orderNo);

    PaymentInfo savePaymentInfo(String orderNo);

    //3.1支付成功，修改支付记录表状态，已支付
    //3.2支付成功，修改订单记录已经支付，扣减库存
    void paySuccess(String orderNo, Map<String, String> resultMap);
}
