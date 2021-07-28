package org.cime.module.monitor.util;

import java.util.Map;

import org.cime.module.monitor.bean.MonitorElement;

/**
 * <h1>监控线程工具类</h1>
 */
public class MonitorUtil {

    private static final String FORMAT = "%30s:%20.2f%20.2f%20.2f%20.2f%20.2f%20d\n";

    /**
     * <h2>打印监控信息</h2>
     *
     * @param meMap 存放待打印的监控信息
     */
    public static void printResult(Map<String, MonitorElement> meMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%30s:%20s%20s%20s%20s%20s%20s\n", "Element", "Avg", "Max", "Min", "First", "Last",
                "Count"));
        for (Map.Entry<String, MonitorElement> entry : meMap.entrySet()) {
            String key = entry.getKey();
            MonitorElement me = entry.getValue();
            sb.append(String.format(FORMAT, key, me.getAvg(), me.getMax(), me.getMin(), me.getFirst(), me.getLast(),
                    me.getCount()));
        }

        System.out.println(sb);
    }
}
