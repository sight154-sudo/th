package com.tanhua.dubbo.server.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.pojo.Visitors;
import com.tanhua.dubbo.server.vo.PageInfo;
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

/**
 * @author: tang
 * @date: Create in 21:32 2021/8/14
 * @description:
 */
@Service(version = "1.0.0")
public class VisitorsApiImpl implements VisitorsApi{


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private static final String VISITOR_REDIS_KEY = "VISITOR_USER";
    /**
     * 保存用户来访信息
     * @param userId 我的id
     * @param visitorUserId 访客id
     * @param from 来源
     * @return
     */
    @Override
    public String saveVisitor(Long userId, Long visitorUserId, String from) {
        //参数校验
        if(!ObjectUtil.isAllNotEmpty(userId,visitorUserId,from)){
            return null;
        }
        //若用户已经来访过则不记录
        String today = DateUtil.today();
        Long minDate = DateUtil.parseDateTime(today+" 00:00:00").getTime();
        Long maxDate = DateUtil.parseDateTime(today+" 23:59:59").getTime();
        Query query = Query.query(Criteria.where("userId").is(userId)
                .and("visitorUserId").is(visitorUserId)
                .andOperator(Criteria.where("date").gte(minDate), Criteria.where("date").lte(maxDate)));

        long count = this.mongoTemplate.count(query, Visitors.class);
        if(count > 0){
            //今天已经访问过
            return null;
        }
        //记录访客数据到数据库
        Visitors visitors = new Visitors();
        visitors.setId(ObjectId.get());
        visitors.setFrom(from);
        visitors.setUserId(userId);
        visitors.setVisitorUserId(visitorUserId);
        visitors.setDate(System.currentTimeMillis());
        this.mongoTemplate.save(visitors);
        return visitors.getId().toHexString();
    }

    /**
     * 查看我的访问者
     * @param userId
     * @return
     */
    @Override
    public List<Visitors> queryMyVisitor(Long userId) {
        //查询最近访问过的用户
        Pageable pageable = PageRequest.of(0,5, Sort.by(Sort.Order.desc("date")));
        Query query = Query.query(Criteria.where("userId").is(userId)).with(pageable);
        //若用户之前查询过，则需要使用redis记录用户查询时的时间，并查询在此之后的访客
        String redisKey = VISITOR_REDIS_KEY;
        //上次用户查询的时间
        Long lastTime = Convert.toLong(this.redisTemplate.opsForHash().get(redisKey, String.valueOf(userId)));
        if(ObjectUtil.isNotEmpty(lastTime)){
            query.addCriteria(Criteria.where("date").gte(lastTime));
        }
        //查询访客信息  TODO 查询访客
        List<Visitors> visitors = this.mongoTemplate.find(query, Visitors.class);
        if(CollUtil.isEmpty(visitors)){
            return null;
        }
        //补充访客与当前用户的缘分值
        return this.queryList(visitors,userId);
    }
    private List<Visitors> queryList(List<Visitors> visitors,Long userId){
        //补充访客与当前用户的缘分值
        visitors.stream().forEach(visitor->{
            Query queryScore = new Query(Criteria.where("userId").is(userId).and("toUserId").is(visitor.getVisitorUserId()));
            RecommendUser recommendUser = this.mongoTemplate.findOne(queryScore, RecommendUser.class);
            if(ObjectUtil.isEmpty(recommendUser)){
                //为空  设置默认值
                visitor.setScore(90d);
            }else{
                visitor.setScore(recommendUser.getScore());
            }
        });
        return visitors;
    }
    /**
     * 查询来访者列表
     * @param userId
     * @return
     */
    @Override
    public PageInfo<Visitors> queryVisitorsList(Long userId,Integer page,Integer pagesize) {
        PageInfo<Visitors> pageInfo = new PageInfo<>();
        pageInfo.setPageSize(pagesize);
        pageInfo.setCurrentPage(page);
        //查询来访者列表 对来访时间倒序排列
        Pageable pageable = PageRequest.of(page-1,pagesize,Sort.by(Sort.Order.desc("date")));
        Query query = Query.query(Criteria.where("userId").is(userId)).with(pageable);
        List<Visitors> visitors = this.mongoTemplate.find(query, Visitors.class);
        if(CollUtil.isEmpty(visitors)){
            return pageInfo;
        }
        //更新查询列表的时间
        String redisKey = VISITOR_REDIS_KEY;
        this.redisTemplate.opsForHash().put(redisKey, String.valueOf(userId),String.valueOf(System.currentTimeMillis()));
        //结果封装
        List<Visitors> result = this.queryList(visitors, userId);
        pageInfo.setRecords(result);
        return pageInfo;
    }


}
