package com.tanhua.server.controller;

import com.tanhua.common.utils.Cache;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.TodayBestService;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author: tang
 * @date: Create in 10:36 2021/8/5
 * @description:
 */
@RestController
@RequestMapping("tanhua")
@Slf4j
public class TodayBestController {

    @Autowired
    private TodayBestService todayBestService;

    /**
     * 根据登陆用户的id查询出推荐的今日佳人信息
     * @return
     */
    @GetMapping("todayBest")
    public ResponseEntity<TodayBest> findTodayBest(){

        try {
            TodayBest todayBest = todayBestService.queryTodayBest();
            if(null != todayBest){
                return ResponseEntity.ok(todayBest);
            }
        } catch (Exception e) {
            log.error("根据用户id查询今日佳人信息失败~userId = "+ UserThreadLocal.getUser().getId(),e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询出用户推荐列表
     * @param queryParam
     * @return
     */
    @GetMapping("recommendation")
    @Cache(time = "60")
    public ResponseEntity<PageResult> queryTodayBestList(RecommendUserQueryParam queryParam){
        PageResult pageResult = null;
        try {
            pageResult = this.todayBestService.queryRecommendation(queryParam);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("查询用户推荐列表出错! userId = "+UserThreadLocal.getUser().getId(),e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pageResult);
    }
}
