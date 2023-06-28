package com.atguigu.ssyx.activity.service;


import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.vo.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 优惠券信息 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-16
 */
public interface CouponInfoService extends IService<CouponInfo> {
//    分页查询优惠卷
    IPage<CouponInfo> selectPageCouponInfo(Page<CouponInfo> couponInfoPage);

//    获取优惠卷信息
    CouponInfo getCouponInfo(Long id);
//    获取优惠卷信息
    Map<String, Object> findCouponRuleList(Long id);

    //添加优惠卷规则数据
    void saveCouponRule(CouponRuleVo couponRuleVo);

    //2.根据skuId和userId获取优惠卷信息
    List<CouponInfo> findCouponInfoList(Long skuId, Long userId);
}
