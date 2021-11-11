package cn.vuffy.emos.wx.service;

import java.util.Set;

/**
 * @description: TODO 类描述
 * @author: luf
 * @date: 2021/11/11
 **/
public interface UserService {
    public int registerUser(String registerCode, String code, String nickname, String photo);

    public Set<String> searchUserPermissions(int userId);

    public Integer login(String code);


}
