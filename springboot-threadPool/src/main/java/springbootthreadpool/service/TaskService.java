package springbootthreadpool.service;

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
