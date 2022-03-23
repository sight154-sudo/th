package com.tanhua.dubbo.server.exception;

import cn.hutool.http.Method;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author: tang
 * @date: Create in 9:07 2021/8/13
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoAuthorizationException extends RuntimeException{
    private String url;
    private String body;
    private Method method;
}
