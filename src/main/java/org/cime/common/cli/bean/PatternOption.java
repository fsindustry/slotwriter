/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package org.cime.common.cli.bean;

import org.apache.commons.cli.Option;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <h1>封装命令行选项信息</h1>
 * <p>重写commons-cli option类,添加校验表达式,属性名,默认值</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PatternOption extends Option {

    /**
     * 属性名
     */
    private String colName;

    /**
     * 校验正则表达式
     */
    private String pattern;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 解析器名称
     */
    private String parserName;

    public PatternOption(String opt, String longOpt, boolean hasArg, String description)
            throws IllegalArgumentException {
        super(opt, longOpt, hasArg, description);
    }
}
