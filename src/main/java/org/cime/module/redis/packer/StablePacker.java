package org.cime.module.redis.packer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.cime.common.cli.bean.Args;
import org.cime.common.core.ParamPacker;
import org.cime.common.core.WorkerParam;

public class StablePacker implements ParamPacker {
    @Override
    public List<WorkerParam> pack(Args args, CountDownLatch startLatch, CountDownLatch endLatch) {
        int threadCount = args.getThreadCount();
        List<WorkerParam> workerList = new ArrayList<>(threadCount);
        long maxSize = args.getMaxSize();
        int valueSize = args.getValueSize();

        //计算需要生成的数据总条数
        long total;
        if (args.getCount() > 0) {
            total = args.getCount();
        } else {
            total = (maxSize % valueSize == 0) ? maxSize / valueSize : (maxSize / valueSize + 1);
        }
        //平均每个线程需要生成的数据条数
        long avg = total / threadCount;
        //计算剩余的数据条数
        long left = total % avg;

        //根据工作线程数封装线程参数
        for (int idx = 0; idx < threadCount; idx++) {

            //计算每个线程需要处理的记录数
            long count = (idx == threadCount - 1) ? (avg + left) : avg;

            Map<String, Object> paramMap = new HashMap<>();
            //需要生成数据的slotId
            paramMap.put("slotSet", args.getSlotSet());
            //pipeline大小
            paramMap.put("batchCount", args.getBatchCount());
            //value值长度
            paramMap.put("valueSize", args.getValueSize());
            //每次生成数据后间隔时间,用于控制qps
            paramMap.put("interval", args.getInterval());
            //生成key的前缀
            paramMap.put("keyPrefix", args.getKeyPrefix());
            //每个线程需要处理的记录数
            paramMap.put("count", count);
            //程序运行的结束时间
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
