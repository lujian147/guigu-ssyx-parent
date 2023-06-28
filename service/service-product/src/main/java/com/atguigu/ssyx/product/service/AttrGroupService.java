package com.atguigu.ssyx.product.service;


import com.atguigu.ssyx.model.product.AttrGroup;
import com.atguigu.ssyx.vo.product.AttrGroupQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 属性分组 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-13
 */
public interface AttrGroupService extends IService<AttrGroup> {

    //获取分页列表
    IPage<AttrGroup> selectPageAttrGroup(Page<AttrGroup> groupPage, AttrGroupQueryVo attrGroupQueryVo);

    List<AttrGroup> findAllList();
}
