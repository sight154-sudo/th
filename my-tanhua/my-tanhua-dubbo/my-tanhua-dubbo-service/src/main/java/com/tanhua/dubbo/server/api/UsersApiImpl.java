package com.tanhua.dubbo.server.api;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.Users;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @author: tang
 * @date: Create in 19:04 2021/8/13
 * @description:
 */
@Service(version = "1.0.0")
public class UsersApiImpl implements UsersApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveUsers(Long userId, Long friendId) {
        //参数校验
        if(!ObjectUtil.isAllNotEmpty(userId,friendId)){
            return;
        }
        //用户是否已经是好友关系
        Query query = Query.query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
        long count = this.mongoTemplate.count(query, Users.class);
        if(count > 0){
            return;
        }
        //添加好友关系到数据库中
        try {
            Users users = new Users();
            users.setId(ObjectId.get());
            users.setUserId(userId);
            users.setFriendId(friendId);
            users.setDate(System.currentTimeMillis());
            this.mongoTemplate.insert(users);
            users.setId(ObjectId.get());
            users.setUserId(friendId);
            users.setFriendId(userId);
            this.mongoTemplate.insert(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUsers(Long userId, Long friendId) {
        //参数校验
        if(!ObjectUtil.isAllNotEmpty(userId,friendId)){
            return;
        }
        //用户是否不是好友关系
        Query query = Query.query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
        long count = this.mongoTemplate.count(query, Users.class);
        if(count == 0){
            return;
        }
        //添加好友关系到数据库中
        try {
            Query query1 = Query.query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
            this.mongoTemplate.remove(query1,Users.class);

            Query query2 = Query.query(Criteria.where("userId").is(friendId).and("friendId").is(userId));
            this.mongoTemplate.remove(query2,Users.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Users> queryUserList(Long userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return this.mongoTemplate.find(query,Users.class);
    }

    @Override
    public PageInfo<Users> queryUsersList(Long userId, Integer page, Integer pageSize) {
        PageInfo<Users> pageInfo = new PageInfo<>();
        pageInfo.setCurrentPage(page);
        pageInfo.setPageSize(pageSize);
        Query query = Query.query(Criteria.where("userId").is(userId)).limit(pageSize).skip((page-1)*pageSize);
        List<Users> users = this.mongoTemplate.find(query, Users.class);
        pageInfo.setRecords(users);
        return pageInfo;
    }
}
