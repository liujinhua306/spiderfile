package com.bluedon.spiderfile;

import com.bluedon.spiderfile.spidertask.DownloadFileScheduled;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpiderfileApplicationTests {
    @Autowired
    private DownloadFileScheduled task;

    @Autowired
    private ThreadPoolTaskExecutor pool;
    @Test
    public void contextLoads() {
        task.taskFordownFile();

        for (;;){

        }

    }

}
