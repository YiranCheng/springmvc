package com.gupaoedu.vip.pattern.delegate.service;

import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.MyService;

/**
 * @author yiran
 */
@MyService
public class UserServiceImpl implements IUserService {
    @Override
    public String getNameById(String id) {
        return "yiran";
    }
}
