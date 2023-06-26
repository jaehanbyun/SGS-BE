package pnu.cse.studyhub.state;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pnu.cse.studyhub.state.dto.UserDto;
import pnu.cse.studyhub.state.dto.request.receive.TCPSignalingReceiveRequest;
import pnu.cse.studyhub.state.dto.request.receive.TCPSignalingReceiveSchedulingRequest;
import pnu.cse.studyhub.state.dto.response.receive.TCPSignalingReceiveResponse;
import pnu.cse.studyhub.state.service.MessageService;
import pnu.cse.studyhub.state.service.RedisService;
import pnu.cse.studyhub.state.util.JsonConverter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageServiceIntegrationTest {
    @Autowired
    private MessageService messageService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private JsonConverter jsonConverter;
    public static TCPSignalingReceiveRequest createRequest(String server,String type, String userId) {
        TCPSignalingReceiveRequest request = new TCPSignalingReceiveRequest();
        request.setServer(server);
        request.setType(type);
        request.setUserId(userId);
        return request;
    }
    public static TCPSignalingReceiveRequest createRequest(String server, String type, String userId, String studyTime) {
        TCPSignalingReceiveRequest request = new TCPSignalingReceiveRequest();
        request.setServer(server);
        request.setType(type);
        request.setUserId(userId);
        request.setStudyTime(studyTime);
        return request;
    }
    public static TCPSignalingReceiveSchedulingRequest createRequest(String server, String type, List<UserDto> users) {
        TCPSignalingReceiveSchedulingRequest request = new TCPSignalingReceiveSchedulingRequest();
        request.setServer(server);
        request.setType(type);
        request.setUsers(users);
        return request;
    }


    @Test
    @DisplayName("시그널링 서버 공부 시간 조회 테스트")
    public void testSignalingServerGetStudyTimeMessage() {
        try {
            TCPSignalingReceiveRequest expectedRequest = createRequest("signaling","STUDY_TIME_FROM_TCP","test");
            String message = jsonConverter.convertToJson(expectedRequest);
            String messageResponse = messageService.processMessage(message);
            TCPSignalingReceiveResponse actualRequest = jsonConverter.convertFromJson(messageResponse, TCPSignalingReceiveResponse.class);

            System.out.println("Expected Message: " + expectedRequest);
            System.out.println("Actual Message: " + actualRequest);

            assertEquals(expectedRequest.getUserId(), actualRequest.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    @DisplayName("시그널링 서버 공부 시간 갱신 테스트")
    public void testSignalingServerSetStudyTimeMessage() {
        try {
            TCPSignalingReceiveRequest expectedRequest = createRequest("signaling","STUDY_TIME_TO_TCP","test", "02:00:00");
            String message = jsonConverter.convertToJson(expectedRequest);
            String messageResponse = messageService.processMessage(message);
            TCPSignalingReceiveResponse actualRequest = jsonConverter.convertFromJson(messageResponse, TCPSignalingReceiveResponse.class);

            System.out.println("Expected Message: " + expectedRequest);
            System.out.println("Actual Message: " + actualRequest);

            assertEquals(expectedRequest.getUserId(),actualRequest.getUserId());
            assertEquals(expectedRequest.getStudyTime(),actualRequest.getStudyTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    @DisplayName("시그널링 서버 스케줄링 테스트")
    public void testSignalingServerSendSchedulingMessage() {
        try {
            List<UserDto> users = new ArrayList<>();
            users.add(new UserDto("test1","01:00:00"));
            users.add(new UserDto("test2","02:00:00"));

            TCPSignalingReceiveSchedulingRequest expectedRequest = createRequest("signaling","SCHEDULED",users);
            String message = jsonConverter.convertToJson(expectedRequest);
            String messageResponse = messageService.processMessage(message);
            TCPSignalingReceiveSchedulingRequest actualRequest = jsonConverter.convertFromJson(messageResponse, TCPSignalingReceiveSchedulingRequest.class);

            System.out.println("Expected Message: " + expectedRequest);
            System.out.println("Actual Message: " + actualRequest);

            assertEquals(expectedRequest,actualRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    @DisplayName("시그널링 서버 스케줄링 전송 완료 테스트")
    public void testSignalingServerSendSchedulingLastMessage() {
        try {
            List<UserDto> users = new ArrayList<>();
            users.add(new UserDto("test1","01:00:00"));
            users.add(new UserDto("test2","02:00:00"));

            TCPSignalingReceiveSchedulingRequest expectedRequest = createRequest("signaling","SCHEDULED_LAST",users);
            String message = jsonConverter.convertToJson(expectedRequest);
            String messageResponse = messageService.processMessage(message);
            TCPSignalingReceiveSchedulingRequest actualRequest = jsonConverter.convertFromJson(messageResponse, TCPSignalingReceiveSchedulingRequest.class);

            System.out.println("Expected Message: " + expectedRequest);
            System.out.println("Actual Message: " + actualRequest);

            assertEquals(expectedRequest,actualRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
