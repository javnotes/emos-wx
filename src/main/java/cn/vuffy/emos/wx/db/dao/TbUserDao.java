package cn.vuffy.emos.wx.db.dao;

import cn.vuffy.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbUserDao {
    public boolean haveRootUser();

    public Integer searchIdByOpenId(String openId);

    public int insert(HashMap param);

}