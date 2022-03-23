package com.tanhua.common.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: tang
 * @date: Create in 9:55 2021/8/13
 * @description:  环信用户
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_huanxin_user")
public class HuanXinUser implements Serializable {

    private Long id; //主键Id

    /**
     * 环信 ID ;也就是 IM 用户名的唯一登录账号，长度不可超过64个字符长度
     */
    private String username;
    /**
     * 登录密码，长度不可超过64个字符长度
     */
    private String password;
    /**
     * 昵称（可选），在 iOS Apns 推送时会使用的昵称（仅在推送通知栏内显示的昵称），
     * 并不是用户个人信息的昵称，环信是不保存用户昵称，头像等个人信息的，
     * 需要自己服务器保存并与给自己用户注册的IM用户名绑定，长度不可超过100个字符
     */
    private String nickname;

    private Long userId; //用户id

    private Date created; //创建时间

    private Date updated; //更新时间
}
