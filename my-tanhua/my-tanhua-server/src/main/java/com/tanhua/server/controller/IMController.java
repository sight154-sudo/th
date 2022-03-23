package com.tanhua.server.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tanhua.server.service.AnnouncementService;
import com.tanhua.server.service.IMService;
import com.tanhua.server.vo.MessageCommentVo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author: tang
 * @date: Create in 12:42 2021/8/13
 * @description:
 */
@RestController
@RequestMapping("messages")
@Slf4j
public class IMController {

    @Autowired
    private IMService imService;

    @Autowired
    private AnnouncementService announcementService;


    /**
     * 通过id查询环信用户信息
     * @param huanxinId
     * @return
     */
    @GetMapping("userinfo")
    public ResponseEntity<UserInfoVo> queryUserInfoByHuanXin(@RequestParam("huanxinId") String huanxinId) {

        try {
            UserInfoVo userInfoVo = imService.queryUserInfoByHuanXin(huanxinId);
            if (ObjectUtil.isNotEmpty(userInfoVo)) {
                return ResponseEntity.ok(userInfoVo);
            }
        } catch (Exception e) {
            log.error("通过环信查询用户信息失败~huanxinId = " + huanxinId, e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 联系人添加
     *
     * @param map
     * @return
     */
    @PostMapping("contacts")
    public ResponseEntity saveLinkedUser(@RequestBody Map<String, Long> map) {
        Long friendId = map.get("userId");
        try {
            imService.saveLinkedUser(friendId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("添加联系人失败失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    @GetMapping("contacts")
    public ResponseEntity<PageResult> queryUsersList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                     @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize,
                                                     @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            PageResult pageResult = imService.queryUsersList(page, pagesize, keyword);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("查询联系人列表失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询用户喜欢列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("likes")
    public ResponseEntity<PageResult> queryLikeCommentList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                 @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize) {
        try {
            PageResult pageResult = imService.queryLikeCommentList(page, pagesize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("查询点赞列表失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询用户喜欢列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("loves")
    public ResponseEntity<PageResult> queryLoveCommentList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize) {
        try {
            PageResult pageResult = imService.queryLoveCommentList(page, pagesize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("查询喜欢列表失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询用户评论列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("comments")
    public ResponseEntity<PageResult> queryUserCommentList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize) {
        try {
            PageResult pageResult = imService.queryUserCommentList(page, pagesize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("查询评论列表失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询系统公告
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("announcements")
    public ResponseEntity<PageResult> queryAnnouncements(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                         @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize){

        try {
            PageResult pageResult = announcementService.queryAnnouncement(page, pagesize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("查询公告失败~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }
}

