package org.cime.common.cli.parser;

/**
 * <h1>数据大小解析器</h1>
 * <p>
 * 将不同单位的数据大小字符串转换为字节数
 * </p>
 */
public class SizeParser implements ArgParser<Long> {

    /**
     * <h2>根据传入字符串，换算成字节数</h2>
     * <p/>
     * 如：<br>
     * 1kb，返回：1024；<br>
     * 1mb，返回：1024*1024；<br>
     * 1gb，返回：1024*1024*1024
     *
     * @param sizeStr 待换算的字符串
     *
     * @return 换算成的字节数
     *
     * @see [类、类#方法、类#成员]
     */
    private static long getSize(String sizeStr) {
        long size = 0;

        // byte
        if (sizeStr.matches("[0-9]+[bB]?")) {
            size = Long.parseLong(sizeStr.split("[bB]")[0]);
        }
        // kb
        else if (sizeStr.matches("[0-9]+[kK][bB]?")) {
            size = Long.parseLong(sizeStr.split("[kK][bB]?")[0]) * 1024;
        }
        // mb
        else if (sizeStr.matches("[0-9]+[mM][bB]?")) {
            size = Long.parseLong(sizeStr.split("[mM][bB]?")[0]) * 1024 * 1024;
        }
        // gb
        else if (sizeStr.matches("[0-9]+[gG][bB]?")) {
            size = Long.parseLong(sizeStr.split("[gG][bB]?")[0]) * 1024 * 1024 * 1024;
        }

        return size;
    }

    @Override
    public Long parse(String args) throws IllegalAccessException {
        return getSize(args);
    }
}
