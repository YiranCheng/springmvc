package com.gupaoedu.vip.pattern.delegate.mvcframework.v1;

import com.gupaoedu.vip.pattern.delegate.controller.HelloController;
import com.gupaoedu.vip.pattern.delegate.controller.UserController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yiran
 */
public class DispatcherServlet extends HttpServlet{

    List<Handler> handlerMapping = new ArrayList<>();

    @Override
    public void init(){
        Class userController = UserController.class;
        Class helloController = HelloController.class;
        try {
            handlerMapping.add(new Handler(userController.newInstance(),userController.getMethod("findById",new Class[]{String.class}),"/user/findById"));
            handlerMapping.add(new Handler(helloController.newInstance(),helloController.getMethod("hello",new Class[]{String.class}),"/hello/hello"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatcher(req,resp);
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
        String url = req.getRequestURI();
        Handler handle = null;
        for (Handler handler : handlerMapping) {
            if (handler.getUrl().equals(url)) {
                handle = handler;
                break;
            }
        }

        Object obj = null;
        try {
            obj = handle.getMethod().invoke(handle.getController(),req.getParameter("id"));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        try {
            resp.getWriter().write(obj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Handler{
        Object controller;
        Method method;
        String url;

        public Handler(Object controller, Method method, String url) {
            this.controller = controller;
            this.method = method;
            this.url = url;
        }

        public Object getController() {
            return controller;
        }

        public void setController(Object controller) {
            this.controller = controller;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
