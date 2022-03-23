package com.tanhua.dubbo.server;

import cn.hutool.core.util.RandomUtil;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * @author: tang
 * @date: Create in 23:12 2021/8/12
 * @description:
 */
@Service
public class RetryService {

    /**
     * value 抛出指定异常重试  maxAttempts 最大重试次数
     * backoff: 重试等待策略  delay： 等待时间  multiplier: 指定延时倍数
     * @param max
     * @return
     */
    @Retryable(value = RuntimeException.class,backoff = @Backoff(delay = 2000l,multiplier = 2),maxAttempts = 3)
    public int execure(int max){
        Integer data = RandomUtil.randomInt(100);
        System.out.println("生成： "+data);
        if(data < max){
            throw new RuntimeException();
        }
        return data;
    }
    /**
     *
     */
    @Recover //全部重试失败后执行
    public int recover(Exception e) {
        System.out.println("全部重试完成。。。。。");
        return 88; //返回默认
    }
}
