package com.atguigu.ssyx.acl.service;

import com.atguigu.ssyx.model.acl.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface PermissionService extends IService<Permission> {
    //查询所有菜单列表
    List<Permission> queryAllPermission();

    void removeChildById(Long id);

    //查看某个角色的权限列表
    Map<String, Object> getPermissionByRoleId(Long roleId);

    ///给某个角色授权
    void saveRolePermission(Long roleId, Long permissionId);
}
