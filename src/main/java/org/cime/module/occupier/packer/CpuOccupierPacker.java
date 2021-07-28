package org.cime.module.occupier.packer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.cime.common.cli.bean.Args;
import org.cime.common.core.ParamPacker;
import org.cime.common.core.WorkerParam;

public class CpuOccupierPacker implements ParamPacker {
    @Override
    public List<WorkerParam> pack(Args args, CountDownLatch startLatch, CountDownLatch endLatch) {

        int threadCount = args.getThreadCount();
        List<WorkerParam> workerList = new ArrayList<>(threadCount);

        //根据工作线程数封装线程参数
        for (int idx = 0; idx < threadCount; idx++) {

            Map<String, Object> paramMap = new HashMap<>();
            //间隔时间,用于调整cpu使用率
            paramMap.put("interval", args.getInterval());
            //线程停止时间
            paramMap.put("deadline", new Date(System.currentTimeMillis() + args.getDuration()));

            //封装参数对象
            WorkerParam param = new WorkerParam();
            param.setStartLatch(startLatch);
            param.setEndLatch(endLatch);
            param.setNeedStartLatch(true);
            param.setNeedEndLatch(true);
            param.setParamMap(paramMap);
            workerList.add(param);
        }

        return workerList;
    }
}
