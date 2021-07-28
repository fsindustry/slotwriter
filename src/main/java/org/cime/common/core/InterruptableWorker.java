package org.cime.common.core;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>可中断的工作线程对象</h1>
 * <p>
 * 作为工作线程的基类,提供interrupt方法,用于中断工作线程操作;
 * </p>
 */
@Slf4j
public abstract class InterruptableWorker implements Worker {

    /**
     * 中断标识,初始为false
     */
    protected volatile boolean interrupt = false;

    /**
     * 工作线程参数对象,存放工作线程需要的参数;
     */
    protected WorkerParam param;

    /**
     * <h2>工作线程中断方法</h2>
     * <p>
     * 设置中断表示为true,表示当前线程为中断状态;
     * </p>
     */
    public void interrupt() {
        interrupt = true;
    }

    /**
     * <h2>注入当前工作线程参数</h2>
     *
     * @param param 工作线程参数
     */
    public void initial(WorkerParam param) {
        this.param = param;
    }

    @Override
    public void run() {

        log.info("work thread [" + Thread.currentThread().getName() + "] start, params: " + param);
        long startTime = System.currentTimeMillis();

        // 获取连接
        try {
            //等待所有线程都启动后,同时执行
            if (param.isNeedStartLatch()) {
                param.getStartLatch().await();
            }

            // 执行工作线程方法
            execute();

        } catch (Exception e) {
            log.error("[" + Thread.currentThread().getName() + "] execute error", e);
        } finally {
            if (param.isNeedStartLatch()) {
                param.getEndLatch().countDown();
            }
        }

        long totalCost = System.currentTimeMillis() - startTime;
        log.info("work thread [" + Thread.currentThread().getName() + "] stop,cost:" + totalCost + "ms ");
    }
}
