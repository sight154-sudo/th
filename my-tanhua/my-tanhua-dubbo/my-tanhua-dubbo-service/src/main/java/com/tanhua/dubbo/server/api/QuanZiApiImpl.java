package com.tanhua.dubbo.server.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.enums.CommentType;
import com.tanhua.dubbo.server.enums.IdType;
import com.tanhua.dubbo.server.pojo.*;
import com.tanhua.dubbo.server.service.IdService;
import com.tanhua.dubbo.server.service.TimeLineService;
import com.tanhua.dubbo.server.vo.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 16:21 2021/8/7
 * @description:
 */
//@Service
@Service(version = "1.0.0", timeout = 10000)
@Slf4j
public class QuanZiApiImpl implements QuanZiApi {

    //评论数据存储在Redis中key的前缀
    private static final String COMMENT_REDIS_KEY_PREFIX = "QUANZI_COMMENT_";

    //用户是否点赞的前缀
    private static final String COMMENT_USER_LIEK_REDIS_KEY_PREFIX = "USER_LIKE_";

    //用户是否喜欢的前缀
    private static final String COMMENT_USER_LOVE_REDIS_KEY_PREFIX = "USER_LOVE_";

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdService idService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TimeLineService timeLineService;


    /**
     * 查询动态列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize) {
        Query queryTimeLine = new Query();
        queryTimeLine.limit(pageSize)
                .skip((page - 1) * pageSize)
                .with(Sort.by(Sort.Direction.DESC, "date"));
        PageInfo pageInfo = new PageInfo();
        pageInfo.setCurrentPage(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0);
        //查询用户下的好友动态的时间线数据
        List<TimeLine> timeLineList = mongoTemplate.find(queryTimeLine, TimeLine.class, "quanzi_time_line_" + userId);
        if (CollUtil.isEmpty(timeLineList)) {
            return pageInfo;
        }
        //根据publishId查询具体的动态详情信息
        List<ObjectId> publishIds = CollUtil.getFieldValues(timeLineList, "publishId").stream().map(s -> new ObjectId(String.valueOf(s))).collect(Collectors.toList());
        Query query = new Query(Criteria.where("id").in(publishIds));
        query.with(Sort.by(Sort.Direction.DESC, "created"));
        List<Publish> publishList = mongoTemplate.find(query, Publish.class);
        pageInfo.setRecords(publishList);
        return pageInfo;
    }

    /**
     * 保存用户发送的动态信息
     * @param publish
     * @return 返回动态发送成功的id
     */
    @Override
    public String savePublish(Publish publish) {
        if (ObjectUtil.isAllEmpty(publish, publish.getText(), publish.getUserId())) {
            return null;
        }
        //保存用户发送的动态信息到发布表中
        //为publish生成唯一自增长的id
        Long publishId = idService.getPublishId(IdType.PUBLISH);
        try {
            //设置动态的唯一id
            publish.setPid(publishId);
            //设置动态发布的时间
            publish.setCreated(System.currentTimeMillis());
            this.mongoTemplate.save(publish);
            //保存信息到相册表中
            Album album = new Album();
            album.setId(ObjectId.get());
            album.setPublishId(publish.getId());
            album.setCreated(System.currentTimeMillis());
            this.mongoTemplate.save(album, "quanzi_album_" + publish.getUserId());
            //保存信息到时间线表中
            //保存到好友的动态列表中
            //使用异步的方式保存
            //查询出好友的id
            this.timeLineService.saveTimeLine(publish.getUserId(), publish.getId());
        } catch (Exception e) {
            //保存信息失败后，事务需要回滚 但在单节点的mongodb中 不支持事务  只有使用集群才支持事务
            log.error("保存用户动态信息失败~userId = " + publish.getUserId());
            return null;
        }

        return publish.getId().toHexString();
    }

