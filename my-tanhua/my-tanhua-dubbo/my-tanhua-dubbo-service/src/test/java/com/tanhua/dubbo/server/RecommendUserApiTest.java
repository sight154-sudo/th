package com.tanhua.dubbo.server;

import com.tanhua.dubbo.server.api.RecommendUserApi;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 23:18 2021/8/4
 * @description:
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RecommendUserApiTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RecommendUserApi recommendUserApi;

    @Test
    public void findMaxScore(){

        Query query = Query.query(Criteria.where("toUserId").is(1l));
//        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC,"score"));
        query.limit(1);
        List<RecommendUser> recommendUsers = mongoTemplate.find(query, RecommendUser.class);
        System.out.println(recommendUsers);
    }

    @Test
    public void queryPageInfoTest(){
        //查询关联的用户列表按分数逆序
        Integer currentPage = 1;
        Integer pageSize = 5;
        Query query = Query.query(Criteria.where("toUserId").is(1l));
        query.with(new Sort(Sort.Direction.DESC,"score"));
        query.limit(pageSize);
        query.skip((currentPage-1)*pageSize);
        List<RecommendUser> recommendUsers = mongoTemplate.find(query, RecommendUser.class);
        for (RecommendUser recommendUser : recommendUsers) {
            System.out.println(recommendUser);
        }

    }


    @Test
    public void queryWithMaxScore(){
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(8l);
        System.out.println(recommendUser);
    }

    @Test
    public void queryPageInfo(){
        Integer currentPage = 1;
        Integer pageSize = 5;
        PageInfo<RecommendUser> list = recommendUserApi.queryPageInfo(1l, currentPage, pageSize);
        for (RecommendUser record : list.getRecords()) {
            System.out.println(record);
        }
    }
}
