package com.andre.andrespringmvc.servlet;

import com.andre.andrespringmvc.annotation.Controller;
import com.andre.andrespringmvc.annotation.RequestMapping;
import com.andre.andrespringmvc.annotation.RequestParam;
import com.andre.andrespringmvc.annotation.ResponseBody;
import com.andre.andrespringmvc.context.AndreWebApplicationContext;
import com.andre.andrespringmvc.handler.AndreHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andre Wang
 * @version 1.0
 */

/**
 * 前端中央控制器
 * 1. HspDispatcherServlet充当原生DispatcherServlet
 * 2. 本质是一个Servlet, 继承HttpServlet
 * 3. 提示: 这里需要使用到java web 讲解的Servlet
 */
public class AndreDispatcherServlet extends HttpServlet {

    //定义属性 handlerList , 保存Handler[url和控制器方法的映射]
    private List<AndreHandler> handlerList = new ArrayList<>();

    //定义属性 ApplicationContext,自己的spring容器
    private AndreWebApplicationContext andreWebApplicationContext = null;


    //dispatcher是个servlet，可以在init时候生成容器初始化
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        //动态获取到web.xml中的 contextConfigLocation，这样各种spring配置文件都能构造器生成容器
//    <init-param>
//      <param-name>contextConfigLocation</param-name>
//      <param-value>classpath:andrespringmvc.xml</param-value>
//    </init-param>
        String contextConfigLocation = servletConfig.getInitParameter("contextConfigLocation");


        //自己的spring容器
        andreWebApplicationContext = new AndreWebApplicationContext(contextConfigLocation);

        andreWebApplicationContext.init();

