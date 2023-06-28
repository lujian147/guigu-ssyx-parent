package com.atguigu.ssyx.acl.controller;

import com.atguigu.ssyx.acl.service.AdminService;
import com.atguigu.ssyx.acl.service.RoleService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.utils.MD5;
import com.atguigu.ssyx.model.acl.Admin;
import com.atguigu.ssyx.model.user.User;
import com.atguigu.ssyx.vo.acl.AdminLoginVo;
import com.atguigu.ssyx.vo.acl.AdminQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "用户接口")
@RestController
@RequestMapping("/admin/acl/user")
//@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private RoleService roleService;



    /**
     *  //为用户进行分配
     * //    url: `${api_name}/doAssign`,
     * //    method: 'post',
     * //    params: {
     * //        adminId,
     * //        roleId
     * //    }
     */
    @ApiOperation("为用户进行分配")
    @PostMapping("doAssign")
    public Result doAssign(@RequestParam Long adminId,
                           @RequestParam Long[] roleId){
        roleService.saveAdminRole(adminId,roleId);
        return Result.ok(null);
    }

    //获取用户所有角色，根据角色id查询用户分配列表
//    url: `${api_name}/toAssign/${adminId}`,
//    method: 'get'
    @ApiOperation("获取用户角色")
    @GetMapping("toAssign/{adminId}")
    public Result toAssign(@PathVariable Long adminId){
        //map集合中包含两部分，所有角色和用户分配列表
        Map<String,Object> map = roleService.getRoleByAdminId(adminId);
        return Result.ok(map);
    }


    //用户列表
    @ApiOperation("用户列表")
    @GetMapping("{current}/{limit}")
    public Result list(@PathVariable Long current,
                       @PathVariable Long limit,
                       AdminQueryVo adminQueryVo){
        Page<Admin> page = new Page<>(current, limit);
        IPage<Admin> pageModel = adminService.selectPageUser(page,adminQueryVo);
        return Result.ok(pageModel);
    }


    //id查询用户
    @ApiOperation("根据id查询用户")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        Admin admin = adminService.getById(id);
        return Result.ok(admin);
    }
    //添加用户
    @ApiOperation("添加用户")
    @PostMapping("save")
    public Result save(@RequestBody Admin admin){
        admin.setPassword(MD5.encrypt(admin.getPassword()));
        adminService.save(admin);
        return Result.ok(null);
    }
    //修改用户
    @ApiOperation("修改用户")
    @PutMapping("update")
    public Result update(@RequestBody Admin admin){
        adminService.updateById(admin);
        return Result.ok(null);
    }
    //删除用户
    @ApiOperation("删除用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        adminService.removeById(id);
        return Result.ok(null);
    }
    ////批量删除用户
    @ApiOperation("批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> ids){
        adminService.removeByIds(ids);
        return Result.ok(null);
    }
}
