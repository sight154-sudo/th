package com.tanhua.dubbo.server.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author: tang
 * @date: Create in 23:01 2021/8/4
 * @description:  推荐用户
 */
@Service(version = "1.0.0")
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserLikeApi userLikeApi;

    /**
     * 查询一位得分最高的推荐用户
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryWithMaxScore(Long userId) {
        Query query = Query.query(Criteria.where("toUserId").is(userId));
        query.with(new Sort(Sort.Direction.DESC,"score"));
        query.limit(1);
        List<RecommendUser> recommendUsers = mongoTemplate.find(query, RecommendUser.class);
        if(null != recommendUsers && recommendUsers.size()>0){
            return recommendUsers.get(0);
        }
        return null;
    }

    /**
     * 按照得分倒序
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer currentPage, Integer pageSize) {
        Query query = Query.query(Criteria.where("toUserId").is(userId));
        query.with(new Sort(Sort.Direction.DESC,"score"));
        query.limit(pageSize);
        query.skip((currentPage-1)*pageSize);
        List<RecommendUser> recommendUsers = mongoTemplate.find(query, RecommendUser.class);

        return new PageInfo<>(0,currentPage,pageSize,recommendUsers);
    }

    /**
     * 查询出推荐用户与登陆用户的缘分值
     * @param userId 推荐用户
     * @param toUserId  登陆用户
     * @return
     */
    @Override
    public Double queryUserScore(Long userId, Long toUserId) {
        if(userId.longValue() == toUserId.longValue()){
            return 80d;
        }
        Query query = Query.query(Criteria.where("userId").is(userId).and("toUserId").is(toUserId));
        RecommendUser recommendUser = this.mongoTemplate.findOne(query, RecommendUser.class);
        if(Objects.isNull(recommendUser)){
            return 80d;
        }
        Double score = recommendUser.getScore();
        if(ObjectUtil.isEmpty(score)){
            return 85d;
        }
        return score;
    }

    /**
     * 查询探花的用户
     * @param userId
     * @param count
     * @return
     */
    @Override
    public List<RecommendUser> queryCardList(Long userId, Integer count) {
        //查询是需要排除喜欢列表与不喜欢列表中的用户
        Pageable pageable = PageRequest.of(0,count,Sort.by(Sort.Order.desc("score")));
        //查询用户喜欢的列表
        List<Long> ids = new ArrayList<>();
        List<Long> userLikeIds = userLikeApi.queryUserLikeList(userId);
        if(CollUtil.isNotEmpty(userLikeIds)){
            ids.addAll(userLikeIds);
        }
        //查询用户不喜欢的列表
        List<Long> userNotLikeIds = userLikeApi.queryUserNotLikeList(userId);
        if(CollUtil.isNotEmpty(userNotLikeIds)){
            ids.addAll(userNotLikeIds);
        }
        //排除后查询
        Query query = new Query(Criteria.where("toUserId").is(userId).and("userId").nin(ids)).with(pageable);
        List<RecommendUser> recommendUsers = this.mongoTemplate.find(query, RecommendUser.class);
        if(CollUtil.isNotEmpty(recommendUsers)){
            return recommendUsers;
        }
        return null;
    }
}
