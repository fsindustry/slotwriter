package org.cime.module.occupier.worker;

import java.util.Date;
import java.util.Map;

import org.cime.common.core.InterruptableWorker;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>cpu占用线程</h1>
 */
@Slf4j
public class MemOccupierWorker extends InterruptableWorker {

    @Override
    public void execute() throws Exception {
        Map<String, Object> paramMap = param.getParamMap();
        Date deadline = (Date) param.getParamMap().get("deadline");
        long sleep = (long) paramMap.get("interval");
        long maxSize = (long) paramMap.get("maxSize");
        int batchCount = (int) paramMap.get("batchCount");

        int left = (int) (maxSize % batchCount);
        int arrCount = (int) (maxSize / batchCount);
        arrCount = (left == 0) ? arrCount : arrCount + 1;

        byte[][] byteArr = new byte[arrCount][batchCount];
        int len = 0;
        for (int i = 0; i < arrCount; i++) {

            if (i == arrCount - 1 && left > 0) {
                len = left;
            } else {
                len = batchCount;
            }

            for (int j = 0; j < len; j++) {
                byteArr[i][j] = 0;

                if (interrupt) {
                    break;
                }
            }
        }

        // 运行指定时长
        long deadlineMills = deadline.getTime();
        Double res = null;
        while (System.currentTimeMillis() <= deadlineMills && !interrupt) {

            // 每次操作后睡眠一定间隔
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
        }
    }
}
