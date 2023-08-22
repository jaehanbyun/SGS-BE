package pnu.cse.studyhub.chat.service;

import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import pnu.cse.studyhub.state.service.ChatActionResponse;
import pnu.cse.studyhub.state.service.ChatSubscribeRequest;
import pnu.cse.studyhub.state.service.ChatUnsubscribeRequest;
import pnu.cse.studyhub.state.service.GrpcMessageServiceGrpc;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcClientService {
    private final GrpcMessageServiceGrpc.GrpcMessageServiceBlockingStub grpcStub;

    public void subscribeRoom(String userId, long roomId, String session) {
        ChatActionResponse response = grpcStub.subscribeChat(
            ChatSubscribeRequest.newBuilder()
                .setUserId(userId)
                .setRoomId(roomId)
                .setSession(session)
                .build()
        );
        log.debug("grpc-subscribe: {}", response);
    };
    public void unsubscribeRoom(String session) {
        ChatActionResponse response = grpcStub.unsubscribeChat(
            ChatUnsubscribeRequest.newBuilder()
                    .setSession(session)
                    .build()
        );
        log.debug("grpc-unsubscribe: {}", response);
    };
}
