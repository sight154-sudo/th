package com.tanhua.common.utils;

import com.tanhua.common.pojo.User;

/**
 * @author: tang
 * @date: Create in 21:25 2021/8/7
 * @description:
 */
public class UserThreadLocal {

    public static final ThreadLocal<User> tl = new ThreadLocal<>();


    public static User getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
    public static void setUser(User user){
        tl.set(user);
    }
}
