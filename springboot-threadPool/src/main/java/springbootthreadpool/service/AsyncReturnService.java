package springbootthreadpool.service;

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
