package pnu.cse.studyhub.state;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pnu.cse.studyhub.state.config.TCPRoomClientGateway;
import pnu.cse.studyhub.state.config.TCPSignalingClientGateway;
import pnu.cse.studyhub.state.dto.request.send.TCPSignalingSendRequest;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;
import pnu.cse.studyhub.state.service.MessageService;
import pnu.cse.studyhub.state.service.RedisService;

@SpringBootTest
public class TCPClientTest {

    @Autowired
    private TCPRoomClientGateway tcpRoomClientGateway;
    @Autowired
    private TCPSignalingClientGateway tcpSignalingClientGateway;
    @Autowired
    private MessageService messageService;
    @Autowired
    private RedisService redisService;

    @Test
    public void testSendRoomServerRoomOutMessage() {
        RealTimeData rtData = new RealTimeData();
        rtData.setUserId("user123");
        rtData.setRoomId(1L);

        try {
            String messageResponse = messageService.sendRoomServerRoomOutMessage(rtData);
            tcpRoomClientGateway.send(messageResponse);
            System.out.println("Response Message: " + messageResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testSendSignalingServerRoomMessage() {
        TCPSignalingSendRequest testRequest =  TCPSignalingSendRequest.builder()
                .server("state")
                .type("ALERT")
                .userId("jeho")
                .roomId(999L)
                .alertCount(1L)
                .build();
        try {
            String messageResponse = messageService.sendSignalingServerRoomMessage(testRequest);
            tcpSignalingClientGateway.send(messageResponse);
            System.out.println("Response Message: " + messageResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testSamples() {
        RealTimeData rtData = new RealTimeData();
        rtData.setSessionId("abcd");
        rtData.setStudyTime(null);
        rtData.setRoomId(null);
        rtData.setUserId("user123");
        rtData.setRoomId(1L);

        try {
            redisService.saveRealTimeData(rtData);
            System.out.println("Response Message: " + rtData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}