package com.atguigu.ssyx.search.repository;

import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.search.SkuEs;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.client.ElasticsearchClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//ES的接口
@FeignClient(value = "service-product")
public interface SkuRepository extends ElasticsearchRepository<SkuEs,Long> {
    //获取爆款商品
    Page<SkuEs> findByOrderByHotScoreDesc(Pageable pageable);

    Page<SkuEs> findByCategoryIdAndWareId(Long categoryId, Long wareId, Pageable pageable);

    Page<SkuEs> findByKeywordAndWareId(String keyword, Long wareId, Pageable pageable);

//    List<SkuEs> findByOrderByHotScoreDesc();
}