    /**
     * 查询出当前用户推荐动态
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Publish> queryRecommendPublishListEX(Long userId, Integer page, Integer pageSize) {
        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setCurrentPage(page);
        pageInfo.setPageSize(pageSize);
        //根据用户id查询出推荐动态集合中的数据
        Query query = Query.query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC, "score")).limit(pageSize).skip((page - 1) * (pageSize));
        List<RecommendQuanZi> recommendQuanZis = this.mongoTemplate.find(query, RecommendQuanZi.class);
        List<Long> pidList = CollUtil.getFieldValues(recommendQuanZis, "publishId").stream().map(pid -> Long.valueOf(String.valueOf(pid))).collect(Collectors.toList());
        //通过Pid查询出publish信息
        query = null;
        query = new Query().addCriteria(Criteria.where("pid").in(pidList)).with(Sort.by(Sort.Direction.DESC, "created"));
        List<Publish> publishes = mongoTemplate.find(query, Publish.class);
        if (CollectionUtils.isEmpty(publishes)) {
            return pageInfo;
        }
        pageInfo.setRecords(publishes);
        return pageInfo;
    }

    /**
     * 查询出当前用户推荐动态 修改
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Publish> queryRecommendPublishList(Long userId, Integer page, Integer pageSize) {
        //TODO 推荐用户动态的前缀
        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setCurrentPage(page);
        pageInfo.setPageSize(pageSize);
        //根据用户id查询出推荐动态集合中的redis中的数据
        String key = "QUANZI_PUBLISH_RECOMMEND_" + userId;
        String recommendPids = this.redisTemplate.opsForValue().get(key);
        if(StrUtil.isEmpty(recommendPids)){
            return pageInfo;
        }
        List<Long> pidArr = Arrays.stream(StringUtils.split(recommendPids, ",")).limit(pageSize).skip((page-1)*pageSize).map(pid->Long.valueOf(pid)).collect(Collectors.toList());
        Pageable pageable = PageRequest.of(page-1,pageSize,Sort.by(Sort.Order.desc("created")));
        /*Query query = Query.query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC, "score")).limit(pageSize).skip((page - 1) * (pageSize));
        List<RecommendQuanZi> recommendQuanZis = this.mongoTemplate.find(query, RecommendQuanZi.class);
        List<Long> pidList = CollUtil.getFieldValues(recommendQuanZis, "publishId").stream().map(pid -> Long.valueOf(String.valueOf(pid))).collect(Collectors.toList());
        //通过Pid查询出publish信息
        query = null;*/
        Query query = new Query(Criteria.where("pid").in(pidArr)).with(pageable);
        List<Publish> publishes = mongoTemplate.find(query, Publish.class);
        if (CollectionUtils.isEmpty(publishes)) {
            return pageInfo;
        }
        pageInfo.setRecords(publishes);
        return pageInfo;
    }



    /**
     * 根据id查询动态
     *
     * @param id
     * @return
     */
    @Override
    public Publish getPublishById(String id) {
        return this.mongoTemplate.findById(new ObjectId(id), Publish.class);
    }

    /**
     * 点赞动态
     *
     * @param userId
     * @param publishId
     * @return
     */
    @Override
    public Boolean likeComment(Long userId, String publishId) {
        //判断用户是否点赞过
        if (this.queryUserIsLike(userId, publishId)) {
            return false;
        }
        //用户未点赞,则保存用户点赞的评论信息
        Boolean flag = this.saveCommentByUserId(userId, publishId, null, CommentType.LIKE);
        if (!flag) {
            return false;
        }
        //修改redis数据中的点赞数 与是否点赞
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.LIKE.toString();
        this.redisTemplate.opsForHash().increment(commentKey, hashKey, 1);
        //标记为点赞
        String userHashKey = getUserLikeCommentHash(userId);
        this.redisTemplate.opsForHash().put(commentKey, userHashKey, "1");
        return true;
    }

    /**
     * 通用点赞 喜欢 功能
     * @param userId
     * @param publishId
     * @param type
     * @return
     */
    public Boolean generalFuncation(Long userId,String publishId,CommentType type){
        //判断用户是否点赞过
        if (this.queryUserIsLike(userId, publishId)) {
            return false;
        }
        //用户未点赞,则保存用户点赞的评论信息
        Boolean flag = this.saveCommentByUserId(userId, publishId, null, CommentType.LIKE);
        if (!flag) {
            return false;
        }
        //修改redis数据中的点赞数 与是否点赞
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.LIKE.toString();
        this.redisTemplate.opsForHash().increment(commentKey, hashKey, 1);
        //标记为点赞
        String userHashKey = getUserLikeCommentHash(userId);
        this.redisTemplate.opsForHash().put(commentKey, userHashKey, "1");
        return true;
    }

    private String getCommentKey(String publishId) {
        return COMMENT_REDIS_KEY_PREFIX + publishId;
    }

    private String getUserLikeCommentHash(Long userId) {
        return COMMENT_USER_LIEK_REDIS_KEY_PREFIX + userId;
    }

    private Boolean saveCommentByUserId(Long userId, String publishId, String commentContent, CommentType type) {
        try {
            Comment comment = new Comment();
            //设置评论动态的id  当前只对动态进行评论  未对评论进行点赞、评论等
            comment.setPublishId(new ObjectId(publishId));
            //设置评论的类型
            comment.setCommentType(type.getType());
            //设置评论的内容
            comment.setContent(commentContent);
            //设置评论的用户id
            comment.setUserId(userId);
            //设置发布动态的用户id
            //TODO 当前只对动态进行评论  未对评论进行点赞、评论等
            Publish publish = this.getPublishById(publishId);
            //若未找到
            if(Objects.isNull(publish)){
                //说明是评论点赞 则需要查询评论的信息
                Comment myComm = this.getContextById(publishId);
                //设置评论的用户id
                if(ObjectUtil.isNotEmpty(myComm)){
                    comment.setPublishUserId(myComm.getUserId());
                }else{
                    //对其他功能设置 数据  例如小视频
                    //查询发布小视频的用户
                    Video video = this.queryVideoById(publishId);
                    if(ObjectUtil.isNotEmpty(video)){
                        comment.setPublishUserId(video.getUserId());
                    }
                }
            }else{
                comment.setPublishUserId(publish.getUserId());
            }
            //设置是否是父节点
            comment.setIsParent(false);
            //设置父节点id
//            comment.setParentId(new ObjectId(publishId));
            //设置评论的时间
            comment.setCreated(System.currentTimeMillis());
            this.mongoTemplate.insert(comment);
        } catch (Exception e) {
            log.error("点赞失败~userId = " + userId + " 评论的id : " + publishId, e);
            return false;
        }
        return true;
    }

    /**
     * 根据视频id查询视频信息
     * @param videoId
     * @return
     */
    private Video queryVideoById(String videoId) {
        return this.mongoTemplate.findById(new ObjectId(videoId),Video.class);
    }

    /**
     * 通过评论id查询数据
     * @param commentId
     * @return
     */
    private Comment getContextById(String commentId) {
        return this.mongoTemplate.findById(commentId,Comment.class);
    }

    /**
     * 取消点赞
     *
     * @param userId
     * @param publishId
     * @return
     */
    @Override
    public Boolean dislikeComment(Long userId, String publishId) {
        //先查询用户是否点赞过
        if(!this.queryUserIsLike(userId,publishId)){
            //未点赞 则不能取消点赞
            return false;
        }
        //删除mongodb中的数据 并且将redis中的点赞数减一  标记用户未点赞
        Boolean flag = this.removeComment(userId, publishId,CommentType.LIKE);
        if(!flag){
            return false;
        }
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.LIKE.toString();
        this.redisTemplate.opsForHash().increment(commentKey, hashKey, -1);
        String userHashKey = getUserLikeCommentHash(userId);
        this.redisTemplate.opsForHash().delete(commentKey,userHashKey);
        return true;
    }


    private Boolean removeComment(Long userId, String publishId,CommentType commentType) {
        Query query = new Query(Criteria.where("publishId").is(new ObjectId(publishId)).and("userId").is(userId).and("commentType").is(commentType.getType()));
        return this.mongoTemplate.remove(query, Comment.class).getDeletedCount()>0;
    }

    /**
     * 查询点赞数
     *
     * @param publishId
     * @return
     */
    @Override
    public Long queryLikeCount(String publishId) {
        //redis是否命中 命中直接返回结果
        /*String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.LIKE.toString();
        Object o = this.redisTemplate.opsForHash().get(commentKey, hashKey);
        if(Objects.nonNull(o)){
            return Convert.toLong(o);
        }
        //未命中 到mongodb中查询数据  并将数据写入到redis中
        long count = this.queryCommentLikeCount(publishId);
        this.redisTemplate.opsForHash().put(commentKey,hashKey,String.valueOf(count));
        return count;*/
        return this.getCommentCount(publishId,CommentType.LIKE);
    }

    private Long getCommentCount(String publishId,CommentType type) {
        //redis是否命中 命中直接返回结果
        String commentKey = getCommentKey(publishId);
        String hashKey = type.toString();
        Object o = this.redisTemplate.opsForHash().get(commentKey, hashKey);
        if(Objects.nonNull(o)){
            return Convert.toLong(o);
        }
        //未命中 到mongodb中查询数据  并将数据写入到redis中
        long count = this.queryCommentCountByCommentType(publishId,type);
        this.redisTemplate.opsForHash().put(commentKey,hashKey,String.valueOf(count));
        return count;
    }

    /**
     * 查询评论的点赞数
     * @param publishId
     * @return
     */
    private Long queryCommentCountByCommentType(String publishId,CommentType type){
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(type.getType()));
        return this.mongoTemplate.count(query,Comment.class);
    }
    /**
     * 查询用户是否点赞该评论
     *
     * @param userId
     * @param publishId
     * @return
     */
    @Override
    public Boolean queryUserIsLike(Long userId, String publishId) {
        return this.commonUserIsLikeOrLove(userId,publishId,CommentType.LIKE);
    }

    private Boolean commonUserIsLikeOrLove(Long userId,String publishId,CommentType type){
        //先查询redis中是否有数据
        String redisKey = this.getCommentKey(publishId);
        String userKey = null;
        if(type.getType() == 1){
            userKey = this.getUserLikeCommentHash(userId);
        }else if(type.getType() == 3){
            userKey = this.getUserLoveCommentHash(userId);
        }
        //查询是否存在
        Object o = this.redisTemplate.opsForHash().get(redisKey, userKey);
        if(ObjectUtil.isNotEmpty(o)){
            return true;
        }
        Boolean flag = this.queryUserIsLikeOrLove(userId, publishId, type);
        if(flag){
            this.redisTemplate.opsForHash().put(redisKey,userKey,"1");
            return true;
        }
        return false;
    }

    private Boolean queryUserIsLikeOrLove(Long userId,String publishId,CommentType type){
        Query query = new Query(Criteria.where("publishId").is(new ObjectId(publishId)).and("userId").is(userId).and("commentType").is(type.getType()));
        return this.mongoTemplate.count(query, Comment.class) > 0;
    }



    /**
     * 喜欢该动态
     * @param userId
     * @param publishId
     * @return
     */
    @Override
    public Boolean loveComment(Long userId, String publishId) {
        //判断用户是否点赞过
        if (this.queryUserIsLove(userId, publishId)) {
            return false;
        }
        //用户未喜欢,则保存用户喜欢的评论信息
        Boolean flag = this.saveCommentByUserId(userId, publishId, null, CommentType.LOVE);
        if (!flag) {
            return false;
        }
        //修改redis数据中的喜欢数 与是否喜欢
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.LOVE.toString();
        this.redisTemplate.opsForHash().increment(commentKey, hashKey, 1);
        //标记为喜欢
        String userHashKey = getUserLoveCommentHash(userId);
        this.redisTemplate.opsForHash().put(commentKey, userHashKey, "1");
        return true;
    }

    /**
     * 点赞评论
     * @param userId
     * @param publishId
     * @return
     */
    public Boolean likeContent(Long userId,String publishId){
        //判断用户是否点赞过
        if (this.queryUserIsLike(userId, publishId)) {
            return false;
        }
        //用户未喜欢,则保存用户喜欢的评论信息
        Boolean flag = this.saveCommentByUserId(userId, publishId, null, CommentType.LIKE);
        if (!flag) {
            return false;
        }
        //修改redis数据中的喜欢数 与是否喜欢
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.LIKE.toString();
        this.redisTemplate.opsForHash().increment(commentKey, hashKey, 1);
        //标记为喜欢
        String userHashKey = getUserLikeCommentHash(userId);
        this.redisTemplate.opsForHash().put(commentKey, userHashKey, "1");
        return true;
    }

    public Boolean dislikeContent(Long userId,String publishId){
        //先查询用户是否点赞过
        if(!this.queryUserIsLike(userId,publishId)){
            //未点赞 则不能取消点赞
            return false;
        }
        //删除mongodb中的数据 并且将redis中的点赞数减一  标记用户未点赞
        Boolean flag = this.removeComment(userId, publishId,CommentType.LIKE);
        if(!flag){
            return false;
        }
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.LIKE.toString();
        this.redisTemplate.opsForHash().increment(commentKey, hashKey, -1);
        String userHashKey = getUserLikeCommentHash(userId);
        this.redisTemplate.opsForHash().delete(commentKey,userHashKey);
        return true;
    }
    private String getUserLoveCommentHash(Long userId) {
        return COMMENT_USER_LOVE_REDIS_KEY_PREFIX+userId;
    }

    /**
     * 取消喜欢
     * @param userId
     * @param publishId
     * @return
     */
    @Override
    public Boolean disloveComment(Long userId, String publishId) {
        //先查询用户是否喜欢过
        if(!this.queryUserIsLove(userId,publishId)){
            //未点赞 则不能取消喜欢
            return false;
        }
        //删除mongodb中的数据 并且将redis中的喜欢数减一  标记用户未喜欢
        Boolean flag = this.removeComment(userId, publishId,CommentType.LOVE);
        if(!flag){
            return false;
        }
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.LOVE.toString();
        this.redisTemplate.opsForHash().increment(commentKey, hashKey, -1);
        String userHashKey = getUserLoveCommentHash(userId);
        this.redisTemplate.opsForHash().delete(commentKey,userHashKey);
        return true;
    }

    /**
     * 查询喜欢数
     * @param publishId
     * @return
     */
    @Override
    public Long queryLoveCount(String publishId) {
        return this.getCommentCount(publishId,CommentType.LOVE);
    }

    @Override
    public Boolean queryUserIsLove(Long userId, String publishId) {
        return this.queryUserIsLikeOrLove(userId,publishId,CommentType.LOVE);
    }

    /**
     * 查询评论列表
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Comment> queryCommentList(String publishId,Integer page,Integer pageSize) {
        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setCurrentPage(page);
        pageInfo.setPageSize(pageSize);
        //查询评论列表
        Query query = new Query(Criteria.where("publishId").is(new ObjectId(publishId)).and("commentType").is(CommentType.COMMENT.getType()));
        query.with(Sort.by(Sort.Direction.ASC,"created")).limit(pageSize).skip((page-1)*pageSize);
        List<Comment> comments = this.mongoTemplate.find(query, Comment.class);
        if(CollUtil.isEmpty(comments)){
            return pageInfo;
        }
        pageInfo.setRecords(comments);
        return pageInfo;
    }

    /**
     * 保存评论
     * @param userId
     * @param publishId
     * @param content
     * @return
     */
    @Override
    public Boolean saveComment(Long userId, String publishId, String content) {
        Boolean flag = this.saveCommentByUserId(userId, publishId, content, CommentType.COMMENT);
        if(!flag){
            return false;
        }
        //修改redis中的数据
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.COMMENT.toString();
        this.redisTemplate.opsForHash().increment(commentKey, hashKey, 1);
        return true;
    }

    /**
     * 查询评论数
     * @param publishId
     * @return
     */
    @Override
    public Long queryCommentCount(String publishId) {
        /*//redis是否命中 命中直接返回结果
        String commentKey = getCommentKey(publishId);
        String hashKey = CommentType.COMMENT.toString();
        Object o = this.redisTemplate.opsForHash().get(commentKey, hashKey);
        if(Objects.nonNull(o)){
            return Convert.toLong(o);
        }
        //未命中 到mongodb中查询数据  并将数据写入到redis中
        long count = this.queryCommentContentCount(publishId,);
        this.redisTemplate.opsForHash().put(commentKey,hashKey,String.valueOf(count));
        return count;*/
        return this.getCommentCount(publishId,CommentType.COMMENT);
    }

   /* private long queryCommentContentCount(String publishId) {
        Query query = new Query(Criteria.where("publishId").is(publishId).and("commentType").is(CommentType.COMMENT.getType()));
        return this.mongoTemplate.count(query,Comment.class);
    }*/

    @Override
    public PageInfo<Comment> queryLikeCommentList(Long userId, Integer page, Integer pagesize) {
        return this.queryCommentListByUser(userId,page,pagesize,CommentType.LIKE);
    }

    @Override
    public PageInfo<Comment> queryLoveCommentList(Long userId, Integer page, Integer pagesize) {
        return this.queryCommentListByUser(userId,page,pagesize,CommentType.LOVE);
    }

    @Override
    public PageInfo<Comment> queryCommentListByUser(Long userId, Integer page, Integer pagesize) {
        return this.queryCommentListByUser(userId,page,pagesize,CommentType.COMMENT);
    }

    private PageInfo<Comment> queryCommentListByUser(Long userId,Integer page,Integer pagesize,CommentType type){

        Query query = Query.query(Criteria.where("publishUserId").is(userId).and("commentType").is(type.getType()))
                .with(Sort.by(Sort.Direction.DESC,"created")).limit(pagesize).skip((page-1)*pagesize);
        List<Comment> comments = this.mongoTemplate.find(query, Comment.class);
        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setCurrentPage(page);
        pageInfo.setPageSize(pagesize);
        if(CollUtil.isEmpty(comments)){
            return pageInfo;
        }
        pageInfo.setRecords(comments);
        return pageInfo;
    }


    /**
     * 查询出用户的动态信息
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    public PageInfo<Publish> queryAlbumList(Long userId, Integer page, Integer pagesize){
        //查询用户的相册表
        Pageable pageable = PageRequest.of(page-1,pagesize,Sort.by(Sort.Direction.DESC,"created"));
        Query query = new Query().with(pageable);
        List<Album> albums = this.mongoTemplate.find(query, Album.class, "quanzi_album_" + userId);
        //通过相册表的动态id 查询出用户所发布过的动态
        List<Object> publishId = CollUtil.getFieldValues(albums, "publishId");
        Query queryPublish = new Query(Criteria.where("id").in(publishId)).with(pageable);
        List<Publish> publishes = this.mongoTemplate.find(queryPublish, Publish.class);
        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setCurrentPage(page);
        pageInfo.setPageSize(pagesize);
        if(CollUtil.isEmpty(publishes)){
            return pageInfo;
        }
        pageInfo.setRecords(publishes);
        return pageInfo;
    }


}
