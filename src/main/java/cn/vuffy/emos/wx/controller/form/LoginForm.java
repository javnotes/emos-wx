package cn.vuffy.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description: 封装客户端提交的数据
 * @author: luf
 * @date: 2021/11/11
 **/

@ApiModel
@Data
public class LoginForm {

    @NotBlank(message = "临时授权不能为空")
    private String code;

}
