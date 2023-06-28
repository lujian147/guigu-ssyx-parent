package com.atguigu.ssyx.search.service.Impl;

import com.atguigu.ssyx.client.activity.ActivityFeignClient;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.enums.SkuType;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.search.SkuEs;
import com.atguigu.ssyx.search.repository.SkuRepository;
import com.atguigu.ssyx.search.service.SkuService;
import com.atguigu.ssyx.vo.search.SkuEsQueryVo;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuRepository skuRepository;


    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ActivityFeignClient activityFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;
    //上架商品
    @Override
    public void upperSku(Long skuId) {
        //1.通过远程调用，根据skuid获取相关信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo == null){
            return;
        }
        Category category = productFeignClient.getCategory(skuInfo.getCategoryId());
        //2.获取数据封装到SkuEs中
        SkuEs skuEs = new SkuEs();
//        封装分类
        if (category != null){
            skuEs.setCategoryId(category.getId());
            skuEs.setCategoryName(category.getName());
        }
        //封装sku信息部分
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName()+","+skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());
        if(skuInfo.getSkuType() == SkuType.COMMON.getCode()) {
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
            skuEs.setStock(skuInfo.getStock());
            skuEs.setSale(skuInfo.getSale());
            skuEs.setPerLimit(skuInfo.getPerLimit());
        } else {
            //TODO 待完善-秒杀商品

        }
        //3.调用方法添加ES
        skuRepository.save(skuEs);
    }

    //下架商品
    @Override
    public void lowerSku(Long skuId) {
        skuRepository.deleteById(skuId);
    }
    //获取爆款商品
    @Override
    public List<SkuEs> findHotSkuList() {
        //ES中使用分页查询
        Pageable pageable = PageRequest.of(0,10);
        Page<SkuEs> pageModel = skuRepository.findByOrderByHotScoreDesc(pageable);
        List<SkuEs> skuEsList = pageModel.getContent();
        return skuEsList;
    }

    @Override
    public Page search(Pageable pageable, SkuEsQueryVo skuEsQueryVo) {
        //1.向skuEsQueryVo中设置wareId
        skuEsQueryVo.setWareId(AuthContextHolder.getWareId());
        //2.调用skuRepository中的方法进行条件查询
        String keyword = skuEsQueryVo.getKeyword();
        Page<SkuEs> pageModel = null;
        if (StringUtils.isEmpty(keyword)){
            //判断keyword是否为空，如果为空，根据仓库id和分类id来查询
            pageModel = skuRepository.findByCategoryIdAndWareId(skuEsQueryVo.getCategoryId(),
                                                                skuEsQueryVo.getWareId(),
                                                                pageable);
        }else {
            //不为空则根据三者来实现查询
            pageModel = skuRepository.findByKeywordAndWareId(keyword,
                                                             skuEsQueryVo.getWareId(),
                                                              pageable);
        }
        //3.查询商品参加优惠活动
        List<SkuEs> skuEsList = pageModel.getContent();
        //遍历所有skuEsList得到所有的skuId
        List<Long> skuIdList = skuEsList.stream()
                                        .map(item -> item.getId())
                                        .collect(Collectors.toList());
        //根据skuIdList列表远程调用，调用service-activity中的接口得到数据
        //返回Map<Long,List<String>> key为SukId,value为活动规则
        Map<Long,List<String>> skuIdToRuleListMap = activityFeignClient.findActivity(skuIdList);//远程调用
        if (skuIdToRuleListMap != null){
            //封装数据到skuEs里面的ruleList属性
            skuEsList.forEach(skuEs -> {
                skuEs.setRuleList(skuIdToRuleListMap.get(skuEs.getId()));
            });
        }
        return pageModel;
    }

    //更新商品热度
    @Override
    public void incrHotScore(Long skuId) {
        String key = "hotScore";
        //redis保存数据，每次+1
        Double hotScore = redisTemplate.opsForZSet().incrementScore(key, "skuId:" + skuId, 1);
        //规则
        if (hotScore % 10 == 0){
            //更新es
            Optional<SkuEs> optional = skuRepository.findById(skuId);
            SkuEs skuEs = optional.get();
            skuEs.setHotScore(Math.round(hotScore));
            skuRepository.save(skuEs);
        }
    }
}
