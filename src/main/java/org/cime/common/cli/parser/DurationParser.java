package org.cime.common.cli.parser;

/**
 * <h1>时间类型参数解析器</h1>
 * <p>将不同单位的时间字符串转换为具体的毫秒数</p>
 */
public class DurationParser implements ArgParser<Long> {
    @Override
    public Long parse(String args) throws IllegalAccessException {
        long size = 0;

        // millisecond
        if (args.matches("^[1-9][\\d]*$")) {
            size = Long.parseLong(args);
        }
        // second
        else if (args.matches("[1-9][\\d]*[sS]")) {
            size = Long.parseLong(args.split("[sS]")[0]) * 1000;
        }
        // minute
        else if (args.matches("[1-9][\\d]*[mM]")) {
            size = Long.parseLong(args.split("[mM]")[0]) * 60 * 1000;
        }
        // hour
        else if (args.matches("[1-9][\\d]*[hH]")) {
            size = Long.parseLong(args.split("[hH]")[0]) * 60 * 60 * 1000;
        }
        // day
        else if (args.matches("[1-9][\\d]*[dD]")) {
            size = Long.parseLong(args.split("[dD]")[0]) * 24 * 60 * 60 * 1000;
        }

        return size;
    }
}
