package com.tanhua.common.service;

import com.aliyun.oss.OSSClient;
import com.tanhua.common.config.AliyunOssConfig;
import com.tanhua.common.vo.PicUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author: tang
 * @date: Create in 22:58 2021/8/2
 * @description:
 */
@Service
@Slf4j
public class PicUploadService {

    @Autowired
    private OSSClient ossClient;

    @Autowired
    private AliyunOssConfig ossConfig;

    //允许上传的文件后缀
    private static final String[] image_suf = {".bmp", ".jpg",".jpeg", ".gif", ".png"};

    public PicUploadResult uploadResult(MultipartFile file){
        PicUploadResult picUploadResult = new PicUploadResult();
        //判断文件后缀名是否合法
        Boolean isLegitimate = false;
        for (String s : image_suf) {
            if(StringUtils.endsWithIgnoreCase(file.getOriginalFilename(),s)){
                isLegitimate = true;
                break;
            }
        }
        if(!isLegitimate){
            //文件名不合法
            picUploadResult.setStatus("error");
            return picUploadResult;
        }
        //设置文件的新路径
        String filename = file.getOriginalFilename();
        String filepath = this.getFilePath(filename);

        //上传图片
        try {
            ossClient.putObject(ossConfig.getBucketName(),filepath,new ByteArrayInputStream(file.getBytes()));
        } catch (IOException e) {
            log.error("文件上传到oss失败~"+filepath);
            picUploadResult.setStatus("error");
            return picUploadResult;
        }

        //上传成功
        picUploadResult.setStatus("done");
        //拼接文件路径  指定服务器上的路径
        picUploadResult.setName(ossConfig.getUrlPrefix()+filepath);
        picUploadResult.setUid(String.valueOf(System.currentTimeMillis()));
        return picUploadResult;
    }

    private String getFilePath(String filename) {
        //路径  images/yyyy/mm/dd/随机数(当前时间的毫秒数与随机数).xxx
        DateTime dateTime = new DateTime();
        return "images/"+dateTime.toString("yyyy")+"/"+
                dateTime.toString("MM")+"/"+
                dateTime.toString("dd")+"/"+
                System.currentTimeMillis()+ RandomUtils.nextInt(10000,99999)+"."+
                StringUtils.substringAfterLast(filename,".");
    }
}
