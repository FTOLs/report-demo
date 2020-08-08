package com.hb.report.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * 执行时间统计切面
 * CostTimeAspect
 * @author: huangbing
 * @date: 2020/8/8 10:31 上午
 */
@Aspect
@Component
public class CostTimeAspect {

    public static final Logger logger = LoggerFactory.getLogger(CostTimeAspect.class);

    @Pointcut("@annotation(com.hb.report.demo.annotation.CostTime)")
    public void costTimeLog(){}

    /**
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("costTimeLog()")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("====== 开始方法 {}.{} 时间统计 ======",
                joinPoint.getTarget().getClass(),
                joinPoint.getSignature().getName());

        Object res = null;

        long begTime = System.currentTimeMillis();
        try {
            res =  joinPoint.proceed();
            begTime = System.currentTimeMillis() - begTime;
            return res;
        } finally {
            try {
                //方法执行完成后增加日志
                logger.info("====== 结束方法 {}.{} 时间统计 总共执行时间：{}======",
                        joinPoint.getTarget().getClass(),
                        joinPoint.getSignature().getName(), begTime);
            }catch (Exception e){
                System.out.println("CostTimeLogAspect 操作失败：" + e.getMessage());
                e.printStackTrace();
            }
        }

    }
}
