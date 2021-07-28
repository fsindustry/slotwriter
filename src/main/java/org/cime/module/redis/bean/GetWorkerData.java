package org.cime.module.redis.bean;


import lombok.Data;
import redis.clients.jedis.Response;

@Data
public class GetWorkerData {

    /**
     * 文件行号
     */
    private Integer lineNum;

    /**
     * 比对数据对应的key
     */
    private String key;

    /**
     * 期望value
     */
    private String expectValue;

    /**
     * 实际需要比对的数据
     */
    private Response<String> resp;
}
