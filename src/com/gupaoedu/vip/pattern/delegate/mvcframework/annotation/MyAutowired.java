package com.gupaoedu.vip.pattern.delegate.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author yiran
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {
    String value() default "";
}
