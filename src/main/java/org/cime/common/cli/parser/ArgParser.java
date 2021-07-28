package org.cime.common.cli.parser;

/**
 * <h1>解析字符串参数,给出相应类型对象</h1>
 *
 * @param <T> 目标数据类型
 */
public interface ArgParser<T> {

    /**
     * <h2>解析命令行参数,返回相应类型数据</h2>
     *
     * @param args 待解析的命令行参数
     *
     * @return 目标数据
     *
     * @throws IllegalAccessException
     */
    T parse(String args) throws IllegalAccessException;

}
