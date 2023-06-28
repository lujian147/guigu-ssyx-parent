package com.atguigu.ssyx.search.controller;

import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.search.SkuEs;
import com.atguigu.ssyx.search.service.SkuService;
import com.atguigu.ssyx.vo.search.SkuEsQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "商品上下架")
@RestController
@RequestMapping("api/search/sku")
public class SkuApiController {

    @Autowired
    private SkuService skuService;


    //更新商品热度
    @ApiOperation(value = "更新商品incrHotScore")
    @GetMapping("inner/incrHotScore/{skuId}")
    public Boolean incrHotScore(@PathVariable("skuId") Long skuId){
        skuService.incrHotScore(skuId);
        return true;
    }

    //查询分类商品
    @ApiOperation("查询分类商品")
    @GetMapping("{page}/{limit}")
    public Result listSku(@PathVariable Integer page,
                          @PathVariable Integer limit,
                          SkuEsQueryVo skuEsQueryVo){
        //创建pageable对象
        Pageable pageable = PageRequest.of(page - 1,limit);
        Page pageModel = skuService.search(pageable,skuEsQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "上架商品")
    @GetMapping("inner/upperSku/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId) {
        skuService.upperSku(skuId);
        return Result.ok(null);
    }

    @ApiOperation(value = "下架商品")
    @GetMapping("inner/lowerSku/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId) {
        skuService.lowerSku(skuId);
        return Result.ok(null);
    }

    //获取爆款商品
    @GetMapping("inner/findHotSkuList")
    public List<SkuEs> findHotSkuList(){
        return skuService.findHotSkuList();
    }
}
