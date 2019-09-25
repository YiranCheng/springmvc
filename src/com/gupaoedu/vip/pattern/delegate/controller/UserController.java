package com.gupaoedu.vip.pattern.delegate.controller;

import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.MyAutowired;
import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.MyController;
import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.MyRequestMapping;
import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.MyRequestParam;
import com.gupaoedu.vip.pattern.delegate.service.IUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yiran
 */
@MyController
public class UserController {

    @MyAutowired
    private IUserService userService;

    @MyRequestMapping("findById")
    public void findById(HttpServletRequest request,
                           HttpServletResponse response,
                           @MyRequestParam("id") String id) throws IOException {
        response.getWriter().write(userService.getNameById(id));
    }
}
