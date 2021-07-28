package org.cime.common.core;

import java.util.List;

import org.cime.common.cli.bean.Args;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>程序关闭钩子线程</h1>
 * <p>
 * 负责在程序中断或者退出后，执行清理工作
 * </p>
 */
@Slf4j
public class ShutDownHook extends Thread {

    /**
     * 命令行参数对象
     */
    private Args args;

    /**
     * 工作线程控制器
     */
    private BaseWorkerCtl workerCtl;

    /**
     * 当前运行的工作线程列表
     */
    private List<Worker> workerList;

    /**
     * 监视线程对象
     */
    private Worker monitor;

    public ShutDownHook(BaseWorkerCtl workerCtl, List<Worker> workerList, Worker monitor, Args args) {
        this.args = args;
        this.workerCtl = workerCtl;
        this.workerList = workerList;
        this.monitor = monitor;
    }

    @Override
    public void run() {

        log.info("start clean ...");

        try {
            workerCtl.clean(args, workerList, monitor);
        } catch (Exception e) {
            log.error("clean error", e);
        }

        log.info("stop clean ...");
    }
}
