package com.atguigu.ssyx.acl.controller;

import com.atguigu.ssyx.acl.service.PermissionService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.acl.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "菜单管理")
@RestController
@RequestMapping("admin/acl/permission")
//@CrossOrigin
public class PermissionController {
    @Autowired
    private PermissionService permissionService;


    //查询所有菜单
//    url: `${api_name}`,
//    method: 'get'
    @ApiOperation("查询所有菜单")
    @GetMapping
    private Result list(){
        List<Permission> list = permissionService.queryAllPermission();
        return Result.ok(list);
    }
    //添加菜单
    //    url: `${api_name}/save`,
//    method: "post",
//    data: permission
    @ApiOperation("添加菜单")
    @PostMapping("save")
    public Result save(@RequestBody Permission permission){
        permissionService.save(permission);
        return Result.ok(null);
    }

    //更新菜单
//    url: `${api_name}/update`,
//    method: "put",
//    data: permission
    @ApiOperation("更新菜单")
    @PutMapping("update")
    public Result update(@RequestBody Permission permission){
        permissionService.updateById(permission);
        return Result.ok(null);
    }
    //递归删除菜单
//    url: `${api_name}/remove/${id}`,
//    method: "delete"
    @ApiOperation("递归删除菜单")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        permissionService.removeChildById(id);
        return Result.ok(null);
    }

    //查看某个角色的权限列表
    //url: `${api_name}/toAssign/${roleId}`,
    //      method: 'get'
    @ApiOperation("查看权限列表")
    @GetMapping("toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId){
        Map<String,Object> map = permissionService.getPermissionByRoleId(roleId);
        return Result.ok(map);
    }
    //给某个角色授权
    //url: `${api_name}/doAssign`,
    //      method: "post",
    //      params: {roleId, permissionId}
    @ApiOperation("给某个角色授权")
    @PostMapping("doAssign")
    public Result doAssign(@RequestParam Long roleId,
                           @RequestParam Long permissionId){
        permissionService.saveRolePermission(roleId,permissionId);
        return Result.ok(null);
    }
}
