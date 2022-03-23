package com.tanhua.server.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.HuanXinService;
import com.tanhua.server.vo.HuanXinUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: tang
 * @date: Create in 11:41 2021/8/13
 * @description:
 */
@RestController
@RequestMapping("huanxin")
@Slf4j
public class HuanXinController {

    @Autowired
    private HuanXinService huanXinService;

    /**
     * 查询环信用户信息
     *
     * @return
     */
    @GetMapping("user")
    public ResponseEntity<HuanXinUserVo> queryHuanXinUser() {
        try {
            HuanXinUserVo huanXinUserVo = huanXinService.queryHuanXinUser();
            if (ObjectUtil.isNotEmpty(huanXinUserVo)) {
                return ResponseEntity.ok(huanXinUserVo);
            }
        } catch (Exception e) {
            log.error("获取环信用户信息失败~userId = " + UserThreadLocal.getUser(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

}
