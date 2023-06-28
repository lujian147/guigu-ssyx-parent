package com.atguigu.ssyx.product.service;

import com.atguigu.ssyx.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SkuAttrValueService extends IService<SkuAttrValue> {
    //获取属性列表
    List<SkuAttrValue> getAttrValueList(Long id);
}
