package org.cime.module.redis.workerCtl;

import java.io.IOException;
import java.util.List;

import org.cime.common.cli.bean.Args;
import org.cime.common.core.Worker;
import org.cime.common.core.BaseWorkerCtl;
import org.cime.common.io.FileUtil;
import org.cime.common.redis.JedisUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>与redis相关的工作线程控制器</h1>
 * <p>控制与redis相关的工作线程的运行流程</p>
 */
@Slf4j
public class RedisWorkerCtl extends BaseWorkerCtl {
    @Override
    public void prepare(Args argsObj) throws Exception {
        // 初始化redis连接池
        JedisUtil.initial(argsObj.getIp(), argsObj.getPort(), argsObj.getThreadCount() + 1, 100 * 1000);

        // 初始化文件输出工具
        try {
            if (null != argsObj.getDumpFilePath() && !"".equals(argsObj.getDumpFilePath().trim())) {
                FileUtil.initial(argsObj.getDumpFilePath());
                //FileUtil.clean();
            }
        } catch (IOException e) {
            String errMsg = "initial file common error.";
            log.error(errMsg, e);
            throw new IOException(errMsg, e);
        }
    }

    @Override
    public void clean(Args argsObj, List<Worker> workerList, Worker monitor) throws Exception {

        //中断主线程
        if (null != workerList && workerList.size() > 0) {
            for (Worker worker : workerList) {
                if (null == worker) {
                    continue;
                }

                worker.interrupt();
            }
        }

        //中断监视线程
        if (null != monitor) {
            monitor.interrupt();
        }

        //等待中断生效
        Thread.sleep(2000);

        //关闭Jedis连接池
        JedisUtil.closePool();

        //关闭文件句柄
        FileUtil.destory();
    }
}
