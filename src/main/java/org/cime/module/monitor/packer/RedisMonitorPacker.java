package org.cime.module.monitor.packer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.cime.common.cli.bean.Args;
import org.cime.common.core.ParamPacker;
import org.cime.common.core.WorkerParam;

/**
 * <h1>RedisMonitorWorker工作线程对应参数打包器</h1>
 */
public class RedisMonitorPacker implements ParamPacker {

    @Override
    public List<WorkerParam> pack(Args args, CountDownLatch startLatch, CountDownLatch endLatch) {
        List<WorkerParam> paramList = new ArrayList<>(1);

        //封装参数对象
        WorkerParam param = new WorkerParam();
        param.setStartLatch(startLatch);
        param.setEndLatch(endLatch);
        param.setNeedStartLatch(true);
        param.setNeedEndLatch(false);
        paramList.add(param);

        return paramList;
    }
}
