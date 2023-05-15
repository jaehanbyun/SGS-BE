//package pnu.cse.studyhub.chat.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//import pnu.cse.studyhub.chat.config.tcp.TCPMessageService;
//import pnu.cse.studyhub.chat.dto.request.TCPSocketSessionRequest;
//
//import java.util.HashMap;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class CustomWebSocketHandler extends TextWebSocketHandler {
//
//    private final TCPMessageService tcpMessageService;
//    HashMap<String, WebSocketSession> sessionMap = new HashMap<>(); // 웹 소켓 세션 저장
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        String request = TCPSocketSessionRequest.builder()
//                .type("CONNECT")
//                .userId("test1")
//                .session(session.getId())
//                .build().toString();
//
//        // 소켓 연결 성공 시
//        tcpMessageService.sendMessage(request);
//        log.info("Socket Connection Establish : " + session);
//        // 소켓 세션 저장 테스트
//        //        sessionMap.put(session.getId(),session);
//        super.afterConnectionEstablished(session);
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        String tcpMessage = TCPSocketSessionRequest.builder()
//                .type("DISCONNECT")
//                .userId("test1")
//                .session(session.getId())
//                .build().toString();
//        tcpMessageService.sendMessage(tcpMessage);
//        // 소켓 세션 삭제 테스트
//        //        sessionMap.remove(session.getId());
//        log.info("Socket Connection Closed : " + session);
//        super.afterConnectionClosed(session, status);
//    }
//}
