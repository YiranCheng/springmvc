package com.gupaoedu.vip.pattern.delegate.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author yiran
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {
    String value() default "";
}
