package org.cime.common.core;

/**
 * <h1>工作线程对象</h1>
 */
public interface Worker extends Runnable {

    /**
     * <h2>工作线程初始化方法</h2>
     *
     * @param param 工作线程需要用到的参数
     */
    void initial(WorkerParam param);

    /**
     * <h2>工作线程中断方法</h2>
     */
    void interrupt();

    /**
     * <h2>工作线程执行方法</h2>
     */
    void execute() throws Exception;

}
