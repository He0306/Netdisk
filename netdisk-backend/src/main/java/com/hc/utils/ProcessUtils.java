package com.hc.utils;

import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * @author: hec
 * @date: 2023-09-17
 * @email: 2740860037@qq.com
 * @description:
 */
public class ProcessUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    public static String executeCommand(String cmd, Boolean outPrintLog) {
        if (StringUtils.isEmpty(cmd)) {
            logger.error("--- 指令执行失败，因为要执行的FFmpeg指令为空！---");
            return null;
        }

        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            // 执行ffmpeg
            // 取出输出流
            PrintStream errorStream = new PrintStream(process.getErrorStream());
            PrintStream inputStream = new PrintStream(process.getInputStream());
            errorStream.start();
            inputStream.start();
            // 获取执行的命令信息
            process.waitFor();
            // 获取执行结果字符串
            String result = errorStream.stringBuffer.append(inputStream.stringBuffer + "\n").toString();
            // 输出执行的命令信息
            if (outPrintLog) {
                logger.info("执行命令：{}，已执行完毕，执行结果：{}", cmd, result);
            } else {
                logger.info("执行命令：{}，已执行完毕", cmd);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(HttpCodeEnum.CODE_419);
        } finally {
            if (null != process) {
                ProcessKiller processKiller = new ProcessKiller(process);
                runtime.addShutdownHook(processKiller);
            }
        }
    }

    /**
     * 在程序退出前结束已有的FFmpeg进程
     */
    private static class ProcessKiller extends Thread {
        private Process process;

        public ProcessKiller(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            this.process.destroy();
        }
    }

    static class PrintStream extends Thread {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        StringBuffer stringBuffer = new StringBuffer();

        public PrintStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                if (null == inputStream) {
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            } catch (Exception e) {
                logger.error("读取输入流出错了！错误信息：" + e.getMessage());
            } finally {
                try {
                    if (null != bufferedReader) {
                        bufferedReader.close();
                    }
                    if (null != inputStream) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    logger.error("调用PrintStream读取输出流后，关闭流时出错！");
                }
            }
        }
    }
}
