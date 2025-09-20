package com.zzyl.nursing.ws;

import cn.hutool.json.JSONUtil;
import com.zzyl.nursing.domain.AlertRule;
import com.zzyl.nursing.domain.DeviceData;
import com.zzyl.nursing.vo.AlertNotifyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@EnableWebSocket
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 连接建立时触发
     *
     * @param session
     * @param sid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("有客户端连接到了服务器 , {}", sid);
        sessionMap.put(sid, session);
    }

    /**
     * 服务端接收到消息时触发
     *
     * @param session
     * @param message
     * @param sid
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("sid") String sid) {
        log.info("接收到了客户端 {} 发来的消息 : {}", sid, message);
    }

    /**
     * 连接关闭时触发
     *
     * @param session
     * @param sid
     */
    @OnClose
    public void onClose(Session session, @PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 通信发生错误时触发
     *
     * @param session
     * @param sid
     * @param throwable
     */
    @OnError
    public void onError(Session session, @PathParam("sid") String sid, Throwable throwable) {
        System.out.println("出现错误:" + sid);
        throwable.printStackTrace();
    }

    /**
     * 广播消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessageToAll(String message) throws IOException {
        Collection<Session> sessions = sessionMap.values();
        if (!CollectionUtils.isEmpty(sessions)) {
            for (Session session : sessions) {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            }
        }
    }


    /**
     * 向指定用户发送报警消息到WebSocket处理器
     *
     * @param userIds 用户ID列表，用于确定消息接收者
     * @param data    设备数据对象，包含设备相关信息
     * @param rule    报警规则对象，用于获取报警数据类型
     */
    public void sendMessageToHandler(List<Long> userIds, DeviceData data, AlertRule rule) {
        // 遍历用户ID列表，向每个用户的WebSocket会话发送报警通知
        userIds.forEach(userid -> {
            Session session = sessionMap.get(userid.toString());
            if (session != null) {
                // 构建报警通知对象
                AlertNotifyVo notifyVo = AlertNotifyVo.builder()
                        .id(data.getId())
                        .accessLocation(data.getAccessLocation())
                        .locationType(data.getLocationType())
                        .physicalLocationType(data.getPhysicalLocationType())
                        .deviceDescription(data.getDeviceDescription())
                        .productName(data.getProductName())
                        .functionName(data.getFunctionId())
                        .dataValue(data.getDataValue())
                        .alertDataType(rule.getAlertDataType())
                        .voiceNotifyStatus(1) // 默认开启语音通知
                        .notifyType(1) // 报警类型
                        .isAllConsumer(false) // 默认不是全员通知
                        .build();
                try {
                    // 通过WebSocket会话发送JSON格式的报警通知
                    log.info("发送报警通知给指定用户：{}", notifyVo);
                    session.getBasicRemote().sendText(JSONUtil.toJsonStr(notifyVo));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}