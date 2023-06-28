package com.atguigu.ssyx.home.service.impl;

import com.atguigu.ssyx.client.activity.ActivityFeignClient;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.client.search.SearchFeignClient;
import com.atguigu.ssyx.home.service.ItemService;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private SearchFeignClient searchFeignClient;

    @Autowired
    private ActivityFeignClient activityFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

//    获取sku详细信息
    @Override
    public Map<String, Object> item(Long skuId, Long userId) {
        Map<String,Object> result = new HashMap<>();
        //skuId查询
        CompletableFuture<SkuInfoVo> skuInfoVoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //通过远程调用获取sku对应的数据
            SkuInfoVo skuInfoVo = productFeignClient.getSkuInfoVo(skuId);//TODO
            result.put("skuInfoVo", skuInfoVo);
            return skuInfoVo;
        },threadPoolExecutor);

        //sku对应的优惠卷信息
        CompletableFuture<Void> activityCompletableFuture = CompletableFuture.runAsync(() -> {
            //远程调用获取优惠卷信息
            Map<String,Object> activityMap = activityFeignClient.findActivityAndCoupon(skuId, userId);//TODO
            result.putAll(activityMap);
        },threadPoolExecutor);

        //更新商品热度
        CompletableFuture<Void> hotCompletableFuture = CompletableFuture.runAsync(() -> {
            //远程调用更新热度 TODO
            searchFeignClient.incrHotScore(skuId);
        },threadPoolExecutor);

        //任务组合
        CompletableFuture.allOf(skuInfoVoCompletableFuture,
                                activityCompletableFuture,
                                hotCompletableFuture)
                                .join();
        return result;
    }
}
