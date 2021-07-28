package org.cime.common.core;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.cime.common.cli.bean.Args;

interface WorkerCtl {
    /**
     * <h2>控制整个工作线程的运行过程</h2>
     *
     * @param argsObj 请求参数对象
     *
     * @throws Exception
     */
    void run(Args argsObj) throws Exception;

    /**
     * <h2>准备工作</h2>
     *
     * @param argsObj 请求参数对象
     *
     * @throws Exception
     */
    void prepare(Args argsObj) throws Exception;

    /**
     * <h2>启动工作线程</h2>
     *
     * @param argsObj    请求参数对象
     * @param startLatch 启动闩
     * @param endLatch   停止闩
     *
     * @return 封装工作线程参数
     *
     * @throws Exception
     */
    List<WorkerParam> packWorkerParam(Args argsObj, CountDownLatch startLatch, CountDownLatch endLatch)
            throws Exception;

    /**
     * <h2>启动工作线程</h2>
     *
     * @param argsObj   请求参数对象
     * @param paramList 工作线程参数队列
     *
     * @return 启动后的工作线程队列
     *
     * @throws Exception
     */
    List<Worker> startWorker(Args argsObj, List<WorkerParam> paramList) throws
            Exception;

    /**
     * <h2>启动工作线程以后要执行的操作</h2>
     *
     * @param argsObj    请求参数对象
     * @param startLatch 启动闩
     * @param endLatch   停止闩
     *
     * @return 启动后的监视线程对象 | NULL(未指定监视线程)
     *
     * @throws Exception
     */
    Worker startMonitor(Args argsObj, CountDownLatch startLatch, CountDownLatch endLatch)
            throws Exception;

    /**
     * <h2>结束工作线程</h2>
     *
     * @param monitor 监视线程对象
     *
     * @throws Exception
     */
    void stopMonitor(Worker monitor) throws Exception;

    /**
     * <h2>清理操作</h2>
     *
     * @param argsObj 请求参数对象
     *
     * @throws Exception
     */
    void clean(Args argsObj, List<Worker> workerList, Worker monitor) throws Exception;
}
