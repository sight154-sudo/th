package com.tanhua.server.controller;

import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.QuanZiService;
import com.tanhua.server.vo.CommentVo;
import com.tanhua.server.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author: tang
 * @date: Create in 15:28 2021/8/10
 * @description:
 */
@RestController
@Slf4j
@RequestMapping("comments")
public class QuanZiCommentController {


    @Autowired
    private QuanZiService quanZiService;

    @GetMapping
    public ResponseEntity<PageResult> queryCommentList(@RequestParam("movementId")String publishId,
                                                      @RequestParam(value = "page",defaultValue = "1")Integer page,
                                                      @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){

        try {
            PageResult pageResult = quanZiService.queryCommentList(publishId,page,pageSize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("查询评论列表失败~userId = "+ UserThreadLocal.getUser().getId()+" publishId : "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    @PostMapping
    public ResponseEntity saveComment(@RequestBody Map<String,String> map){
        String publishId = map.get("movementId");
        String content = map.get("comment");
        try {
            quanZiService.saveComment(publishId,content);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("提交失败~userId = "+ UserThreadLocal.getUser().getId()+" publishId : "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 点赞评论
     * @param publishId
     * @return
     */
    @GetMapping("{id}/like")
    public ResponseEntity<Object> likeContent(@PathVariable("id") String publishId){
        try {
            Long count = quanZiService.likeContent(publishId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户点赞失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}/dislike")
    public ResponseEntity<Object> dislikeContent(@PathVariable("id") String publishId){
        try {
            Long count = quanZiService.dislikeContent(publishId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("用户取消点赞失败~userId = "+UserThreadLocal.getUser()+" publishId: "+publishId,e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
