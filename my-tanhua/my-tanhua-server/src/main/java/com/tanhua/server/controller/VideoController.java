package com.tanhua.server.controller;

import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.VideoService;
import com.tanhua.server.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author: tang
 * @date: Create in 23:42 2021/8/10
 * @description:
 */
@RestController
@RequestMapping("smallVideos")
@Slf4j
public class VideoController {

    @Autowired
    private VideoService videoService;


    @PostMapping
    public ResponseEntity uploadVideo(@RequestParam("videoThumbnail") MultipartFile videoThumbnail,
                                      @RequestParam("videoFile")MultipartFile videoFile){

        try {
            videoService.uploadVideo(videoThumbnail,videoFile);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("小视频上传失败~userId = "+ UserThreadLocal.getUser().getId(),e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping
    public ResponseEntity<PageResult> queryVideoList(@RequestParam(value = "page",defaultValue = "1")Integer page,
                                                     @RequestParam(value = "pagesize",defaultValue = "10")Integer pagesize){

        try {
            PageResult pageresult = videoService.queryVideoList(page,pagesize);
            return ResponseEntity.ok(pageresult);
        } catch (Exception e) {
            log.error("查询视频列表失败~userId = "+UserThreadLocal.getUser().getId(),e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


    }


    @PostMapping("{id}/like")
    public ResponseEntity<Long> videoLike(@PathVariable("id")String videoId){
        try {
            Long count = videoService.likeVideo(videoId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户点赞视频失败~userId = "+UserThreadLocal.getUser()+" publishId: "+videoId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    @PostMapping("{id}/dislike")
    public ResponseEntity<Long> videodisLike(@PathVariable("id")String videoId){
        try {
            Long count = videoService.dislikeVideo(videoId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户点赞视频失败~userId = "+UserThreadLocal.getUser()+" publishId: "+videoId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}/comments")
    public ResponseEntity<PageResult> queryVideoContent(@PathVariable("id")String videoId,
                                                        @RequestParam(value = "page",defaultValue = "1")Integer page,
                                                        @RequestParam(value = "pagesize",defaultValue = "10")Integer pagesize){
        try {
            PageResult pageresult = videoService.queryVideoContentList(videoId,page,pagesize);
            return ResponseEntity.ok(pageresult);
        } catch (Exception e) {
            log.error("查询视频评论列表失败~userId = "+UserThreadLocal.getUser().getId(),e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 点赞评论
     * @param publishId
     * @return
     */
    @PostMapping("comments/{id}/like")
    public ResponseEntity<Object> likeContent(@PathVariable("id") String publishId){
        try {
            Long count = videoService.likeContent(publishId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户点赞小视频评论失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 点赞评论
     * @param publishId
     * @return
     */
    @PostMapping("comments/{id}/dislike")
    public ResponseEntity<Object> dislikeContent(@PathVariable("id") String publishId){
        try {
            Long count = videoService.dislikeContent(publishId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户取消点赞小视频评论失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 发布评论
     * @param map
     * @return
     */
    @PostMapping("{id}/comments")
    public ResponseEntity saveComment(@RequestBody Map<String,String> map,
                                      @PathVariable("id")String publishId){
        String content = map.get("comment");
        try {
            videoService.saveComment(publishId,content);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("评论小视频失败~userId = "+ UserThreadLocal.getUser().getId()+" publishId : "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @PostMapping("{uid}/userFocus")
    public ResponseEntity followUser(@PathVariable("uid") Long followUserId){


        try {
            videoService.followUser(followUserId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("关注用户失败~userId = "+ UserThreadLocal.getUser().getId()+" followUserId : "+followUserId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @PostMapping("{uid}/userUnFocus")
    public ResponseEntity disfollowUser(@PathVariable("uid") Long followUserId){


        try {
            videoService.disfollowUser(followUserId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("关注用户失败~userId = "+ UserThreadLocal.getUser().getId()+" followUserId : "+followUserId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
