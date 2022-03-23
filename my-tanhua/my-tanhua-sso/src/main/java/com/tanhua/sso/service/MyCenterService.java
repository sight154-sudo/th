package com.tanhua.sso.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tanhua.common.pojo.User;
import com.tanhua.sso.vo.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Struct;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 20:54 2021/8/17
 * @description:
 */
@Service
public class MyCenterService {

    @Autowired
    private SmsService smsService;
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 更改手机号  发送验证码
     * @param token
     * @return
     */
    public Boolean sendVerificationCode(String token) {
        //校验token
        User user = userService.getTokenInfo(token);
        if(ObjectUtil.isEmpty(user)){
            return false;
        }
        //向用户手机发送验证码
        ErrorResult errorResult = smsService.sendCheckCode(user.getMobile());
        return errorResult == null;

    }

    /**
     * 更改手机号 校验验证码
     * @param map
     * @param token
     * @return
     */
    public Boolean checkVerificationCode(Map<String, String> map, String token) {
        String checkCode = map.get("verificationCode");
        if(StrUtil.isEmpty(checkCode)){
            return false;
        }
        User user = userService.getTokenInfo(token);
        if(ObjectUtil.isEmpty(user)){
            return false;
        }
        //校验验证码
        String redisKey = "CHECK_CODE_"+user.getMobile();
        String code = redisTemplate.opsForValue().get(redisKey);
        if(StrUtil.isEmpty(code) || !(StrUtil.equals(code,checkCode))){
            return false;
        }
        //校验成功后删除redis中的数据。
        this.redisTemplate.delete(redisKey);
        return true;
    }

    /**
     * 保存用户修改的手机号
     * @param map
     * @param token
     */
    public Boolean saveUserPhone(Map<String, String> map, String token) {
        String phone = map.get("phone");
        if(StrUtil.isEmpty(phone)){
            return false;
        }
        User user = userService.getTokenInfo(token);
        if(ObjectUtil.isEmpty(user)){
            return false;
        }
        //保存用户手机号 若手机号已注册 则需要重新输入新手机号
        Boolean flag = this.userService.mobileIsExists(phone);
        if(flag){
            //手机号已注册
            return false;
        }
        //修改手机号
        try {
            this.userService.updateMobile(user.getId(),phone);
            //用户更改手机号成功后 删除redis中缓存的手机号数据
            String redisKey = "TANHUA_USER_MOBILE_"+user.getId();
            this.redisTemplate.delete(redisKey);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("手机号更换失败");
        }
    }
}
