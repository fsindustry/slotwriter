package org.cime.module.monitor.packer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.cime.common.cli.bean.Args;
import org.cime.common.core.ParamPacker;
import org.cime.common.core.WorkerParam;

/**
 * <h1>ProxyMonitorWorker工作线程对应参数打包器</h1>
 */
public class ProxyMonitorPacker implements ParamPacker {

    @Override
    public List<WorkerParam> pack(Args args, CountDownLatch startLatch, CountDownLatch endLatch) {

        List<WorkerParam> paramList = new ArrayList<>(1);
        Map<String, Object> paramMap = new HashMap<>();

        //proxy的IP地址
        paramMap.put("ip", args.getIp());
        //proxy的状态端口
        paramMap.put("statePort", args.getStatePort());

        //封装参数对象
        WorkerParam param = new WorkerParam();
        param.setStartLatch(startLatch);
        param.setEndLatch(endLatch);
        param.setNeedStartLatch(true);
        param.setNeedEndLatch(false);
        param.setParamMap(paramMap);
        paramList.add(param);

        return paramList;
    }
}
