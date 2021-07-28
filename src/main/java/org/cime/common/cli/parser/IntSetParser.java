package org.cime.common.cli.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <h1>IntegerList解析器</h1>
 * <p>
 * 用来将以逗号分隔的字符串转换为整型数组
 * </p>
 */
public class IntSetParser implements ArgParser<Set<Integer>> {

    /**
     * 配置文件中多个元素的分隔符
     */
    private static final String SEPARATOR = ",";

    @Override
    public Set<Integer> parse(String args) throws IllegalAccessException {

        if (null == args || "".equals(args.trim())) {
            return null;
        }

        List<String> slotStringList = Arrays.asList(args.split(SEPARATOR));
        Set<String> strSet = new HashSet<>(slotStringList);
        return strSet.stream().map(Integer::new).collect(Collectors.toSet());
    }
}
