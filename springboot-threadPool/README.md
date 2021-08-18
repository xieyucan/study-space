# 工程简介
```javascript
本文带你快速了解@Async注解的用法，包括异步方法无返回值、有返回值，最后总结了@Async注解失效的几个坑。
```
在 SpringBoot 应用中，经常会遇到在一个业务接口中依赖其他多个服务接口。如果同步执行的话，则本次接口时间取决于依赖服务接口花费的时间之和；
如果同步执行依赖服务接口，则本次接口时最长的那个依赖服务接口时间，合理使用多线程，可以大大缩短接口时间。场景如下：

## 快速开始
启动配置，添加@EnableAsync注解来开启异步调用，添加@EnableScheduling注解来开启定时任务。
```java
package com.xh.springbootthreadpool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SpringbootThreadPoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootThreadPoolApplication.class, args);
    }

}

```

线程池配置，核心线程数、最大线程数建议根据实际的业务场景和系统资源配置去设置，并在设置后进行实测来确认最优配置。
```java
package com.xh.springbootthreadpool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author xiehui1956@gmail.com on 2021/8/16 10:45 上午
 * @version 1.0.0
 */
@Configuration
public class AsyncConfig {

    /**
     * 创建线程池Bean
     *
     * @return
     */
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(8);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列数
        executor.setQueueCapacity(20);
        // 空闲等待时间
        executor.setKeepAliveSeconds(10);
        // 线程池名前缀
        executor.setThreadNamePrefix("async-thread-");
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }
}

```

无返回异步任务
有返回结果的方法请求使用CompletableFuture，CompletableFuture是对Feature的增强，Feature只能处理简单的异步任务，而CompletableFuture
不仅可以将多个异步任务进行复杂组合，而且可以设置任务超时时间。
```java
package com.xh.springbootthreadpool.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 异步服务
 *
 * @author xiehui1956@gmail.com on 2021/8/16 10:52 上午
 * @version 1.0.0
 */
@Slf4j
@Service
public class AsyncService {

    /**
     * 异步业务逻辑处理
     *
     * @param msg 参数
     */
    @Async("asyncExecutor")
    public void requestMes(String msg) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info(msg);
    }

}

```

带返回值任务
```java
package com.xh.springbootthreadpool.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 带返回值的异步服务
 * 模拟一个业务依赖三个服务场景
 *
 * @author xiehui1956@gmail.com on 2021/8/16 11:10 上午
 * @version 1.0.0
 */
@Slf4j
@Service
public class AsyncReturnService {

    /**
     * 依赖服务1
     *
     * @param requestParam1 请求参数
     * @return 返回随机数
     */
    @Async("asyncExecutor")
    public CompletableFuture<Integer> requestUrl_1(String requestParam1) {
        int anInt = ThreadLocalRandom.current().nextInt(10);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("request param-1 = {}, return value = {}", requestParam1, anInt);
        return CompletableFuture.completedFuture(anInt);
    }

    /**
     * 依赖服务2
     *
     * @param requestParam1 请求参数
     * @return 返回随机数
     */
    @Async("asyncExecutor")
    public CompletableFuture<Integer> requestUrl_2(String requestParam1) {
        int anInt = ThreadLocalRandom.current().nextInt(10);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("request param-2 = {}, return value = {}", requestParam1, anInt);
        return CompletableFuture.completedFuture(anInt);
    }

    /**
     * 依赖服务2
     *
     * @param requestParam1 请求参数
     * @return 返回随机数
     */
    @Async("asyncExecutor")
    public CompletableFuture<Integer> requestUrl_3(String requestParam1) {
        int anInt = ThreadLocalRandom.current().nextInt(10);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("request param-3 = {}, return value = {}", requestParam1, anInt);
        return CompletableFuture.completedFuture(anInt);
    }

}

```

