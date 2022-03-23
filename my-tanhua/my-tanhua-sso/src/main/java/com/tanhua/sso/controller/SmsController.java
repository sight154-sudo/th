package com.tanhua.sso.controller;

import com.tanhua.sso.service.SmsService;
import com.tanhua.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: tang
 * @date: Create in 10:48 2021/8/2
 * @description:
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class SmsController {


    @Autowired
    private SmsService smsService;
    /**
     * 根据传递过来的手机号，向其发送验证码
     *
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<ErrorResult> sendCheckCode(@RequestBody Map<String,String> param){
        //获取手机号
        String phone = param.get("phone");
        //设置service
        ErrorResult result = null;
        try {
            result = smsService.sendCheckCode(phone);
            //根据返回值结果 判断发送验证码 是否成功  若结果为null 则发送成功 否则发送失败
            if(null == result){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            log.error("发送验证码失败~phone",phone,result);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
