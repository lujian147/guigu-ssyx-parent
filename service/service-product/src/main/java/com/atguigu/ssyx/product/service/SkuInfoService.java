package com.atguigu.ssyx.product.service;


import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * sku信息 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-13
 */
public interface SkuInfoService extends IService<SkuInfo> {

    IPage<SkuInfo> selectPageSkuInfo(Page<SkuInfo> infoPage, SkuInfoQueryVo skuInfoQueryVo);

    //商品添加方法
    void saveSkuInfo(SkuInfoVo skuInfoVo);

    //获取商品信息
    SkuInfoVo getSkuInfoVo(Long id);

    //修改商品信息
    void updateSkuInfoVo(SkuInfoVo skuInfoVo);

    //商品审核
    void check(Long id, Integer status);

    ////商品上架
    void publish(Long id, Integer status);

    //新人专享
    void isNewPerson(Long id, Integer status);

    //根据skuId列表得到sku信息列表
    List<SkuInfo> findSkuInfoList(List<Long> skuList);

    //根据关键字匹配sku列表
    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    //获取新人专享商品
    List<SkuInfo> findNewPersonList();
}
