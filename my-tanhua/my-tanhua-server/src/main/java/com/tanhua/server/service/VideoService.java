package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.common.pojo.User;
import com.tanhua.common.pojo.UserInfo;
import com.tanhua.common.service.PicUploadService;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.common.vo.PicUploadResult;
import com.tanhua.dubbo.server.api.QuanZiApi;
import com.tanhua.dubbo.server.api.VideoApi;
import com.tanhua.dubbo.server.pojo.Video;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.VideoVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: tang
 * @date: Create in 23:45 2021/8/10
 * @description:
 */
@Service
public class VideoService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private PicUploadService picUploadService;

    @Value("${fdfs.web-server-url}")
    private String serverUrl;

    @Reference(version = "1.0.0")
    private VideoApi videoApi;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private QuanZiService quanZiService;

    @Autowired
    private VideoMQService videoMQService;


    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    /**
     * 上传小视频
     *
     * @param videoThumbnail
     * @param videoFile
     */
    public void uploadVideo(MultipartFile videoThumbnail, MultipartFile videoFile) {
        User user = UserThreadLocal.getUser();
        Video video = new Video();
        //上传小视频 到fastdfs文件系统
        StorePath storePath = null;
        try {
            storePath = this.storageClient.uploadFile(videoFile.getInputStream(), videoFile.getSize(), StringUtils.substringAfterLast(videoFile.getOriginalFilename(), "."), null);
//      group1/M00/00/00/wKgfUV2GJSuAOUd_AAHnjh7KpOc1.1.jpg
            String videoUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
            System.out.println(videoUrl);
            //上传小视频封面
            PicUploadResult picUploadResult = picUploadService.uploadResult(videoThumbnail);
            if (StringUtils.isBlank(picUploadResult.getName())) {
                throw new RuntimeException("小视频封面上传失败");
            }
            video.setVideoUrl(videoUrl);
            video.setPicUrl(picUploadResult.getName());
            video.setUserId(user.getId());
            video.setSeeType(1);//设置可见类型 默认为公开
            String vid = videoApi.saveVideo(video);
            if(StrUtil.isNotBlank(vid)){
                this.videoMQService.videoMsg(vid);
            }
        } catch (IOException e) {
            throw new RuntimeException("小视频上传失败");
        }
    }

    /**
     * 查询小视频列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult queryVideoList(Integer page, Integer pagesize) {
        PageResult pageResult = new PageResult();
        pageResult.setPagesize(pagesize);
        pageResult.setPage(page);
        User user = UserThreadLocal.getUser();
        //调用服务查询推荐的小视频
        PageInfo<Video> pageInfo = videoApi.queryVideoList(user.getId(), page, pagesize);
        //封装数据
        List<Video> records = pageInfo.getRecords();
        if(CollUtil.isEmpty(records)){
            return pageResult;
        }
        List<VideoVo> videoVoList = fillVideo(records);
        pageResult.setItems(videoVoList);
        return pageResult;
    }

    private List<VideoVo> fillVideo(List<Video> records) {
        List<Object> ids = CollUtil.getFieldValues(records,"userId");
        //查询用户的详情信息
        Map<Long,UserInfo> map = userInfoService.queryUserInfoByUserIds(ids).stream().collect(Collectors.toMap(k->k.getUserId(), v->v));
        //填充视频信息
        List<VideoVo> videoVoList = records.stream().map(record -> {
            VideoVo videoVo = new VideoVo();
            videoVo.setId(record.getId().toHexString());
            videoVo.setUserId(record.getUserId());
            videoVo.setVideoUrl(record.getVideoUrl());
            videoVo.setCover(record.getPicUrl());

            videoVo.setSignature("我就是我~"); //TODO 签名

            videoVo.setCommentCount(Convert.toInt(this.quanZiApi.queryCommentCount(videoVo.getId()))); //TODO 评论数
            videoVo.setHasFocus(this.videoApi.isFollowUser(UserThreadLocal.getUser().getId(),record.getUserId())?1:0); //TODO 是否关注
            videoVo.setHasLiked(quanZiApi.queryUserIsLike(UserThreadLocal.getUser().getId(), videoVo.getId())?1:0); //TODO 是否点赞（1是，0否）
            videoVo.setLikeCount(Convert.toInt(this.quanZiApi.queryLikeCount(videoVo.getId())));//TODO 点赞数

            //填充用户信息
            UserInfo userInfo = map.get(videoVo.getUserId());
            if(ObjectUtil.isNotEmpty(userInfo)){
                videoVo.setAvatar(userInfo.getLogo());
                videoVo.setNickname(userInfo.getNickName());
            }
            return videoVo;
        }).collect(Collectors.toList());
        return videoVoList;
    }

    /**
     * 用户点赞视频
     * @param videoId
     * @return
     */
    public Long likeVideo(String videoId) {
        User user = UserThreadLocal.getUser();
        Boolean flag = this.quanZiApi.likeComment(user.getId(), videoId);
        if(!flag){
            return null;
        }else{
            this.videoMQService.likeVideoMsg(videoId);
        }
        return this.quanZiApi.queryLikeCount(videoId);
    }

    /**
     * 取消点赞视频
     * @param videoId
     * @return
     */
    public Long dislikeVideo(String videoId) {
        User user = UserThreadLocal.getUser();
        Boolean flag = this.quanZiApi.dislikeComment(user.getId(), videoId);
        if(!flag){
            return null;
        }else{
            this.videoMQService.disLikeVideoMsg(videoId);
        }
        return this.quanZiApi.queryLikeCount(videoId);
    }

    /**
     * 小视频评论列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult queryVideoContentList(String videoId,Integer page, Integer pagesize) {
        PageResult pageResult = quanZiService.queryCommentList(videoId, page, pagesize);
        return pageResult;
    }

    public Long likeContent(String publishId) {
        return quanZiService.likeContent(publishId);
    }

    public Long dislikeContent(String publishId) {
        return quanZiService.dislikeContent(publishId);
    }

    /**
     * 提交评论
     * @param publishId
     * @param content
     */
    public void saveComment(String publishId, String content) {
        quanZiService.saveComment(publishId,content);
        this.videoMQService.commentVideoMsg(publishId);
    }

    public void followUser(Long followUserId) {
        User user = UserThreadLocal.getUser();
        videoApi.followUser(user.getId(),followUserId);
    }

    public void disfollowUser(Long followUserId) {
        User user = UserThreadLocal.getUser();
        videoApi.disFollowUser(user.getId(),followUserId);
    }
}
