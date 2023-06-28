package com.atguigu.ssyx.product.api;

import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.service.CategoryService;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "ES实现商品上下架接口")
@RestController
@RequestMapping("/api/product")
@CrossOrigin
public class ProductInnnerController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuInfoService skuInfoService;

    //根据skuid获取分类信息
    @ApiOperation(value = "根据分类id获取分类信息")
    @GetMapping("inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable Long categoryId) {
        return categoryService.getById(categoryId);
    }

    //根据skuid获取sku信息
    @ApiOperation(value = "根据skuId获取sku信息")
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId) {
        return skuInfoService.getById(skuId);
    }


    //根据skuId列表得到sku信息列表
    @ApiOperation(value = "批量获取sku信息")
    @PostMapping("inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuList){
        List<SkuInfo> skuInfoList = skuInfoService.findSkuInfoList(skuList);
        return skuInfoList;
    }

    //根据分类列表获取分类信息列表
    @ApiOperation("根据分类列表获取分类信息列表")
    @PostMapping("inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryList){
        List<Category> categoryInfoList = categoryService.listByIds(categoryList);
        return categoryInfoList;
    }

    //根据关键字匹配sku列表
    @ApiOperation(value = "根据关键字获取sku列表")
    @GetMapping("inner/findSkuInfoByKeyword/{keyword}")
    public List<SkuInfo> findSkuInfoByKeyword(@PathVariable("keyword") String keyword){
        List<SkuInfo> skuInfoList = skuInfoService.findSkuInfoByKeyword(keyword);
        return skuInfoList;
    }

    //获取所有分类
    @ApiOperation(value = "获取分类信息")
    @GetMapping("inner/findAllCategoryList")
    public List<Category> findAllCategoryList() {
        return categoryService.findAllList();
    }

    //获取新人专享商品
    @ApiOperation(value = "获取新人专享")
    @GetMapping("inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList() {
        return skuInfoService.findNewPersonList();
    }


    //根据skuId获取sku信息
    @GetMapping("inner/getSkuInfoVo/{skuId}")
    public SkuInfoVo getSkuInfoVo(@PathVariable Long skuId){
        return skuInfoService.getSkuInfoVo(skuId);
    }
}
