package com.tanhua.sso.service;

import com.arcsoft.face.*;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author: tang
 * @date: Create in 21:01 2021/8/3
 * @description:人脸识别
 */
@Service
public class FaceEngineService {

    @Value("${arcsoft.appid}")
    private String appId ;
    @Value("${arcsoft.sdkKey}")
    private String sdkKey ;
    @Value("${arcsoft.libPath}")
    private String libPath;


    private FaceEngine faceEngine;


    /**
     *  初始化人脸识别引擎
     */
    @PostConstruct
    public void init(){


        FaceEngine faceEngine = new FaceEngine(libPath);
        //激活引擎
        int errorCode = faceEngine.activeOnline(appId, sdkKey);

        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            System.out.println("引擎激活失败");
        }


        ActiveFileInfo activeFileInfo=new ActiveFileInfo();
        errorCode = faceEngine.getActiveFileInfo(activeFileInfo);
        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            System.out.println("获取激活文件信息失败");
        }

        //引擎配置
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);
        engineConfiguration.setDetectFaceMaxNum(10);
        engineConfiguration.setDetectFaceScaleVal(16);
        //功能配置
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setSupportAge(true);
        functionConfiguration.setSupportFace3dAngle(true);
        functionConfiguration.setSupportFaceDetect(true);
        functionConfiguration.setSupportFaceRecognition(true);
        functionConfiguration.setSupportGender(true);
        functionConfiguration.setSupportLiveness(true);
        functionConfiguration.setSupportIRLiveness(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);


        //初始化引擎
        errorCode = faceEngine.init(engineConfiguration);

        if (errorCode != ErrorInfo.MOK.getValue()) {
            System.out.println("初始化引擎失败");
            throw new RuntimeException("初始化引擎失败");
        }
        this.faceEngine = faceEngine;
    }

    /**
     * 人脸识别
     * @param imageInfo
     * @return true人像  false 非人像
     */
    public Boolean checkIsPortrait(ImageInfo imageInfo){
//        ImageInfo imageInfo = getRGBData(new File("E:\\111\\t.jpg"));
        List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
        faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);

        return !faceInfoList.isEmpty();
    }


    /**
     * 通过文件识别
     * @param file
     * @return
     */
    public Boolean checkIsPortrait(File file){
        ImageInfo imageInfo = ImageFactory.getRGBData(file);
        return checkIsPortrait(imageInfo);
    }

    /**
     * 使用字节数组判断
     * @param bytes
     * @return
     */
    public Boolean checkIsPortrait(byte[] bytes){
        ImageInfo imageInfo = ImageFactory.getRGBData(bytes);
        return checkIsPortrait(imageInfo);
    }
}
