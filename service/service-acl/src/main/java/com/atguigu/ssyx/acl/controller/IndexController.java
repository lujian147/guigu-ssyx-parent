package com.atguigu.ssyx.acl.controller;


import com.atguigu.ssyx.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "登录接口")
@RestController
@RequestMapping("/admin/acl/index")
//@CrossOrigin //解决跨域问题
public class IndexController {



    //登录方法
    @ApiOperation("登录")
    @PostMapping("login")
    public Result login(){
        Map<String,String> map = new HashMap<>();
        map.put("token","admin-token");
        return Result.ok(map);
    }

    @ApiOperation("获取信息")
    @GetMapping("info")
    public Result getInfo(){
        Map<String,String> map = new HashMap<>();
//        map.put();
        return Result.ok(map);
    }


    @ApiOperation("退出")
    @PostMapping("logout")
    public Result logout(){
        return Result.ok(null);
    }

}
