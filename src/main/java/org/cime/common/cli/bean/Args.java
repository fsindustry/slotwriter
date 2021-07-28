package org.cime.common.cli.bean;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

/**
 * <h1>参数对象,用来保存命令行参数</h1>
 */
@Data
public class Args {

    /**
     * redis服务器IP
     */
    private String ip;

    /**
     * redis服务器端口号
     */
    private int port;

    /**
     * redis对应密码
     */
    private String password;

    /**
     * 存放要写入数据的进程数
     */
    private int threadCount = Runtime.getRuntime().availableProcessors();

    /**
     * 写入器类型
     */
    private String workType;

    /**
     * 一批处理数据的条数
     */
    private int batchCount;

    /**
     * 写入数据的slot大小，默认100mb
     */
    private long maxSize;

    /**
     * 单个value的大小，默认16kb
     */
    private int valueSize;

    /**
     * 数据dump文件路径
     */
    private String dumpFilePath;

    /**
     * 存放要写入数据的slotID
     */
    private Set<Integer> slotSet = new HashSet<>();

    /**
     * 配置文件扩展
     */
    private String extendFilePath;

    /**
     * 处理数据条数,分为如下几种情况:
     * 1.set/cmpset:要写入的数据条数,优先于maxSize参数;
     * 2.longterm:生成的长稳数据条数;
     */
    private long count;

    /**
     * 线程执行完一次操作需要睡眠的时间
     */
    private long interval;

    /**
     * 持续时间
     */
    private long duration;

    /**
     * 监视器类型
     */
    private String monitorType;

    /**
     * 状态端口
     */
    private int statePort;

    /**
     * key前缀
     */
    private String keyPrefix;
}
