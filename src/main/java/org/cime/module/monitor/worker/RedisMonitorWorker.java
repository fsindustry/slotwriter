package org.cime.module.monitor.worker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cime.common.redis.JedisUtil;
import org.cime.common.core.InterruptableWorker;
import org.cime.module.monitor.bean.MonitorElement;
import org.cime.module.monitor.util.MonitorUtil;

import redis.clients.jedis.Jedis;

/**
 * <h1>redis监视线程</h1>
 * <p>用来统计redis运行结果信息</p>
 */
public class RedisMonitorWorker extends InterruptableWorker {

    /**
     * 定义要监控的属性名称
     */
    private String[] monitorAttrArr = {"used_memory", "used_memory_rss", "used_memory_lua",
            "mem_fragmentation_ratio", "instantaneous_ops_per_sec"};

    /**
     * 存放监控结果
     */
    private Map<String, MonitorElement> monitorEleMap = new HashMap<>();

    /**
     * <h2>计算各统计项</h2>
     *
     * @param resultArr  获取到的监控信息
     * @param monitorSet 要监控的属性集合
     */
    private void compute(String[] resultArr, Set<String> monitorSet) {

        for (String result : resultArr) {

            // 过滤注释
            if (result.matches("^(#.+)|([\\s])*$")) {
                continue;
            }

            // 获取属性名和属性值
            String[] kvArr = result.split(":");
            String key = kvArr[0].trim();

            // 过滤要监控属性
            if (!monitorSet.contains(key)) {
                continue;
            }
            Double currValue = new Double(kvArr[1].trim());

            // 若是第一次统计
            if (!monitorEleMap.containsKey(key)) {
                MonitorElement newMe = new MonitorElement();
                newMe.setFirst(currValue);
                newMe.setAvg(currValue);
                newMe.setMax(currValue);
                newMe.setMin(currValue);
                newMe.setLast(currValue);
                newMe.setCount(1L);
                monitorEleMap.put(key, newMe);
                continue;
            }

            MonitorElement currMe = monitorEleMap.get(key);
            currMe.setLast(currValue);

            if (currValue < currMe.getMin()) {
                currMe.setMin(currValue);
            }

            if (currValue > currMe.getMax()) {
                currMe.setMax(currValue);
            }

            long lastCount = currMe.getCount();
            double newAvg = (lastCount * currMe.getAvg() + currValue) / (lastCount + 1);
            currMe.setAvg(newAvg);
            currMe.setCount(++lastCount);
        }
    }

    @Override
    public void execute() throws Exception {

        Set<String> monitorSet = new HashSet<>(Arrays.asList(monitorAttrArr));
        try (Jedis jedis = JedisUtil.getConnection()) {
            // 收到中断后跳出循环
            while (!interrupt) {

                // 获取监控信息
                String[] resultArr = jedis.info().split("\r\n");

                // 计算监控信息
                compute(resultArr, monitorSet);

                // 每1s统计一次
                Thread.sleep(1000);
            }
        } finally {
            // 输出统计结果
            MonitorUtil.printResult(monitorEleMap);
        }
    }
}


