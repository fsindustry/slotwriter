package org.cime.module.writer.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cime.common.redis.JedisUtil;
import org.cime.common.core.WorkerParam;
import org.cime.module.redis.worker.CmpSetWorker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;


public class CmpSetWorkerTest {

    @Before
    public void setUp() throws Exception {
        // 初始化redis连接池
        try {
            JedisUtil.initial("127.0.0.1", 6379, 2, 100 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 清理测试环境
        try (Jedis jedis = JedisUtil.getConnection()) {
            jedis.flushAll();
        }
    }

    @Test
    public void testExecute() throws Exception {

        // 创建被测对象,封装入参
        CmpSetWorker worker = new CmpSetWorker();
        WorkerParam param = new WorkerParam();
        param.setNeedStartLatch(false);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ip", "127.0.0.1");
        paramMap.put("port", 6379);
        paramMap.put("slotSet", null);
        paramMap.put("batchCount", 5);
        paramMap.put("valueSize", 10);
        paramMap.put("start", 0L);
        paramMap.put("count", 5L);
        paramMap.put("interval", 1000L);
        param.setParamMap(paramMap);
        worker.initial(param);

        int valueSize = (int) paramMap.get("valueSize");
        long count = (long) paramMap.get("count");

        // 执行方法
        worker.execute();

        // 匹配结果
        try (Jedis jedis = JedisUtil.getConnection()) {
            Set<String> keys = jedis.keys("*");
            Assert.assertEquals(count, keys.size());
            for (String key : keys) {
                String value = jedis.get(key);
                Assert.assertEquals(valueSize, value.length());
            }
        }
    }

    @After
    public void tearDown() throws Exception {

        // 清理测试环境
        try (Jedis jedis = JedisUtil.getConnection()) {
            jedis.flushAll();
        }
        // 关闭连接池
        JedisUtil.closePool();
    }
}