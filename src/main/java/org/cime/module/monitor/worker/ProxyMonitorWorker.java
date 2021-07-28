package org.cime.module.monitor.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cime.common.core.InterruptableWorker;
import org.cime.module.monitor.bean.MonitorElement;
import org.cime.module.monitor.util.MonitorUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>proxy监视线程</h1>
 * <p>用来统计proxy运行结果信息</p>
 */
@Slf4j
public class ProxyMonitorWorker extends InterruptableWorker {

    /**
     * 定义要监控的属性名称
     */
    private String[] monitorAttrArr = {"proxy_qps", "proxy_read_qps", "proxy_write_qps", "real_total_qps",
            "real_read_qps", "real_write_qps"};

    /**
     * 存放监控结果
     */
    private Map<String, MonitorElement> monitorEleMap = new HashMap<>();

    /**
     * <h2>执行一个监控属性的一次计算操作</h2>
     *
     * @param key       监控属性名称
     * @param currValue 监控属性当前值
     */
    private void computeUnit(String key, Double currValue) {
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
        } else {
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

    /**
     * <h2>递归解析json对象,获取需要监控的属性值</h2>
     *
     * @param jsonObj    待解析的json对象
     * @param monitorSet 要监控的属性集合
     */
    private void parse(JsonObject jsonObj, Set<String> monitorSet) {
        for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
            String key = entry.getKey();
            JsonElement jsonEle = entry.getValue();

            // 若为基础数据类型,则到达终止条件
            if (jsonEle.isJsonPrimitive()) {
                if (monitorSet.contains(key)) {
                    Double currValue = jsonEle.getAsDouble();
                    computeUnit(key, currValue);
                }
            }
            // 若为复合数据类型,则继续递归
            else if (jsonEle.isJsonObject()) {
                //继续递归操作
                parse(jsonEle.getAsJsonObject(), monitorSet);
            }
        }
    }

    @Override
    public void execute() throws Exception {

        Map<String, Object> paramMap = param.getParamMap();
        String ip = (String) paramMap.get("ip");
        int statePort = (int) paramMap.get("statePort");

        Set<String> monitorSet = new HashSet<>(Arrays.asList(monitorAttrArr));
        BufferedReader reader = null;
        // 收到中断后跳出循环
        while (!interrupt) {

            try (Socket socket = new Socket(ip, statePort)) {

                if (!socket.isConnected()) {
                    String errMsg = "connect proxy failed!";
                    log.error(errMsg);
                    throw new IOException(errMsg);
                }

                // 读取数据
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                // 解析json,获取监控信息
                String content = sb.toString();

                if (null == content || "".equals(content.trim())) {
                    log.error("monitor info is null!");
                }

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(content);
                JsonObject jsonObj = element.getAsJsonObject();
                parse(jsonObj, monitorSet);

            } finally {

                // 关闭输入流
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        log.error("failed to close reader!");
                    }
                }
            }

            // 每1s统计一次
            Thread.sleep(1000);
        }

        // 输出统计结果
        MonitorUtil.printResult(monitorEleMap);
    }
}
