package pnu.cse.studyhub.state.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import pnu.cse.studyhub.state.service.GrpcMessageServiceImpl;
import pnu.cse.studyhub.state.service.RedisService;

@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig implements ApplicationRunner {
    private final RedisService redisService;
    private static final int PORT = 8095;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Server server = ServerBuilder.forPort(PORT)
                .addService(new GrpcMessageServiceImpl(redisService))
                .build();

        server.start();
    }
}
