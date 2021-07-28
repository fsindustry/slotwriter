package org.cime.common.io;

import java.util.List;

import org.junit.Test;

public class FileUtilTest {

    @Test
    public void execCmd() throws Exception {


        //        String[] cmd1 = {"interval", "10"};
        //        resultList = FileUtil.execCmd(cmd1);
        //        System.out.println(resultList);
        //
        //        String[] cmd2 = {"ls", "-al"};
        //        resultList = FileUtil.execCmd(cmd2);
        //        System.out.println(resultList);
        //
        //        String[] cmd3 = {"wc", "-l", "."};
        //        resultList = FileUtil.execCmd(cmd3);
        //        System.out.println(resultList);

        String[] cmd4 = {"sh","-c","wc -l slotwriter.iml | awk '{print $1}'"};
        String result = FileUtil.execCmd(cmd4);
        System.out.println(result);

    }

}