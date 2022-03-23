package com.tanhua;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.IMService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: tang
 * @date: Create in 19:38 2021/8/13
 * @description:
 */
@SpringBootTest(classes = ServerApplication.class)
@RunWith(SpringRunner.class)
public class IMServiceTest {

    @Autowired
    private IMService imService;

    @Test
    public void testUsers() {
        for (int i = 1; i <= 99; i++) {
            for (int j = 0; j < 10; j++) {
                User user = new User();
                user.setId(Convert.toLong(i));
                UserThreadLocal.setUser(user);
                this.imService.saveLinkedUser(this.getFriendId(user.getId()));
            }
        }
    }

    private Long getFriendId(Long userId) {
        Long friendId = RandomUtil.randomLong(1, 100);
        if (friendId.intValue() == userId.intValue()) {
            getFriendId(userId);
        }
        return friendId;
    }
}
