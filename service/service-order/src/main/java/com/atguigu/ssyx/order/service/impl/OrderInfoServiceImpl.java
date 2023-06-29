package com.atguigu.ssyx.order.service.impl;

import com.atguigu.ssyx.model.order.OrderInfo;
import com.atguigu.ssyx.order.mapper.OrderInfoMapper;
import com.atguigu.ssyx.order.service.OrderInfoService;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import com.atguigu.ssyx.vo.order.OrderSubmitVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

//    确认订单
    @Override
    public OrderConfirmVo confirmOrder() {
        return null;
    }

//    生成订单
    @Override
    public Long submitOrder(OrderSubmitVo orderParamVo) {
        return null;
    }

//    获取订单详情
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        return null;
    }
}
