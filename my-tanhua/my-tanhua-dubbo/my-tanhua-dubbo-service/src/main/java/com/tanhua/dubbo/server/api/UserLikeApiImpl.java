package com.tanhua.dubbo.server.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.UserLike;
import com.tanhua.dubbo.server.vo.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author: tang
 * @date: Create in 18:53 2021/8/15
 * @description:
 */
@Service(version = "1.0.0")
@Slf4j
public class UserLikeApiImpl implements UserLikeApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;



    public static final String LIKE_REDIS_KEY_PREFIX = "USER_LIKE_";

    public static final String NOT_LIKE_REDIS_KEY_PREFIX = "USER_NOT_LIKE_";

    public static final String USER_MUTUALLIKE_PREFIX = "USER_MUTUALL_";

    /**
     * 右滑喜欢
     * @param userId
     * @param likeUserId
     * @return
     */
    @Override
    public void userLike(Long userId, Long likeUserId) {
        //判断用户是否已经喜欢
        if(this.isLike(userId,likeUserId)){
            return;
        }
        //添加信息到数据库中
        try {
            UserLike userLike = new UserLike();
            userLike.setCreated(System.currentTimeMillis());
            userLike.setId(ObjectId.get());
            userLike.setUserId(userId);
            userLike.setLikeUserId(likeUserId);
            this.mongoTemplate.save(userLike);
            //添加信息到redis中
            String redisKey = getUserLikeRedisKey(userId);
            String hashKey = String.valueOf(likeUserId);
            this.redisTemplate.opsForHash().put(redisKey,hashKey,"1");
        } catch (Exception e) {
            log.error("添加用户喜欢的用户失败~userId = "+userId+" likeUserId = "+likeUserId,e);
        }
    }

    /**
     * 判断用户是否喜欢
     * @param userId
     * @param likeUserId
     * @return
     */
    public Boolean isLike(Long userId,Long likeUserId){
        //首先查询redis中的数据 如果查询到 则返回 查询不到则查询mongodb
        String redisKey = getUserLikeRedisKey(userId);
        String hashKey = String.valueOf(likeUserId);
        //判断是否存在
        Object o = this.redisTemplate.opsForHash().get(redisKey, hashKey);
        if(ObjectUtil.isNotEmpty(o)){
            return true;
        }
        //查询mongodb
        //TODO 查询比较占用资源
        Query query = Query.query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));
        long count = this.mongoTemplate.count(query, UserLike.class);
        if(count > 0 ){
            this.redisTemplate.opsForHash().put(redisKey,hashKey,"1");
            return true;
        }
        return false;
    }

    private String getUserLikeRedisKey(Long userId){
        return LIKE_REDIS_KEY_PREFIX+userId;
    }

    private String getNotLikeRedisKey(Long userId){
        return NOT_LIKE_REDIS_KEY_PREFIX+userId;
    }

    @Override
    public void userdisLike(Long userId, Long dislikeUserId) {
        //判断用户是否本身不喜欢
        if(this.isNotLike(userId,dislikeUserId)){
            return;
        }
        //将数据添加到redis中
        String redisKey = getNotLikeRedisKey(userId);
        String hashKey = String.valueOf(dislikeUserId);
        this.redisTemplate.opsForHash().put(redisKey,hashKey,"1");
        return;
    }

    /**
     * 用户是否互相喜欢
     * @param userId
     * @param likeUserId
     * @return
     */
    @Override
    public Boolean isMutualLike(Long userId, Long likeUserId) {
        return isLike(userId,likeUserId) && isLike(likeUserId,userId);
    }

    /**
     * 查询用户喜欢的列表
     * @param userId
     * @return
     */
    @Override
    public List<Long> queryUserLikeList(Long userId) {
        //查询用户喜欢的列表
        //若redis中有数据，则返回redis中的数据
        String redisKey = getUserLikeRedisKey(userId);
        Set<Object> ids = this.redisTemplate.opsForHash().keys(redisKey);
        if(CollUtil.isNotEmpty(ids)){
            List<Long> userIds = ids.stream().map(id -> Convert.toLong(id)).collect(Collectors.toList());
            return userIds;
        }
        //否则 查询mongodb中的数据
        Query query = Query.query(Criteria.where("userId").is(userId));
        List<UserLike> userLikes = this.mongoTemplate.find(query, UserLike.class);
        //收集数据 并存放到redis中
        List<Long> userIds = userLikes.stream().map(userLike -> userLike.getLikeUserId()).collect(Collectors.toList());
        userIds.stream().forEach(uId->this.redisTemplate.opsForHash().put(redisKey,String.valueOf(uId),"1"));
        return userIds;
    }

    /**
     * 查询用户不喜欢的列表
     * @param userId
     * @return
     */
    @Override
    public List<Long> queryUserNotLikeList(Long userId) {
        List<Long> userNotlikeIds = this.redisTemplate.opsForHash().keys(getNotLikeRedisKey(userId)).stream().map(s -> Convert.toLong(s)).collect(Collectors.toList());
        return userNotlikeIds;
    }

    /**
     * 用户是否不喜欢
     * @param userId
     * @param dislikeUserId
     * @return
     */
    public Boolean isNotLike(Long userId,Long dislikeUserId){
        //首先查询redis中的数据 如果查询到 则返回 查询不到则查询mongodb
        String redisKey = getNotLikeRedisKey(userId);
        String hashKey = String.valueOf(dislikeUserId);
        //判断是否存在  key: USER_LIKE_1  field  3 : "1"
        return this.redisTemplate.opsForHash().get(redisKey, hashKey)!=null;
    }

    /**
     * 互相关注数
     * @param userId
     * @return
     */
    @Override
    public Long queryMutualLikeCount(Long userId) {
        //实现互相关注数 可以使用 redis   或使用 mongodb
        List<Long> likeList = this.queryUserLikeList(userId);
        String mutuAllLikeKey = this.getMutuAllLikeKey(userId);
        long count = 0l;
        for (Long id : likeList) {
            String redisKey = this.getUserLikeRedisKey(id);
            if(this.redisTemplate.opsForHash().hasKey(redisKey,String.valueOf(userId))){
                this.redisTemplate.opsForSet().add(mutuAllLikeKey,String.valueOf(id));
                count++;
            }
        }
        return count;
    }

    private String getMutuAllLikeKey(Long userId){
        return USER_MUTUALLIKE_PREFIX+userId;
    }

    /**
     * 用户喜欢数
     * @param userId
     * @return
     */
    @Override
    public Long queryLikeCount(Long userId) {
        return Convert.toLong(this.queryUserLikeList(userId).size());
    }

    /**
     * 用户粉丝数
     * @param userId
     * @return
     */
    @Override
    public Long queryFanCount(Long userId) {
        //查询mongodb
        Query query = Query.query(Criteria.where("likeUserId").is(userId));
        return this.mongoTemplate.count(query,UserLike.class);
    }

    /**
     * 查询用户互相喜欢的列表
     * @param userId
     * @return
     */
    @Override
    public PageInfo<UserLike> queryMutualLikeList(Long userId,Integer page,Integer pagesize) {
        //先查询出用户喜欢人的信息
        List<Long> userLikeIds = this.queryUserLikeList(userId);
        //再查询mongodb中喜欢人与用户是否喜欢
        Query query = Query.query(Criteria.where("userId").in(userLikeIds).and("likeUserId").is(userId));
        return this.queryList(query,page,pagesize);
    }
    /**
     * 查询用户喜欢人列表
     * @param userId
     * @return
     */
    @Override
    public PageInfo<UserLike> queryLikeList(Long userId,Integer page,Integer pagesize) {
        Query query = Query.query(Criteria.where("userId").in(userId));
        return this.queryList(query,page,pagesize);
    }

    /**
     *  查询用户粉丝列表
     * @param userId
     * @return
     */
    @Override
    public PageInfo<UserLike> queryFanList(Long userId,Integer page,Integer pagesize) {
        Query query = Query.query(Criteria.where("likeUserId").is(userId));
        return this.queryList(query,page,pagesize);
    }

    private PageInfo<UserLike> queryList(Query query, Integer page, Integer pagesize){
        Pageable pageable = PageRequest.of(page-1,pagesize, Sort.by(Sort.Order.desc("created")));
        query.with(pageable);
        PageInfo<UserLike> pageinfo = new PageInfo<>();
        pageinfo.setPageSize(pagesize);
        pageinfo.setCurrentPage(page);
        List<UserLike> result = this.mongoTemplate.find(query, UserLike.class);
        if(CollUtil.isEmpty(result)){
            return pageinfo;
        }
        pageinfo.setRecords(result);
        return pageinfo;
    }

    /**
     * 用户取消喜欢
     * @param userId
     */
    @Override
    public void cancelUserLike(Long userId,Long disLikeUserId) {
        //判断用户是否本身不喜欢
        if(this.isNotLike(userId,disLikeUserId)){
            return;
        }
        //删除数据库中的数据
        try {
            Query query = Query.query(Criteria.where("userId").is(userId).and("likeUserId").is(disLikeUserId));
            this.mongoTemplate.remove(query,UserLike.class);
            //更新redis中的数据
            //将数据添加到redis中
            String redisKey = getNotLikeRedisKey(userId);
            String hashKey = String.valueOf(disLikeUserId);
            //添加到不喜欢列表中
            this.redisTemplate.opsForHash().put(redisKey,hashKey,"1");
            //从喜欢列表中剃除
            String likeHashKey = getUserLikeRedisKey(userId);
            this.redisTemplate.opsForHash().delete(likeHashKey,hashKey);
        } catch (Exception e) {
            throw new RuntimeException("用户喜欢失败~");
        }
        return;
    }

}
