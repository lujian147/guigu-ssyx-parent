package com.atguigu.ssyx.product.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.atguigu.ssyx.vo.search.SkuEsQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * sku信息 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-13
 */
@Api(tags = "SKU接口")
@RestController
@RequestMapping("/admin/product/skuInfo")
//@CrossOrigin
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;


    //url: `${api_name}/${page}/${limit}`,
    //      method: 'get',
    //      params: searchObj
    //列表功能
    @ApiOperation("列表功能")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       SkuInfoQueryVo skuInfoQueryVo){
        Page<SkuInfo> infoPage = new Page<>(page, limit);
        IPage<SkuInfo> pageModel = skuInfoService.selectPageSkuInfo(infoPage,skuInfoQueryVo);
        return Result.ok(pageModel);
    }

    //url: `${api_name}/save`,
    //      method: 'post',
    //      data: role
    ////商品添加方法
    @ApiOperation("商品添加方法")
    @PostMapping("save")
    public Result save(@RequestBody SkuInfoVo skuInfoVo){
        skuInfoService.saveSkuInfo(skuInfoVo);
        return Result.ok(null);
    }

//    url: `${api_name}/get/${id}`,
//    method: 'get'
    //获取商品信息
    @ApiOperation("获取商品信息")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        SkuInfoVo skuInfoVo = skuInfoService.getSkuInfoVo(id);
        return Result.ok(skuInfoVo);
    }

    //url: `${api_name}/update`,
    //      method: 'put',
    //      data: role
    //修改商品信息
    @ApiOperation("修改商品信息")
    @PutMapping("update")
    public Result update(@RequestBody SkuInfoVo skuInfoVo){
        skuInfoService.updateSkuInfoVo(skuInfoVo);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        skuInfoService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        skuInfoService.removeByIds(idList);
        return Result.ok(null);
    }

    //url: `${api_name}/check/${id}/${status}`,
    //      method: 'get'
    //商品审核
    @ApiOperation("商品审核")
    @GetMapping("check/{id}/{status}")
    public Result check(@PathVariable Long id,
                        @PathVariable Integer status){
        skuInfoService.check(id,status);
        return Result.ok(null);
    }

//    url: `${api_name}/publish/${id}/${status}`,
//    method: 'get'
    //商品上架
    @ApiOperation("商品上架")
    @GetMapping("publish/{id}/{status}")
    public Result publish(@PathVariable Long id,
                          @PathVariable Integer status){
        skuInfoService.publish(id,status);
        return Result.ok(null);
    }

    //url: `${api_name}/isNewPerson/${id}/${status}`,
    //      method: 'get'
    //新人专享
    @ApiOperation("新人专享")
    @GetMapping("isNewPerson/{id}/{status}")
    public Result isNewPerson(@PathVariable Long id,
                              @PathVariable Integer status){
        skuInfoService.isNewPerson(id,status);
        return Result.ok(null);
    }



}

