package com.bluedon.spiderfile.spidertask;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author liujh
 * @date 2019/9/19 14:09
 */
@Component
@Slf4j
@Data
@EnableScheduling
public class DownloadFileScheduled {
    @Autowired
    private DownTask downTask;

    @Autowired
    private ThreadPoolTaskExecutor pool;

    //@Scheduled(cron = "0 0/1 * * * ?")
    public void taskFordownFile() {
        List<String> urls = null;
        try {
             urls = readUrl();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String url :urls){
            downTask.downfile(url);
        }

    }

    private List<String> readUrl() throws IOException {
        return FileUtils.readLines(new File("C:\\Users\\20160712\\Desktop\\spideraddr.txt"), "utf-8");
    }

}
