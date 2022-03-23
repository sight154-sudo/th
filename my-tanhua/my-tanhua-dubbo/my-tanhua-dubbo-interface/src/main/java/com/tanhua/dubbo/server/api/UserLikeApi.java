package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.UserLike;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 18:50 2021/8/15
 * @description:  探花功能接口
 */
public interface UserLikeApi {

    /**
     * 右滑用户喜欢
     * @param userId
     * @param likeUserId
     * @return
     */
    void userLike(Long userId,Long likeUserId);

    /**
     * 左滑用户不喜欢
     * @param userId
     * @param dislikeUserId
     * @return
     */
    void userdisLike(Long userId,Long dislikeUserId);

    /**
     * 查询用户是否相互喜欢
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isMutualLike(Long userId,Long likeUserId);

    /**
     * 判断用户是否喜欢
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isLike(Long userId,Long likeUserId);

    /**
     * 判断用户是否不喜欢
     * @param userId
     * @param dislikeUserId
     * @return
     */
    Boolean isNotLike(Long userId,Long dislikeUserId);
    /**
     * 查询用户喜欢的列表
     * @param userId
     * @return
     */
    List<Long> queryUserLikeList(Long userId);

    /**
     * 查询用户不喜欢的列表
     * @param userId
     * @return
     */
    List<Long> queryUserNotLikeList(Long userId);

    /**
     * 相互喜欢的数量
     * @return
     */
    Long queryMutualLikeCount(Long userId);

    /**
     * 喜欢数
     * @return
     */
    Long queryLikeCount(Long userId);

    /**
     * 粉丝数
     * @return
     */
    Long queryFanCount(Long userId);

    /**
     * 查询用户互相喜欢的列表
     * @param userId
     * @return
     */
    PageInfo<UserLike> queryMutualLikeList(Long userId,Integer page,Integer pagesize);

    /**
     * 查询用户喜欢人列表
     * @param userId
     * @return
     */
    PageInfo<UserLike> queryLikeList(Long userId,Integer page,Integer pagesize);

    /**
     *  查询用户粉丝列表
     * @param userId
     * @return
     */
    PageInfo<UserLike> queryFanList(Long userId,Integer page,Integer pagesize);

    void cancelUserLike(Long userId,Long disLikeUserId);
}
