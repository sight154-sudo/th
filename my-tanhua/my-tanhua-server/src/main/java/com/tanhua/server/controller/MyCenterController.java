package com.tanhua.server.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tanhua.common.pojo.Settings;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.common.utils.Cache;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.MyCenterService;
import com.tanhua.server.vo.CountsVo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.SettingsVo;
import com.tanhua.server.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author: tang
 * @date: Create in 12:58 2021/8/13
 * @description:
 */
@RestController
@RequestMapping("users")
@Slf4j
public class MyCenterController {

    @Autowired
    private MyCenterService myCenterService;

    /**
     * 查询聊天用户的详情信息
     * @param userId
     * @param huanxinId
     * @return
     */
    @GetMapping
    @Cache
    public ResponseEntity<UserInfoVo> queryUserInfo(@RequestParam(value = "userID", required = false) Long userId,
                                                    @RequestParam(value = "huanxinID", required = false) String huanxinId) {
        try {
            UserInfoVo userInfoVo = myCenterService.queryUserInfo(userId, huanxinId);
            if (ObjectUtil.isNotEmpty(userInfoVo)) {
                return ResponseEntity.ok(userInfoVo);
            }
        } catch (Exception e) {
            log.error("查询用户信息失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 更新用户信息
     * @param userInfoVo
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> saveUserInfo(@RequestBody UserInfoVo userInfoVo) {
        System.out.println(userInfoVo);
        try {
            myCenterService.saveUserInfo(userInfoVo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("更新用户信息失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }



    @GetMapping("{uid}/alreadyLove")
    public ResponseEntity<Boolean> queryUserAlreadyLove(@PathVariable("uid") Long userId){
        try {
            Boolean flag = myCenterService.queryUserAlreadyLove(userId);
            if (ObjectUtil.isNotEmpty(flag)) {
                return ResponseEntity.ok(flag);
            }
        } catch (Exception e) {
            log.error("查询用户是否已喜欢失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    /**
     * 查询用户的喜欢 粉丝 互相喜欢数
     * @return
     */
    @GetMapping("counts")
    public ResponseEntity<CountsVo> queryCounts(){
        try {
            CountsVo countsVo = myCenterService.queryCounts();
            if (ObjectUtil.isNotEmpty(countsVo)) {
                return ResponseEntity.ok(countsVo);
            }
        } catch (Exception e) {
            log.error("查询用户统计数失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询用户统一关注，访问等列表
     * @param type
     * @param page
     * @param pagesize
     * @param nickname
     * @return
     */
    @GetMapping("friends/{type}")
    public ResponseEntity<PageResult> queryGeneralList(@PathVariable("type") String type,
                                                       @RequestParam(value = "page",defaultValue = "1")Integer page,
                                                       @RequestParam(value = "pagesize",defaultValue = "10")Integer pagesize,
                                                       @RequestParam(value = "nickname",required = false)String nickname){
        try {
            PageResult pageResult = myCenterService.queryCeneralList(type,page,pagesize,nickname);
            if (ObjectUtil.isNotEmpty(pageResult)) {
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            log.error("查询用户统一关注，访问等列表失败~userId = "+ UserThreadLocal.getUser(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 取消用户喜欢
     * @param userId
     * @return
     */
    @DeleteMapping("like/{uid}")
    public ResponseEntity<Void> cancelUserLike(@PathVariable("uid") Long userId){

        try {
            myCenterService.cancelUserLike(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("取消用户喜欢失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 用户喜欢粉丝
     * @param userId
     * @return
     */
    @PostMapping("fans/{uid}")
    public ResponseEntity<Void> userlikeFans(@PathVariable("uid") Long userId){
        try {
            myCenterService.userlikeFans(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("用户喜欢粉丝失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询用户通用设置
     * @return
     */
    @GetMapping("settings")
    public ResponseEntity<SettingsVo> queryGlobalSettings(){
        try {
            SettingsVo settingsVo = myCenterService.querySettings();
            return ResponseEntity.ok(settingsVo);
        } catch (Exception e) {
            log.error("查询用户通用设置失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 保存用户通用设置
     * @param settings
     * @return
     */
    @PostMapping("notifications/setting")
    public ResponseEntity<Void> saveSettings(@RequestBody Settings settings){

        try {
            myCenterService.saveGlobalSetting(settings);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("保存用户通用设置失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 设置用户陌生人问题
     * @return
     */
    @PostMapping("questions")
    public ResponseEntity<Void> setUserQuestion(@RequestBody Map<String,String> map){
        try {
            myCenterService.setUserQuestion(map);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("保存用户陌生人问题失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询用户黑名单列表
     * @return
     */
    @GetMapping("blacklist")
    public ResponseEntity<PageResult> queryBlacklist(@RequestParam(value = "page",defaultValue = "1")Integer page,
                                                     @RequestParam(value = "pagesize",defaultValue = "10") Integer pagesize){
        try {
            PageResult pageResult = myCenterService.queryBlacklist(page,pagesize);
            if(ObjectUtil.isNotEmpty(pageResult)){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            log.error("查询用户黑名单失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 移除黑名单
     * @param userId
     * @return
     */
    @DeleteMapping("blacklist/{uid}")
    public ResponseEntity<Void> removeBlackList(@PathVariable("uid") Long userId){
        try {
            myCenterService.removeBlackList(userId);
        } catch (Exception e) {
            log.error("查询用户黑名单失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
