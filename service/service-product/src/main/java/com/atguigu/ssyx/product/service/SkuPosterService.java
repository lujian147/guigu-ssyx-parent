package com.atguigu.ssyx.product.service;

import com.atguigu.ssyx.model.product.SkuPoster;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SkuPosterService extends IService<SkuPoster> {
    //获取海报列表
    List<SkuPoster> getPosterList(Long id);
}
