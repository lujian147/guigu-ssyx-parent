package com.atguigu.ssyx.user.service;

import com.atguigu.ssyx.model.user.User;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import com.atguigu.ssyx.vo.user.UserLoginVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    User getUserByOpenId(String openid);

    LeaderAddressVo getLeaderAddressVoByUserId(Long userId);


    UserLoginVo getUserLoginVo(Long id);
}
