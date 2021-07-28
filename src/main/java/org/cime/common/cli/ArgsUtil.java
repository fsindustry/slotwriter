/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package org.cime.common.cli;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.cime.common.cli.bean.PatternOption;
import org.cime.common.cli.parser.ArgParser;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>命令行参数解析工具</h1>
 * <p>
 * 提供命令行解析所要使用的工具方法;
 * </p>
 */
@Slf4j
public class ArgsUtil {

    /**
     * 参数配置文件名
     */
    private static final String ARGS_CONF = "args.properties";

    /**
     * 参数对应实体类名称key
     */
    private static final String ARGS_CLASS_NAME_KEY = "args.class";

    /**
     * 实体类对应字段名key
     */
    private static final String ARGS_COLUMN_NAMES_KEY = "args.columns";

    /**
     * 参数分隔符
     */
    private static final String ARGS_SEPARATOR = "\\|";

    /**
     * 封装命令行参数类
     */
    private static Options defOpts;

    /**
     * 参数对应实体类类名
     */
    private static String argsClassName;

    /**
     * <h2>加载参数配置文件,初始化参数对象</h2>
     *
     * @throws ParserConfigurationException 解析参数配置文件异常
     */
    private static void initial() throws ParserConfigurationException {

        // 读取配置文件,封装参数
        defOpts = new Options();
        try {
            Properties properties = new Properties();
            InputStream inStream = ClassLoader.getSystemClassLoader().getResourceAsStream(ARGS_CONF);
            properties.load(new InputStreamReader(inStream));

            argsClassName = properties.getProperty(ARGS_CLASS_NAME_KEY);
            String[] argsColumnNames = properties.getProperty(ARGS_COLUMN_NAMES_KEY).split(ARGS_SEPARATOR);

            for (String colName : argsColumnNames) {
                String opt = properties.getProperty(colName + ".opt");
                String longOpt = properties.getProperty(colName + ".longOpt");
                String hasArgs = properties.getProperty(colName + ".hasArgs");
                String desc = properties.getProperty(colName + ".desc");
                String pattern = properties.getProperty(colName + ".pattern");
                String isRequired = properties.getProperty(colName + ".isRequired");
                String defaultValue = properties.getProperty(colName + ".defaultValue");
                String parserName = properties.getProperty(colName + ".parser");

                if (null == opt
                        || "".equals(opt.trim())
                        || null == longOpt
                        || "".equals(longOpt.trim())) {

                    throw new IllegalArgumentException("option [" + colName + "] config error,please check "
                            + ARGS_CONF);
                }

                PatternOption option = new PatternOption(opt, longOpt, Boolean.parseBoolean(hasArgs), desc);
                option.setRequired(Boolean.parseBoolean(isRequired));
                option.setColName(colName);
                option.setPattern(pattern);
                option.setDefaultValue(defaultValue);
                option.setParserName(parserName);

                defOpts.addOption(option);
            }
        } catch (Exception e) {
            log.error("load config file [" + ARGS_CONF + "] error.", e);
            throw new ParserConfigurationException("");
        }
    }

    /**
     * <h2>执行参数解析操作,将参数封装到参数实体类中</h2>
     *
     * @param args 命令行参数
     *
     * @return 封装号的参数实体类;
     *
     * @throws Exception 解析参数异常;
     */
    public static Object parseArgs(String[] args) throws Exception {

        // 若未初始化,则先初始化
        if (null == defOpts) {
            initial();
        }

        // 如果没有参数,则打印帮助信息
        if (args.length <= 0) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.setWidth(110);
            helpFormatter.printHelp("slotwriter", defOpts, true);
            throw new IllegalArgumentException();
        }

        Object argsObj;
        try {

            // 解析命令行参数
            argsObj = Class.forName(argsClassName).newInstance();
            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(defOpts, args);

            for (Option option : defOpts.getOptions()) {

                PatternOption defOpt = (PatternOption) option;
                String opt = option.getOpt();

                // 校验参数值的合法性
                String pattern = defOpt.getPattern();
                String value = cli.getOptionValue(opt, defOpt.getDefaultValue());
                if (null != pattern
                        && !"".equals(pattern.trim())
                        && !value.matches(pattern)) {
                    String errMsg = "opt [" + opt + "] value [" + value + "] not matched the pattern [" + pattern + "]";
                    log.error(errMsg);
                    throw new IllegalArgumentException(errMsg);
                }

                // 如果没有解析器,则按照默认方式解析
                String parserName = defOpt.getParserName();
                if (null == parserName || "".equals(parserName)) {
                    // 设置给指定参数对象
                    BeanUtils.setProperty(argsObj, defOpt.getColName(), value);
                }
                // 如果指定解析器,则根据解析器解析
                else {
                    Class<? extends ArgParser> parserClass = (Class<? extends ArgParser>) Class.forName(parserName);
                    ArgParser<?> argParser = parserClass.newInstance();
                    BeanUtils.setProperty(argsObj, defOpt.getColName(), argParser.parse(value));
                }
            }

        } catch (Exception e) {
            String errMsg = "parse cmd args error.";
            log.error(errMsg, e);
            throw new IllegalArgumentException(errMsg);
        }

        return argsObj;
    }
}
