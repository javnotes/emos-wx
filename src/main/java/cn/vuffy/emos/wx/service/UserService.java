package cn.vuffy.emos.wx.service;

/**
 * @description: TODO 类描述
 * @author: luffyhub
 * @date: 2021/11/11
 **/
public interface UserService {

    public int registerUser(String registerCode, String Code, String nickname, String photo);

    @Override
    public Set<String> searchUserPersmissions(int userId) {
        Set<String> permissinos = userDao.serarchPermissions(userId);
    }

}
