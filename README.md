# StudyHub - Backend Repo 
### WebRTC 기반 스터디 웹 애플리케이션
#### 팀원 : [이제호(BE)](https://github.com/jhl8109)&ensp; [김돈우(BE)](https://github.com/kimdonwoo)&ensp; [박재현(BE)](https://github.com/wogusqkr0515)&ensp; [변재한(인프라)](https://github.com/jaehanbyun)

### 목차
[1. 목표](#목표)<br>
[2. 주요 기능](#주요-기능)<br>
[3. 사용 기술](#사용-기술)<br>
[4. 아키텍처](#아키텍처)<br>
[5. DB 스키마](#db-스키마)<br>
[6. 상세 기능](#상세-기능)<br>
[7. 서버별 주요 코드](#서버별-주요-코드)<br>
[8. 서버별 주요 고려사항](#서버별-주요-고려사항)<br>
[9. Docs](#docs)<br>

## 목표
원격, 실시간으로 스터디에 참여할 수 있도록하여 학습동기를 부여한다. 또한, 이를 지원하기위해 WebRTC를 활용하여 화면 공유, 자료 공유 등을 제공하며, 채팅, 일정 관리 기능을 통해 효율성을 높인다.
<br>

## 주요 기능
1. 스터디 그룹 형성: 사용자들이 특정 주제나 과목에 관심을 가지고 함께 스터디 그룹을 형성할 수 있다.
2. 실시간 모임: 원격으로 스터디 그룹 멤버들이 스터디방을 생성하고 참여할 수 있다.
3. 자료 공유: 사용자들은 공부 자료, 노트, 문제 해결 방법 등을 공유하고 함께 작업할 수 있다.
4. 화면 공유: 화면 공유 기능을 통해 다른 사용자들에게 자신의 화면을 보여주고 설명할 수 있다.
5. 채팅: 실시간 채팅 기능을 통해 멤버들은 토론하거나 질문을 주고받을 수 있다.
6. 캘린더 및 시간 관리: 개인 학습 일정 또는 개인 학습 시간을 기록할 수 있다.
<br>

## 사용 기술
- 백엔드 : Spring Boot, Spring Cloud(Eureka, Gateway)
- 데이터베이스 : MySQL, MongoDB
- 메시지 브로커 : Kafka
- 캐시 : Redis
- 배포 : Docker, AWS EC2, AWS RDS, AWS S3, Mongo Atlas, Git Action
<br>

## 아키텍처
|상세 아키텍처|배포 아키텍처|
|---|---|
|<img alt="스크린샷 2023-04-09 오전 1 36 21" src="https://user-images.githubusercontent.com/78259314/230732623-49a5034e-20e2-4d3d-8ca1-0b6f5128feef.png">|<img alt="스크린샷 2023-04-09 오전 1 35 53" src="https://user-images.githubusercontent.com/78259314/230732608-1d69e8c8-2004-4750-8688-2c7278cfb779.png">|
<br>

## DB 스키마
![image](https://user-images.githubusercontent.com/78259314/230767396-092a899d-4bc3-4761-83c0-972e235c9b54.png)
<br>

## 상세 기능
|채팅|상태관리|
|---|---|
|<img src=https://github.com/jaehanbyun/SGS-BE/assets/78259314/76a6324e-62cb-44cc-95e3-8d42310471e3>|<img src=https://github.com/jaehanbyun/SGS-BE/assets/78259314/585aadbb-02c3-492c-8329-551c845b4398>|
<br>

## 서버별 주요 코드
- [채팅 서버](https://github.com/jaehanbyun/SGS-BE/tree/main/chat)
- [상태관리 서버](https://github.com/jaehanbyun/SGS-BE/tree/main/state)
<br>

## 배포
|서비스|URL|포트 풀|
|---|---|---|
|전체(Gateway)|http://${AWS-public-IP}|:8000|
|채팅|http://${AWS-public-IP}|:8031~8039|
|상태관리|http://${AWS-public-IP}|:8091~8099|

<br>

## 서버별 주요 고려사항
### 게이트웨이
---
### 채팅 서버
--
### 상태관리 서버

<br>

## Docs
- [API명세](https://thrilling-detail-a35.notion.site/API-ae7df35b9aaf4c629b1290c9a4f779d0?pvs=4)








