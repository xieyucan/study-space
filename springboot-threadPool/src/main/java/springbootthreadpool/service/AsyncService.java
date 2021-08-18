package springbootthreadpool.service;

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
