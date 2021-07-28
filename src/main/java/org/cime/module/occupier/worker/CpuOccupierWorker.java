package org.cime.module.occupier.worker;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.cime.common.core.InterruptableWorker;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>cpu占用线程</h1>
 */
@Slf4j
public class CpuOccupierWorker extends InterruptableWorker {

    @Override
    public void execute() throws Exception {
        Map<String, Object> paramMap = param.getParamMap();
        Date deadline = (Date) param.getParamMap().get("deadline");
        long sleep = (long) paramMap.get("interval");

        // 运行指定时长
        long deadlineMills = deadline.getTime();
        Double res = null;
        while (System.currentTimeMillis() <= deadlineMills && !interrupt) {

            res = ThreadLocalRandom.current().nextDouble();
            res += ThreadLocalRandom.current().nextDouble();
            res -= ThreadLocalRandom.current().nextDouble();

            // 每次操作后睡眠一定间隔
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
        }
    }
}
