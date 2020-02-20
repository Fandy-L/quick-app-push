package com.chuang.qapp.service.impl;

import com.chuang.qapp.compatible.DefaultStatus;
import com.chuang.qapp.service.AsyncTaskService;
import com.chuang.qapp.utils.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 * @author fandy.lin
 */
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {
    private ExecutorService executorService;

    @Value("${thread.pool.core.size:20}")
    private Integer corePoolSize;

    @Value("${thread.pool.max.size:100}")
    private Integer maximumPoolSize;

    @Value("${thread.pool.keepalive.time:3000}")
    private Long keepAliveTime;

    @Value("${thread.pool.queue.size:500}")
    private Integer queueSize;

    @PostConstruct
    private void init() {
        executorService = new ThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        Preconditions.checkNotNull(runnable, DefaultStatus.INVALID_ARGUMENTS);
        return executorService.submit(runnable);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}
