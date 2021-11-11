package cn.vuffy.emos.wx.controller.form;

import jdk.jfr.BooleanFlag;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @description: TODO 类描述
 * @author: luffyhub
 * @date: 2021/11/11
 **/
public class RegisterForm {

    @NotBlank(message = "注册码不能为空")
    @Pattern(regexp = "")
    private String registerCode;

    @NotBlank(message = "微信你临时授权码不能为空")
    private String code;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "头像不能为空")
    private String photo;
}
