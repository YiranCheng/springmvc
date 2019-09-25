package com.gupaoedu.vip.pattern.delegate.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author yiran
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    String value() default "";
}
