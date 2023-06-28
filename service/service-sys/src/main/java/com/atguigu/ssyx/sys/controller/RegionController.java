package com.atguigu.ssyx.sys.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.sys.Region;
import com.atguigu.ssyx.sys.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 地区表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-11
 */
@Api(tags = "区域接口")
@RestController
@RequestMapping("/admin/sys/region")
//@CrossOrigin
public class RegionController {

    @Autowired
    private RegionService regionService;


    //url: `${api_name}/findRegionByKeyword/${keyword}`,
    //      method: 'get'
    ////根据输入值查询区域
    @ApiOperation("根据输入值查询区域")
    @GetMapping("findRegionByKeyword/{keyword}")
    public Result findRegionByKeyword(@PathVariable("keyword") String keyword){
        List<Region> list = regionService.getRegionByKeyword(keyword);
        return Result.ok(list);
    }

}

