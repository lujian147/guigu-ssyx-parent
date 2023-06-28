package com.atguigu.ssyx.acl.controller;


import com.atguigu.ssyx.acl.service.RoleService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.acl.Role;
import com.atguigu.ssyx.vo.acl.RoleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/admin/acl/role")
//@CrossOrigin //跨域
public class RoleController {
    @Autowired
    private RoleService roleService;

    //角色条件分页查询
    @ApiOperation("角色列表")
    @GetMapping("{current}/{limit}")
    public Result pageList(@PathVariable Long current,
                           @PathVariable Long limit,
                           RoleQueryVo roleQueryVo){
        //创建分页对象，传入分页条件
        Page<Role> pageParam = new Page<>(current,limit);
        //调用service层的方法实现分页查询
        IPage<Role> pageModel = roleService.selectRolePage(pageParam,roleQueryVo);
        return Result.ok(pageModel);
    }
    //根据id查询角色对象
    @ApiOperation(("根据id查询角色对象"))
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        Role role = roleService.getById(id);
        return Result.ok(role);
    }

    //添加角色
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody Role role){
        boolean is_success = roleService.save(role);
        if (is_success){
            return Result.ok(null);
        }
        else {
            return Result.fail(null);
        }
    }

    //修改角色
    @ApiOperation("修改角色")
    @PutMapping("update")
    public Result update(@RequestBody Role role){
        roleService.updateById(role);
        return Result.ok(null);
    }

    //根据id删除角色
    @ApiOperation("根据id删除角色")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        roleService.removeById(id);
        return Result.ok(null);
    }

    //批量删除角色
    @ApiOperation("批量删除角色")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        roleService.removeByIds(idList);
        return Result.ok(null);
    }
}
