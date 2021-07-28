package org.cime.module.redis.packer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.cime.common.cli.bean.Args;
import org.cime.common.core.ParamPacker;
import org.cime.common.core.WorkerParam;
import org.cime.common.io.FileUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetPacker implements ParamPacker {
    @Override
    public List<WorkerParam> pack(Args args, CountDownLatch startLatch, CountDownLatch endLatch) throws Exception {

        int threadCount = args.getThreadCount();
        File[] splitFiles = FileUtil.splitFiles(args.getDumpFilePath(), threadCount);
        List<WorkerParam> paramList = new ArrayList<>(threadCount);

        for (int idx = 0; idx < threadCount; idx++) {

            Map<String, Object> paramMap = new HashMap<>();
            //pipeline大小
            paramMap.put("batchCount", args.getBatchCount());
            //每次生成数据后间隔时间,用于控制qps
            paramMap.put("interval", args.getInterval());
            //线程要处理的文件路径
            paramMap.put("dumpFile", splitFiles[idx]);

            WorkerParam param = new WorkerParam();
            param.setStartLatch(startLatch);
            param.setEndLatch(endLatch);
            param.setNeedStartLatch(true);
            param.setNeedEndLatch(true);
            param.setParamMap(paramMap);
            paramList.add(param);
        }
        return paramList;
    }
}
