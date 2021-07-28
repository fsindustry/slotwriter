package org.cime.module.writer.worker;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.cime.common.redis.JedisUtil;
import org.cime.common.core.WorkerParam;
import org.cime.module.redis.worker.StableWorker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

public class StableWorkerTest {

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
        StableWorker worker = new StableWorker();
        WorkerParam param = new WorkerParam();
        param.setNeedStartLatch(false);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("slotSet", null);
        paramMap.put("interval", 1000L);
        paramMap.put("batchCount", 5);
        paramMap.put("count", 10L);
        paramMap.put("valueSize", 10);
        paramMap.put("deadline", new Date(System.currentTimeMillis() + 60 * 1000));
        param.setParamMap(paramMap);
        worker.initial(param);

        // 执行方法
        worker.execute();
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

    @Test
    public void testSet() throws Exception {

    }

    @Test
    public void testGet() throws Exception {

    }

    @Test
    public void testDel() throws Exception {

    }

    @Test
    public void testCmp() throws Exception {

    }
}