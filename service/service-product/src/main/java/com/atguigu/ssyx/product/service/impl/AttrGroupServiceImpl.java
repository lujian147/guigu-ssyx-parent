package com.atguigu.ssyx.product.service.impl;


import com.atguigu.ssyx.model.product.AttrGroup;
import com.atguigu.ssyx.product.mapper.AttrGroupMapper;
import com.atguigu.ssyx.product.service.AttrGroupService;
import com.atguigu.ssyx.vo.product.AttrGroupQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 属性分组 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-13
 */
@Service
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroup> implements AttrGroupService {

    @Override
    public IPage<AttrGroup> selectPageAttrGroup(Page<AttrGroup> groupPage, AttrGroupQueryVo attrGroupQueryVo) {
        LambdaQueryWrapper<AttrGroup> queryWrapper = new LambdaQueryWrapper<>();
        String name = attrGroupQueryVo.getName();
        if (!StringUtils.isEmpty(name)){
            queryWrapper.like(AttrGroup::getName,name);
        }
        Page<AttrGroup> list = baseMapper.selectPage(groupPage, queryWrapper);
        return list;
    }

    @Override
    public List<AttrGroup> findAllList() {
        LambdaQueryWrapper<AttrGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(AttrGroup::getSort);
        List<AttrGroup> list = this.list(queryWrapper);
        return list;
    }
}
