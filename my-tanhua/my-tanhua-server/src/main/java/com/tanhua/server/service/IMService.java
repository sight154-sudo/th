package com.tanhua.server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.common.mapper.UserInfoMapper;
import com.tanhua.common.pojo.HuanXinUser;
import com.tanhua.common.pojo.User;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.HuanXinApi;
import com.tanhua.dubbo.server.api.QuanZiApi;
import com.tanhua.dubbo.server.api.UsersApi;
import com.tanhua.dubbo.server.pojo.Comment;
import com.tanhua.dubbo.server.pojo.Users;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.vo.MessageCommentVo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.UserInfoVo;
import com.tanhua.server.vo.UsersVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 12:42 2021/8/13
 * @description:
 */
@Service
public class IMService {


    @Reference(version = "1.0.0")
    private HuanXinApi huanXinApi;

    @Reference(version = "1.0.0")
    private UsersApi usersApi;

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 查询用户信息通过环信id
     * @param huanxinId
     * @return
     */
    public UserInfoVo queryUserInfoByHuanXin(String huanxinId) {
        HuanXinUser huanXinUser = huanXinApi.queryHuanXinUserByUsername(huanxinId);
        if(ObjectUtil.isEmpty(huanXinUser)){
            return null;
        }
        UserInfo userInfo = userInfoService.queryUserInfoByUserId(huanXinUser.getUserId());
        UserInfoVo userInfoVo = BeanUtil.copyProperties(userInfo,UserInfoVo.class,"marriage");
        userInfoVo.setGender(userInfo.getSex().getValue()==1?"man":"woman");
        userInfoVo.setMarriage(userInfo.getMarriage().equals("已婚")?1:0);
        return userInfoVo;
    }

    /**
     * 保存好友关系
     * @param friendId
     */
    public void saveLinkedUser(Long friendId) {
        if(null == friendId){
            return;
        }
        User user = UserThreadLocal.getUser();
        try {
            huanXinApi.addUserFriend(user.getId(),friendId);
            usersApi.saveUsers(user.getId(),friendId);
        } catch (Exception e) {
            throw new RuntimeException("添加好友关系失败");
        }
    }
    /**
     * 解除好友关系
     * @param id
     * @param userId
     */
    public void removeFriend(Long id, Long userId) {
        if(null == userId){
            return;
        }
        try {
            this.huanXinApi.removeUserFriend(id,userId);
            usersApi.removeUsers(id,userId);
        } catch (Exception e) {
            throw new RuntimeException("解除好友关系失败");
        }
    }
    /**
     * 查询联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    public PageResult queryUsersList(Integer page,Integer pagesize,String keyword){
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        User user = UserThreadLocal.getUser();
        List<Users> list;
        if(StringUtils.isNotBlank(keyword)){
            //关键字不为空，则查询所有
            list = this.usersApi.queryUserList(user.getId());
        }else{
            PageInfo<Users> pageInfo = this.usersApi.queryUsersList(user.getId(), page, pagesize);
            list = pageInfo.getRecords();
        }
        if(CollUtil.isEmpty(list)){
            return pageResult;
        }
        List<Object> ids = CollUtil.getFieldValues(list, "friendId");
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserInfo::getUserId,ids);
        if(StringUtils.isNotBlank(keyword)){
            wrapper.like(UserInfo::getNickName,keyword);
        }
        List<UserInfo> userInfoList = userInfoMapper.selectList(wrapper);
        //填充属性
        List<UsersVo> collect = userInfoList.stream().map(userInfo -> {
            UsersVo usersVo = new UsersVo();
            usersVo.setId(userInfo.getUserId());
            usersVo.setAge(userInfo.getAge());
            usersVo.setAvatar(userInfo.getLogo());
            usersVo.setGender(userInfo.getSex().name().toLowerCase());
            usersVo.setNickname(userInfo.getNickName());
            //环信用户账号
            usersVo.setUserId("HX_"+userInfo.getUserId());
            usersVo.setCity(StringUtils.substringBefore(userInfo.getCity(), "-"));
            return usersVo;
        }).collect(Collectors.toList());
        pageResult.setItems(collect);
        return pageResult;
    }

    public PageResult queryLikeCommentList(Integer page, Integer pagesize) {
        User user = UserThreadLocal.getUser();
        PageInfo<Comment> pageInfo = this.quanZiApi.queryLikeCommentList(user.getId(), page, pagesize);
        return this.commenCommentList(pageInfo,page,pagesize);
    }

    public PageResult queryLoveCommentList(Integer page, Integer pagesize) {
        User user = UserThreadLocal.getUser();
        PageInfo<Comment> pageInfo = this.quanZiApi.queryLoveCommentList(user.getId(), page, pagesize);
        return this.commenCommentList(pageInfo,page,pagesize);
    }

    public PageResult queryUserCommentList(Integer page, Integer pagesize) {
        User user = UserThreadLocal.getUser();
        PageInfo<Comment> pageInfo = this.quanZiApi.queryCommentListByUser(user.getId(), page, pagesize);
        return this.commenCommentList(pageInfo,page,pagesize);
    }

    private PageResult commenCommentList(PageInfo<Comment> pageInfo,Integer page,Integer pagesize){
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        List<Comment> records = pageInfo.getRecords();
        if(CollUtil.isEmpty(records)){
            return null;
        }
        List<Object> ids = CollUtil.getFieldValues(records, "userId");
        Map<Long, UserInfo> map = this.userInfoService.queryUserInfoByUserIds(ids).stream().collect(Collectors.toMap(k -> k.getUserId(), v -> v));
        List<MessageCommentVo> collect = records.stream().map(record -> {
            MessageCommentVo mcvo = new MessageCommentVo();
            mcvo.setId(record.getId().toHexString());
            mcvo.setCreateDate(DateUtil.format(new Date(record.getCreated()), "yyyy-MM-dd HH:mm"));
            UserInfo userInfo = map.get(record.getUserId());
            if (ObjectUtil.isNotEmpty(userInfo)) {
                mcvo.setNickname(userInfo.getNickName());
                mcvo.setAvatar(userInfo.getLogo());
            }
            return mcvo;
        }).collect(Collectors.toList());
        pageResult.setItems(collect);
        return pageResult;
    }

}
