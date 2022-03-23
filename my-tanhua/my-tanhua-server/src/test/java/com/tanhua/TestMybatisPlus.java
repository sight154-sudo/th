package com.tanhua;

import com.tanhua.common.pojo.Settings;
import com.tanhua.server.service.SettingsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 17:16 2021/8/17
 * @description:
 */
@SpringBootTest(classes = ServerApplication.class)
@RunWith(SpringRunner.class)
public class TestMybatisPlus {

    @Autowired
    private SettingsService settingsService;


    @Test
    public void testMapper(){
        Settings settings = new Settings();
        settings.setId(1l);
        settings.setLikeNotification(true);
        settings.setPinglunNotification(false);
        settings.setGonggaoNotification(true);
        settingsService.updateUserSettings(settings);
    }
}
