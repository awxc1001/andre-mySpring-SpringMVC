package com.andre.andrespringmvc.context;

import com.andre.andrespringmvc.annotation.AutoWired;
import com.andre.andrespringmvc.annotation.Controller;
import com.andre.andrespringmvc.annotation.Service;
import com.andre.andrespringmvc.handler.AndreHandler;
import com.andre.xml.AndreXMLParser;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Andre Wang
 * @version 1.0
 */
public class AndreWebApplicationContext {

    private List<String> classFullPathList = new ArrayList<>();


    //for simplicity, we just assume ioc is beanMap with singleton objects
    //public,allow easy visit for DispatcherServlet
    public ConcurrentHashMap<String, Object> ioc = new ConcurrentHashMap<>();

    public AndreWebApplicationContext(){

    }
    private String configLocation;
    //andrespringmvc.xml， any spring config file for constructor if needed
    public AndreWebApplicationContext(String configLocation){
        this.configLocation = configLocation;
    }

    //编写方法,完成自己的spring容器的初始化
    public void init() {

//        String basePackage = AndreXMLParser.getComponentScanPackage("andrespringmvc.xml");

//        <init-param>
//      <param-name>contextConfigLocation</param-name>
//      <param-value>classpath:andrespringmvc.xml</param-value>
//    </init-param> 获取的要进行分割
        String basePackage = AndreXMLParser.getComponentScanPackage(this.configLocation.split(":")[1]);

        //处理多个要扫描的包
        String[] packages = basePackage.split(",");
        if (packages.length > 0) {
            for (String basePack : packages) {
                scanBasePackage(basePack);
            }
        }
        System.out.println("classFullPathList: " + classFullPathList);
        //将有spring注解的注入ioc中
        executeInstance();
        System.out.println("After Scan, Singletons: "+ioc);

         //完成注入的bean对象,的属性的装配
        executeAutoWired();
        System.out.println("After Autowired, ioc = " + ioc);


    }

    /**
     * @param scanPackage 表示要扫描的包，比如"com.andre.controller"
     */
    public void scanBasePackage(String scanPackage) {


        //下面打印出来的一样，this可能就是指定不同对象
        //        String name = this.getClass().getName();
        //        String name1 = AndreWebApplicationContext.class.getName();
        //        System.out.println(name);
        //        System.out.println(name1);

        //获取类加载器在target的路径
        //得到包所在的工作路径[绝对路径]
        //下面这句话的含义是 通过类的加载器，得到指定的包对应的 工作路径[绝对路径]
        //比如 "com.hspedu.controller" => url 是 D:\JAVAWEB\springmvc_code\andre-myspringmvc\target\andre-myspringmvc\WEB-INF\classes\com\andre\controller
        //如果不用替换符号，debug下来，url会是空的
        //细节说明： 1. 不要直接使用Junit测试, 否则 url null
        //             2. 启动tomcat来吃测试
        URL scanURL = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        System.out.println("url for scan is: " + scanURL);

        //io文件处理,目录也是file的一种，接下来就是遍历要扫描目录的所有文件了
        File scanPackDir = new File(scanURL.getFile());

        //遍历dir[文件/子目录]
        for (File file : scanPackDir.listFiles()) {
            if (file.isDirectory()) { //如果是一个目录，需要递归扫描
                scanBasePackage(scanPackage + "." + file.getName());
            } else {
                //说明:这时，在target扫描到的文件，可能是.class, 也可能是其它文件
                //就算是.class, 也存在是不是需要注入到容器
                //目前先把文件的全路径都保存到集合，不要管后缀，后面在注入对象到容器时，再处理
                String classFullPath =
                        scanPackage + "." + file.getName().replaceAll(".class", "");
                classFullPathList.add(classFullPath);
            }
        }
    }

    //编写方法,将扫描到的类, 在满足条件的情况下，反射到ioc容器
    public void executeInstance() {
        //判断是否扫描到类
        if (classFullPathList.size() == 0) {//说明没有扫描到类
            return;
        }

        //遍历classFullPathList,进行反射
        for (String classFullPath : classFullPathList) {

            try { //先获取每个class对象然后做各个注解的判断
                Class<?> clazz = Class.forName(classFullPath);
                 //说明当前这个类有@Controller
                if (clazz.isAnnotationPresent(Controller.class)) {

                    //得到类名首字母小写
                    String beanName = StringUtils.uncapitalize(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                }
                // for other annotations, elaborate
                else if(clazz.isAnnotationPresent(Service.class)){
                    Service serviceAnnotation = clazz.getAnnotation(Service.class);
                    String serviceBeanName = serviceAnnotation.value();
                    //反射创建对象
                    Object beanInstance = clazz.newInstance();

                    //如果没有就首字母小写,但必须是对应接口的名字，不是impl的名字
                    if("".equals(serviceBeanName)){
                        Class<?>[] interfaces = clazz.getInterfaces();
                        for (Class<?> anInterface : interfaces) {
                            serviceBeanName = StringUtils.uncapitalize(anInterface.getSimpleName());
                            //对象和beanName一起注入ioc
                            ioc.put(serviceBeanName, clazz.newInstance());
                        }
                        //3. 留一个作业,使用类名的首字母小写来注入bean
                        //   通过 clazz 来即可.
                    }else{//有value就直接注入
                        ioc.put( serviceBeanName , beanInstance );
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //编写方法，完成属性的自动装配
    public void executeAutoWired() {
         //判断ioc有没有要装配的对象
        if (ioc.isEmpty()) {
            throw new RuntimeException("ioc 容器没有bean对象");
        }
        //遍历ioc容器中的所有注入的bean对象, 然后获取到bean的所有字段/属性，判断是否需要
        //装配
        /**
         * entry => <String,Object > String 就是注入对象时名称 Object就是bean对象
         */
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            //String key = entry.getKey();
            Object bean = entry.getValue();

            //遍历每个bean的字段属性，看看哪些属性需要自动配装
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                //判断当前这个字段，是否有@AutoWired
                if(declaredField.isAnnotationPresent(AutoWired.class)){
                    AutoWired autoWiredAnnotation = declaredField.getAnnotation(AutoWired.class);
                    String autoWiredBeanName = autoWiredAnnotation.value();

                    if(autoWiredBeanName.equals("")){//如果没有设置value,按照默认规则
                        //即得到字段类型的名称的首字母小写，作为名字来进行装配
                        Class<?> type = declaredField.getType();
                        autoWiredBeanName =
                                StringUtils.uncapitalize(type.getSimpleName());
                    }
                     //如果设置value, 直接按照beanName来进行装配
                    //从ioc容器中获取到bean
                    System.out.println(autoWiredBeanName);
                    if (null == ioc.get(autoWiredBeanName)) {//说明指定的名字对应的bean不在ioc容器
                        throw new RuntimeException("ioc容器中, 不存在要装配的bean");
                    }
                    //防止属性是private, 需要暴力破解
                    declaredField.setAccessible(true);
                    //反射配置生成field字段属性
                    try {
                        declaredField.set(bean, ioc.get(autoWiredBeanName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }


    //如果有自定义名字，beanName要做修改，太复杂了，还得看看处理各个annotation代表的接口或者类
    public void getBeanName(Class<?> clazz) {

    }
}
