package com.zzyl.nursing.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class TaskTest {
    @Autowired
    private WebSocketServer webSocketServer;

//    @Scheduled(fixedDelay = 5000)
    public void test() {
        try {
            log.info("测试服务端给客户端发消息");
            webSocketServer.sendMessageToAll("测试服务端给客户端发送消息" + LocalDateTime.now());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
