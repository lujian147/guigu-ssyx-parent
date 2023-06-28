package com.atguigu.ssyx.search.service;

import com.atguigu.ssyx.model.search.SkuEs;
import com.atguigu.ssyx.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SkuService {
    void upperSku(Long skuId);

    void lowerSku(Long skuId);

    //获取爆款商品
    List<SkuEs> findHotSkuList();

    //查询分类商品
    Page search(Pageable pageable, SkuEsQueryVo skuEsQueryVo);

    //更新商品热度
    void incrHotScore(Long skuId);
}
