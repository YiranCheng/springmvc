package com.gupaoedu.vip.pattern.delegate.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author yiran
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    String value() default "";
}
