package pnu.cse.studyhub.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.auth.dto.StudyTimeDto;
import pnu.cse.studyhub.auth.dto.TCPUserSchedulingRequest;
import pnu.cse.studyhub.auth.dto.UserInfoDto;
import pnu.cse.studyhub.auth.model.User;
import pnu.cse.studyhub.auth.model.UserAccount;
import pnu.cse.studyhub.auth.repository.StudyTimeRepository;
import pnu.cse.studyhub.auth.util.JsonConverter;

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
    private final StudyTimeRepository studyTimeRepository;

    private final JsonConverter jsonConverter;
    public String processMessage(String message) {
        // String 형태로 받은 message
        log.info("Received message: {}", message);

        try {
            JSONObject json = jsonConverter.stringToJson(message);
            log.info("Check user list = {}", json.toJSONString());

            JSONArray userArray = (JSONArray) json.get("users");
            log.info("Check for JSONArray users = {}", userArray.toJSONString());

            for (int i = 0; i < userArray.size(); i++) {
                JSONObject jo = (JSONObject) userArray.get(i);

                LocalDateTime localDateTime = LocalDateTime.now(); // Or any other LocalDateTime
                Date now = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
                Date date = new Date(now.getTime() - (1000 * 60 * 60 * 24));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formatedDate = dateFormat.format(date);
                log.info("time test month ={} day = {}", formatedDate.substring(0, 7), formatedDate.substring(8, 10));
                String month = formatedDate.substring(0, 7);
                String day = formatedDate.substring(8, 10);
                String studyTime = jo.getAsString("study_time");
                if(studyTime.equals(null) || studyTime == null) {
                    studyTime = "00:00:00";
                }
                UserInfoDto userInfoDto = new UserInfoDto(jo.getAsString("user_id"), month, day, studyTime);
                User user = User.createInfo(userInfoDto);
                studyTimeRepository.save(user);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        return message;
    }
}
