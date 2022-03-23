package com.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.common.enums.SexEnum;
import com.tanhua.common.mapper.UserInfoMapper;
import com.tanhua.common.pojo.User;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.common.service.PicUploadService;
import com.tanhua.common.vo.PicUploadResult;
import com.tanhua.sso.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @author: tang
 * @date: Create in 21:50 2021/8/3
 * @description:
 */
@Service
@Slf4j
@Transactional
public class UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    private FaceEngineService faceEngineService;

    @Autowired
    private UserService userService;


    /**
     * 保存用户基本信息
     * @param map
     * @param token
     * @return
     */
    public ErrorResult saveUserInfo(Map<String, String> map, String token) {
        //获取用户的token信息
        try {
            User user = userService.getTokenInfo(token);
            if(null == user){
                return ErrorResult.builder().errCode("00003").errMessage("用户未登陆").build();
            }
            //保存用户基本信息
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(user.getId());
            userInfo.setSex(StringUtils.equalsIgnoreCase(map.get("gender"),"man")? SexEnum.MAN:SexEnum.WOMAN);
            userInfo.setNickName(map.get("nickname"));
            userInfo.setBirthday(map.get("birthday"));
            userInfo.setCity(map.get("city"));
            /*LocalDate over = LocalDate.parse(map.get("birthday"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate now = LocalDate.now();*/
            DateTime over = DateTime.parse(map.get("birthday"));
            DateTime now = DateTime.now();
            userInfo.setAge(now.getYear()-over.getYear());
            userInfoMapper.insert(userInfo);
            return null;
        } catch (Exception e) {
            log.error("保存用户基本信息失败~");
        }
        return ErrorResult.builder().errCode("00004").errMessage("保存用户基本信息失败").build();
    }



    /**
     * 保存用户头像
     * @param headPhoto
     * @param token
     * @return
     */
    public Boolean saveUserHeadPic(MultipartFile headPhoto, String token) {
        User user = userService.getTokenInfo(token);
        if(null == user){
            log.error("用户未登陆~");
            return false;
        }
        //判断用户上传是否为人脸
        Boolean isPortrait;
        try {
            isPortrait = faceEngineService.checkIsPortrait(headPhoto.getBytes());
        } catch (IOException e) {
            log.error("判断头像出错~"+e);
            return false;
        }
        if(!isPortrait){
            return false;
        }
        //上传用户头像
        PicUploadResult result = picUploadService.uploadResult(headPhoto);
        if(StringUtils.isBlank(result.getName())){
            log.error("用户头像上传失败~");
            return false;
        }

        //保存用户头像信息到数据库中
        UserInfo userInfo = new UserInfo();
        userInfo.setLogo(result.getName());
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("user_id",user.getId());
        return userInfoMapper.update(userInfo,wrapper) == 1;
    }
}
