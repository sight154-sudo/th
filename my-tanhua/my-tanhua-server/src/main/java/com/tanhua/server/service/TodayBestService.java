package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.common.pojo.User;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 11:11 2021/8/5
 * @description:
 */
@Service
public class TodayBestService {

    @Autowired
    private UserService userService;

    @Value("${tanhua.sso.default.user}")
    private Long defaultUser;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RecommendUserService recommendUserService;
    /**
     * 根据用户登陆的信息 查询出今日佳人
     * @return
     */
    public TodayBest queryTodayBest() {
        //根据用户登陆信息 查询用户信息
        User user = UserThreadLocal.getUser();
        if(null == user){
            //token过期
            return null;
        }
        //查询出推荐的今日佳人
        TodayBest todayBest = recommendUserService.queryTodayBest(user.getId());
        if(null == todayBest){
            //如果没有匹配 设置一个默认的佳人
            todayBest = new TodayBest();
            todayBest.setId(defaultUser);
            todayBest.setFateValue(80l);
        }
        //根据用户id  从数据库中查询信息
        UserInfo userInfo = userInfoService.queryUserInfoByUserId(todayBest.getId());
        if(null == userInfo){
            return null;
        }
        //设置返回结果信息
        todayBest.setAge(userInfo.getAge());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setGender(userInfo.getSex().getValue() == 1?"man":"woman");
        String[] split = StringUtils.split(userInfo.getTags(), ",");
        todayBest.setTags(split);
        todayBest.setAvatar(userInfo.getLogo());

        return todayBest;
    }

    /**
     * 通过条件查询列表
     * @param queryParam
     * @return
     */
    public PageResult  queryRecommendation(RecommendUserQueryParam queryParam) {

        User user = UserThreadLocal.getUser();
        if(null == user){
            throw new RuntimeException("用户未认证");
        }
        //获取分页条件
        Integer pagesize = queryParam.getPagesize();
        Integer page = queryParam.getPage();
        //通过用户的id查询mongo中与用户相关联的用户信息
        PageInfo<RecommendUser> pageInfo = recommendUserService.queryRecommentList(user.getId(),page,pagesize);

        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
//        pageResult.setPages(pageInfo.getRecords().size());
        //设置列表信息
        List<RecommendUser> records = pageInfo.getRecords();
        if(CollectionUtils.isEmpty(records)){
            return pageResult;
        }
        //根据推荐用户的id,查询出推荐用户的信息
        Set ids = new HashSet();
        //封装数据
        for (RecommendUser record : records) {
            ids.add(record.getUserId());
        }
        //根据条件 查询信息
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        //拼接条件  城市不为空
        if(StringUtils.isNotBlank(queryParam.getCity())){
//            wrapper.like("city",queryParam.getCity());
        }
        //学历条件
        if(StringUtils.isNotBlank(queryParam.getEducation())){
//           wrapper.eq("edu",queryParam.getEducation());
        }
        //年龄条件
        if(Objects.nonNull(queryParam.getAge())){
//            wrapper.le("age",queryParam.getAge());
        }
        //性别条件
        if(StringUtils.isNotBlank(queryParam.getGender())){
//            wrapper.eq("sex",queryParam.getGender().equalsIgnoreCase("man")?"1":"2");
        }
        //指定user_id
        wrapper.in("user_id",ids);
        //查询推荐用户的详细用户信息
        List<UserInfo> userInfoList = userInfoService.queryUserInfoList(wrapper);
        //对集合进行转换
        List<TodayBest> collect = userInfoList.stream().map(userInfo -> {
            TodayBest todayBest = new TodayBest();
            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ","));
            todayBest.setAvatar(userInfo.getLogo());
            for (RecommendUser record : records) {
                if (record.getUserId() == userInfo.getUserId()) {
                    Double score = Math.floor(record.getScore());
                    todayBest.setFateValue(score.longValue());
                    break;
                }
            }
            return todayBest;
        }).collect(Collectors.toList());
        Collections.sort(collect,(o1, o2) -> (int) (o2.getFateValue()-o1.getFateValue()));
        pageResult.setItems(collect);
        return pageResult;
    }




}
