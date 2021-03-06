package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.common.mapper.QuestionMapper;
import com.tanhua.common.pojo.*;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.api.*;
import com.tanhua.dubbo.server.enums.HuanXinMessageType;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.dubbo.server.vo.UserLocationVo;
import com.tanhua.server.vo.NearUserVo;
import com.tanhua.server.vo.TodayBest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 15:37 2021/8/14
 * @description:
 */
@Service
public class TanHuaService {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RecommendUserService recommendUserService;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private BlackListService blackListService;

    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;

    @Reference(version = "1.0.0")
    private HuanXinApi huanXinApi;
    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;
    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    @Autowired
    private HuanXinService huanXinService;

    @Autowired
    private IMService imService;

    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;

    @Value("${tanhua.default.recommend.users}")
    private String defaultRecommendUsers;

    public TodayBest queryUserInfo(Long userId) {
        UserInfo userInfo = userInfoService.queryUserInfoByUserId(userId);
        if(ObjectUtil.isEmpty(userInfo)){
            return null;
        }
        TodayBest todayBest = new TodayBest();
        todayBest.setId(userId);
        todayBest.setAge(userInfo.getAge());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setGender(userInfo.getSex().getValue() == 1?"man":"woman");
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ","));
        todayBest.setAvatar(userInfo.getLogo());

        todayBest.setFateValue(recommendUserService.queryFateValue(userId, UserThreadLocal.getUser().getId()).longValue());


        //?????????????????????????????????
        try {
            if(userId.longValue() != UserThreadLocal.getUser().getId().longValue()){
                visitorsApi.saveVisitor(userId,UserThreadLocal.getUser().getId(),"????????????");
            }
        } catch (Exception e) {
            throw new RuntimeException("????????????????????????~");
        }
        return todayBest;
    }
    /**
     * ?????????????????????
     * @param userId
     * @return
     */
    public String queryQuestion(Long userId) {

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getUserId,userId);
        Question question = questionMapper.selectOne(wrapper);
        if(ObjectUtil.isNotEmpty(question)){
            String txt = question.getTxt();
            if(StringUtils.isNotBlank(txt)){
                return txt;
            }
        }
        //???????????? ???????????????
        return "????????????????????????";
    }

    /**
     * ?????????????????????
     * @param map
     */
    public void replyQuestion(Map<String, Object> map) {
        Long userId = Convert.toLong(map.get("userId"));
        String reply = Convert.toStr(map.get("reply"));
        User user = UserThreadLocal.getUser();
        //TODO ???????????????????????????????????????????????? ??????????????????
        List<BlackList> blackLists = this.blackListService.queryBlackList(user.getId());
        if(blackLists.contains(userId)){
            return;
        }
        HuanXinUser from = this.huanXinService.queryHuanXinUserByUid(user.getId());
        HuanXinUser to = this.huanXinService.queryHuanXinUserByUid(userId);
//        HuanXinUser from = huanXinApi.queryHuanXinUser(user.getId());
//        HuanXinUser to = huanXinApi.queryHuanXinUser(userId);
        UserInfo fromUserInfo = userInfoService.queryUserInfoByUserId(user.getId());
        //{"userId":1,"huanXinId":"HX_1","nickname":"????????????","strangerQuestion":"????????????????????????????????????????????????????????????"
        // ,"reply":"???????????????????????????????????????????????????????????????????????????????????????~"}
        //??????????????????
        Map<String,Object> msg = new HashMap<>();
        msg.put("userId",user.getId());
        msg.put("huanXinId",from.getUsername());
        msg.put("nickname",fromUserInfo.getNickName());
        msg.put("strangerQuestion",this.queryQuestion(userId));
        msg.put("reply",reply);
        huanXinApi.sendMsgFromAdmin(to.getUsername(), HuanXinMessageType.TXT, JSONUtil.toJsonStr(msg));
    }

    /**
     * ???????????????
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> searchNear(String gender, Double distance) {
        List<NearUserVo> list = Collections.emptyList();
        User user = UserThreadLocal.getUser();
        //???????????????????????????
        UserLocationVo ownLocation = userLocationApi.queryUserLocation(user.getId());
        //???????????????
        PageInfo<UserLocationVo> pageInfo = userLocationApi.queryUserFromLocation(ownLocation.getLongitude(),
                ownLocation.getLatitude(),
                distance, 1, 50);

        //????????????
        List<UserLocationVo> records = pageInfo.getRecords().stream().filter(record->record.getUserId().longValue() != user.getId().longValue()).collect(Collectors.toList());
        if(CollUtil.isEmpty(records)){
            return list;
        }
        //????????????  ????????????
        List<Object> uids = CollUtil.getFieldValues(records, "userId");
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper();
        wrapper.notIn(UserInfo::getUserId,uids);
        if(ObjectUtil.isNotEmpty(gender)){
            wrapper.eq(UserInfo::getSex,gender.equalsIgnoreCase("man")?1:2);
        }
        //????????????
        Map<Long, UserInfo> map = userInfoService.queryUserInfoList(wrapper).stream().collect(Collectors.toMap(k -> k.getUserId(), v -> v));
        list = records.stream().map(record -> {
            NearUserVo nearUserVo = new NearUserVo();
            UserInfo userInfo = map.get(record.getUserId());
            if (ObjectUtil.isNotEmpty(userInfo)) {
                nearUserVo.setUserId(userInfo.getUserId());
                nearUserVo.setNickname(userInfo.getNickName());
                nearUserVo.setAvatar(userInfo.getLogo());
            }
            return nearUserVo;
        }).collect(Collectors.toList());
        return list;
    }

    /**
     * ??????????????????
     * @return
     */
    public List<TodayBest> queryCardsList() {
        User user = UserThreadLocal.getUser();
        //???????????????????????????
        List<RecommendUser> recommendUsers = recommendUserApi.queryCardList(user.getId(), 50);
        if(CollUtil.isEmpty(recommendUsers)){
            //??????????????? ??????????????????????????????
            recommendUsers = new ArrayList<>();
            List<String> collect = Arrays.stream(StringUtils.split(defaultRecommendUsers, ",")).collect(Collectors.toList());
            for (String s : collect) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Convert.toLong(s));
                recommendUser.setToUserId(user.getId());
                recommendUsers.add(recommendUser);
            }
        }
        //?????????????????????  ????????????10???
        int showCount = Math.min(10,recommendUsers.size());
        Set<Long> result = new HashSet<>();

        while( result.size() < showCount){
            int index = RandomUtil.randomInt(0,recommendUsers.size());
            result.add(recommendUsers.get(index).getUserId());
        }
        //??????id??????
//        List<Object> userIds = CollUtil.getFieldValues(result, "userId");
        List<UserInfo> userInfoList = userInfoService.queryUserInfoByUserIds(result);
        List<TodayBest> collect = userInfoList.stream().map(userInfo -> {
            TodayBest todayBest = new TodayBest();
            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ","));
            todayBest.setAvatar(userInfo.getLogo());
            return todayBest;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * ????????????
     * @param likeUserId
     */
    public void userLike(Long likeUserId) {
        //?????????????????????????????????????????????????????? ???????????????????????????
        User user = UserThreadLocal.getUser();
        userLikeApi.userLike(user.getId(),likeUserId);
        if(userLikeApi.isMutualLike(user.getId(),likeUserId)){
            //????????????
            this.imService.saveLinkedUser(likeUserId);
        }
    }

    /**
     * ???????????????
     * @param disLikeUserId
     */
    public void userNotLike(Long disLikeUserId) {
        User user = UserThreadLocal.getUser();
        userLikeApi.userdisLike(user.getId(),disLikeUserId);
    }

}
