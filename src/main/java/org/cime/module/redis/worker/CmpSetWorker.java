package org.cime.module.redis.worker;

import java.util.Map;
import java.util.Set;

import org.cime.common.core.InterruptableWorker;
import org.cime.common.gen.StringGen;
import org.cime.common.hash.Crc16;
import org.cime.common.io.FileUtil;
import org.cime.common.redis.JedisUtil;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * <h1>set命令写入器</h1>
 * <p>
 * 所有数据都使用set命令写入
 * </p>
 */
@Slf4j
public class CmpSetWorker extends InterruptableWorker {

    @Override
    public void execute() throws Exception {

        long startTime = System.currentTimeMillis();

        Map<String, Object> paramMap = param.getParamMap();
        Set<Integer> slotSet = (Set<Integer>) paramMap.get("slotSet");
        int batchCount = (int) paramMap.get("batchCount");
        int valueSize = (int) paramMap.get("valueSize");
        long startIdx = (long) paramMap.get("start");
        long count = (long) paramMap.get("count");
        long sleep = (long) paramMap.get("interval");
        String keyPrefix = (String) paramMap.get("keyPrefix");
        long errCount = 0;
        long genCount = 1;

        // 初始化数据生成类
        StringGen gen = new StringGen();
        gen.intial(paramMap);

        try (Jedis jedis = JedisUtil.getConnection()) {

            Client client = jedis.getClient();
            String ip = client.getHost();
            int port = client.getPort();

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
                try {
                    jedis.set(keyStr, value);
                }
                // TODO 通过异常控制写入逻辑,严重不符合规范的代码，小朋友不要学习^-^
                catch (JedisDataException e) {
                    // 当读写出现异常时，重新写入，直到连接正常后，成功写入后退出
                    String errMsg = e.getMessage();
                    while (!interrupt) {

                        log.info("[" + ip + ":" + port + ":" + keyStr + "]" + "[forbid write]" + errMsg);

                        try {
                            jedis.set(keyStr, value);
                            break;
                        } catch (JedisDataException e1) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e2) {
                                log.error("wait forbid write error...", e2);
                                break;
                            }
                            errMsg = e1.getMessage();
                        }
                    }

                    log.info("[" + ip + ":" + port + ":" + keyStr + "]" + "[retrive write]");
                }

                // 读数据，比对数据
                try {
                    String actual = jedis.get(keyStr);
                    if (!value.equals(actual)) {
                        log.error(
                                "[" + ip + ":" + port + ":" + keyStr + "] " + " inconsistent" + "，expect: " + value
                                        + ", actual:" + actual);
                        errCount++;
                    }
                } catch (Exception e) {
                    log.error("compare data error, key:" + keyStr, e);
                }

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
                sb.delete(0, sb.length());
            }
        }

        long totalCost = System.currentTimeMillis() - startTime;

        if (genCount < count) {
            log.info("Thread [" + Thread.currentThread().getName()
                    + "] execute was interrupted , expect count:"
                    + count + " , actual count:" + genCount + ", failed count : " + errCount + "  , cost:" + totalCost
                    + "ms");
        } else {
            log.info("Thread [" + Thread.currentThread().getName()
                    + "] write finished ，key count:"
                    + count + ", failed count : " + errCount + ", value size:" + count * valueSize + " bytes,cost:"
                    + totalCost + "ms");
        }
    }
}
