package org.cime.module.redis.worker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.cime.common.core.InterruptableWorker;
import org.cime.common.gen.StringGen;
import org.cime.common.hash.Crc16;
import org.cime.common.redis.JedisUtil;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * <h1>长稳测试工作线程</h1>
 */
@Slf4j
public class StableWorker extends InterruptableWorker {

    /**
     * 定义操作名称,与操作方法相对应
     */
    private static String[] operTypes = {"set", "get", "del", "cmp"};

    /**
     * 存放生成的数据
     */
    private Map<String, String> dataMap = new HashMap<>();

    /**
     * 字符串生成器
     */
    private StringGen gen = new StringGen();

    /**
     * 存放用于生成key的索引
     */
    private long currIdx;

    /**
     * 管道提交数量
     */
    private int batchCount;

    /**
     * 当前线程维持数据量
     */
    private long count;

    /**
     * 存放需要写入数据的slot
     */
    private Set<Integer> slotSet;

    /**
     * 生成key的前缀
     */
    private String keyPrefix;

    /**
     * <h2>比对redis中的数据与本地数据是否一致</h2>
     *
     * @param resMap 从redis中读取的数据
     */
    private List<String> compare(Map<String, Response<String>> resMap) {
        List<String> failKeys = new ArrayList<>();
        for (Map.Entry<String, Response<String>> entry : resMap.entrySet()) {
            String key = entry.getKey();
            String actual = entry.getValue().get();
            String expect = dataMap.get(key);

            //如果不一致,则打印日志,并从map中删除相应数据
            if (!expect.equals(actual)) {
                log.error("key[" + key + "] inconsistent , expect:" + expect + ", actual:" + actual);
                failKeys.add(key);
            }
        }

        return failKeys;
    }

    @Override
    public void execute() throws Exception {

        long startTime = System.currentTimeMillis();

        Map<String, Object> paramMap = param.getParamMap();
        slotSet = (Set<Integer>) paramMap.get("slotSet");

        long sleep = (long) paramMap.get("interval");
        this.batchCount = (int) paramMap.get("batchCount");
        this.count = (long) paramMap.get("count");
        this.keyPrefix = (String) paramMap.get("keyPrefix");
        Date deadline = (Date) param.getParamMap().get("deadline");

        gen.intial(paramMap);

        try (Jedis jedis = JedisUtil.getConnection()) {

            //运行指定时长
            long deadlineMills = deadline.getTime();
            while (System.currentTimeMillis() <= deadlineMills && !interrupt) {
                //随机获取一个操作,并执行
                String operType = operTypes[ThreadLocalRandom.current().nextInt(operTypes.length)];

                //通过反射调用
                Method oper = this.getClass().getDeclaredMethod(operType, Jedis.class);
                oper.invoke(this, jedis);

                //每次操作后睡眠一定间隔
                if (sleep > 0) {
                    Thread.sleep(sleep);
                }
            }
        }

        long totalCost = System.currentTimeMillis() - startTime;
        log.info("Thread [" + Thread.currentThread().getName()
                + "] execute finished ，key count:"
                + count + ", cost:" + totalCost + "ms");
    }

    /**
     * <h2>redis写操作</h2>
     * <p>
     * 通过pipeline执行写操作,写入随机数据;
     * </p>
     *
     * @param jedis redis连接
     *
     * @throws Exception 操作异常
     */
    public void set(Jedis jedis) throws Exception {

        long startTime = System.currentTimeMillis();

        Pipeline pipeline = jedis.pipelined();

        //如果数据达到指定数量,则只进行更新;
        if (dataMap.size() >= count) {

            long operCount = ThreadLocalRandom.current().nextLong(count) + 1;

            Iterator<Map.Entry<String, String>> entries = dataMap.entrySet().iterator();
            for (long i = 0; i < operCount; i++) {

                if (!entries.hasNext()) {
                    break;
                }

                Map.Entry<String, String> entry = entries.next();
                String key = entry.getKey();
                String value = gen.generate();
                pipeline.set(entry.getKey(), value);
                dataMap.put(key, value);

                if (dataMap.size() % batchCount == 0) {
                    pipeline.sync();
                    pipeline = jedis.pipelined();
                }
            }

            pipeline.sync();

            if (log.isDebugEnabled()) {
                long cost = System.currentTimeMillis() - startTime;
                log.debug("operType: update, max count:" + count + ", current count:" + dataMap.size() + ", "
                        + "operCount:" +
                        operCount + ", cost:" + cost + " ms");
            }
        }
        //如果数据未达到指定数量,则添加新数据;
        else {
            long operCount = ThreadLocalRandom.current().nextLong(count - dataMap.size()) + 1;

            for (long genCount = 0; genCount < operCount; ) {
                String key;
                if (null != keyPrefix && !"".equals(keyPrefix.trim())) {
                    key = keyPrefix + Thread.currentThread().getName() + "-" + (currIdx++);
                } else {
                    key = Thread.currentThread().getName() + "-" + (currIdx++);
                }

                // 如果指定了slotId，则只匹配指定的slotId
                int destSlotId = Crc16.getSlotId(key);
                if (null != slotSet && !slotSet.contains(destSlotId)) {
                    continue;
                }

                String value = gen.generate();
                pipeline.set(key, value);
                dataMap.put(key, value);

                if (dataMap.size() % batchCount == 0) {
                    pipeline.sync();
                    pipeline = jedis.pipelined();
                }

                //记录生成多少数据;
                //注意:不能放到for中计数,因为continue语句的情况不需要计数;
                genCount++;
            }
            pipeline.sync();

            if (log.isDebugEnabled()) {
                long cost = System.currentTimeMillis() - startTime;
                log.debug("operType: add, max count:" + count + ", current count:" + dataMap.size() + ", "
                        + "operCount:" +
                        operCount + ", cost:" + cost + " ms");
            }
        }
    }

