package pnu.cse.studyhub.state.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.dto.request.receive.TCPChatReceiveRequest;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcMessageServiceImpl extends GrpcMessageServiceGrpc.GrpcMessageServiceImplBase {

    private final RedisService redisService;

    @Override
    public void subscribeChat(ChatSubscribeRequest request, StreamObserver<ChatActionResponse> responseObserver) {
        log.debug("SubscribeChat request: {}", request);

        RealTimeData realTimeData = redisService.findRealTimeData(request.getUserId());
        RealTimeData savedRealTimeData = null;
        if (realTimeData != null) { // 오늘 접속 이력이 있는 경우
            realTimeData.setRoomId(request.getRoomId());
            realTimeData.setSessionId(request.getSession());
            savedRealTimeData = redisService.saveRealTimeDataAndSession(realTimeData);
        } else { // null, 오늘 접속 이력이 없는 경우
            realTimeData = makeRealTimeData(request);
            savedRealTimeData = redisService.saveRealTimeDataAndSession(realTimeData);
        }


        ChatActionResponse response = ChatActionResponse.newBuilder()
                .setUserId(savedRealTimeData.getUserId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void unsubscribeChat(ChatUnsubscribeRequest request, StreamObserver<ChatActionResponse> responseObserver) {
        log.debug("UnsubscribeChat request: {}", request);

        String userId = redisService.findUserIdBySessionId(request.getSession());
        if (userId != null) {
            // 최신 버전으로 수정 요망
            redisService.deleteRealTimeDataAndSession(userId, request.getSession());
        } else {
            // 미접속자의 접속 해제 -> 예외 처리
        }
        ChatActionResponse response = ChatActionResponse.newBuilder()
                .setUserId(userId)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private RealTimeData makeRealTimeData(ChatSubscribeRequest chatRequest) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(chatRequest.getUserId());
        realTimeData.setRoomId(chatRequest.getRoomId());
        realTimeData.setSessionId(chatRequest.getSession());
        return realTimeData;
    }
}
