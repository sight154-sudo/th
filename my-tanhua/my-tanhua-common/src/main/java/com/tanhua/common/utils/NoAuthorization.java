package com.tanhua.common.utils;

import java.lang.annotation.*;

/**
 * @author: tang
 * @date: Create in 21:24 2021/8/7
 * @description:
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface NoAuthorization {
}
