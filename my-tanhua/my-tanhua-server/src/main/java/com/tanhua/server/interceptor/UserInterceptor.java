package com.tanhua.server.interceptor;

import cn.hutool.core.util.ObjectUtil;
import com.tanhua.common.pojo.User;
import com.tanhua.common.utils.NoAuthorization;
import com.tanhua.common.utils.UserThreadLocal;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: tang
 * @date: Create in 21:28 2021/8/7
 * @description:
 */
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        //若方法上有此注解，则不拦截
        if(((HandlerMethod) handler).hasMethodAnnotation(NoAuthorization.class)){
            return true;
        }
        //获取请求头上的token信息
        User user = userService.queryUserByToken(request.getHeader("Authorization"));
        if(ObjectUtil.isNotEmpty(user)){
            UserThreadLocal.setUser(user);
            return true;
        }
        //返回401状态码 用户未认证
        response.setStatus(401);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.removeUser();
    }
}
