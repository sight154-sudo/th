package com.tanhua.server.controller;

import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.QuanZiService;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.QuanZiVo;
import com.tanhua.server.vo.VisitorsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 18:09 2021/8/7
 * @description:
 */
@RestController
@RequestMapping("movements")
@Slf4j
public class QuanZiController {

    @Autowired
    private QuanZiService quanZiService;

    /**
     * 展示好友动态
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping
    public ResponseEntity<PageResult> queryMoveMents(@RequestParam(value = "page",defaultValue = "1")Integer page,
                                                     @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){

        try {
            PageResult result = quanZiService.queryMoveMents(page,pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询用户好友动态失败~",e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 保存用户的动态
     * @param textContent
     * @param file
     * @param location
     * @param longitude
     * @param latitude
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> savePublish(@RequestParam("textContent") String textContent,
                                            @RequestParam("imageContent")MultipartFile[] file,
                                            @RequestParam("location") String location,
                                            @RequestParam("longitude") String longitude,
                                            @RequestParam("latitude") String latitude){
        User user = UserThreadLocal.getUser();
        try {
            String publishId = quanZiService.savePublish(textContent,file,location,longitude,latitude);
            if(StringUtils.isNotBlank(publishId)){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            log.error("保存用户好友动态失败~userId = "+user.getId(),e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询用户推荐动态
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("recommend")
    public ResponseEntity<PageResult> queryRecommendQuanziList(@RequestParam(value = "page",defaultValue = "1")Integer page,
                                                     @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){

        try {
            PageResult result = quanZiService.queryRecommendQuanziList(page,pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询用户推荐动态失败~",e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}/like")
    public ResponseEntity<Object> likeComment(@PathVariable("id") String publishId){
        try {
            Long count = quanZiService.likeComment(publishId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户点赞失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}/dislike")
    public ResponseEntity<Object> dislikeComment(@PathVariable("id") String publishId){
        try {
            Long count = quanZiService.dislikeComment(publishId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户取消点赞失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}/love")
    public ResponseEntity<Object>  loveComment(@PathVariable("id") String publishId){
        try {
            Long count = quanZiService.loveComment(publishId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户点赞失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}/unlove")
    public ResponseEntity<Object> disloveComment(@PathVariable("id") String publishId){
        try {
            Long count = quanZiService.disloveComment(publishId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户取消点赞失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}")
    public ResponseEntity<QuanZiVo> querySinglogPublish(@PathVariable("id")String publishId){
        try {
            QuanZiVo quanZiVo = quanZiService.querySinglonPublish(publishId);
            return ResponseEntity.ok(quanZiVo);
        } catch (Exception e) {
            log.error("用户单条动态查询失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询访客
     * @return
     */
    @GetMapping("visitors")
    public ResponseEntity<List<VisitorsVo>> queryVisitorsList(){
        try {
            List<VisitorsVo> list = this.quanZiService.queryVisitorsList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询用户动态
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("all")
    public ResponseEntity<PageResult> queryUserPublish(@RequestParam("userId")Long userId,
                                                       @RequestParam(value = "page",defaultValue = "1")Integer page,
                                                       @RequestParam(value = "pagesize",defaultValue = "10")Integer pagesize){
        try {
            PageResult pageResult = this.quanZiService.queryAlbumList(userId,page,pagesize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("查询用户动态失败~",e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
