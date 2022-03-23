package com.tanhua.server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.common.enums.SexEnum;
import com.tanhua.common.pojo.*;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.RecommendUserApi;
import com.tanhua.dubbo.server.api.UserLikeApi;
import com.tanhua.dubbo.server.api.VisitorsApi;
import com.tanhua.dubbo.server.pojo.UserLike;
import com.tanhua.dubbo.server.pojo.Visitors;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 12:59 2021/8/13
 * @description:
 */
@Service
public class MyCenterService {


    @Autowired
    private UserInfoService userInfoService;

    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;
    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;

    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    @Autowired
    private TanHuaService tanHuaService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private IMService imService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private BlackListService blackListService;

    /**
     * 查询用户详情信息
     * @param userId
     * @param huanxinId
     * @return
     */
    public UserInfoVo queryUserInfo(Long userId,String huanxinId) {
        if(ObjectUtil.isEmpty(userId)){
            User user = UserThreadLocal.getUser();
            userId = user.getId();
        }
        UserInfo userInfo = userInfoService.queryUserInfoByUserId(userId);
        UserInfoVo userInfoVo = BeanUtil.copyProperties(userInfo,UserInfoVo.class,"marriage");
        userInfoVo.setGender(userInfo.getSex().getValue()==1?"男":"女");
        userInfoVo.setMarriage(userInfo.getMarriage().equals("已婚")?1:0);
        return userInfoVo;
    }

    /**
     * 更新用户信息
     * @param userInfoVo
     */
    public void saveUserInfo(UserInfoVo userInfoVo) {
        User user = UserThreadLocal.getUser();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setAge(Integer.valueOf(userInfoVo.getAge()));
        userInfo.setSex(StringUtils.equalsIgnoreCase(userInfoVo.getGender(), "man") ? SexEnum.MAN : SexEnum.WOMAN);
        userInfo.setBirthday(userInfoVo.getBirthday());
        userInfo.setCity(userInfoVo.getCity());
        userInfo.setEdu(userInfoVo.getEducation());
        userInfo.setIncome(StringUtils.replaceAll(userInfoVo.getIncome(), "K", ""));
        userInfo.setIndustry(userInfoVo.getProfession());
        userInfo.setMarriage(userInfoVo.getMarriage() == 1 ? "已婚" : "未婚");
        this.userInfoService.updateUserInfo(userInfo);
    }

    /**
     * 查询用户统计数
     * @return
     */
    public CountsVo queryCounts() {
        User user = UserThreadLocal.getUser();
        Long mutualLikeCount = userLikeApi.queryMutualLikeCount(user.getId());
        Long queryLikeCount = userLikeApi.queryLikeCount(user.getId());
        Long fanCount = userLikeApi.queryFanCount(user.getId());
        CountsVo countsVo = new CountsVo();
        countsVo.setFanCount(fanCount);
        countsVo.setLoveCount(queryLikeCount);
        countsVo.setEachLoveCount(mutualLikeCount);
        return countsVo;
    }

    /**
     * 查询用户互相喜欢，关注，粉丝，谁看过我列表
     * @param type
     * @param page
     * @param pagesize
     * @param nickname
     * @return
     */
    public PageResult queryCeneralList(String type, Integer page, Integer pagesize, String nickname) {
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        Long userId = UserThreadLocal.getUser().getId();
        //1 互相喜欢  2：我关注  3：粉丝  4：谁看过我
        List<Object> userIds = this.queryUserIds(type,userId,page,pagesize);
        if(CollUtil.isEmpty(userIds)){
            return pageResult;
        }
        //指定查询条件
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        if(StrUtil.isNotBlank(nickname)){
            wrapper.like(UserInfo::getNickName,nickname);
        }
        if(CollUtil.isNotEmpty(userIds)){
            wrapper.in(UserInfo::getUserId,userIds);
        }
        //查询结果
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(wrapper);
        //封装属性
        List<UserLikeListVo> userLikeListVos = userInfoList.stream().map(userInfo -> {
            UserLikeListVo userLikeListVo = new UserLikeListVo();
            userLikeListVo.setId(userInfo.getUserId());
            userLikeListVo.setAvatar(userInfo.getLogo());
            userLikeListVo.setAge(userInfo.getAge());
            userLikeListVo.setEducation(userInfo.getTags());
            userLikeListVo.setNickname(userInfo.getNickName());
            userLikeListVo.setCity(userInfo.getCity());
            userLikeListVo.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
            userLikeListVo.setMarriage(userInfo.getMarriage().equals("未婚") ? 0 : 1);
            //查询用户之间的缘分值
            Double score = this.recommendUserApi.queryUserScore(userId, userInfo.getUserId());
            userLikeListVo.setMatchRate(Convert.toInt(score));
            userLikeListVo.setAlreadyLove(userLikeApi.isLike(userId, userInfo.getUserId()));
            return userLikeListVo;
        }).collect(Collectors.toList());
        Collections.sort(userLikeListVos,(o1, o2) -> o2.getMatchRate()-o1.getMatchRate());
        pageResult.setItems(userLikeListVos);
        return pageResult;
    }

