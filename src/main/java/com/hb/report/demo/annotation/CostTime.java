package com.hb.report.demo.annotation;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
/**
 * 执行时间计算注解类
 * CostTime
 * @author: huangbing
 * @date: 2020/8/8 10:29 上午
 */
public @interface CostTime {

}
