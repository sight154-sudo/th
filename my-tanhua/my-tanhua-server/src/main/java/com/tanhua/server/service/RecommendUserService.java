package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.dubbo.server.api.RecommendUserApi;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.vo.TodayBest;
import org.springframework.stereotype.Service;

/**
 * @author: tang
 * @date: Create in 11:35 2021/8/5
 * @description: 调用远程服务 查询出今日佳人
 */
@Service
public class RecommendUserService {

    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    /**
     * 根据用户id  查询出推荐的今日佳人
     * @param id
     * @return
     */
    public TodayBest queryTodayBest(Long id) {
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(id);
        if(null == recommendUser){
            return null;
        }
        double fateValue = Math.floor(recommendUser.getScore());

        return TodayBest.builder().id(recommendUser.getUserId()).fateValue(Double.valueOf(fateValue).longValue()).build();
    }

    /**
     * 根据用户id 查询出关联的用户
     * @param userId
     * @return
     */
    public PageInfo<RecommendUser> queryRecommentList(Long userId,Integer pageNum,Integer pageSize) {
        return recommendUserApi.queryPageInfo(userId, pageNum, pageSize);

    }


    /**
     * 获取缘分值
     * @param userId
     * @return
     */
    public Double queryFateValue(Long userId,Long toUserId){
        return recommendUserApi.queryUserScore(userId,toUserId);
    }
}
