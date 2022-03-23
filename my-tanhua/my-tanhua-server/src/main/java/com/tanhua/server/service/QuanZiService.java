package com.tanhua.server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.common.pojo.User;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.common.service.PicUploadService;
import com.tanhua.common.utils.RelativeDateFormat;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.common.vo.PicUploadResult;
import com.tanhua.dubbo.server.api.QuanZiApi;
import com.tanhua.dubbo.server.api.VisitorsApi;
import com.tanhua.dubbo.server.enums.CommentType;
import com.tanhua.dubbo.server.pojo.Comment;
import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.pojo.Visitors;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.vo.CommentVo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.QuanZiVo;
import com.tanhua.server.vo.VisitorsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 18:09 2021/8/7
 * @description:
 */
@Service
public class QuanZiService {


    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    private QuanziMQService quanziMQService;

    /**
     * 查询出用户的好友动态
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryMoveMents(Integer page, Integer pageSize) {
        PageResult result = getPageResult(page,pageSize);
        User user = UserThreadLocal.getUser();
        //查询出好友动态
        PageInfo<Publish> publishList = quanZiApi.queryPublishList(user.getId(), page, pageSize);
        //根据用户id查询出用户的详情信息
        List<QuanZiVo> quanZiVos = fillQuanZiVo(publishList.getRecords());
        //进行排序
        result.setItems(quanZiVos);
        return result;
    }



    /**
     * 保存用户的动态
     * @param textContent
     * @param file
     * @param location
     * @param longitude
     * @param latitude
     * @return
     */
    public String savePublish(String textContent, MultipartFile[] file, String location, String longitude, String latitude) {
        Publish publish = new Publish();
        //获取登陆用户的信息
        User user  = UserThreadLocal.getUser();
        //根据请求的参数 补全publish的信息
        //设置发布的用户id
        publish.setUserId(user.getId());
        //设置发布的内容
        publish.setText(textContent);
        //设置位置
        publish.setLocationName(location);
        //设置经度
        publish.setLatitude(latitude);
        //设置纬度
        publish.setLongitude(longitude);
        List<String> medias = new ArrayList<>();
        for (MultipartFile multipartFile : file) {
            //上传动态的图片
            PicUploadResult picUploadResult = picUploadService.uploadResult(multipartFile);
            if(StringUtils.isNotBlank(picUploadResult.getName())){
                medias.add(picUploadResult.getName());
            }
        }
        //设置上传图片的地址
        publish.setMedias(medias);
        String publishId = quanZiApi.savePublish(publish);
        if(StrUtil.isNotBlank(publishId)){
            //发送消息
            quanziMQService.publishMsg(publishId);
        }
        return publishId;
    }

    /**
     * 查询推荐动态表
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryRecommendQuanziList(Integer page, Integer pageSize) {
        PageResult result = getPageResult(page,pageSize);
        User user = UserThreadLocal.getUser();
        if(Objects.isNull(user)){
            return result;
        }
        //查询出好友动态
        PageInfo<Publish> publishList = quanZiApi.queryRecommendPublishList(user.getId(), page, pageSize);
        List<QuanZiVo> quanZiVoList = fillQuanZiVo(publishList.getRecords());
        //进行排序
        result.setItems(quanZiVoList);
        return result;
    }

    private PageResult getPageResult(Integer page, Integer pageSize) {
        PageResult result = new PageResult();
        //设置分页参数
        result.setPage(page);
        result.setPagesize(pageSize);
        return result;
    }

    /**
     * 填充用户详情信息
     * @param quanZiVo
     * @param userInfo
     */
    private void fillUserInfo(QuanZiVo quanZiVo, UserInfo userInfo) {
        BeanUtil.copyProperties(userInfo, quanZiVo, "id");
        quanZiVo.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
        quanZiVo.setTags(StringUtils.split(userInfo.getTags(), ","));
        quanZiVo.setCommentCount(Convert.toInt(quanZiApi.queryCommentCount(quanZiVo.getId()))); //TODO 评论数
        quanZiVo.setDistance("1.2公里"); //TODO 距离
        quanZiVo.setHasLiked(quanZiApi.queryUserIsLike(UserThreadLocal.getUser().getId(), quanZiVo.getId())?1:0); //TODO 是否点赞（1是，0否）
//        quanZiVo.setHasLiked(0); //TODO 是否点赞（1是，0否）
        quanZiVo.setLikeCount(Convert.toInt(quanZiApi.queryLikeCount(quanZiVo.getId()))); //TODO 点赞数
//        quanZiVo.setLikeCount(0); //TODO 点赞数
        quanZiVo.setHasLoved(quanZiApi.queryUserIsLove(UserThreadLocal.getUser().getId(),quanZiVo.getId())?1:0); //TODO 是否喜欢（1是，0否）
        quanZiVo.setLoveCount(Convert.toInt(quanZiApi.queryLoveCount(quanZiVo.getId()))); //TODO 喜欢数
    }

