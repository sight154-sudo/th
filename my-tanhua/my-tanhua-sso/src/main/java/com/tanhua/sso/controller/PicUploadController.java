package com.tanhua.sso.controller;

import com.tanhua.common.service.PicUploadService;
import com.tanhua.common.vo.PicUploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: tang
 * @date: Create in 22:55 2021/8/2
 * @description:
 */
@RestController
@RequestMapping("pic/upload")
public class PicUploadController {

    @Autowired
    private PicUploadService picUploadService;

    @PostMapping
    public PicUploadResult uploadPic(MultipartFile file){
        return picUploadService.uploadResult(file);
    }
}
