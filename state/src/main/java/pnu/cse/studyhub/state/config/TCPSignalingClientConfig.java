package pnu.cse.studyhub.state.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.CachingClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class TCPSignalingClientConfig implements ApplicationEventPublisherAware {
    @Value("${tcp.signaling.host}")
    private String host;

    @Value("${tcp.signaling.port}")
    private int port;

    @Value("${tcp.signaling.connection.poolSize}")
    private int connectionPoolSize;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Bean
    public AbstractClientConnectionFactory signalingClientConnectionFactory() {
        TcpNioClientConnectionFactory tcpNioClientConnectionFactory = new TcpNioClientConnectionFactory(host, port);
        tcpNioClientConnectionFactory.setUsingDirectBuffers(true);
        tcpNioClientConnectionFactory.setApplicationEventPublisher(applicationEventPublisher);
        return new CachingClientConnectionFactory(tcpNioClientConnectionFactory, connectionPoolSize);
    }


    @Bean
    public MessageChannel signalingOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "signalingOutboundChannel")
    public MessageHandler signalingOutboundGateway(AbstractClientConnectionFactory signalingClientConnectionFactory) {
        TcpOutboundGateway tcpOutboundGateway = new TcpOutboundGateway();
        tcpOutboundGateway.setConnectionFactory(signalingClientConnectionFactory);
        return tcpOutboundGateway;
    }
}