        //调用 initHandlerMapping ， 完成url和控制器方法的映射
        initHandlerMapping();
        //输出handlerList
        System.out.println("handlerList init result = " + handlerList);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        System.out.println("AndreDispatcherServlet doPost()");
        //调用方法，完成分发请求
        executeDispatch(req, resp);
    }

    //编写方法，完成url 和 控制器方法的映射
    private void initHandlerMapping() {
        if (andreWebApplicationContext.ioc.isEmpty()) {
            //判断当前的ioc容器是否为null
            return;
        }

        //遍历ioc容器的bean对象,然后进行url映射处理
        //java基础 map的遍历
        for (Map.Entry<String, Object> entry : andreWebApplicationContext.ioc.entrySet()) {
            //先取出注入的Object的clazz对象
            Class<?> clazz = entry.getValue().getClass();
            if (clazz.isAnnotationPresent(Controller.class)) {
                //取出它的所有方法
                Method[] declaredMethods = clazz.getDeclaredMethods();
                //遍历方法
                for (Method declaredMethod : declaredMethods) {
                    //判断该方法是否有@RequestMapping
                    if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                        //取出@RequestMapping值->就是映射路径
                        RequestMapping requestMappingAnnotation =
                                declaredMethod.getAnnotation(RequestMapping.class);
                        //这里小伙伴可以把工程路径+url
                        //getServletContext().getContextPath()
                        // /springmvc/monster/list
                        String url = requestMappingAnnotation.value();
                        //创建Handler对象->就是一个映射关系
                        AndreHandler andreHandler = new AndreHandler(url, entry.getValue(), declaredMethod);
                        //放入到handlerList
                        handlerList.add(andreHandler);
                    }
                }
            }
        }

    }

    //编写方法，通过request对象，返回HspHandler对象
    //如果没有，就返回null
    private AndreHandler getAndreHandler(HttpServletRequest request) {
        //1.先获取的用户请求的uri 比如http://localhost:8080/springmvc/monster/list
        //  uri = /springmvc/monster/list
        //2. 这里小伙伴要注意得到uri 和 保存url 是有一个工程路径的问题
        // 两个方案解决 =>第一个方案: 简单 tomcat 直接配置 application context =>/
        // 第二个方案 保存 hsphandler对象 url 拼接 getServletContext().getContextPath()
        String requestURI = request.getRequestURI();
        System.out.println();
        //遍历handlerList
        for (AndreHandler andreHandler : handlerList) {
            if (requestURI.equals(andreHandler.getUrl())) {//说明匹配成功
                return andreHandler;
            }
        }
        return null;
    }

    //编写方法，完成分发请求任务
    private void executeDispatch(HttpServletRequest request,
                                 HttpServletResponse response) {

        AndreHandler andreHandler = getAndreHandler(request);


        try {
            if (andreHandler == null) {
                response.getWriter().print("<h1>404 NOT FOUND 说明用户请求的路径/资源不存在</h1>");
            } else {//匹配成功, 反射调用控制器的方法
                //目标将: HttpServletRequest 和 HttpServletResponse封装到参数数组
                //1. 得到目标方法的所有形参参数信息[对应的数组]
                Class<?>[] parameterTypes = andreHandler.getMethod().getParameterTypes();

                //2. 创建一个对应长度参数数组[对应实参数组], 在后面反射调用目标方法时，会使用到
                Object[] paramList = new Object[parameterTypes.length];
                System.out.println("paramList is: "+ paramList);

                //3遍历parameterTypes形参数组,根据形参数组信息，将实参填充到上面实参数组容器里
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    //如果这个形参是HttpServletRequest, 将request填充到params
                    //因为不清楚各个形参位置，按length位置遍历后将request和response放进去
                    //在原生SpringMVC中,是按照类型来进行匹配，这里简化使用名字来进行匹配
                    if ("HttpServletRequest".equals(parameterType.getSimpleName())) {
                        paramList[i] = request;
                    } else if ("HttpServletResponse".equals(parameterType.getSimpleName())) {
                        paramList[i] = response;
                    }
                }

                //将http请求参数封装到params数组中, 提示，要注意填充实参的时候，顺序问题

                //1. 获取http请求的参数集合
                //
                //http://localhost:8080/monster/find?name=牛魔王&hobby=打篮球&hobby=喝酒
                //2. 返回的Map<String,String[]> String:表示http请求的参数名比如name，可以对应@requestParam
                //   String[]:表示http请求的参数值。是数组的原因是因为前端有可能是多选框，导致多个参数值
                //处理提交的数据中文乱码request.setCharacterEncoding("utf-8");
                Map<String, String[]> parameterMap =
                        request.getParameterMap();

                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    //取出key，这name就是对应请求的参数名
                    String parameterName = entry.getKey();
                    //说明:这里只考虑提交的参数是单值的情况，即不考虑类似checkbox提示的数据
                    //    这里做了简化，如果小伙伴考虑多值情况，就要做更复杂的string处理
                    String value = entry.getValue()[0];
                    //得到请求的参数对应目标方法的第几个形参，然后将其填充
                    //这里专门编写一个方法，得到请求的参数对应的是第几个形参 //getRequestParameterIndex()
                    int requestParamIndex = getRequestParameterIndex(andreHandler.getMethod(), parameterName);
                    if(requestParamIndex !=-1){ //说明找到了
                        paramList[requestParamIndex] = value;
                    }else{ //没有@requestParam对应的url参数，用默认机制匹配
                         //思路
                        //1. 得到目标方法的所有形参的名称-专门编写方法获取形参名:getParamterNames
                        //2. 对得到目标方法的所有形参名进行遍历,如果匹配就把当前请求的参数值，填充到params
                        List<String> parameterNames = getParameterNames(andreHandler.getMethod());
                          for (int i = 0; i < parameterNames.size(); i++) {
                            //如果请求参数名和目标方法的形参名一样，说明匹配成功
                            if (parameterName.equals(parameterNames.get(i))) {
                                paramList[i] = value;//填充到实参数组
                                break;
                            }
                        }
                    }

                }
                //invoke method
                Object invokeResult =
                        andreHandler.getMethod().invoke(andreHandler.getController(), paramList);
                //视图解析返回对应对应前端
                executeFrondEnd(request,response,invokeResult,andreHandler.getMethod());
                /**
                 * 解读
                 * 1. 下面这样写法，其实是针对目标方法是 m(HttpServletRequest request , HttpServletResponse response)
                 * 2. 这里准备将需要传递给目标方法的 实参=>封装到参数数组=》然后以反射调用的方式传递给目标方法
                 * 3. public Object invoke(Object obj, Object... args)..
                 */
                //这么写太死了，如果有requestparam，直接没法反射了
                //andreHandler.getMethod().invoke(andreHandler.getController(),request,response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //通过执行方法返回String的话就按forward或者redirect进行前端显示
    public void executeFrondEnd(HttpServletRequest request,
                                HttpServletResponse response, Object invokeResult,
                                Method handlerMethod) throws Exception{
        //这里就是对返回的结果进行解析=>原生springmvc 可以通过视图解析器来完成
                //这里直接解析，只要把视图解析的核心机制讲清楚就OK
                if (invokeResult instanceof String) {
                    String viewName = (String) invokeResult;
                    if(viewName.contains(":")){//说明返回的String 结果forward:/login_ok.jsp 或者 redirect:/xxx/xx/xx.xx
                        String viewType = viewName.split(":")[0];//forward | redirect
                        String viewPage = viewName.split(":")[1];//是要跳转的页面名
                        //判断是forward 还是 redirect
                        if("forward".equals(viewType)) {//说明希望请求转发
                            request.getRequestDispatcher(viewPage)
                                    .forward(request,response);
                        } else if("redirect".equals(viewType)) {//说明希望重定向
                            response.sendRedirect(viewPage);
                        }
                    } else {//默认是请求转发
                        request.getRequestDispatcher(viewName)
                                .forward(request,response);
                    }
                }//如果是返回数组类型
                else if(invokeResult instanceof List){
                    if(handlerMethod.isAnnotationPresent(ResponseBody.class)){

                        //把result [ArrayList] 转成json格式数据-》返回
                        //这里需要使用到java中如何将 ArrayList 转成 json
                        //这里需要使用jackson包下的工具类可以轻松的搞定.
                        ObjectMapper objectMapper = new ObjectMapper();
                        String resultJson =
                                objectMapper.writeValueAsString(invokeResult);

                        response.setContentType("text/html;charset=utf-8");
                        //就直接返回
                        PrintWriter writer = response.getWriter();
                        writer.write(resultJson);
                        writer.flush();
                        writer.close();
                    }

                }
    }


    /**
     * @param method 目标方法
     * @param parameterName   请求的参数名
     * @return 是目标方法的第几个形参
     */
    //编写方法，返回请求参数是目标方法的第几个形参
    public int getRequestParameterIndex(Method method, String parameterName) {

        //1.得到传过来的method的所有形参参数
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParamAnnotation =
                        parameters[i].getAnnotation(RequestParam.class);

                //                后端 Handler 的目标方法
                //@RequestMapping(value = "/monster/find")
                //public void findMonstersByName(HttpServletRequest request,
                //HttpServletResponse response,
                //@RequestParam(value = "name") String name) {

                String requestParamValue = requestParamAnnotation.value();
                //这里就是匹配的比较
                if (parameterName.equals(requestParamValue)){
                    return i;//找到请求的参数，对应的目标方法的形参的位置
                }
            }
        }
        //如果没有匹配成功，就返回-1
        return -1;
    }



    /**
     * @param method 目标方法
     * @return 所有形参的名称, 并放入到集合中返回
     */
    //编写方法, 得到目标方法的所有形参的名称,并放入到集合中返回
    public List<String> getParameterNames(Method method) {

        List<String> parametersList = new ArrayList<>();
        //获取到所以的参数名->这里有一个小细节
        //在默认情况下 parameter.getName() 得到的名字不是形参真正名字
        //而是 [arg0, arg1, arg2...], 这里要引入一个插件，使用java8特性，这样才能解决
        Parameter[] parameters = method.getParameters();
        //遍历parameters 取出名称，放入parametersList
        for (Parameter parameter : parameters) {
            parametersList.add(parameter.getName());
        }
        System.out.println("paramList for the target method=" + parametersList);
        return parametersList;
    }


}
