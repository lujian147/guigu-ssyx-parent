package com.atguigu.ssyx.activity.service.impl;


import com.atguigu.ssyx.activity.mapper.ActivityInfoMapper;
import com.atguigu.ssyx.activity.mapper.ActivityRuleMapper;
import com.atguigu.ssyx.activity.mapper.ActivitySkuMapper;
import com.atguigu.ssyx.activity.service.ActivityInfoService;
import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.enums.ActivityType;
import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.activity.ActivityRule;
import com.atguigu.ssyx.model.activity.ActivitySku;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.ActivityRuleVo;
import com.atguigu.ssyx.vo.order.CartInfoVo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-16
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    @Autowired
    private ActivityRuleMapper activityRuleMapper;

    @Autowired
    private ActivitySkuMapper activitySkuMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CouponInfoService couponInfoService;

    //    根据skuId获取促销与优惠券信息
    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        Map<String,Object> result = new HashMap<>();
        //1.根据skuId获取sku营销活动，一个活动有多个规则
        List<ActivityRule> activityRuleList = this.findActivityRuleBySkuId(skuId);
        //2.根据skuId和userId获取优惠卷信息
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfoList(skuId,userId);
//        3.封装数据到map集合中
        result.put("couponInfoList",couponInfoList);
        result.put("activityRuleList",activityRuleList);
        return result;
    }

    @Override
    public List<ActivityRule> findActivityRuleBySkuId(Long skuId) {
        List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);
        for (ActivityRule activityRule :activityRuleList) {
            String ruleDesc = this.getRuleDesc(activityRule);
            activityRule.setRuleDesc(ruleDesc);
        }
        return activityRuleList;
    }

    //获取购物车中满足条件的优惠卷和信息
    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        //1.获取购物车中每个购物项参与的活动的数据，根据活动规则进行分组
        //一个规则对应多个商品，cartInfoVo
        List<CartInfoVo> cartInfoVoList = this.findCartActivityList(cartInfoList);

        //2.计算商品参与活动之后最终的金额是多少

        //3.获取购物车里面可用的优惠卷列表

        //4。计算商品使用优惠卷之后的金额是多少，只能使用一张优惠卷

        //5.计算没有参与活动，没有使用优惠卷原始金额

        //6.参与活动，使用优惠卷总金额

        //7.封装需要数据到OrderConfirmVo，返回
        return null;
    }

    //获取购物车对应规则数据
    @Override
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        List<CartInfoVo> cartInfoVoList = new ArrayList<>();
        //1.获取所有skuId
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());
        //2.根据所有的skuId获取参与活动
        List<ActivitySku> activitySkuList = baseMapper.selectCartActivity(skuIdList);
        //根据活动进行分组，每个活动里面有哪些skuId信息
        //map里面的字段是分组字段，活动id
        //value是每组里面skuId
        Map<Long, Set<Long>> activityIdToSkuIdListMap = activitySkuList.stream().collect(
                        Collectors.groupingBy(
                        ActivitySku::getActivityId,
                        Collectors.mapping(ActivitySku::getSkuId, Collectors.toSet())));

        //获取活动里面规则数据
        //key是活动id value是活动里面规则列表
        Map<Long,List<ActivityRule>> activityIdToActivityRuleListMap = new HashMap<>();
        //所有活动id
        Set<Long> activityIdSet = activitySkuList.stream().map(ActivitySku::getActivityId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(activityIdSet)){
            //activity_rule
            LambdaQueryWrapper<ActivityRule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(ActivityRule::getConditionAmount,ActivityRule::getConditionNum);
            queryWrapper.in(ActivityRule::getActivityId,activityIdSet);
            List<ActivityRule> activityRuleList = activityRuleMapper.selectList(queryWrapper);
            //封装到activityIdToActivityRuleListMap集合中去,根据活动id进行分组
            activityIdToActivityRuleListMap = activityRuleList.stream().
                    collect(Collectors.groupingBy(activityRule -> activityRule.getActivityId()));

            //有活动的购物项的skuId
            Set<Long> activitySkuIdSet = new HashSet<>();
            if (!CollectionUtils.isEmpty(activityIdToActivityRuleListMap)){
                Iterator<Map.Entry<Long, Set<Long>>> iterator = activityIdToSkuIdListMap.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<Long, Set<Long>> next = iterator.next();
                    //活动id
                    Long activityId = next.getKey();
                    //每个活动对应的购物项列表
                    Set<Long> currentActivitySkuIdSet = next.getValue();
                    //获取当前活动对应的购物项
                    List<CartInfo> currentActivityCartInfoList = cartInfoList.stream().
                            filter(cartInfo -> currentActivitySkuIdSet.contains(cartInfo.getSkuId()))
                            .collect(Collectors.toList());
                    //计算购物项总的金额和总的数量
                    BigDecimal activityTotalAmount = this.computeTotalAmount(currentActivityCartInfoList);
                    int activityTotalNum = this.computeCartNum(currentActivityCartInfoList);
                    //计算活动对应规则
                    List<ActivityRule> currentActivityRuleList = activityIdToActivityRuleListMap.get(activityId);
                    ActivityType activityType = currentActivityRuleList.get(0).getActivityType();
                    //判断活动类型是满减还是打折
                    ActivityRule activityRule = null;
                    if (activityType == ActivityType.FULL_REDUCTION){//满减
                        activityRule = this.computeFullReduction(activityTotalAmount, currentActivityRuleList);
                    }else {//满量
                        activityRule = this.computeFullDiscount(activityTotalNum, activityTotalAmount, currentActivityRuleList);
                    }
                    //封装数据到cartInfoVoList
                    CartInfoVo cartInfoVo = new CartInfoVo();
                    cartInfoVo.setActivityRule(activityRule);
                    cartInfoVo.setCartInfoList(currentActivityCartInfoList);
                    cartInfoVoList.add(cartInfoVo);

                    //记录那些购物项参加了活动
                    activitySkuIdSet.addAll(currentActivitySkuIdSet);
                }
            }
            // 没有活动的购物项skuId
            //获取那些skuId没有参加活动
            skuIdList.removeAll(activitySkuIdSet);
            if (!CollectionUtils.isEmpty(skuIdList)){
                //skuId对应数据
                Map<Long, CartInfo> skuIdCartInfoMap = cartInfoList.stream()
                        .collect(Collectors.toMap(CartInfo::getSkuId, CartInfo -> CartInfo));
                for (Long skuId:skuIdList) {
                    CartInfoVo cartInfoVo = new CartInfoVo();
                    cartInfoVo.setActivityRule(null);
                    List<CartInfo> cartInfos = new ArrayList<>();
                    cartInfos.add(skuIdCartInfoMap.get(skuId));
                    cartInfoVo.setCartInfoList(cartInfos);
                    cartInfoVoList.add(cartInfoVo);
                };
            }
        }
        return cartInfoVoList;
    }


    @Override
    public IPage<ActivityInfo> selectPage(Page<ActivityInfo> activityInfoPage) {
        LambdaQueryWrapper<ActivityInfo> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.orderByAsc(ActivityInfo::getCreateTime);
        IPage<ActivityInfo> activityInfo = baseMapper.selectPage(activityInfoPage,queryWrapper);
        //分页查询对象里面获取列表数据
        List<ActivityInfo> records = activityInfo.getRecords();
        records.stream().forEach(item -> {
            item.setActivityTypeString(item.getActivityType().getComment());
        });
        return activityInfo;
    }

    @Override
    public Map<String, Object> findActivityRuleList(Long id) {
        Map<String,Object> result = new HashMap<>();
        //根据活动id，查询规则列表activity_rule表
        LambdaQueryWrapper<ActivityRule> ruleQueryWrapper = new LambdaQueryWrapper<>();
        ruleQueryWrapper.eq(ActivityRule::getActivityId,id);
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(ruleQueryWrapper);
        result.put("activityRuleList",activityRuleList);
        //根据活动id,查询使用规则商品skuid列表activity_sku表
        LambdaQueryWrapper<ActivitySku> skuQueryWrapper = new LambdaQueryWrapper<>();
        skuQueryWrapper.eq(ActivitySku::getActivityId,id);
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(skuQueryWrapper);
        //获取所有skuid
        List<Long> skuList = activitySkuList.stream().map(ActivitySku::getSkuId).collect(Collectors.toList());
        //远程调用service-product模块接口，根据skuid列表，得到商品信息
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(skuList);
        result.put("skuInfoList",skuInfoList);
        return result;
    }

    //添加活动规则

    @Override
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        //根据活动id先删除之前的规则数据
        Long activityId = activityRuleVo.getActivityId();
        //删除ActivityRule中的数据
        activityRuleMapper.delete(new LambdaQueryWrapper<ActivityRule>().eq(ActivityRule::getActivityId,activityId));
        //删除ActivitySku中的数据
        activitySkuMapper.delete(new LambdaQueryWrapper<ActivitySku>().eq(ActivitySku::getActivityId,activityId));

        //获取规则列表数据进行添加
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        ActivityInfo activityInfo = baseMapper.selectById(activityId);
        for (ActivityRule activityRule:activityRuleList) {
            activityRule.setActivityId(activityId);///活动id
            activityRule.setActivityType(activityInfo.getActivityType());
            activityRuleMapper.insert(activityRule);
        }
        //获取规则范围数据进行添加
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        for (ActivitySku activitySku:activitySkuList) {
            activitySku.setActivityId(activityId);//封装活动id
            activitySkuMapper.insert(activitySku);
        }

    }

    //根据关键字查询匹配sku信息
    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        //第一步 根据关键字查询匹配信息
        //1.service-product模块中创建接口，根据关键字查询匹配信息
        //2.service-activity远程调用得到sku内容列表
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        if (skuInfoList.size() == 0){
            return skuInfoList;
        }
        //从skuInfoList集合获取所有的skuId
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfo::getId).collect(Collectors.toList());
        //第二步 判断添加商品之前是否参加过活动
        // 如果之前参加过，活动正在进行中，排除商品
        //1.查询两张表判断activity_info,activity_sku编写sql语句实现
        List<Long> existSkuIdList = baseMapper.selectSkuIdListExist(skuIdList);

        //2.判断逻辑处理:排除已经参加的活动商品
        List<SkuInfo> findSkuList = new ArrayList<>();
        //遍历全部sku列表
        for (SkuInfo skuInfo:skuInfoList) {
            if (!existSkuIdList.contains(skuInfo.getId())){
                findSkuList.add(skuInfo);
            }
        }
        return findSkuList;
    }
    //    根据skuId列表获取促销信息
    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        Map<Long,List<String>> result = new HashMap<>();
        //1.遍历skuIdList得到每个skuId
        skuIdList.forEach(skuId -> {
            //2.根据skuId进行查询，查询sku对应活动里面的规则列表
            List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);
            //3.进行数据封装，规则名称
            if (!CollectionUtils.isEmpty(activityRuleList)){
                //把规则名称处理
//                for (ActivityRule activityRule:activityRuleList) {
//                    activityRule.setRuleDesc(this.getRuleDesc(activityRule));
//                }
//                List<String> ruleList = activityRuleList.stream()
//                        .map(activityRule -> activityRule.getRuleDesc()).collect(Collectors.toList());
                List<String> ruleList = new ArrayList<>();
                for (ActivityRule activityRule:activityRuleList) {
                    ruleList.add(this.getRuleDesc(activityRule));
                }
                result.put(skuId,ruleList);
            }
        });
        return result;
    }




    //构造规则名称的方法
    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuffer ruleDesc = new StringBuffer();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }

    /**
     * 计算满量打折最优规则
     * @param totalNum
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠折扣从大到小排序了
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    /**
     * 计算满减最优规则
     * @param totalAmount
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    private int computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }
}
