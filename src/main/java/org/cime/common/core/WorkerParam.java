package org.cime.common.core;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import lombok.Data;

/**
 * <h1>工作线程参数对象</h1>
 * <p>用来存放工作线程运行需要的参数</p>
 */
@Data
public class WorkerParam {

    /**
     * 是否需要启动闩
     */
    private boolean needStartLatch;

    /**
     * 是否需要结束闩
     */
    private boolean needEndLatch;

    /**
     * 启动闩
     */
    private CountDownLatch startLatch;

    /**
     * 结束闩
     */
    private CountDownLatch endLatch;

    /**
     * 工作线程的各种定制参数,不同的工作线程参数不同
     */
    private Map<String, Object> paramMap;
}
