package com.tanhua.server.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.TanHuaService;
import com.tanhua.server.vo.NearUserVo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.TodayBest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 14:55 2021/8/14
 * @description:
 */
@RestController
@RequestMapping("tanhua")
@Slf4j
public class TanHuaController {

    @Autowired
    private TanHuaService tanHuaService;

    /**
     * 查询佳人的详情信息
     * @param userId
     * @return
     */
    @GetMapping("{id}/personalInfo")
    public ResponseEntity<TodayBest> queryUserInfo(@PathVariable("id")Long userId){

        try {
            TodayBest todayBest = tanHuaService.queryUserInfo(userId);
            if(null != todayBest){
                return ResponseEntity.ok(todayBest);
            }
        } catch (Exception e) {
            log.error("根据用户佳人信息失败~userId = "+ UserThreadLocal.getUser().getId(),e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查看问题
     * @param userId
     * @return
     */
    @GetMapping("strangerQuestions")
    public ResponseEntity<String> queryQuestion(@RequestParam("userId") Long userId) {
        try {
            String question = tanHuaService.queryQuestion(userId);
            if (ObjectUtil.isNotEmpty(question)) {
                return ResponseEntity.ok(question);
            }
        } catch (Exception e) {
            log.error("获取陌生人问题失败~userId = " + UserThreadLocal.getUser(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 回复陌生人消息
     * @param map
     * @return
     */
    @PostMapping("strangerQuestions")
    public ResponseEntity<Void>  replyQuestion(@RequestBody Map<String,Object> map){
        try {
            tanHuaService.replyQuestion(map);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("回复陌生人问题失败~userId = " + UserThreadLocal.getUser(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @GetMapping("search")
    public ResponseEntity<List<NearUserVo>> searchNear(@RequestParam(value = "gender",required = false)String gender,
                                                       @RequestParam(value = "distance",defaultValue = "2000")Double distance){
        try {
            List<NearUserVo> list = tanHuaService.searchNear(gender,distance);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("搜索附近失败~userId = " + UserThreadLocal.getUser(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 展示探花列表
     * @return
     */
    @GetMapping("cards")
    public ResponseEntity<List<TodayBest>> queryCardsList(){

        try {
            List<TodayBest> list = tanHuaService.queryCardsList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("查询探花列表失败~userId = " + UserThreadLocal.getUser(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 右滑喜欢
     * @param likeUserId
     * @return
     */
    @GetMapping("{id}/love")
    public ResponseEntity<Void> userLike(@PathVariable("id") Long likeUserId){

        try {
            tanHuaService.userLike(likeUserId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("探花喜欢~userId = " + UserThreadLocal.getUser(), e);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 左滑不喜欢
     * @param likeUserId
     * @return
     */
    @GetMapping("{id}/unlove")
    public ResponseEntity<Void> userNotLike(@PathVariable("id") Long likeUserId){

        try {
            tanHuaService.userNotLike(likeUserId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("探花不喜欢~userId = " + UserThreadLocal.getUser(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
