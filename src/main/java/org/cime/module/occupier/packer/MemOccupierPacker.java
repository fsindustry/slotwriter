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

public class MemOccupierPacker implements ParamPacker {
    @Override
    public List<WorkerParam> pack(Args args, CountDownLatch startLatch, CountDownLatch endLatch) {

        int threadCount = args.getThreadCount();
        List<WorkerParam> workerList = new ArrayList<>(threadCount);
        long maxSize = args.getMaxSize();
        int left = (int) (maxSize % threadCount);

        //根据工作线程数封装线程参数
        for (int idx = 0; idx < threadCount; idx++) {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("interval", args.getInterval());
            paramMap.put("deadline", new Date(System.currentTimeMillis() + args.getDuration()));
            paramMap.put("batchCount", args.getBatchCount());

            //计算每个线程需要占用的内存
            long avg;
            if (idx == (threadCount - 1) && left > 0) {
                avg = maxSize / threadCount + left;
            } else {
                avg = maxSize / threadCount;
            }
            paramMap.put("maxSize", avg);

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
