package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Video;
import com.tanhua.dubbo.server.vo.PageInfo;

/**
 * @author: tang
 * @date: Create in 21:57 2021/8/10
 * @description:
 */
public interface VideoApi {

    /**
     * 保存小视频
     *
     * @param video
     * @return 保存成功后，返回视频id
     */
    String saveVideo(Video video);

    Video queryVideoById(String videoId);
    /**
     * 查询推荐小视频的列表
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageInfo<Video> queryVideoList(Long userId,Integer page,Integer pagesize);
    /**
     * 查询推荐小视频的列表 修改
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageInfo<Video> queryVideoListEx(Long userId,Integer page,Integer pagesize);

    /**
     * 关注用户
     * @param userId
     * @param followUserId
     * @return
     */
    Boolean followUser(Long userId,Long followUserId);

    /**
     * 取消关注
     * @param userId
     * @param followUserId
     * @return
     */
    Boolean disFollowUser(Long userId,Long followUserId);

    /**
     * 判断用户是否关注
     * @param userId
     * @param followUserId
     * @return
     */
    Boolean isFollowUser(Long userId,Long followUserId);

}
