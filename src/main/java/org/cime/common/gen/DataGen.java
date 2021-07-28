/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package org.cime.common.gen;

import java.util.Map;

/**
 * <h1>数据产生器接口</h1>
 *
 * @param <T> 要生成的数据类型
 */
public interface DataGen<T> {

    void intial(Map<String, Object> configMap);

    T generate();
}
