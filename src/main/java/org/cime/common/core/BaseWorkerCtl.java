package org.cime.common.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.cime.common.cli.bean.Args;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>控制模板类,用于定义启动程序需要的执行步骤</h1>
 */
@Slf4j
public class BaseWorkerCtl implements WorkerCtl {

    /**
     * <h2>控制整个工作线程的运行过程</h2>
     *
     * @param argsObj 请求参数对象
     *
     * @throws Exception
     */
    @Override
    public void run(Args argsObj) throws Exception {

        //创建启动闩、结束闩
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(argsObj.getThreadCount());

        //准备工作
        prepare(argsObj);

        //打包工作线程参数
        List<WorkerParam> paramList = packWorkerParam(argsObj, startLatch, endLatch);

        //启动工作线程
        List<Worker> workerList = startWorker(argsObj, paramList);

        //启动监视线程
        Worker monitor = startMonitor(argsObj, startLatch, endLatch);

        //启动关闭钩子
        Runtime.getRuntime().addShutdownHook(new ShutDownHook(this, workerList, monitor, argsObj));

        //同时启动线程,并等待线程执行完成
        startLatch.countDown();
        endLatch.await();

        //停止监视器线程
        stopMonitor(monitor);

        //清理工作
        clean(argsObj, workerList, monitor);
    }

    /**
     * <h2>准备工作</h2>
     *
     * @param argsObj 请求参数对象
     *
     * @throws Exception
     */
    @Override
    public void prepare(Args argsObj) throws Exception {
        log.info("prepare() default empty...");
    }

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
    @Override
    public List<WorkerParam> packWorkerParam(Args argsObj, CountDownLatch startLatch, CountDownLatch endLatch)
            throws Exception {

        List<WorkerParam> paramList;
        try {
            //获取工作线程对应的打包器
            ParamPacker packer = WorkerEnum.getPacker(argsObj.getWorkType());
            assert packer != null;
            //打包参数并返回
            paramList = packer.pack(argsObj, startLatch, endLatch);
        } catch (Exception e) {
            String errMsg = "pack worker param error.";
            log.error(errMsg, e);
            throw new Exception(errMsg, e);
        }

        return paramList;
    }

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
    @Override
    public List<Worker> startWorker(Args argsObj, List<WorkerParam> paramList) throws
            Exception {
        // 初始化工作线程
        List<Worker> workerList = new ArrayList<>(argsObj.getThreadCount());
        try {
            for (WorkerParam workerParam : paramList) {
                Worker worker = WorkerEnum.getWoker(argsObj.getWorkType());
                assert worker != null;
                worker.initial(workerParam);
                workerList.add(worker);
                new Thread(worker).start();
            }
        } catch (Exception e) {
            String errMsg = "start work thread error.";
            log.error(errMsg, e);
            throw new Exception(errMsg, e);
        }

        return workerList;
    }

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
    @Override
    public Worker startMonitor(Args argsObj, CountDownLatch startLatch, CountDownLatch endLatch)
            throws Exception {
        // 如果检视线程不为空,则启动监视线程
        String monitorType = argsObj.getMonitorType();
        Worker monitor = null;
        if (null != monitorType && !"".equals(monitorType.trim())) {
            try {
                ParamPacker packer = WorkerEnum.getPacker(argsObj.getMonitorType());
                assert packer != null;
                WorkerParam param = packer.pack(argsObj, startLatch, endLatch).get(0);
                monitor = WorkerEnum.getWoker(argsObj.getMonitorType());
                assert monitor != null;
                monitor.initial(param);
                new Thread(monitor).start();
            } catch (Exception e) {
                String errMsg = "start monitor thread error.";
                log.error(errMsg, e);
                throw new Exception(errMsg, e);
            }
        }

        return monitor;
    }

    /**
     * <h2>结束工作线程</h2>
     *
     * @param monitor 监视线程对象
     *
     * @throws Exception
     */
    @Override
    public void stopMonitor(Worker monitor) throws Exception {
        try {
            // 通知监视线程停止监控
            if (monitor != null) {
                monitor.interrupt();
            }
        } catch (Exception e) {
            String errMsg = "stop monitor thread error.";
            log.error(errMsg, e);
            throw new Exception(errMsg, e);
        }
    }

    /**
     * <h2>清理操作</h2>
     *
     * @param argsObj 请求参数对象
     *
     * @throws Exception
     */
    @Override
    public void clean(Args argsObj, List<Worker> workerList, Worker monitor) throws Exception {
        log.info("prepare() default empty...");
    }
}
