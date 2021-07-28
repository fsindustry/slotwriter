package org.cime.common.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * <h1>redis连接工具</h1>
 * <p/>
 * <功能详细描述>
 *
 * @author FS_INDUSTRY
 * @see [相关类/方法]
 */
public class JedisUtil {

    /**
     * 存放连接池对象,线程池对象本身线程安全
     */
    private static JedisPool pool;

    /**
     * 私有化构造器,不需要初始化实例
     */
    private JedisUtil() {
    }

    /**
     * <h2>初始化redis连接池</h2>
     *
     * @param ip      redis服务器IP
     * @param port    redis服务器监听端口
     * @param size    连接池大小
     * @param timeout 连接超时时间
     */
    public static void initial(String ip, int port, int size, int timeout) {
        // 配置连接池，保证连接数不小于开启的线程数
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(size);
        poolConfig.setMaxWaitMillis(timeout);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnCreate(true);
        poolConfig.setTestOnBorrow(true);

        // 初始化连接池,设置超时时间100秒
        if (pool == null) {
            pool = new JedisPool(poolConfig, ip, port, timeout);
        }
    }

    /**
     * <h2>获取redis连接</h2>
     * <p/>
     *
     * @return 返回连接对象
     *
     * @see [类、类#方法、类#成员]
     */
    public static Jedis getConnection() {
        if (pool == null) {
            throw new IllegalStateException("connection pool uninitialized !");
        }

        // 创建redis连接
        return pool.getResource();
    }

    /**
     * <h2>关闭连接池</h2>
     * <p/>
     *
     * @see [类、类#方法、类#成员]
     */
    public static void closePool() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }
}
