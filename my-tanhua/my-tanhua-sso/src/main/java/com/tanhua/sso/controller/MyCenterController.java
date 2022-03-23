package com.tanhua.sso.controller;

import com.mysql.fabric.HashShardMapping;
import com.tanhua.sso.service.MyCenterService;
import com.tanhua.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 15:10 2021/8/16
 * @description:
 */
@RestController
@RequestMapping("users")
@Slf4j
public class MyCenterController {

    @Autowired
    private UserInfoController userInfoController;

    @Autowired
    private MyCenterService myCenterService;


    /**
     * 上传头像
     * @param file
     * @param token
     * @return
     */
    @PostMapping("header")
    public ResponseEntity<ErrorResult> saveLogo(@RequestParam("headPhoto") MultipartFile file,
                                                @RequestHeader("Authorization") String token) {
        return this.userInfoController.saveUserHeadPic(file, token);
    }

    /**
     * 修改手机号，发送验证码
     * @param token
     * @return
     */
    @PostMapping("phone/sendVerificationCode")
    public ResponseEntity<ErrorResult> sendVerificationCode(@RequestHeader("Authorization") String token){
        try {
            myCenterService.sendVerificationCode(token);
            //根据返回值结果 判断发送验证码 是否成功  若结果为null 则发送成功 否则发送失败
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            log.error("修改手机号时发送验证码失败~",e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("phone/checkVerificationCode")
    public ResponseEntity<Object> checkVerificationCode(@RequestBody Map<String,String> map,
                                                         @RequestHeader("Authorization") String token){
        try {
            Boolean flag = myCenterService.checkVerificationCode(map,token);
            //根据返回值结果 判断发送验证码 是否成功  若结果为null 则发送成功 否则发送失败
            Map<String,Boolean> result = new HashMap<>();
            result.put("verification",flag);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("修改手机号时校验验证码失败~",e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 保存用户修改的手机号
     * @param map
     * @param token
     * @return
     */
    @PostMapping("phone")
    public ResponseEntity<Void> saveUserPhone(@RequestBody Map<String,String> map,
                                              @RequestHeader("Authorization") String token){
        try {
            Boolean flag = myCenterService.saveUserPhone(map,token);
            //根据返回值结果 判断发送验证码 是否成功  若结果为null 则发送成功 否则发送失败
            if(flag){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            log.error("修改手机失败~",e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
