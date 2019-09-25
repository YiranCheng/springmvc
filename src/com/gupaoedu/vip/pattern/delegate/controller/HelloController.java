package com.gupaoedu.vip.pattern.delegate.controller;

import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.MyController;
import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.MyRequestMapping;
import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.MyRequestParam;

/**
 * @author yiran
 */
@MyController
@MyRequestMapping("hello")
public class HelloController {

    @MyRequestMapping("123")
    public String hello(@MyRequestParam("id") Integer id){
        return "你好么" + id;
    }
}
