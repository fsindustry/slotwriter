package org.cime.module.monitor.bean;

import lombok.Data;

/**
 * <h1>监控元素,用来存放一个监控项对应的数据</h1>
 */
@Data
public class MonitorElement {

    /**
     * 第一次取值
     */
    private Double first;

    /**
     * 最大值
     */
    private Double max;

    /**
     * 最小值
     */
    private Double min;

    /**
     * 平均值
     */
    private Double avg;

    /**
     * 最后一次取值
     */
    private Double last;

    /**
     * 统计次数
     */
    private Long count;
}
