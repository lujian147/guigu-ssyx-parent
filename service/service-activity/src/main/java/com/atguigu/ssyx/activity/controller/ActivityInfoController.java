package com.atguigu.ssyx.activity.controller;


import com.atguigu.ssyx.activity.service.ActivityInfoService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.activity.ActivityRule;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.ActivityRuleVo;
import com.atguigu.ssyx.vo.order.CartInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.ibatis.io.ResolverUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-16
 */
@Api(tags = "商品活动接口")
@RestController
@RequestMapping("/admin/activity/activityInfo")
//@CrossOrigin
public class ActivityInfoController {
    @Autowired
    private ActivityInfoService activityInfoService;


    /**
     *  url: `${api_name}/${page}/${limit}`,
     *  method: 'get'
     *  分页查询
     * @param page
     * @param limit
     * @return
     */
    @ApiOperation("分页查询展示商品列表")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit){
        Page<ActivityInfo> activityInfoPage = new Page<>(page,limit);
        IPage<ActivityInfo> pageModel= activityInfoService.selectPage(activityInfoPage);
        return Result.ok(pageModel);
    }

    //url: `${api_name}/get/${id}`,
    //      method: 'get'
    @ApiOperation("获取活动商品")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        ActivityInfo activityInfo = activityInfoService.getById(id);
        activityInfo.setActivityTypeString(activityInfo.getActivityType().getComment());
        return Result.ok(activityInfo);
    }

    /**
     * url: `${api_name}/save`,
     *       method: 'post',
     *       data: role
     * @param activityInfo
     * @return
     */
    @ApiOperation("添加活动信息")
    @PostMapping("save")
    public Result save(@RequestBody ActivityInfo activityInfo){
        activityInfoService.save(activityInfo);
        return Result.ok(null);
    }


    /**
     * //url: `${api_name}/update`,
     * //      method: 'put',
     * //      data: role
     * @param activityInfo
     * @return
     */
    @ApiOperation("修改活动信息")
    @PutMapping("update")
    public Result update(@RequestBody ActivityInfo activityInfo){
        activityInfoService.updateById(activityInfo);
        return Result.ok(null);
    }

    /**
     *     url: `${api_name}/remove/${id}`,
     *     method: 'delete'
     * @param id
     * @return
     */
    @ApiOperation("删除活动信息")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        activityInfoService.removeById(id);
        return Result.ok(null);
    }

    /**
     *   url: `${api_name}/batchRemove`,
     *           method: 'delete',
     *           data: idList
     * @param idList
     * @return
     */
    @ApiOperation("批量删除商品信息")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        activityInfoService.removeByIds(idList);
        return Result.ok(null);
    }


    // url: `${api_name}/findActivityRuleList/${id}`,
    //      method: 'get'
    @ApiOperation(value = "获取活动规则")
    @GetMapping("findActivityRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id){
        Map<String,Object> activityRuleMap = activityInfoService.findActivityRuleList(id);
        return Result.ok(activityRuleMap);
    }



    /**
     * 在活动里面添加规则数据
     *     url: `${api_name}/saveActivityRule`,
     *       method: 'post',
     *       data: rule
     * @param activityRuleVo
     * @return
     */
    @ApiOperation("在活动里面添加规则数据")
    @PostMapping("saveActivityRule")
    public Result saveActivityRule(@RequestBody ActivityRuleVo activityRuleVo){
        activityInfoService.saveActivityRule(activityRuleVo);
        return Result.ok(null);
    }



    /**
     *     根据关键字查询匹配sku信息
     *     url: `${api_name}/findSkuInfoByKeyword/${keyword}`,
     *           method: 'get'
     * @param keyword
     * @return
     */
    @ApiOperation("根据关键字查询匹配sku信息")
    @GetMapping("findSkuInfoByKeyword/{keyword}")
    public Result findSkuInfoByKeyword(@PathVariable("keyword") String keyword){
        List<SkuInfo> skuInfoList = activityInfoService.findSkuInfoByKeyword(keyword);
        return Result.ok(skuInfoList);
    }




}

