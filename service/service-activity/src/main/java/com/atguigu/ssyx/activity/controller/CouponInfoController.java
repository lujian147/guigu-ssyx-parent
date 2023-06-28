package com.atguigu.ssyx.activity.controller;


import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.vo.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 优惠券信息 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-16
 */
@Api(tags = "优惠卷接口")
@RestController
@RequestMapping("/admin/activity/couponInfo")
//@CrossOrigin
public class CouponInfoController {
    @Autowired
    private CouponInfoService couponInfoService;


    /**
     * url: `${api_name}/${page}/${limit}`,
     *           method: 'get'
     * @param page
     * @param limit
     * @return
     */
    @ApiOperation("分页查询优惠卷")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit){
        Page<CouponInfo> couponInfoPage = new Page<>(page,limit);
        IPage<CouponInfo> pageModel = couponInfoService.selectPageCouponInfo(couponInfoPage);
        return Result.ok(pageModel);
    }

    /**
     * url: `${api_name}/get/${id}`,
     *       method: 'get'
     * @param id
     * @return
     */
    @ApiOperation("获取优惠卷")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        CouponInfo couponInfo = couponInfoService.getCouponInfo(id);
        return Result.ok(couponInfo);
    }

    @ApiOperation(value = "新增优惠券")
    @PostMapping("save")
    public Result save(@RequestBody CouponInfo couponInfo) {
        couponInfoService.save(couponInfo);
        return Result.ok(null);
    }

    @ApiOperation(value = "修改优惠券")
    @PutMapping("update")
    public Result updateById(@RequestBody CouponInfo couponInfo) {
        couponInfoService.updateById(couponInfo);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除优惠券")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable String id) {
        couponInfoService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value="根据id列表删除优惠券")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        couponInfoService.removeByIds(idList);
        return Result.ok(null);
    }


    /**
     *     url: `${api_name}/findCouponRuleList/${id}`,
     *           method: 'get'
     * @param id
     * @return
     */
    @ApiOperation("获取优惠卷信息")
    @GetMapping("findCouponRuleList/{id}")
    public Result findCouponRuleList(@PathVariable Long id){
        Map<String,Object> map = couponInfoService.findCouponRuleList(id);
        return Result.ok(map);
    }



    /**
     *     添加优惠卷规则数据
     *      url: `${api_name}/saveCouponRule`,
     *           method: 'post',
     *           data: rule
     * @param couponRuleVo
     * @return
     */
    @ApiOperation("添加优惠卷规则数据")
    @PostMapping("saveCouponRule")
    public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo){
        couponInfoService.saveCouponRule(couponRuleVo);
        return Result.ok(null);
    }
}

