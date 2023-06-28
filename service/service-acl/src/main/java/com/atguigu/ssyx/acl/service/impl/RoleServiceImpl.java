package com.atguigu.ssyx.acl.service.impl;

import com.atguigu.ssyx.acl.mapper.RoleMapper;
import com.atguigu.ssyx.acl.service.AdminRoleService;
import com.atguigu.ssyx.acl.service.RoleService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.acl.AdminRole;
import com.atguigu.ssyx.model.acl.Role;
import com.atguigu.ssyx.vo.acl.RoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private AdminRoleService adminRoleService;
    @Override
    public IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo) {
        //获取条件值
        String roleName = roleQueryVo.getRoleName();

        //判断条件是否为空，不为空封装查询条件
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        //创建mp的条件对象
        if (!StringUtils.isEmpty(roleName)){
            queryWrapper.like(Role::getRoleName,roleName);
        }
        //调用方法实现分页查询
        IPage<Role> rolePage = baseMapper.selectPage(pageParam, queryWrapper);
        return rolePage;
    }

    //获取用户所有角色，根据角色id查询用户分配列表
    @Override
    public Map<String, Object> getRoleByAdminId(Long adminId) {
        //1.获取所有角色
        List<Role> allRolesList = baseMapper.selectList(null);
        //2.根据用户id查询用户分配列表
        //2.1 根据用户id查询 用户角色关系表 admin_role 查询用户分配角色id列表
        LambdaQueryWrapper<AdminRole> queryWrapper = new LambdaQueryWrapper<>();
        //设置查询条件
        queryWrapper.eq(AdminRole::getAdminId,adminId);
        List<AdminRole> adminRoleList = adminRoleService.list(queryWrapper);
        //2.2 通过第一步返回集合，获取所有角色id的列表 List<Long>
        List<Long> roleIdList = adminRoleList.stream().
                                                map(item -> item.getRoleId()).
                                                collect(Collectors.toList());
        //2.3 创建新的list集合，用于存储用户分配角色
        ArrayList<Role> assignRoleList = new ArrayList<>();
        //2.4 遍历所有角色列表 allRoleList.得到每个角色
        for (Role role:allRolesList) {
            //判断
            if (roleIdList.contains(role.getId())){
                assignRoleList.add(role);
            }
        }
        //判断所有角色里面是否包含已经分配角色id，封装到2.3里面新的list集合
        Map<String,Object> result = new HashMap<>();
        //所有角色列表
        result.put("allRolesList",allRolesList);
        //用户分配角色列表
        result.put("assignRoles",assignRoleList);
        return result;
    }

    /**
     * //为用户进行分配
     * @param adminId
     * @param roleId
     */
    @Override
    public void saveAdminRole(Long adminId, Long[] roleId) {
        //1.删除用户已经分配的角色数据
        //根据用户id删除admin_role表里面对应数据
        LambdaQueryWrapper<AdminRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminRole::getAdminId,adminId);
        adminRoleService.remove(queryWrapper);
        //2. 重新分配
        //遍历多个角色id，得到每个角色id，拿着每个角色id+用户id生成角色用户关系
//        for (Long id:roleId) {
//            AdminRole adminRole = new AdminRole();
//            adminRole.setAdminId(adminId);
//            adminRole.setRoleId(id);
//            adminRoleService.save(adminRole);
//        }

        List<AdminRole> list = new ArrayList<>();
        for (Long id:roleId) {
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(adminId);
            adminRole.setRoleId(id);
            list.add(adminRole);
        }
        adminRoleService.saveBatch(list);
    }

}
