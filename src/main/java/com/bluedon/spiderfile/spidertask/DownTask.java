package com.bluedon.spiderfile.spidertask;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author liujh
 * @date 2019/9/20 8:54
 */
@ConfigurationProperties(prefix = "file.store")
@Component
@Data
@Slf4j
public class DownTask {
    private String path;
    private DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Autowired
    private CloseableHttpAsyncClient httpclient;

    @Async("taskExecutor")
    public void downfile(String url) {
        httpclient.start();
        final HttpGet request = new HttpGet(url);
        httpclient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                try {
                    log.info("{}请求完毕 url:{}响应{}",Thread.currentThread().getName(),url,response.getStatusLine());
                    saveFile(url,response.getEntity().getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void failed(final Exception ex) {
               log.error("{} 请求失败 url:{} get fail {}",Thread.currentThread().getName(),url,ex.getMessage());
            }
            @Override
            public void cancelled() {
                log.info("{} cancelled",request.getRequestLine());
            }

        });
    }



    /*@Async
    public void downfile(String url) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Resource> httpEntity = new HttpEntity<Resource>(headers);
        log.info("{}正在下载链接{}的内容",Thread.currentThread().getName(),url);
        ResponseEntity<byte[]> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET,
                    httpEntity, byte[].class);
        } catch (Exception e) {
            //log.error(Thread.currentThread().getName() + "" +e.getCause());
        }
        if (response!=null) {
            log.info("{}+{}响应状态：{}", Thread.currentThread().getName(),url,response.getStatusCode());
            try {
                saveFile(url,response.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    public void saveFile(String url, InputStream data) throws IOException {
        String date = df.format(LocalDateTime.now());
        String filePath = path +"/"+ date;
        String filename = url.replaceAll("http[s]?://", "").replaceAll("[/|?|\\||\\*]","_");
        //FileUtils.writeByteArrayToFile(new File(filePath+"/"+filename),data);
        IOUtils.copy(data,new FileOutputStream(filePath+"/"+filename));
    }
}
