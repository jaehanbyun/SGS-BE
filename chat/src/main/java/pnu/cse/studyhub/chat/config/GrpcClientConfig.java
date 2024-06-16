package pnu.cse.studyhub.chat.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pnu.cse.studyhub.state.service.GrpcMessageServiceGrpc;

@Configuration
public class GrpcClientConfig {
    @Value("${grpc.server.port}")
    private int port;
    @Value("${grpc.server.host}")
    private String host;
    @Bean
    public ManagedChannel GrpcServerChannel() {
        return ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();
    }
    @Bean
    public GrpcMessageServiceGrpc.GrpcMessageServiceBlockingStub GrpcStub(ManagedChannel grpcServerChannel) {
        GrpcMessageServiceGrpc.GrpcMessageServiceBlockingStub stub = GrpcMessageServiceGrpc.newBlockingStub(grpcServerChannel);
        return stub;
    }
}
