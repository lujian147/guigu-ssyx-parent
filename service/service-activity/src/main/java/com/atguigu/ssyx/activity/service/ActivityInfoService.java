package com.atguigu.ssyx.activity.service;


import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.activity.ActivityRule;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.ActivityRuleVo;
import com.atguigu.ssyx.vo.order.CartInfoVo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-16
 */
public interface ActivityInfoService extends IService<ActivityInfo> {

    IPage<ActivityInfo> selectPage(Page<ActivityInfo> activityInfoPage);

    //获取活动规则
    Map<String, Object> findActivityRuleList(Long activityId);
//    在活动里面添加规则数据
    void saveActivityRule(ActivityRuleVo activityRuleVo);

    //根据关键字查询匹配sku信息
    List<SkuInfo> findSkuInfoByKeyword(String keyword);
    //    根据skuId列表获取促销信息
    Map<Long, List<String>> findActivity(List<Long> skuIdList);

    //    根据skuId获取促销与优惠券信息
    Map<String, Object> findActivityAndCoupon(Long skuId, Long userId);


    //根据skuId获取规则数据
    List<ActivityRule> findActivityRuleBySkuId(Long skuId);

    //获取购物车中满足条件的优惠卷和信息
    OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);


    //获取购物车对应规则数据
    List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList);
}
