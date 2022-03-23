package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 22:41 2021/8/4
 * @description:
 */
public interface RecommendUserApi {

    /**
     * 查询一位得分最高的推荐用户
     *
     * @param userId
     * @return
     */
    RecommendUser queryWithMaxScore(Long userId);

    /**
     * 按照得分倒序
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询出推荐用户与登陆用户的缘分值
     * @param userId 推荐用户
     * @param toUserId  登陆用户
     * @return
     */
    Double queryUserScore(Long userId,Long toUserId);

    /**
     *
     * @param userId
     * @return
     */
    List<RecommendUser> queryCardList(Long userId,Integer count);
}
