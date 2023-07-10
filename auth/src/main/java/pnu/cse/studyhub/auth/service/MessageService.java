package pnu.cse.studyhub.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.auth.dto.StudyTimeDto;
import pnu.cse.studyhub.auth.dto.TCPUserSchedulingRequest;
import pnu.cse.studyhub.auth.dto.UserInfoDto;
import pnu.cse.studyhub.auth.model.User;
import pnu.cse.studyhub.auth.model.UserAccount;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
//TCP 요청 처리하는 클래스
public class MessageService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public String processMessage(String message) {
        // String 형태로 받은 message
        log.info("Received message: {}", message);

        try {
            // 받은 String message를 TCPUserSchedulingRequest 객체로 변환 (JSON -> Object)
            // ex) {server : state, type : SCEHDULED, users : [{user_id : 1, study_time : 1}]}
            TCPUserSchedulingRequest tcpUserSchedulingRequest = objectMapper.readValue(message, TCPUserSchedulingRequest.class);
            // 유저 리스트 (userID, studyTime), 근데 userId, studyTime, 이런 식으로 보내서 이름이 현재 안맞는듯
            // userInfoDto의 JsonProperty 를 user_id, study_time으로 바꾸던지, 새로 dto를 하나 파던지 해야할듯?
            // 아래 코드는 user 정보 리스트 불러오는거. 여기서 date 추가해서 db에 저장하면 될듯?
            List<UserInfoDto> userInfoDtoList = tcpUserSchedulingRequest.getUsers();
            for(UserInfoDto userInfoDto : userInfoDtoList) {
                String userId = userInfoDto.getId();
                // 아마 date는 null 값이 들어올 것으로 추측
                // date는 DB에 적합한 형태로 바꿔야함. String, Date, LocalDateTime 등
                LocalDateTime localDateTime = LocalDateTime.now(); // Or any other LocalDateTime
                Date now = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formatedDate = dateFormat.format(now);
                log.info("time test month ={} day = {}",formatedDate.substring(0,7), formatedDate.substring(8,10));
                String month = formatedDate.substring(0,7);
                String day = formatedDate.substring(8,10);
                String studyTime = userInfoDto.getStudyTime();

                userInfoDto.setMonth(month);
                userInfoDto.setDay(day);

                User user = User.createInfo(userInfoDto);
            }
            log.info("Parsed message: {}", tcpUserSchedulingRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 그냥 정상적으로 처리 되었음을 알릴만한 메세지 응답해주면 될듯
        // 그냥 아무 메세지 보내도 되고? Success 같은 거나 아니면 tcp 처리 후에 보낼만한 응답이나..
        return message;
    }
}
