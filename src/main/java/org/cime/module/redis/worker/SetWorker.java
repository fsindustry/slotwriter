package org.cime.module.redis.worker;

import java.util.Map;
import java.util.Set;

import org.cime.common.core.InterruptableWorker;
import org.cime.common.gen.StringGen;
import org.cime.common.hash.Crc16;
import org.cime.common.io.FileUtil;
import org.cime.common.redis.JedisUtil;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * <h1>set写入器</h1>
 * <p>以pipeline方式执行set命令写入数据</p>
 */
@Slf4j
public class SetWorker extends InterruptableWorker {

    /**
     * <h2>数据写入方法</h2>
     * <p>
     * 执行数据写入操作;
     * </p>
     */
    @Override
    public void execute() throws Exception {

        long startTime = System.currentTimeMillis();

        Map<String, Object> paramMap = param.getParamMap();
        Set<Integer> slotSet = (Set<Integer>) paramMap.get("slotSet");
        int batchCount = (Integer) paramMap.get("batchCount");
        int valueSize = (int) paramMap.get("valueSize");
        long startIdx = (long) paramMap.get("start");
        long count = (long) paramMap.get("count");
        long sleep = (long) paramMap.get("interval");
        String keyPrefix = (String) paramMap.get("keyPrefix");

        // 初始化数据生成类
        StringGen gen = new StringGen();
        gen.intial(paramMap);
        long genCount = 1;

        try (Jedis jedis = JedisUtil.getConnection()) {

            // 获取管道
            Pipeline p = jedis.pipelined();
            StringBuilder sb = new StringBuilder();
            for (long keyIdx = startIdx; genCount <= count; keyIdx++) {
                if (interrupt) {
                    break;
                }

                String keyStr;
                if (null != keyPrefix && !"".equals(keyPrefix.trim())) {
                    keyStr = keyPrefix + String.valueOf(keyIdx);
                } else {
                    keyStr = String.valueOf(keyIdx);
                }

                // 如果指定了slotId，则只匹配指定的slotId
                int destSlotId = Crc16.getSlotId(keyStr);
                if (null != slotSet && !slotSet.contains(destSlotId)) {
                    continue;
                }

                String value = gen.generate();
                // 写数据
                p.set(keyStr, value);
                sb.append(keyStr);
                sb.append(" ");
                sb.append(value);
                sb.append("\n");

                // 分批提交,防止内存过大
                if (genCount % batchCount == 0) {
                    // 如果设置了生成dump文件，则进行持久化
                    if (FileUtil.isFileEnable()) {
                        FileUtil.append(sb);
                    }
                    // 提交数据，重新获取管道
                    p.sync();
                    p = jedis.pipelined();
                    sb.delete(0, sb.length());
                }

                // 每次操作后睡眠一定间隔
                if (sleep > 0) {
                    Thread.sleep(sleep);
                }

                //记录生成多少数据;
                //注意:不能放到for中计数,因为continue语句的情况不需要计数;
                genCount++;
            }

            // 处理剩余数据，清空缓存
            if (sb.length() > 0) {
                if (FileUtil.isFileEnable()) {
                    FileUtil.append(sb);
                }
                // 提交数据
                p.sync();
                sb.delete(0, sb.length());
            }
        }

        long totalCost = System.currentTimeMillis() - startTime;

        if (genCount < count) {
            log.info("Thread [" + Thread.currentThread().getName()
                    + "] execute was interrupted , expect count:"
                    + count + " , actual count:" + genCount + "  , cost:" + totalCost + "ms");
        } else {
            log.info("Thread [" + Thread.currentThread().getName()
                    + "] execute finished ，key count:"
                    + count + ",value size:" + count * valueSize + " bytes,cost:" + totalCost + "ms");

        }

    }
}
