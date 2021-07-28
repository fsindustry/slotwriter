package org.cime;

import org.cime.common.cli.ArgsUtil;
import org.cime.common.cli.bean.Args;
import org.cime.common.core.BaseWorkerCtl;
import org.cime.common.core.WorkerEnum;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>程序入口</h1>
 */
@Slf4j
public class Main {

    /**
     * 程序入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {

        log.info("slotwriter start...");
        long startTime = System.currentTimeMillis();

        // 1.解析命令行参数
        Args argsObj = null;
        try {
            argsObj = (Args) ArgsUtil.parseArgs(args);
            log.info(argsObj.toString());
        } catch (Exception e) {
            log.error("parse cmd args error, system will exit(1).", e);
            System.exit(1);
        }

        // 2.根据工作线程类型获取线程控制器
        try {
            BaseWorkerCtl workerCtl = WorkerEnum.getCtl(argsObj.getWorkType());
            assert workerCtl != null;
            workerCtl.run(argsObj);
        } catch (Exception e) {
            log.error("run worker error, system will exit(1).", e);
            System.exit(1);
        }

        long totalCost = System.currentTimeMillis() - startTime;
        log.info("slotwriter stop,cost:" + totalCost + " ms");
    }
}
