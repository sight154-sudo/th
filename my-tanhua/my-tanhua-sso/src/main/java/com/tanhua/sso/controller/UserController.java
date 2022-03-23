package com.tanhua.sso.controller;

import com.tanhua.common.pojo.User;
import com.tanhua.sso.service.UserService;
import com.tanhua.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 17:52 2021/8/2
 * @description:
 */
@RestController
@RequestMapping("user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("loginVerification")
    public ResponseEntity<Object> loginCheck(@RequestBody Map<String,String> map){
        //获取手机号与验证码
        String phone = map.get("phone");
        String verificationCode = map.get("verificationCode");
        try {
            String obj = userService.login(phone,verificationCode);
            if(StringUtils.isNotBlank(obj)){
                //处理结果
                String[] ss = StringUtils.split(obj, "|");
                Map<String,Object> data = new HashMap<>();
                data.put("token",ss[0]);
                data.put("isNew",Boolean.valueOf(ss[1]));
                return ResponseEntity.ok(data);
            }
        } catch (Exception e) {
            log.error("登陆出错!!",e);
        }
        log.info("验证码输入错误~phone="+phone);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResult("000002","验证码错误~"));
    }


    @GetMapping("{token}")
    public User getUserByToken(@PathVariable String token){
        return userService.getTokenInfo(token);
    }
}
