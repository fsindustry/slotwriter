package org.cime.common.core;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.cime.common.cli.bean.Args;

/**
 * <h1>参数包装器基类</h1>
 * <p>
 * 根据不同的工作线程类型,打包不同的线程参数;
 * </p>
 */
public interface ParamPacker {

    /**
     * <h2>打包工作线程参数</h2>
     *
     * @param args       命令行参数对象
     * @param startLatch 启动闩
     * @param endLatch   结束闩
     *
     * @return 封装后的工作线程参数列表
     */
    List<WorkerParam> pack(Args args, CountDownLatch startLatch, CountDownLatch endLatch) throws Exception;
}
