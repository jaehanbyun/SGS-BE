package pnu.cse.studyhub.signaling.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.signaling.config.tcp.TCPMessageService;

import pnu.cse.studyhub.signaling.util.UserRegistry;
import pnu.cse.studyhub.signaling.util.UserSession;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import static pnu.cse.studyhub.signaling.util.Message.resetStudyTimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final UserRegistry userRegistry;
    private final TCPMessageService tcpMessageService;

    //@Scheduled(cron = "0 0 5 * * *")
    @Scheduled(cron = "0 */5 * * * *")
    public void run() throws IOException {
        log.info("[05:00] Scheduling Start !!");

        int countInTCP = 0;
        int nowUserCount = 0;

        // 현재 접속 되어 있는 모든 유저 객체 들고옴
        Collection<UserSession> users = userRegistry.getAllUsers();
        List<Map<String, Object>> serializedUsers = new ArrayList<>();
        int userSize = users.size();

        for (UserSession user : users) {

            if(user.getTimer()){ // 켜져있다면
                user.setTimer(false); // 끄고 시간 업데이트
                user.countStudyTime(LocalTime.now(),user.getOnTime());
            }

            serializedUsers.add(Map.of(
                    "study_time", user.studyTimeToString(),
                    "user_id", user.getUserId()
            ));

            user.setStudyTime(LocalTime.of(0,0,0));
            user.sendMessage(resetStudyTimeMessage(user.getUserId()));

            countInTCP++;
            nowUserCount++;
            if(nowUserCount == userSize) continue;

            if (countInTCP == 25) {
                studyTimeScheduledTCP(serializedUsers,"SCHEDULER");
                serializedUsers.clear();
                countInTCP = 0;
            }
        }
        if (!serializedUsers.isEmpty()) {
            studyTimeScheduledTCP(serializedUsers,"SCHEDULER_LAST");
        }

        log.info("[05:00] Scheduling End !!");
    }

    private void studyTimeScheduledTCP(List<Map<String, Object>> Users,String Type) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("server", "signaling_scheduling");
        jsonMap.put("type", Type);
        jsonMap.put("users", Users);

        Gson gson = new Gson();
        String json = gson.toJson(jsonMap);

        tcpMessageService.sendMessage(json);
    }


    /*
        아마도 매일 새벽 5시때마다 @Scheduled(cron = "0 0 5 * * *") ㅇ
        즉, 시그널링 -> 상태관리

        현재 시그널링 서버에 존재하는 모든 유저 객체에 대한 공부시간을 상태관리 서버로 보내줌
            근데 여기서 On인 유저에 대해서는 현재시간 기준으로 계산하고 보내줘야함

            보내주고 나서 해당 유저의 타이머를 off하고 타이머 시간을 00:00:00 으로 설정해주기 (확정 x)
            그리고 클라이언트 쪽에 message를 보내면 모든 유저들의 타이머를 off하고 타이머 시간을 00:00:00 으로 설정

        상태관리서버로 TCP 보낼 때, 하나의 TCP 마다 25명씩 담아서 보내기 (성능 개선)
            한번에 보낼 수 있는 TCP 메시지의 크기를 확인 하지만 오류 처리가 힘들듯..? (나중에 생각)

     */
}
