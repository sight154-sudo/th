package com.tanhua.sso.controller;

import com.tanhua.sso.service.UserInfoService;
import com.tanhua.sso.vo.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author: tang
 * @date: Create in 22:06 2021/8/3
 * @description:
 */
@RestController
@RequestMapping("user")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 保存用户基本信息
     * @param map
     * @param token
     * @return
     */
    @PostMapping("loginReginfo")
    public ResponseEntity<ErrorResult> saveUserInfo(@RequestBody Map<String,String> map,
                                                    @RequestHeader("Authorization") String token){

        ErrorResult result = userInfoService.saveUserInfo(map,token);

        if(null == result){
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 保存用户头像
     * @param headPhoto
     * @param token
     * @return
     */
    @PostMapping("loginReginfo/head")
    public ResponseEntity<ErrorResult> saveUserHeadPic(@RequestParam("headPhoto")MultipartFile headPhoto,
                                                       @RequestHeader("Authorization") String token){

        Boolean flag = userInfoService.saveUserHeadPic(headPhoto,token);

        if(flag){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResult.builder().errCode("00003").errMessage("用户头像上传失败").build());
    }


}
