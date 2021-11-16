package cn.vuffy.emos.wx.config;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @description: 常量封装类
 * @author: luf
 * @create: 2021-11-17 00:20
 **/
@Data
@Component
public class SystemConstants {
    public String attendanceStartTime;
    public String attendanceTime;
    public String attendanceEndTime;
    public String closingStartTime;
    public String closingTime;
    public String closingEndTime;
}
