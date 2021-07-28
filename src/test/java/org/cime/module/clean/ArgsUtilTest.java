package org.cime.module.clean;

import org.cime.common.cli.ArgsUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArgsUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testParseArgs() throws Exception {
        String[] args = {"-i", "127.0.0.1", "-p", "8080", "-e",
                "/Users/baidu/Documents/02_workspace/workspace_idea/slotwriter/src/main/resources/extend.json"};
        ArgsUtil.parseArgs(args);
    }
}