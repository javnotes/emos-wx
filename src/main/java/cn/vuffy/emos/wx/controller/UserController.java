package cn.vuffy.emos.wx.controller;

import cn.vuffy.emos.wx.common.util.R;
import cn.vuffy.emos.wx.config.shiro.JwtUtil;
import cn.vuffy.emos.wx.controller.form.LoginForm;
import cn.vuffy.emos.wx.controller.form.RegisterForm;
import cn.vuffy.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @description: 处理移动端提交的请求
 * @author: luf
 * @date: 2021/11/11
 **/

@RestController
@RequestMapping("/user")
@Api("用户模块web接口")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    /**
     * @param form 客户端提交过来的数据
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm form) {
        int userId = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        // 根据用户的主键id生成token
        String token = jwtUtil.createToken(userId);
        Set<String> permsSet = userService.searchUserPermissions(userId);
        savaCacheToken(token, userId);
        return R.ok("用户注册成功").put("token", token).put("permission", permsSet);
    }

    @PostMapping("/login")
    @ApiOperation("登录系统")
    public R login(@Valid @RequestBody LoginForm form) {
        int id = userService.login(form.getCode());
        String token = jwtUtil.createToken(id);
        Set<String> permsSet = userService.searchUserPermissions(id);
        savaCacheToken(token, id);
        return R.ok("登录成功").put("token", token).put("permissino", permsSet);
    }

    /**
     * 将token保存至Redis
     *
     * @param token
     * @param userId
     */
    private void savaCacheToken(String token, int userId) {
        redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
    }
}
