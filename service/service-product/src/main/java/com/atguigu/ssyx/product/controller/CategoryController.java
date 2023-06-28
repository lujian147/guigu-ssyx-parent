package com.atguigu.ssyx.product.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.product.service.CategoryService;
import com.atguigu.ssyx.vo.product.CategoryQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 商品三级分类 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-13
 */
@Api(tags = "商品分类接口")
@RestController
@RequestMapping("/admin/product/category")
//@CrossOrigin
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    //url: `${api_name}/${page}/${limit}`,
    //      method: 'get',
    //      params: searchObj
    //分页查询
    @ApiOperation("分页查询商品分类")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       CategoryQueryVo categoryQueryVo){
        Page<Category> categoryPage = new Page<>(page,limit);
        IPage<Category> pageModel = categoryService.selectPageCategory(categoryPage,categoryQueryVo);
        return Result.ok(pageModel);
    }

//    url: `${api_name}/get/${id}`,
//    method: 'get'
    //获取商品分类
    @ApiOperation("获取商品分类")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        Category category = categoryService.getById(id);
        return Result.ok(category);
    }

    //url: `${api_name}/save`,
    //      method: 'post',
    //      data: role
    //新增商品分类
    @ApiOperation("新增商品分类")
    @PostMapping("save")
    public Result save(@RequestBody Category category){
        categoryService.save(category);
        return Result.ok(null);
    }

    //url: `${api_name}/update`,
    //      method: 'put',
    //      data: role
    //修改商品分类
    @ApiOperation("修改商品分类")
    @PutMapping("update")
    public Result update(@RequestBody Category category){
        categoryService.updateById(category);
        return Result.ok(null);
    }

    //url: `${api_name}/remove/${id}`,
    //      method: 'delete'
    //删除商品分类
    @ApiOperation("删除商品分类")
    @DeleteMapping("remove/{id}")
    public Result delete(@PathVariable Long id){
        categoryService.removeById(id);
        return Result.ok(null);
    }

    //url: `${api_name}/batchRemove`,
    //      method: 'delete',
    //      data: idList
    //批量删除商品分类
    @ApiOperation("批量删除商品分类")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        categoryService.removeByIds(idList);
        return Result.ok(null);
    }

    //url: `${api_name}/findAllList`,
    //      method: 'get'
    //获得所有商品分类
    @ApiOperation("获得所有商品分类")
    @GetMapping("findAllList")
    public Result findAllList(){
        List<Category> list = categoryService.findAllList();
        return Result.ok(list);
    }
}

