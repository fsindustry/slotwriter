package org.cime.module.redis.worker;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cime.common.core.InterruptableWorker;
import org.cime.common.redis.JedisUtil;
import org.cime.module.redis.bean.GetWorkerData;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * <h1>get读取器</h1>
 * <p>以pipeline方式执行get命令读取数据,并比对一致性</p>
 */
@Slf4j
public class GetWorker extends InterruptableWorker {

    private static final String DUMP_FILE_SEPARATOR = "[\\s]+";

    @Override
    public void execute() throws Exception {

        long startTime = System.currentTimeMillis();

        //获取参数
        Map<String, Object> paramMap = this.param.getParamMap();
        //pipeline大小
        int batchCount = (Integer) paramMap.get("batchCount");
        //每次生成数据后间隔时间,用于控制qps
        long sleep = (long) paramMap.get("interval");
        //线程要处理的文件路径
        File dumpFile = (File) paramMap.get("dumpFile");

        Integer totalCount;
        Integer errorCount = 0;

        //逐行读取文件,进行比对
        try (Jedis jedis = JedisUtil.getConnection();
             LineNumberReader reader = new LineNumberReader(new FileReader(dumpFile))) {

            Pipeline pipeline = jedis.pipelined();

            Integer lineNum;
            List<GetWorkerData> dataList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum = reader.getLineNumber();
                String[] columns = line.split(DUMP_FILE_SEPARATOR);
                String key = columns[0];
                String expectValue = columns[1];
                Response<String> resp = pipeline.get(key);

                GetWorkerData data = new GetWorkerData();
                data.setLineNum(lineNum);
                data.setKey(key);
                data.setExpectValue(expectValue);
                data.setResp(resp);
                dataList.add(data);

                if ((lineNum > 1) && ((lineNum - 1) % batchCount == 0)) {
                    pipeline.sync();
                    errorCount += compare(dataList);
                    dataList.clear();
                    pipeline = jedis.pipelined();
                }

                // 每次操作后睡眠一定间隔
                if (sleep > 0) {
                    Thread.sleep(sleep);
                }
            }

            totalCount = reader.getLineNumber();
        }

        //删除子文件
        dumpFile.deleteOnExit();

        long cost = System.currentTimeMillis() - startTime;
        log.info("Thread [" + Thread.currentThread().getName()
                + "] execute finished , key count:"
                + totalCount + " , error count:" + errorCount + " , cost:" + cost + "ms");
    }

    /**
     * <h2>执行比较操作</h2>
     *
     * @param dataList 待比较的数据集合
     *
     * @return 不一致的数据数目
     */
    private Integer compare(List<GetWorkerData> dataList) {

        Integer errorCount = 0;
        for (GetWorkerData data : dataList) {
            String actualValue = data.getResp().get();
            if (!data.getExpectValue().equals(actualValue)) {
                log.error("data inconsistent , data key :" + data.getKey() + " , expect value :" + data.getExpectValue()
                        + " , actual value :" + actualValue);
                errorCount++;
            }
        }

        return errorCount;
    }
}
