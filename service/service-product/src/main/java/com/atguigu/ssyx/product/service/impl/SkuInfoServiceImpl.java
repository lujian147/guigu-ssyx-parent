package com.atguigu.ssyx.product.service.impl;


import com.atguigu.ssyx.model.product.SkuAttrValue;
import com.atguigu.ssyx.model.product.SkuImage;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.product.SkuPoster;
import com.atguigu.ssyx.mq.constant.MqConst;
import com.atguigu.ssyx.mq.service.RabbitService;
import com.atguigu.ssyx.product.mapper.SkuInfoMapper;
import com.atguigu.ssyx.product.service.SkuAttrValueService;
import com.atguigu.ssyx.product.service.SkuImageService;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.product.service.SkuPosterService;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * sku信息 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-13
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {
    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private SkuImageService skuImageService;

    @Autowired
    private SkuPosterService skuPosterService;

    @Autowired
    public RabbitService rabbitService;

    @Override
    public IPage<SkuInfo> selectPageSkuInfo(Page<SkuInfo> infoPage, SkuInfoQueryVo skuInfoQueryVo) {
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        String skuType = skuInfoQueryVo.getSkuType();
        Long categoryId = skuInfoQueryVo.getCategoryId();
        String keyword = skuInfoQueryVo.getKeyword();
        if (!StringUtils.isEmpty(skuType)){
            queryWrapper.like(SkuInfo::getSkuType,skuType);
        }
        if (!StringUtils.isEmpty(categoryId)){
            queryWrapper.eq(SkuInfo::getCategoryId,categoryId);
        }
        if (!StringUtils.isEmpty(keyword)){
            queryWrapper.like(SkuInfo::getSkuName,keyword);
        }
        Page<SkuInfo> skuInfoPage = baseMapper.selectPage(infoPage, queryWrapper);
        return skuInfoPage;
    }

    @Override
    public void saveSkuInfo(SkuInfoVo skuInfoVo) {
        //添加基本信息SkuInfo
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        this.save(skuInfo);
        //添加海报信息
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if (!CollectionUtils.isEmpty(skuPosterList)){
            for (SkuPoster skuPoster:skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }
        //添加图片信息
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if (!CollectionUtils.isEmpty(skuImagesList)){
            for (SkuImage skuImage:skuImagesList) {
                skuImage.setSkuId(skuInfo.getId());
            }
            skuImageService.saveBatch(skuImagesList);
        }

        //添加平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue:skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    //获取商品信息
    @Override
    public SkuInfoVo getSkuInfoVo(Long id) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();
        //获取基础信息SKuInfo
        SkuInfo skuInfo = baseMapper.selectById(id);
        BeanUtils.copyProperties(skuInfo,skuInfoVo);
        //获取图片列表
        List<SkuImage> skuImageList = skuImageService.getImageList(id);
        //获取海报列表
        List<SkuPoster> skuPosterList = skuPosterService.getPosterList(id);
        //获取属性列表
        List<SkuAttrValue> skuAttrValueList = skuAttrValueService.getAttrValueList(id);
        //封装到skuInfoVo中
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuImagesList(skuImageList);
        skuInfoVo.setSkuAttrValueList(skuAttrValueList);
        return skuInfoVo;
    }


    @Override
    public void updateSkuInfoVo(SkuInfoVo skuInfoVo) {

        Long id = skuInfoVo.getId();
        //修改基本信息
        this.updateById(skuInfoVo);
        //保存sku详情
        skuPosterService.remove(new LambdaQueryWrapper<SkuPoster>().eq(SkuPoster::getSkuId, id));
        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)) {
            int sort = 1;
            for(SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(id);
                sort++;
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //删除sku图片
        skuImageService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, id));
        //保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)) {
            int sort = 1;
            for(SkuImage skuImages : skuImagesList) {
                skuImages.setSkuId(id);
                skuImages.setSort(sort);
                sort++;
            }
            skuImageService.saveBatch(skuImagesList);
        }

        //删除sku平台属性
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, id));
        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for(SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(id);
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public void check(Long id, Integer status) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(id);
        skuInfo.setCheckStatus(status);
        baseMapper.updateById(skuInfo);
    }

    //商品上架
    @Override
    public void publish(Long id, Integer status) {
        if (status == 1) {
            SkuInfo skuInfo = new SkuInfo();
            skuInfo.setId(id);
            skuInfo.setPublishStatus(status);
            baseMapper.updateById(skuInfo);
            //TODO 商品上架 后续会完善：发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,
                                        MqConst.ROUTING_GOODS_UPPER,id);
        }
        else {

        }SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(id);
        skuInfo.setPublishStatus(status);
        baseMapper.updateById(skuInfo);
        //TODO 商品上架 后续会完善：发送mq消息更新es数据
        rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,MqConst.ROUTING_GOODS_LOWER,id);
    }

    //新人专享
    @Override
    public void isNewPerson(Long id, Integer status) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(id);
        skuInfo.setIsNewPerson(status);
        baseMapper.updateById(skuInfo);
    }

    @Override
    public List<SkuInfo> findSkuInfoList(List<Long> skuList) {
        List<SkuInfo> skuInfoList = baseMapper.selectBatchIds(skuList);
        return skuInfoList;
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        List<SkuInfo> skuInfoList = baseMapper.selectList(new LambdaQueryWrapper<SkuInfo>().like(SkuInfo::getSkuName, keyword));
        return skuInfoList;
    }
    //获取新人专享商品
    @Override
    public List<SkuInfo> findNewPersonList() {
        //条件1 ：is_new_person = 1
        //条件2 ：publish_status = 1
        //条件3 ：显示三个
        Page<SkuInfo> pageParam = new Page<>(1,3);
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuInfo::getIsNewPerson,1);
        queryWrapper.eq(SkuInfo::getPublishStatus,1);
        queryWrapper.orderByDesc(SkuInfo::getStock);
        IPage<SkuInfo> pageModel = baseMapper.selectPage(pageParam,queryWrapper);
        List<SkuInfo> skuInfoList = pageModel.getRecords();
        return skuInfoList;
    }
}
