package springbootthreadpool.config;

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
