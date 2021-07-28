package org.cime.common.core;

import org.cime.module.monitor.packer.ProxyMonitorPacker;
import org.cime.module.monitor.packer.RedisMonitorPacker;
import org.cime.module.monitor.worker.ProxyMonitorWorker;
import org.cime.module.monitor.worker.RedisMonitorWorker;
import org.cime.module.occupier.packer.CpuOccupierPacker;
import org.cime.module.occupier.packer.MemOccupierPacker;
import org.cime.module.occupier.worker.CpuOccupierWorker;
import org.cime.module.occupier.worker.MemOccupierWorker;
import org.cime.module.redis.packer.GetPacker;
import org.cime.module.redis.packer.SetPacker;
import org.cime.module.redis.packer.StablePacker;
import org.cime.module.redis.worker.CmpSetWorker;
import org.cime.module.redis.worker.GetWorker;
import org.cime.module.redis.worker.SetWorker;
import org.cime.module.redis.worker.StableWorker;
import org.cime.module.redis.workerCtl.RedisWorkerCtl;

/**
 * <h1>工作线程枚举类</h1>
 * <p>
 * 存放工作线程类型字符串和对应的class
 * </p>
 */
public enum WorkerEnum {

    /**
     * SET:使用set命令写入
     */
    SET("set", SetWorker.class, SetPacker.class, RedisWorkerCtl.class),

    /**
     * CMPSET:使用set命令写入，并进行数据比对
     */
    CMPSET("cmpset", CmpSetWorker.class, SetPacker.class, RedisWorkerCtl.class),

    /**
     * GET:使用get命令读取比对
     */
    GET("get", GetWorker.class, GetPacker.class, RedisWorkerCtl.class),

    /**
     * STABLE:长稳测试,支持长时间运行
     */
    STABLE("stable", StableWorker.class, StablePacker.class, RedisWorkerCtl.class),

    /**
     * PROXY_MONITOR:proxy监视线程;
     */
    PROXY_MONITOR("pmonitor", ProxyMonitorWorker.class, ProxyMonitorPacker.class, null),

    /**
     * REDIS_MONITOR:redis监视线程;
     */
    REDIS_MONITOR("rmonitor", RedisMonitorWorker.class, RedisMonitorPacker.class, null),

    /**
     * CPU_OCCUPIER:cpu占用线程
     */
    CPU_OCCUPIER("cpu", CpuOccupierWorker.class, CpuOccupierPacker.class, BaseWorkerCtl.class),

    /**
     * MEM_OCCUPIER:内存占用线程
     */
    MEM_OCCUPIER("mem", MemOccupierWorker.class, MemOccupierPacker.class, BaseWorkerCtl.class);

    /**
     * 工作线程类型标识
     */
    private String wokerType;

    /**
     * 工作线程类名
     */
    private Class<? extends Worker> workerClass;

    /**
     * 参数打包器类名
     */
    private Class<? extends ParamPacker> packerClass;

    /**
     * 工作线程控制器类名
     */
    private Class<? extends BaseWorkerCtl> workerCtlClass;

    WorkerEnum(String writerType, Class<? extends Worker> workerClass, Class<? extends ParamPacker> packerClass,
               Class<? extends BaseWorkerCtl> workerCtlClass) {
        this.wokerType = writerType;
        this.workerClass = workerClass;
        this.packerClass = packerClass;
        this.workerCtlClass = workerCtlClass;
    }

    /**
     * <h2>根据wokerType获取对应的工作线程实例</h2>
     *
     * @param workerType 工作线程类型;
     *
     * @return 匹配成功, 工作线程实例;匹配失败,返回null;
     *
     * @throws Exception 获取工作线程实例异常;
     */
    public static Worker getWoker(String workerType)
            throws Exception {
        workerType = workerType.toLowerCase();
        for (WorkerEnum writerEnum : WorkerEnum.values()) {
            if (writerEnum.wokerType.equals(workerType)) {
                return writerEnum.workerClass.newInstance();
            }
        }

        return null;
    }

    /**
     * <h2>根据wokerType获取对应的参数打包器实例</h2>
     *
     * @param wokerType 工作线程类型;
     *
     * @return 匹配成功, 工作线程实例;匹配失败,返回null;
     *
     * @throws Exception 获取工作线程实例异常;
     */
    public static ParamPacker getPacker(String wokerType)
            throws Exception {
        wokerType = wokerType.toLowerCase();
        for (WorkerEnum writerEnum : WorkerEnum.values()) {
            if (writerEnum.wokerType.equals(wokerType)) {
                return writerEnum.packerClass.newInstance();
            }
        }

        return null;
    }

    /**
     * <h2>根据wokerType获取对应的控制器实例</h2>
     *
     * @param workerType 工作线程类型;
     *
     * @return 匹配成功, 工作线程控制器实例;匹配失败,返回null;
     *
     * @throws Exception 获取工作线程控制器实例异常;
     */
    public static BaseWorkerCtl getCtl(String workerType)
            throws Exception {
        workerType = workerType.toLowerCase();
        for (WorkerEnum writerEnum : WorkerEnum.values()) {
            if (writerEnum.wokerType.equals(workerType)) {
                return writerEnum.workerCtlClass.newInstance();
            }
        }

        return null;
    }
}
