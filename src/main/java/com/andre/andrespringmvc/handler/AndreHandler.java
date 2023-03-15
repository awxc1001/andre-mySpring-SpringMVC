package com.andre.andrespringmvc.handler;

import java.lang.reflect.Method;

/**
 * @author 韩顺平
 * @version 1.0
 * HspHandler 对象记录请求的 url 和 控制器方法映射关系
 */
public class AndreHandler {
    private String url;
    private Object controller;
    private Method method;

    public AndreHandler(String url, Object controller, Method method) {
        this.url = url;
        this.controller = controller;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
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

    @Override
    public String toString() {
        return "AndreHandler{" +
                "url='" + url + '\'' +
                ", controller=" + controller +
                ", method=" + method +
                '}';
    }
}
