package com.atguigu.ssyx.product.service.impl;

import com.atguigu.ssyx.model.product.SkuAttrValue;
import com.atguigu.ssyx.product.mapper.SkuAttrValueMapper;
import com.atguigu.ssyx.product.service.SkuAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValue> implements SkuAttrValueService {
    @Override
    public List<SkuAttrValue> getAttrValueList(Long id) {
        LambdaQueryWrapper<SkuAttrValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuAttrValue::getSkuId,id);
        List<SkuAttrValue> skuAttrValueList = baseMapper.selectList(queryWrapper);
        return skuAttrValueList;
    }
}
