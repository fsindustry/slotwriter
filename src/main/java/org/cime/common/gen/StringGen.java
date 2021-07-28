/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package org.cime.common.gen;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <h1>字符串数据生成类</h1>
 * <p>
 * 生成随机字符串
 * </p>
 */
public class StringGen implements DataGen<String> {

    /**
     * 字符表
     */
    private static String template = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_";

    /**
     * 生成字符串长度
     */
    private int valueSize;

    /**
     * 用来存放生成随机字符串
     */
    private StringBuilder sb = new StringBuilder();

    @Override
    public void intial(Map<String, Object> paramMap) {
        valueSize = (int) paramMap.get("valueSize");
    }

    @Override
    public String generate() {
        sb.delete(0, sb.length());
        for (int i = 0; i < valueSize; i++) {
            int index = ThreadLocalRandom.current().nextInt(template.length());
            sb.append(template.charAt(index));
        }

        return sb.toString();
    }
}