    /**
     * 填充结果
     * @param publishList
     * @return
     */
    private List<QuanZiVo> fillQuanZiVo(List<Publish> publishList) {
        //根据用户id查询出用户的详情信息
        List<Object> userIds = CollUtil.getFieldValues(publishList, "userId");
        //通过userId查询出用户详情信息
        List<UserInfo> userInfoList = userInfoService.queryUserInfoByUserIds(userIds);
        //对集合进行转换
        List<QuanZiVo> quanZiVoList = publishList.stream().map(publish -> {
            QuanZiVo quanZiVo = new QuanZiVo();
            quanZiVo.setId(publish.getId().toString());
            quanZiVo.setUserId(publish.getUserId());
            quanZiVo.setTextContent(publish.getText());
            quanZiVo.setImageContent(publish.getMedias().toArray(new String[]{}));
            quanZiVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
            return quanZiVo;
        }).collect(Collectors.toList());
        quanZiVoList.forEach(quanZiVo -> {
            for (UserInfo userInfo : userInfoList) {
                if (quanZiVo.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    fillUserInfo(quanZiVo, userInfo);
                }
            }
        });
        return quanZiVoList;
    }

    /**
     * 点赞功能
     * @param publishId
     * @param type
     * @return
     */
    public Long myComment(String publishId, CommentType type){
        //TODO 通用功能
        User user = UserThreadLocal.getUser();
        Boolean likeComment = quanZiApi.likeComment(user.getId(), publishId);
        if(!likeComment){
            throw new RuntimeException("用户点赞失败+userId"+user.getId());
        }
        return quanZiApi.queryLikeCount(publishId);
    }

    /**
     * 点赞动态
     * @param publishId
     * @return
     */
    public Long likeComment(String publishId){
        User user = UserThreadLocal.getUser();
        Boolean likeComment = quanZiApi.likeComment(user.getId(), publishId);
        if(!likeComment){
            throw new RuntimeException("用户点赞失败+userId"+user.getId());
        }else{
            //发消息
            this.quanziMQService.likePublishMsg(publishId);
        }
        return quanZiApi.queryLikeCount(publishId);
    }

    /**
     * 取消点赞
     * @param publishId
     * @return
     */
    public Long dislikeComment(String publishId){
        User user = UserThreadLocal.getUser();
        Boolean likeComment = quanZiApi.dislikeComment(user.getId(), publishId);
        if(!likeComment){
            throw new RuntimeException("用户取消点赞失败+userId"+user.getId());
        }else{
            //发消息
            this.quanziMQService.disLikePublishMsg(publishId);
        }
        return quanZiApi.queryLikeCount(publishId);
    }

    /**
     * 喜欢动态
     * @param publishId
     * @return
     */
    public Long loveComment(String publishId){
        User user = UserThreadLocal.getUser();
        Boolean loveComment = quanZiApi.loveComment(user.getId(), publishId);
        if(!loveComment){
            throw new RuntimeException("用户点击喜欢失败+userId"+user.getId());
        }else{
            //发消息
            this.quanziMQService.lovePublishMsg(publishId);
        }
        return quanZiApi.queryLoveCount(publishId);
    }

    /**
     * 取消喜欢
     * @param publishId
     * @return
     */
    public Long disloveComment(String publishId){
        User user = UserThreadLocal.getUser();
        Boolean loveComment = quanZiApi.disloveComment(user.getId(), publishId);
        if(!loveComment){
            throw new RuntimeException("用户取消点击喜欢失败+userId"+user.getId());
        }else{
            //发消息
            this.quanziMQService.disLovePublishMsg(publishId);
        }
        return quanZiApi.queryLoveCount(publishId);
    }

    /**
     * 查询单条动态
     * @param publishId
     * @return
     */
    public QuanZiVo querySinglonPublish(String publishId) {
        Publish publish = quanZiApi.getPublishById(publishId);
        if(Objects.isNull(publish)){
            return null;
        }
        //发送消息
        this.quanziMQService.queryPublishMsg(publishId);
        //填充信息
        return this.fillQuanZiVo(Arrays.asList(publish)).get(0);
    }

