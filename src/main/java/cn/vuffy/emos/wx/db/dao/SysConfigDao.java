package cn.vuffy.emos.wx.db.dao;

import cn.vuffy.emos.wx.db.pojo.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysConfigDao {
    public List<SysConfig> selectAllParam();
}