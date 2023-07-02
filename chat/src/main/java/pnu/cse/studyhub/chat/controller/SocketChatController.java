package pnu.cse.studyhub.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import pnu.cse.studyhub.chat.dto.response.FailedResponse;
import pnu.cse.studyhub.chat.exception.ErrorCode;
import pnu.cse.studyhub.chat.exception.kafka.KafkaAbnormalException;
import pnu.cse.studyhub.chat.exception.kafka.KafkaInterruptException;
import pnu.cse.studyhub.chat.exception.kafka.KafkaSerializationException;
import pnu.cse.studyhub.chat.exception.kafka.KafkaTimeOutException;
import pnu.cse.studyhub.chat.repository.entity.Chat;
import pnu.cse.studyhub.chat.service.KafkaConsumer;
import pnu.cse.studyhub.chat.service.KafkaProducer;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SocketChatController {

    @Value("${spring.kafka.topic}")
    private String TOPIC;
    private final KafkaProducer kafkaProducer;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/send")
    @SendTo("/topic/group")
    public Chat broadcastMessage(@Payload Chat chat, Principal principal) {
        try {
            kafkaProducer.send(TOPIC, chat);
        } catch (TimeoutException e) {
            throw new KafkaTimeOutException(e.getMessage());
        } catch (SerializationException e) {
            throw new KafkaSerializationException(e.getMessage());
        } catch (InterruptException e) {
            throw new KafkaInterruptException(e.getMessage());
        } catch (KafkaException e) {
            throw new KafkaAbnormalException(e.getMessage());
        }
        return chat;
    }
    // MessageMapping에 연결된 메소드에서 발생하는 예외를 처리하는 핸들러
    // @MessageMapping 또는 @SubscribeEvent로 주석이 달린 메서드에서 WebSocket 메시지를 처리하는 동안 발생하는 예외를 처리
    @MessageExceptionHandler({KafkaTimeOutException.class, KafkaSerializationException.class, KafkaInterruptException.class, KafkaAbnormalException.class})
    @SendToUser("/queue/errors")
    public FailedResponse handleException(Throwable exception, StompHeaderAccessor stompHeaderAccessor) {
        log.debug("ExceptionHandler : {}", exception.getMessage());
        FailedResponse failedResponse;
        if (exception instanceof TimeoutException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_TIMEOUT);
        else if (exception instanceof SerializationException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_SERIALIZE_FAILED);
        else if (exception instanceof  InterruptException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_INTERRUPTTED);
        else if (exception instanceof KafkaException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_UNKNOWN_ERROR);
        else
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.UNKNOWN_SERVER_ERROR);
        return failedResponse;
    }
    // MessageMapping에 연결되지 않은 메소드에서 발생하는 예외를 처리하는 핸들러
    // 전반적으로 발생하는 예외 처리
    @ExceptionHandler({KafkaTimeOutException.class, KafkaSerializationException.class, KafkaInterruptException.class, KafkaAbnormalException.class})
    @SendToUser("/queue/errors")
    public FailedResponse handleKafkaException(Exception exception, Principal principal) {
        log.debug("ExceptionHandler : {}", exception.getMessage());
        FailedResponse failedResponse;
        if (exception instanceof TimeoutException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_TIMEOUT);
        else if (exception instanceof SerializationException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_SERIALIZE_FAILED);
        else if (exception instanceof  InterruptException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_INTERRUPTTED);
        else if (exception instanceof KafkaException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_UNKNOWN_ERROR);
        else
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.UNKNOWN_SERVER_ERROR);
        return failedResponse;
    }
    private FailedResponse createFailedResponse(String message, ErrorCode errorCode) {
        return new FailedResponse<>(
                "Failed",
                errorCode.getMessage(),
                errorCode.getStatus(),
                errorCode.getCode(),
                message);
    }
}
