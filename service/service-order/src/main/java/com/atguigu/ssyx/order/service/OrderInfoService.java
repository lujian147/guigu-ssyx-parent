package com.atguigu.ssyx.order.service;

import com.atguigu.ssyx.model.order.OrderInfo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import com.atguigu.ssyx.vo.order.OrderSubmitVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-29
 */
public interface OrderInfoService extends IService<OrderInfo> {


//    确认订单
    OrderConfirmVo confirmOrder();

//    生成订单
    Long submitOrder(OrderSubmitVo orderParamVo);

//    获取订单详情
    OrderInfo getOrderInfoById(Long orderId);

    //根据orderNo查询订单信息
    OrderInfo getOrderInfoByOrderNo(String orderNo);
}
