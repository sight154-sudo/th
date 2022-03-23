package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Comment;
import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.vo.PageInfo;

/**
 * @author: tang
 * @date: Create in 16:15 2021/8/7
 * @description: 查询出好友动态的接口
 */
 public interface QuanZiApi {

    /**
     * 查询好友动态列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
     PageInfo<Publish> queryPublishList(Long userId,Integer page,Integer pageSize);

    /**
     * 保存动态
     * @param publish
     * @return
     */
     String savePublish(Publish publish);

    /**
     * 查询推荐动态列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
     PageInfo<Publish> queryRecommendPublishList(Long userId,Integer page,Integer pageSize);
    /**
     * 查询推荐动态列表  修改
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
     PageInfo<Publish> queryRecommendPublishListEX(Long userId, Integer page, Integer pageSize);
    /**
     * 根据id查询动态
     * @param id
     * @return
     */
     Publish getPublishById(String id);

    /**
     * 点赞
     * @return
     */
     Boolean likeComment(Long userId,String publishId);

    /**
     * 取消点赞
     * @param userId
     * @param publishId
     * @return
     */
     Boolean dislikeComment(Long userId,String publishId);

    /**
     * 查询点赞数
     * @param publishId
     * @return
     */
     Long queryLikeCount(String publishId);

    /**
     * 查询用户是否点赞该动态
     * @param userId
     * @param publishId
     * @return
     */
     Boolean queryUserIsLike(Long userId,String publishId);



    /**
     * 喜欢
     * @return
     */
     Boolean loveComment(Long userId,String publishId);

    /**
     * 取消喜欢
     * @param userId
     * @param publishId
     * @return
     */
     Boolean disloveComment(Long userId,String publishId);

    /**
     * 查询喜欢数
     * @param publishId
     * @return
     */
    Long queryLoveCount(String publishId);

    /**
     * 查询用户是否喜欢该动态
     * @param userId
     * @param publishId
     * @return
     */
    Boolean queryUserIsLove(Long userId,String publishId);

    /**
     * 查询评论列表
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Comment> queryCommentList(String publishId,Integer page,Integer pageSize);

    /**
     * 保存评论
     * @param userId
     * @param publishId
     * @param content
     * @return
     */
     Boolean saveComment(Long userId,String publishId,String content);

    /**
     * 查询评论数
     * @param publishId
     * @return
     */
     Long queryCommentCount(String publishId);

    /**
     * 喜欢
     * @return
     */
     Boolean likeContent(Long userId,String publishId);

    /**
     * 取消喜欢
     * @param userId
     * @param publishId
     * @return
     */
     Boolean dislikeContent(Long userId,String publishId);

    /**
     * 查询点赞列表
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageInfo<Comment> queryLikeCommentList(Long userId,Integer page,Integer pagesize);

    /**
     * 查询喜欢列表
      * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageInfo<Comment> queryLoveCommentList(Long userId,Integer page,Integer pagesize);

    /**
     * 查询评论列表
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageInfo<Comment> queryCommentListByUser(Long userId,Integer page,Integer pagesize);


    PageInfo<Publish> queryAlbumList(Long userId,Integer page,Integer pagesize);
}
