package com.atguigu.ssyx.acl.service.impl;

import com.atguigu.ssyx.acl.mapper.PermissionMapper;
import com.atguigu.ssyx.acl.service.PermissionService;
import com.atguigu.ssyx.acl.utils.PermissionHelper;
import com.atguigu.ssyx.model.acl.Permission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.sun.javafx.tk.PermissionHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    //查询所有菜单
    @Override
    public List<Permission> queryAllPermission() {
        //查询所有菜单
        List<Permission> allPermissionList = baseMapper.selectList(null);
        //转换要求数据格式
        List<Permission> result = PermissionHelper.buildPermission(allPermissionList);
        return result;
    }

    //递归删除菜单
    @Override
    public void removeChildById(Long id) {
        //创建集合idList，用于保存要删除的所有菜单id
        List<Long> idList = new ArrayList<>();
        //根据当前菜单id，获取所有的子菜单id
        //如果子菜单下面还有子菜单还要获取
        this.getAllPermissionId(id,idList);

        //添加菜单id
        idList.add(id);
        //调用方法删除多个菜单id
        baseMapper.deleteBatchIds(idList);
    }


    /**
     * 递归找当前菜单的所有子菜单
     * @param id
     * @param idList
     */
    private void getAllPermissionId(Long id, List<Long> idList) {
        //根据当前id,查询所有的子菜单id
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getPid,id);
        List<Permission> childList = baseMapper.selectList(queryWrapper);
        //递归
        childList.stream().forEach(item ->{
            idList.add(item.getPid());
            this.getAllPermissionId(item.getPid(),idList);
        });
    }


    //查看某个角色的权限列表
    @Override
    public Map<String, Object> getPermissionByRoleId(Long roleId) {
        return null;
    }

    //给某个角色授权
    @Override
    public void saveRolePermission(Long roleId, Long permissionId) {

    }
}
