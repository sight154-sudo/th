package com.tanhua.dubbo.server;

import cn.hutool.core.convert.Convert;
import com.tanhua.dubbo.server.api.UserLikeApi;
import com.tanhua.dubbo.server.pojo.UserLike;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 19:54 2021/8/15
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserLikeApiTest {

    @Autowired
    private UserLikeApi userLikeApi;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void queryUserLikeList() {
        /*for (int i = 1; i <= 99; i++) {
            userLikeApi.queryUserLikeList(Convert.toLong(i));
        }*/
        userLikeApi.queryUserLikeList(1l).forEach(s-> System.out.println(s));
    }
    @Test
    public void queryUserNotLikeList(){
        userLikeApi.queryUserNotLikeList(1l).forEach(s-> System.out.println(s));
    }

    @Test
    public void userLike(){
        userLikeApi.userLike(1l,18l);
    }


    @Test
    public void userNotlike(){
        userLikeApi.userdisLike(1l,32l);
    }


    @Test
    public void queryUserNotlikeList(){
        userLikeApi.queryUserNotLikeList(1l).forEach(s-> System.out.println(s));
    }

    @Test
    public void isMutualLike(){
        System.out.println(userLikeApi.isMutualLike(1l, 2l));
    }


    @Test
    public void userLike1(){
        System.out.println(userLikeApi.queryMutualLikeCount(2l));
        System.out.println(userLikeApi.queryLikeCount(2l));
        System.out.println(userLikeApi.queryFanCount(2l));
    }


    @Test
    public void testDelSameUserLike(){
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.project("userId","likeUserId").and("difference: { $eq: [\"$userId\", \"$likeUserId\"]}"),
                Aggregation.match(Criteria.where("difference").is(true)));
        AggregationResults<UserLike> results = mongoTemplate.aggregate(aggregation, "user_like",UserLike.class);
        List<UserLike> mappedResults = results.getMappedResults();
        System.out.println(mappedResults);

    }

    @Test
    public void aggregateTest(){

    }
}