    /**
     * <h2>redis读操作</h2>
     * <p>
     * 通过pipeline随机读取结果并进行比对;
     * </p>
     *
     * @param jedis redis连接
     *
     * @throws Exception 操作异常
     */
    public void get(Jedis jedis) throws Exception {

        long startTime = System.currentTimeMillis();

        if (dataMap.isEmpty()) {
            return;
        }

        long operCount = ThreadLocalRandom.current().nextLong(dataMap.size()) + 1;
        Pipeline pipeline = jedis.pipelined();

        Iterator<Map.Entry<String, String>> entries = dataMap.entrySet().iterator();
        Map<String, Response<String>> resMap = new HashMap<>();
        for (long i = 0; i < operCount; i++) {

            if (!entries.hasNext()) {
                break;
            }

            Map.Entry<String, String> entry = entries.next();
            resMap.put(entry.getKey(), pipeline.get(entry.getKey()));

            if (dataMap.size() % batchCount == 0) {
                pipeline.sync();
                //执行数据比对操作
                List<String> failKeys = compare(resMap);
                failKeys.forEach(resMap::remove);
                failKeys.clear();
                resMap.clear();
                pipeline = jedis.pipelined();
            }
        }

        if (resMap.size() > 0) {
            pipeline.sync();
            //执行数据比对操作
            List<String> failKeys = compare(resMap);
            failKeys.forEach(resMap::remove);
            failKeys.clear();
            resMap.clear();
        }

        if (log.isDebugEnabled()) {
            long cost = System.currentTimeMillis() - startTime;
            log.debug("operType: get, max count:" + count + ", current count:" + dataMap.size() + ", operCount:" +
                    operCount + ", cost:" + cost + " ms");
        }
    }

    /**
     * <h2>redis删除操作</h2>
     * <p>
     * 通过pipeline读取结果并进行比对;
     * </p>
     *
     * @param jedis redis连接
     *
     * @throws Exception 操作异常
     */
    public void del(Jedis jedis) throws Exception {

        long startTime = System.currentTimeMillis();

        if (dataMap.isEmpty()) {
            return;
        }

        Map<String, Object> paramMap = param.getParamMap();
        long operCount = ThreadLocalRandom.current().nextLong(paramMap.size()) + 1;

        Iterator<Map.Entry<String, String>> entries = dataMap.entrySet().iterator();
        for (int i = 0; i < operCount; i++) {
            if (!entries.hasNext()) {
                break;
            }
            jedis.del(entries.next().getKey());
            entries.remove();
        }

        if (log.isDebugEnabled()) {
            long cost = System.currentTimeMillis() - startTime;
            log.debug("operType: del, max count:" + count + ", current count:" + dataMap.size() + ", operCount:" +
                    operCount + ", cost:" + cost + " ms");
        }
    }

    /**
     * <h2>redis全量比对操作</h2>
     * <p>
     * 通过pipeline读取所有结果并进行比对;
     * </p>
     *
     * @param jedis redis连接
     *
     * @throws Exception 操作异常
     */
    public void cmp(Jedis jedis) throws Exception {

        long startTime = System.currentTimeMillis();

        if (dataMap.isEmpty()) {
            return;
        }

        Pipeline pipeline = jedis.pipelined();

        Iterator<Map.Entry<String, String>> entries = dataMap.entrySet().iterator();
        Map<String, Response<String>> resMap = new HashMap<>();
        List<String> failKeys = new ArrayList<>();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            resMap.put(entry.getKey(), pipeline.get(entry.getKey()));

            if (dataMap.size() % batchCount == 0) {
                pipeline.sync();
                //执行数据比对操作
                failKeys.addAll(compare(resMap));
                resMap.clear();
                pipeline = jedis.pipelined();
            }
        }

        if (resMap.size() > 0) {
            pipeline.sync();
            //执行数据比对操作
            failKeys.addAll(compare(resMap));
            resMap.clear();
        }

        //删除不一致的key
        if (failKeys.size() > 0) {
            for (String key : failKeys) {
                dataMap.remove(key);
                jedis.del(key);
            }
            failKeys.clear();
        }

        if (log.isDebugEnabled()) {
            long cost = System.currentTimeMillis() - startTime;
            log.debug("operType: cmp, max count:" + count + ", current count:" + dataMap.size() + ", operCount:" +
                    dataMap.size() + ", cost:" + cost + " ms");
        }
    }
}
