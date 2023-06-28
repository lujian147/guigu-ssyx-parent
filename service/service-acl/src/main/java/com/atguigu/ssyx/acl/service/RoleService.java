package com.atguigu.ssyx.acl.service;

import com.atguigu.ssyx.model.acl.Role;
import com.atguigu.ssyx.vo.acl.RoleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface RoleService extends IService<Role> {

    IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo);


    //获取用户所有角色，根据角色id查询用户分配列表
    Map<String, Object> getRoleByAdminId(Long adminId);
    //为用户进行分配

    void saveAdminRole(Long adminId, Long[] roleId);
}
