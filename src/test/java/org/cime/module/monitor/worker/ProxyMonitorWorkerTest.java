package org.cime.module.monitor.worker;

import java.util.HashMap;
import java.util.Map;

import org.cime.common.redis.JedisUtil;
import org.cime.common.core.WorkerParam;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProxyMonitorWorkerTest {

    @Before
    public void setUp() throws Exception {

        // 初始化redis连接池
        try {
            JedisUtil.initial("10.48.56.211", 8998, 1, 100 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testExecute() throws Exception {

        WorkerParam param = new WorkerParam();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ip", "10.48.56.211");
        paramMap.put("statePort", 8999);
        param.setParamMap(paramMap);

        ProxyMonitorWorker worker = new ProxyMonitorWorker();
        worker.initial(param);
        worker.execute();
        worker.interrupt();
    }
}