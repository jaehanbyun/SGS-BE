package pnu.cse.studyhub.signaling.config;

import lombok.RequiredArgsConstructor;
import org.kurento.client.KurentoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KurentoConfig {

    @Value("${kurento.client.server.ws_uri}")
    private String kurentoServerUri;


//    @Bean
//    public KurentoClient kurentoClient() {
//        KurentoClientConfig kurentoClientConfig = new KurentoClientConfig.Builder()
//                .addIceCandidateServers(iceServers)
//                .build();
//        return KurentoClient.create(kurentoServerUri, kurentoClientConfig);
//    }

    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create();
    }

}