    /**
     * 查询评论列表
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryCommentList(String publishId, Integer page, Integer pageSize) {
        User user = UserThreadLocal.getUser();
        PageResult pageResult = getPageResult(page,pageSize);
        PageInfo<Comment> pageInfo = quanZiApi.queryCommentList(publishId, page, pageSize);
        if(CollUtil.isEmpty(pageInfo.getRecords())){
            return pageResult;
        }
        //封装属性
        List<Comment> records = pageInfo.getRecords();
        List<Object> ids = CollUtil.getFieldValues(records,"userId");
        List<UserInfo> userInfoList = userInfoService.queryUserInfoByUserIds(ids);
        Map<Long, UserInfo> map = userInfoList.stream().collect(Collectors.toMap(k -> k.getUserId(), v -> v));
        List<CommentVo> collects = records.stream().map(record -> {
            CommentVo commVo = new CommentVo();
            commVo.setId(record.getId().toHexString());
            commVo.setContent(record.getContent());
            commVo.setCreateDate(DateUtil.format(new Date(record.getCreated()), "HH:mm"));
            commVo.setLikeCount(Convert.toInt(quanZiApi.queryLikeCount(commVo.getId())));
            commVo.setHasLiked(quanZiApi.queryUserIsLike(user.getId(), commVo.getId()) ? 1 : 0);
            UserInfo userInfo = map.get(record.getUserId());
            if (ObjectUtil.isNotEmpty(userInfo)) {
                commVo.setNickname(userInfo.getNickName());
                commVo.setAvatar(userInfo.getLogo());
            }
            return commVo;
        }).collect(Collectors.toList());
        pageResult.setItems(collects);
//        pageResult.setPageSize(records.size());
        return pageResult;
    }

    /**
     * 提交评论
     * @param publishId
     * @param comment
     */
    public void saveComment(String publishId, String comment) {
        User user = UserThreadLocal.getUser();
        Boolean flag = quanZiApi.saveComment(user.getId(), publishId, comment);
        if(flag){
            //发送消息
            this.quanziMQService.commentPublishMsg(publishId);
        }

    }

    /**
     * 点赞评论
     * @param publishId
     * @return
     */
    public Long likeContent(String publishId) {
        User user = UserThreadLocal.getUser();
        Boolean likeContent = quanZiApi.likeContent(user.getId(), publishId);
        if(!likeContent){
            throw new RuntimeException("用户点赞失败+userId"+user.getId());
        }
        return quanZiApi.queryLikeCount(publishId);
    }

    /**
     * 取消点赞评论
     * @param publishId
     * @return
     */
    public Long dislikeContent(String publishId) {
        User user = UserThreadLocal.getUser();
        Boolean loveContent = quanZiApi.dislikeContent(user.getId(), publishId);
        if(!loveContent){
            throw new RuntimeException("用户取消点击喜欢失败+userId"+user.getId());
        }
        return quanZiApi.queryLikeCount(publishId);
    }

    /**
     * 查询用户动态
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult queryAlbumList(Long userId, Integer page, Integer pagesize) {
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        PageInfo<Publish> pageInfo = quanZiApi.queryAlbumList(userId, page, pagesize);
        List<Publish> records = pageInfo.getRecords();
        if(CollUtil.isEmpty(records)){
            return pageResult;
        }
        List<QuanZiVo> quanZiVos = fillQuanZiVo(records);
        pageResult.setItems(quanZiVos);
        return pageResult;
    }

    /**
     * 查询访客
     * @return
     */
    public List<VisitorsVo> queryVisitorsList() {
        User user = UserThreadLocal.getUser();
        List<Visitors> visitors = visitorsApi.queryMyVisitor(user.getId());
        if(CollUtil.isEmpty(visitors)){
            return null;
        }
        //封装数据
        List<Object> visitorUserIds = CollUtil.getFieldValues(visitors, "visitorUserId");
        //查询来访的信息
        Map<Long, UserInfo> map = userInfoService.queryUserInfoByUserIds(visitorUserIds).stream().collect(Collectors.toMap(k -> k.getUserId(), v -> v));
        List<VisitorsVo> collect = visitors.stream().map(visitor -> {
            VisitorsVo visitorsVo = new VisitorsVo();
            visitorsVo.setId(visitor.getVisitorUserId());
            visitorsVo.setFateValue(Convert.toInt(visitor.getScore()));
            UserInfo userInfo = map.get(visitor.getVisitorUserId());
            if (ObjectUtil.isNotEmpty(userInfo)) {
                visitorsVo.setAge(userInfo.getAge());
                visitorsVo.setAvatar(userInfo.getLogo());
                visitorsVo.setGender(userInfo.getSex().name().toLowerCase());
                visitorsVo.setNickname(userInfo.getNickName());
                visitorsVo.setTags(StringUtils.split(userInfo.getTags(), ','));
            }
            return visitorsVo;
        }).collect(Collectors.toList());
        return collect;
    }
}
