package cn.vuffy.emos.wx.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.vuffy.emos.wx.db.dao.TbUserDao;
import cn.vuffy.emos.wx.exception.EmosException;
import cn.vuffy.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * @description: TODO 类描述
 * @author: luf
 * @date: 2021/11/11
 **/
@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登陆凭证错误");
        }
        return openId;
    }

    @Override
    /**
     * registerCode 注册邀请码
     */
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        // 判断是否是要注册为管理员
        final String adminRegisterCode = "000000";

        // role=[0]对应超级管理员的权限
        // status=1对应员工在职的状态

        if (adminRegisterCode.equals(registerCode)) {
            //查询超级管理员帐户是否已经绑定
            boolean bool = userDao.haveRootUser();
            if (!bool) {
                // 系统中还没有绑定超级管理员
                String openId = getOpenId(code);
                HashMap param = new HashMap();
                param.put("openId", openId);
                param.put("nickname", nickname);
                param.put("photo", photo);
                param.put("role", "[0]");
                param.put("status", 1);
                param.put("createTime", new Date());
                param.put("root", true);
                userDao.insert(param);
                int id = userDao.searchIdByOpenId(openId);
                return id;
            } else {
                // 如果系统中已有管理员账号，则抛出异常
                throw new EmosException("无法绑定超级管理员账户");
            }
        } else {
            // 一般员工的注册
            return 0;
        }

    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = userDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer userId = userDao.searchIdByOpenId(openId);
        if (null == userId) {
            throw new EmosException("账户不存在");
        }
        //TODO 从消息队列中接收消息，转移到消息表
        return userId;
    }
}



