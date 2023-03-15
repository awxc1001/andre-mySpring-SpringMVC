package com.andre.andrespringmvc.annotation;

import java.lang.annotation.*;

/**
 * @author Andre Wang
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}
