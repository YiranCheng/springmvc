package com.gupaoedu.vip.pattern.delegate.mvcframework.v2;

import com.gupaoedu.vip.pattern.delegate.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yiran
 */
public class DispatcherServlet extends HttpServlet {
    private static final String LOCATION = "contextConfigLocation";

    private Properties p = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String,Object> ioc = new HashMap<>();

    private List<Handler> handlerMapping = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter(LOCATION));
        
        //2.扫描所有相关的类
        doScanner(p.getProperty("scanPackage"));
        
        //3.初始化所有相关的类的实例，并保存到IOC容器中
        try {
            doInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //4.依赖注入
        doAutowired();
        
        //5.构造HandlerMapping
        initHandlerMapping();
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String,Object> entry:ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)){
                continue;
            }
            String path = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping mapping = clazz.getAnnotation(MyRequestMapping.class);
                path = mapping.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping mapping = method.getAnnotation(MyRequestMapping.class);
                String  regex = ("/" + path + "/" + mapping.value()).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMapping.add(new Handler(entry.getValue(),method,pattern));
            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String,Object> entry : ioc.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MyAutowired.class)){
                    continue;
                }
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() throws Exception {
        if (classNames.isEmpty()) {
            return;
        }

        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(MyController.class)){
                String name = toLowerFirstCase(clazz.getSimpleName());
                ioc.put(name,clazz.newInstance());
            }else if (clazz.isAnnotationPresent(MyService.class)) {
                MyService myService = clazz.getAnnotation(MyService.class);
                String value = myService.value();
                if (!"".equals(value)){
                    ioc.put(value,clazz.newInstance());
                    continue;
                }
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> i:interfaces) {
                    ioc.put(i.getName(),clazz.newInstance());
                }
            }else {
                continue;
            }
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.","/"));
        File dir = new File(url.getFile());
        for (File file: dir.listFiles()) {
            if (file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {
                classNames.add(scanPackage + "." + file.getName().replace(".class",""));
            }
        }
    }

    private void doLoadConfig(String location) {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(location);
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        doPost(request,response);
    }

    @Override
    public void  doPost(HttpServletRequest request, HttpServletResponse response){
        doDispatcher(request,response);
    }

    private void doDispatcher(HttpServletRequest request, HttpServletResponse response) {
        Handler handler = getHandler(request);
        if (handler == null) {
            try {
                response.getWriter().write("404 NOT FOUND");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Class<?>[] paramTypes = handler.method.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];

        Map<String,String[]> params = request.getParameterMap();
        for (Map.Entry<String,String[]> param:params.entrySet()){
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","");
            if (handler.paramIndex.containsKey(param.getKey())){
                int index = handler.paramIndex.get(param.getKey());
                paramValues[index] = convert(paramTypes[index],value);
            }
        }

        if (handler.paramIndex.containsKey(HttpServletRequest.class.getName())){
            int reqIndex = handler.paramIndex.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = request;
        }
        if (handler.paramIndex.containsKey(HttpServletResponse.class.getName())){
            int respIndex = handler.paramIndex.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = response;
        }

        try {
            Object result = handler.method.invoke(handler.controller,paramValues);
            if (result != null) {
                response.getWriter().write(result.toString());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object convert(Class clazz, String value) {
        if (clazz == Integer.class){
            return Integer.valueOf(value);
        }
        return value;
    }

    private Handler getHandler(HttpServletRequest request) {
        if (handlerMapping == null) {
            return null;
        }
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        for (Handler handler : handlerMapping) {
            Matcher matcher = handler.pattern.matcher(url);
            if (matcher.matches()){
                return handler;
            }
        }
        return null;
    }

    private class Handler{
        private Object controller;
        private Method method;
        protected Pattern pattern;
        private Map<String,Integer> paramIndex;

        private Handler(Object controller,Method method,Pattern pattern) {
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;

            paramIndex = new HashMap<>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method) {
            Annotation[][] pa = method.getParameterAnnotations();
            for (int i=0;i<pa.length;i++) {
                for (Annotation a:pa[i]){
                    if (a instanceof MyRequestParam) {
                        String value = ((MyRequestParam) a).value();
                        if (!"".equals(value)){
                            paramIndex.put(value,i);
                        }
                    }
                }
            }
            Class<?>[] paramsType = method.getParameterTypes();
            for (int i=0;i<paramsType.length;i++) {
                Class<?> type = paramsType[i];
                if (type == HttpServletRequest.class ||
                        type == HttpServletResponse.class){
                    paramIndex.put(type.getName(),i);
                }
            }
        }
    }
}