任务执行入口
```java
package com.xh.springbootthreadpool.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * 定时任务
 *
 * @author xiehui1956@gmail.com on 2021/8/16 10:57 上午
 * @version 1.0.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class TaskService {

    private AsyncService asyncService;

    private AsyncReturnService asyncReturnService;

    /**
     * 无返回异步任务
     */
    @Scheduled(initialDelay = 10, fixedDelay = 1000)
    protected void requestData() {
        asyncService.requestMes("requestData-index = " + ThreadLocalRandom.current().nextInt(100));
    }

    /**
     * 带返回异步任务
     */
    @Scheduled(initialDelay = 10, fixedDelay = 1000)
    protected void requestReturnData() {
        long start = System.currentTimeMillis();

        CompletableFuture<Integer> requestUrl_1 = asyncReturnService.requestUrl_1("index = " + 1);
        CompletableFuture<Integer> requestUrl_2 = asyncReturnService.requestUrl_2("index = " + 2);
        CompletableFuture<Integer> requestUrl_3 = asyncReturnService.requestUrl_3("index = " + 3);

        CompletableFuture.allOf(requestUrl_1, requestUrl_2, requestUrl_3).join();
        try {
            int result = requestUrl_1.get(5, TimeUnit.SECONDS).intValue()
                    + requestUrl_2.get(5, TimeUnit.SECONDS).intValue()
                    + requestUrl_3.get(5, TimeUnit.SECONDS).intValue();
            log.info("result = {}", result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        log.warn("requestReturnData-time = {}", ((System.currentTimeMillis() - start) / 1000));
    }

}

```

执行结果
```java
2021-08-16 12:35:58.610  INFO 97182 --- [           main] c.x.s.SpringbootThreadPoolApplication    : Started SpringbootThreadPoolApplication in 0.749 seconds (JVM running for 1.336)
2021-08-16 12:36:01.631  INFO 97182 --- [ async-thread-3] c.x.s.service.AsyncReturnService         : request param-3 = index = 3, return value = 9
2021-08-16 12:36:01.631  INFO 97182 --- [ async-thread-2] c.x.s.service.AsyncReturnService         : request param-2 = index = 2, return value = 8
2021-08-16 12:36:01.631  INFO 97182 --- [ async-thread-1] c.x.s.service.AsyncReturnService         : request param-1 = index = 1, return value = 1
2021-08-16 12:36:01.633  INFO 97182 --- [   scheduling-1] c.x.s.service.TaskService                : result = 18
2021-08-16 12:36:01.634  WARN 97182 --- [   scheduling-1] c.x.s.service.TaskService                : requestReturnData-time = 3
2021-08-16 12:36:02.639  INFO 97182 --- [ async-thread-4] c.x.s.service.AsyncService               : requestData-index = 45
2021-08-16 12:36:05.640  INFO 97182 --- [ async-thread-5] c.x.s.service.AsyncReturnService         : request param-1 = index = 1, return value = 5
2021-08-16 12:36:05.644  INFO 97182 --- [ async-thread-6] c.x.s.service.AsyncReturnService         : request param-2 = index = 2, return value = 9
2021-08-16 12:36:05.644  INFO 97182 --- [ async-thread-7] c.x.s.service.AsyncReturnService         : request param-3 = index = 3, return value = 5
2021-08-16 12:36:05.645  INFO 97182 --- [   scheduling-1] c.x.s.service.TaskService                : result = 19
2021-08-16 12:36:05.645  WARN 97182 --- [   scheduling-1] c.x.s.service.TaskService                : requestReturnData-time = 3
2021-08-16 12:36:06.648  INFO 97182 --- [ async-thread-8] c.x.s.service.AsyncService               : requestData-index = 30
2021-08-16 12:36:09.648  INFO 97182 --- [ async-thread-3] c.x.s.service.AsyncReturnService         : request param-3 = index = 3, return value = 3
2021-08-16 12:36:09.648  INFO 97182 --- [ async-thread-2] c.x.s.service.AsyncReturnService         : request param-2 = index = 2, return value = 5
2021-08-16 12:36:09.648  INFO 97182 --- [ async-thread-1] c.x.s.service.AsyncReturnService         : request param-1 = index = 1, return value = 2
2021-08-16 12:36:09.649  INFO 97182 --- [   scheduling-1] c.x.s.service.TaskService                : result = 10
2021-08-16 12:36:09.649  WARN 97182 --- [   scheduling-1] c.x.s.service.TaskService                : requestReturnData-time = 3
2021-08-16 12:36:10.651  INFO 97182 --- [ async-thread-4] c.x.s.service.AsyncService               : requestData-index = 10
```

@Async注解失效场景：
```javascript
1. SpringBoot应用没有开启Async功能(没有添加@EnableAsync);
2. 异步方法使用static关键词修饰；
3. 异步类没有交给spring容器管理。
```

PS: Spring多数功能是基于AOP实现的(类似上面的异步、事务等), 如果不通过代理直接调用会使功能失效。