    /**
     * 收集用户id
     * @param type
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    private List<Object> queryUserIds(String type,Long userId,Integer page,Integer pagesize){
        List<Object> userIds = null;
        switch (type){
            case "1":{
                PageInfo<UserLike> pageInfo = userLikeApi.queryMutualLikeList(userId, page, pagesize);//收集userId
                if(CollUtil.isNotEmpty(pageInfo.getRecords())){
                    userIds = CollUtil.getFieldValues(pageInfo.getRecords(),"userId");
                }
                break;
            }
            case "2":{
                PageInfo<UserLike> pageInfo = userLikeApi.queryLikeList(userId,page,pagesize);//收集likeUserId
                if(CollUtil.isNotEmpty(pageInfo.getRecords())){
                    userIds = CollUtil.getFieldValues(pageInfo.getRecords(),"likeUserId");
                }
                break;
            }
            case "3":{
                PageInfo<UserLike> pageInfo = userLikeApi.queryFanList(userId,page,pagesize);//收集userId
                if(CollUtil.isNotEmpty(pageInfo.getRecords())){
                    userIds = CollUtil.getFieldValues(pageInfo.getRecords(),"userId");
                }
                break;
            }
            case "4":{
                PageInfo<Visitors> pageInfo = visitorsApi.queryVisitorsList(userId,page,pagesize);
                if(CollUtil.isNotEmpty(pageInfo.getRecords())){
                    userIds = CollUtil.getFieldValues(pageInfo.getRecords(),"visitorUserId");
                }
                break;
            }
            default:
                return null;
        }
        return userIds;
    }

    /**
     * 取消用户喜欢
     * @param userId
     */
    public void cancelUserLike(Long userId) {
        User user = UserThreadLocal.getUser();
        //用户是否互相喜欢
        Boolean flag = this.userLikeApi.isMutualLike(user.getId(), userId);
        //用户取消喜欢
        this.userLikeApi.cancelUserLike(user.getId(),userId);
        //若用户互相喜欢 则需要解除好友关系  并且也解析环信上的好友关系
        if(flag){
            this.imService.removeFriend(user.getId(),userId);
        }
    }

    /**
     * 用户喜欢粉丝
     * @param userId
     */
    public void userlikeFans(Long userId) {
        //用户喜欢粉丝后需要添加好友关系
        this.tanHuaService.userLike(userId);
    }

    /**
     * 查询用户通用设置
     * @return
     */
    public SettingsVo querySettings() {
        SettingsVo settingsVo = new SettingsVo();
        User user = UserThreadLocal.getUser();
        settingsVo.setId(user.getId());
        settingsVo.setPhone(user.getMobile());
        Question question = this.questionService.queryQuestion(user.getId());
        if(ObjectUtil.isEmpty(question)){
            return settingsVo;
        }
        settingsVo.setStrangerQuestion(question.getTxt());
        Settings settings = this.settingsService.queryUserSettings(user.getId());
        if(ObjectUtil.isEmpty(settings)){
            return settingsVo;
        }
        //封装数据
        settingsVo.setLikeNotification(settings.getLikeNotification());
        settingsVo.setPinglunNotification(settings.getPinglunNotification());
        settingsVo.setGonggaoNotification(settings.getGonggaoNotification());
        return settingsVo;
    }

    /**
     * 修改用户通用设置
     * @param settings
     */
    public void saveGlobalSetting(Settings settings) {
        User user = UserThreadLocal.getUser();
        settings.setId(user.getId());
        this.settingsService.updateUserSettings(settings);
    }

    /**
     * 设置陌生人问题
     * @param map
     */
    public void setUserQuestion(Map<String, String> map) {
        String content = map.get("content");
        if(StrUtil.isBlank(content)){
            return;
        }
        User user = UserThreadLocal.getUser();
        Question question = this.questionService.queryQuestion(user.getId());
        if(ObjectUtil.isEmpty(question)){
            question = new Question();
            question.setUserId(user.getId());
            question.setTxt(content);
            this.questionService.insertQuestion(question);
        }else{
            question.setTxt(content);
            this.questionService.updateQuestion(question);
        }
    }

    /**
     * 查询用户黑名单
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult queryBlacklist(Integer page, Integer pagesize) {
        User user = UserThreadLocal.getUser();
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        IPage<BlackList> blackListIPage = this.blackListService.queryPageBlackList(user.getId(), page, pagesize);
        List<BlackList> records = blackListIPage.getRecords();
        if(CollUtil.isEmpty(records)){
            return pageResult;
        }
        List<Object> blackUserIds = CollUtil.getFieldValues(records, "blackUserId");
        Map<Long, UserInfo> map = this.userInfoService.queryUserInfoByUserIds(blackUserIds).stream().collect(Collectors.toMap(k -> k.getUserId(), v -> v));
        List<BlackListVo> collect = records.stream().map(record -> {
            BlackListVo blackListVo = new BlackListVo();
            blackListVo.setId(record.getBlackUserId());
            UserInfo userInfo = map.get(record.getBlackUserId());
            if (ObjectUtil.isNotEmpty(userInfo)) {
                blackListVo.setNickname(userInfo.getNickName());
                blackListVo.setAge(userInfo.getAge());
                blackListVo.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
                blackListVo.setAvatar(userInfo.getLogo());
            }
            return blackListVo;
        }).collect(Collectors.toList());
        pageResult.setItems(collect);
        return pageResult;
    }

    /**
     * 移除用户黑名单
     * @param userId
     */
    public void removeBlackList(Long userId) {
        User user = UserThreadLocal.getUser();
        this.blackListService.removeBlackList(user.getId(),userId);
    }

    /**
     * 查询用户是否已喜欢
     * @param userId
     * @return
     */
    public Boolean queryUserAlreadyLove(Long userId) {
        User user = UserThreadLocal.getUser();
        return this.userLikeApi.isLike(user.getId(),userId);
    }
}
