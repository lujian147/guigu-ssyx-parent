package com.atguigu.ssyx.home.service.impl;

import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.client.search.SearchFeignClient;
import com.atguigu.ssyx.client.user.UserFeignClient;
import com.atguigu.ssyx.home.service.HomeService;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.search.SkuEs;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private SearchFeignClient searchFeignClient;
    //首页数据显示
    @Override
    public Map<String, Object> homeData(Long userId) {
        Map<String,Object> result = new HashMap<>();
        //1 根据userId获取当前登录用户的提货地址信息
        //远程调用service-user模块接口获取需要数据
        LeaderAddressVo userAddressVo = userFeignClient.getUserAddressByUserId(userId);
        result.put("leaderAddressVo",userAddressVo);
        //2 获取所有分类信息，远程调用service-product模块中的接口
        List<Category> categoryList = productFeignClient.findAllCategoryList();
        result.put("categoryList",categoryList);
        //3 获取新人专享，远程调用service-product模块中的接口
        List<SkuInfo> newPersonSkuInfoList = productFeignClient.findNewPersonSkuInfoList();
        result.put("newPersonSkuInfoList",newPersonSkuInfoList);
        //4 获取爆款产品，远程调用service-search模块中的接口
        //hotScore降序排序
        List<SkuEs> hotSkuList = searchFeignClient.findHotSkuList();
        result.put("hotSkuList",hotSkuList);
        //5 封装获取到的数据进行返回
        return result;
    }
}
