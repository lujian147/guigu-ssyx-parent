package com.atguigu.ssyx.product.mapper;


import com.atguigu.ssyx.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * sku信息 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2023-06-13
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    //解锁库存
    void unlockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);


    // 验库存：查询，返回的是满足要求的库存列表
    SkuInfo checkStock(@Param("skuId")Long skuId,@Param("skuNum") Integer skuNum);


    // 锁库存：更新
    Integer lockStock(@Param("skuId")Long skuId,@Param("skuNum") Integer skuNum);
}
