package com.tanhua.sso.service;

import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

/**
 * @author: tang
 * @date: Create in 21:10 2021/8/3
 * @description:
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class FaceEngineServiceTest {

    @Autowired
    private FaceEngineService faceEngineService;

    @Test
    public void checkIsPortrait(){
        ImageInfo imageInfo = ImageFactory.getRGBData(new File("E:\\111\\1.jpg"));
        Boolean flag = faceEngineService.checkIsPortrait(imageInfo);
        System.out.println(flag);
    }
}
