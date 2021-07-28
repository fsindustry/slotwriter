package org.cime.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

/**
 * <h1>文件读写工具</h1>
 * <p>
 * 初始化文件对象,并提供读写等操作;
 * </p>
 */
@Slf4j
public class FileUtil {

    private static Integer CMD_EXEC_STATUS_NORMAL = 0;

    /**
     * 用于对操作加锁
     */
    private static Lock lock = new ReentrantLock();

    /**
     * 标识文件是否可用
     */
    private static volatile boolean fileEnable = false;

    /**
     * 存放待操作的目标文件
     */
    private static File destFile;

    /**
     * 存放初始化的文件流对象
     */
    private static FileWriter writer;

    /**
     * <h2>初始化文件工具</h2>
     *
     * @param filePath 待操作的文件路径
     *
     * @throws IOException 文件初始化异常
     */
    public static void initial(String filePath)
            throws IOException {
        lock.lock();
        try {
            destFile = new File(filePath);
            // 如果文件不存在，则创建文件；
            if (!destFile.exists()) {
                if (!destFile.createNewFile()) {
                    throw new IOException("create file [" + filePath + "] failed!");
                }
            }

            writer = new FileWriter(destFile, true);
            fileEnable = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * <h2>判断文件是否是可用的</h2>
     *
     * @return true, 可用;false,不可用;
     */
    public static boolean isFileEnable() {
        return fileEnable;
    }

    /**
     * <h2>将传入字符串附加到文件末尾</h2>
     *
     * @param sb 存放字符串的StringBuilder
     *
     * @throws IOException
     */
    public static void append(StringBuilder sb) throws IOException {

        if (destFile == null || !destFile.exists()) {
            throw new IllegalStateException("call initial() first");
        }

        if (null == sb) {
            throw new IllegalArgumentException("StringBuilder is empty!");
        }

        lock.lock();
        try {
            writer.append(sb);
            writer.flush();
        } finally {
            lock.unlock();
        }
    }

    /**
     * <h2>清除文件内容</h2>
     *
     * @throws IOException 操作异常;
     */
    public static void clean() throws IOException {
        if (destFile == null || !destFile.exists()) {
            throw new IllegalStateException("call initial() first");
        }

        lock.lock();
        try (FileOutputStream fileOut = new FileOutputStream(destFile, false)) {
            fileOut.flush();
        } finally {
            lock.unlock();
        }
    }

    /**
     * <h2>销毁文件对象</h2>
     *
     * @throws IOException 操作异常;
     */
    public static void destory() throws IOException {
        lock.lock();
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
                writer = null;
            }
            destFile = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * <h2>执行操作系统命令</h2>
     *
     * @param cmds 待执行命令数组
     *
     * @return 命令执行结果
     *
     * @throws Exception
     */
    public static String execCmd(String[] cmds) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        //获得命令执行后在控制台的输出信息
        Process proc = Runtime.getRuntime().exec(cmds);

        int retCode = proc.waitFor();

        //如果执行命令失败,则抛出异常
        if (retCode != CMD_EXEC_STATUS_NORMAL) {
            try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                 BufferedReader stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()))
            ) {
                String lineStr;
                while ((lineStr = stdIn.readLine()) != null) {
                    stringBuilder.append(lineStr);
                }

                while ((lineStr = stdErr.readLine()) != null) {
                    stringBuilder.append(lineStr);
                }
            }
            log.error(stringBuilder.toString());
            throw new Exception(stringBuilder.toString());
        }

        //封装正常的执行结果
        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String lineStr;
            while ((lineStr = stdIn.readLine()) != null) {
                stringBuilder.append(lineStr);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * <h2>将文件按照行号拆分为指定个数的子文件<h2/>
     *
     * @param filePath  待拆分的文件
     * @param fileCount 线程数
     *
     * @return 拆分后的子文件对象
     */
    public static File[] splitFiles(String filePath, int fileCount) throws Exception {

        File[] splitFiles;
        try {

            File dumpFile = new File(filePath);
            File dir = dumpFile.getParentFile();

            //获取行号
            String[] getLineCountCmd = {"sh", "-c", "wc -l " + dumpFile.getAbsolutePath() + " | awk '{print $1}'"};
            String result = FileUtil.execCmd(getLineCountCmd);
            if ("".equals(result.trim())) {
                String errMsg = "get file[" + dumpFile + "]'s line count error, errMsg: " + result;
                log.error(errMsg);
                throw new Exception(errMsg);
            }
            long lineCount = Long.parseLong(result.trim());

            //拆分文件
            long avgCount = lineCount / fileCount + 1;
            String prefix = dir.getCanonicalPath() + File.separator + dumpFile.getName() + ".";
            String[] splitFileCmd = {"split", "-d", "-l", String.valueOf(avgCount), dumpFile.getName(), prefix};
            FileUtil.execCmd(splitFileCmd);

            //获取文件列表
            splitFiles = dir.listFiles(new FilenameFilter() {

                private String pattern = "^" + dumpFile.getName() + "\\.[0-9]*";

                @Override
                public boolean accept(File dir, String name) {
                    return name.matches(pattern);
                }
            });

        } catch (Exception e) {
            String errMsg = "read dump file error.";
            log.error(errMsg, e);
            throw new Exception(errMsg, e);
        }

        return splitFiles;
    }
}