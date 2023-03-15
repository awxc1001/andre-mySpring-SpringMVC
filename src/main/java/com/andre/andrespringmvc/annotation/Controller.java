package com.andre.andrespringmvc.annotation;

import java.lang.annotation.*;

/**
 * @author Andre Wang
 * @version 1.0
 */
@Target(ElementType.TYPE) //可以修饰类型，包括自己定义的类型
@Retention(RetentionPolicy.RUNTIME) //运行java程序时保留注解，时可以被反射
@Documented //保留生成在java文档
public @interface Controller {
    String value() default "";
}
