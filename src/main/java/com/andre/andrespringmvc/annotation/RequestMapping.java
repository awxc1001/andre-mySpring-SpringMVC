package com.andre.andrespringmvc.annotation;

import java.lang.annotation.*;

/**
 * @author Andre Wang
 * @version 1.0
 */
@Target({ElementType.TYPE,ElementType.METHOD}) //可以修饰类型加上方法，包括自己定义的类型
@Retention(RetentionPolicy.RUNTIME) //运行java程序时保留注解，时可以被反射
@Documented
public @interface RequestMapping {
    String value()  default "";
}
