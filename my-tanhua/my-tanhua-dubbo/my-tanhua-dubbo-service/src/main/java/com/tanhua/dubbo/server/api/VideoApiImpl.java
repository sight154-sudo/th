package com.tanhua.dubbo.server.api;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.enums.IdType;
import com.tanhua.dubbo.server.pojo.FollowUser;
import com.tanhua.dubbo.server.pojo.RecommendVideo;
import com.tanhua.dubbo.server.pojo.Video;
import com.tanhua.dubbo.server.service.IdService;
import com.tanhua.dubbo.server.vo.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 21:58 2021/8/10
 * @description: 小视频模块
 */
@Service(version = "1.0.0")
@Slf4j
public class VideoApiImpl implements VideoApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    private static final String VIDEO_FOLLOW_USER_KEY_PREFIX = "VIDEO_FOLLOW_USER_";
    /**
     * 上传小视频
     * @param video
     * @return
     */
    @Override
    public String saveVideo(Video video) {
        //判断参数是否合法
        if(!(ObjectUtil.isAllNotEmpty(video.getUserId(),video.getPicUrl(),video.getVideoUrl()))){
            //条件不符合，则保存视频失败
            return null;
        }
        //生成自增长的video的id
        Long vidId = idService.getPublishId(IdType.VIDEO);
        video.setVid(vidId);
        //设置时间
        video.setCreated(System.currentTimeMillis());
        this.mongoTemplate.save(video);
        return video.getId().toHexString();
    }


    @Override
    public Video queryVideoById(String videoId) {
        return this.mongoTemplate.findById(videoId,Video.class);
    }

    /**
     * 展示小视频列表
     * @return
     */
    @Override
    public PageInfo<Video> queryVideoList(Long userId,Integer page,Integer pagesize) {
        //先展示推荐给当前用户的小视频、
        PageInfo<Video> pageInfo = new PageInfo<>();
        pageInfo.setPageSize(pagesize);
        pageInfo.setCurrentPage(page);
        Long count = queryVideoCount(userId);
        Query query = new Query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC,"score")).limit(pagesize).skip((page-1)*pagesize);
        List<Long> ids = Collections.EMPTY_LIST;
        List<RecommendVideo> recommendVideos = this.mongoTemplate.find(query, RecommendVideo.class);
        if(null == recommendVideos || recommendVideos.size()==0){
            //说明展示的小视频不足 需要展示一些系统视频
//            PageUtil.totalPage()
//            int size = pagesize-recommendVideos.size();
            int curPage = Convert.toInt(page - count/(Convert.toLong(pagesize)));
            Query sysQuery = new Query().with(Sort.by(Sort.Direction.DESC,"created")).limit(pagesize).skip((curPage-1)*pagesize);
            List<Video> videos = this.mongoTemplate.find(sysQuery, Video.class);
            //当推荐小视频展示完成后，则再展示系统视频
            pageInfo.setRecords(videos);
            return pageInfo;
        }
        ids = recommendVideos.stream().map(video->video.getVideoId()).collect(Collectors.toList());
        //通过videoId查询小视频详情信息
        Query videoQuery = new Query(Criteria.where("vid").in(ids));

        List<Video> videos = this.mongoTemplate.find(videoQuery, Video.class);
        pageInfo.setRecords(videos);
        return pageInfo;
    }

    /**
     * 展示小视频列表 修改
     * @return
     */
    @Override
    public PageInfo<Video> queryVideoListEx(Long userId,Integer page,Integer pagesize) {
        //先展示推荐给当前用户的小视频、
        PageInfo<Video> pageInfo = new PageInfo<>();
        pageInfo.setPageSize(pagesize);
        pageInfo.setCurrentPage(page);
        //TODO 从redis中获取推荐视频的数据
        String redisKey = "QUANZI_VIDEO_RECOMMEND_" + userId;
        String strIds = this.redisTemplate.opsForValue().get(redisKey);
        if(StrUtil.isEmpty(strIds)){
            return pageInfo;
        }
        //设置分页参数
        String[] ids = StringUtils.split(strIds, ",");
        Integer RecommendCount = ids.length;
        Integer count = page*pagesize;
        Query query = new Query();
        if(RecommendCount >= count){
            //查询的推荐视频
            List<Long> vids = Arrays.stream(ids).limit(pagesize).skip((page - 1) * pagesize).map(id->Long.valueOf(id)).collect(Collectors.toList());
            query.addCriteria(Criteria.where("vid").in(vids));
        }else{
            //查询系统中的视频
            Integer CurrentPage = RecommendCount/pagesize;
            //重新设置页码
            page = page - CurrentPage;
            query.with(Sort.by(Sort.Direction.DESC,"created")).limit(pagesize).skip((page-1)*pagesize);
        }
        //从数据库中查询结果
        List<Video> videos = this.mongoTemplate.find(query, Video.class);
        pageInfo.setRecords(videos);
        return pageInfo;

    }

    /**
     * 关注用户
     * @param userId
     * @param followUserId
     * @return
     */
    @Override
    public Boolean followUser(Long userId, Long followUserId) {
        //参数判断
        if(!ObjectUtil.isAllNotEmpty(userId,followUserId)){
            return false;
        }
        //判断用户是否关注
        if(this.isFollowUser(userId,followUserId)){
            return false;
        }
        //添加信息到数据库中
        Boolean flag = this.saveFollowUser(userId,followUserId);
        if(!flag){
            return false;
        }
        //在redis中存放用户已关注的标识
        String redisKey = VIDEO_FOLLOW_USER_KEY_PREFIX+userId;
        String hashKey = String.valueOf(followUserId);
        this.redisTemplate.opsForHash().put(redisKey,hashKey,"1");
        return true;
    }

    private Boolean saveFollowUser(Long userId, Long followUserId) {
        try {
            FollowUser followUser = new FollowUser();
            followUser.setId(new ObjectId());
            followUser.setUserId(userId);
            followUser.setFollowUserId(followUserId);
            followUser.setCreated(System.currentTimeMillis());
            this.mongoTemplate.insert(followUser);
            return true;
        } catch (Exception e) {
            log.error("保存关注用户失败~ userId = "+userId+" followUserId = "+followUserId,e);
        }
        return false;
    }

    /**
     * 取消关注
     * @param userId
     * @param followUserId
     * @return
     */
    @Override
    public Boolean disFollowUser(Long userId, Long followUserId) {
        //参数判断
        if(!ObjectUtil.isAllNotEmpty(userId,followUserId)){
            return false;
        }
        //判断用户是否关注
        if(!this.isFollowUser(userId,followUserId)){
            return false;
        }
        //清除数据库中的数据
        boolean flag = this.removeFollowUser(userId,followUserId);
        if(!flag){
            return false;
        }
        //清除标识
        String redisKey = VIDEO_FOLLOW_USER_KEY_PREFIX+userId;
        String hashKey = String.valueOf(followUserId);
        this.redisTemplate.opsForHash().delete(redisKey,hashKey);
        return true;
    }

    private boolean removeFollowUser(Long userId, Long followUserId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("followUserId").is(followUserId));
        return this.mongoTemplate.remove(query,FollowUser.class).getDeletedCount()>0;
    }

    /**
     * 判断是否关注
     * @param userId
     * @param followUserId
     * @return
     */
    @Override
    public Boolean isFollowUser(Long userId, Long followUserId) {
        //是否命中缓存
        String redisKey = VIDEO_FOLLOW_USER_KEY_PREFIX+userId;
        String hashKey = String.valueOf(followUserId);
        Object o = this.redisTemplate.opsForHash().get(redisKey, hashKey);
        if(ObjectUtil.isNotEmpty(o)){
            return true;
        }
        //未命中查询数据库中是否有
        boolean flag = this.queryVideoFollowUserisExists(userId,followUserId);
        if(flag){
            this.redisTemplate.opsForHash().put(redisKey,hashKey,"1");
            return true;
        }
        return false;
    }

    private boolean queryVideoFollowUserisExists(Long userId, Long followUserId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("followUserId").is(followUserId));
        return this.mongoTemplate.count(query, FollowUser.class)>0;
    }

    private Long queryVideoCount(Long userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return this.mongoTemplate.count(query,RecommendVideo.class);
    }
}
