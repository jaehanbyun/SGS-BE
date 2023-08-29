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
|전체 아키텍처|배포 아키텍처|
|---|---|
|<img alt="아키텍처" src="https://github.com/jaehanbyun/SGS-BE/assets/78259314/fa19a171-c150-410f-9815-84c43d241192">|<img alt="스크린샷 2023-04-09 오전 1 35 53" src="https://user-images.githubusercontent.com/78259314/230732608-1d69e8c8-2004-4750-8688-2c7278cfb779.png">|
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
- Kafka는 메시지 큐와 같은 역할을 수행한다. 따라서, 채팅 서버가 여러 개 있는 경우 분산되어 요청되는 채팅에 대해서도 원활하게 처리한다.
- STOMP를 사용하였으며 pub/sub구조를 활용한다. 채팅 서버는 이를 통해 subscribe를 intercept하여 구독한 방으로 현재 사용자의 접속 상태 및 접속 위치를 파악한다.
> - 고려한 점
>   - Kafka에 파일, 이미지 등의 blob 데이터가 들어간 경우 채팅 서버 전반적인 성능의 하락이 일어날 수 있다고 생각하였다. (Blob 데이터가 사용자 -> 채팅, 채팅 -> 카프카, 카프카 -> 채팅으로 3회 전달)
>   - 따라서, 이를 극복하기 위해 HTTP 통신으로 채팅 서버에 blob 데이터를 전달하고, 이를 AWS S3로부터 uri로 반환하여, 해당 uri로 kafka에 produce 되도록 구현하였다. (Blob 데이터가 사용자 -> 채팅, 채팅 -> AWS S3로 2회 전달되고 이 후 uri를 반환받아 uri 가 채팅 <-> 카프카로 전달)
---
### 상태관리 서버
- 상태관리 서버는 Cache Storage인 Redis에 실시간 데이터를 저장.
- 여러 서버에서 동시에 Redis에 접근하지 않고 상태관리를 위한 상태관리 서버를 별도로 구현
- 실시간성을 높이기 위해 TCP 통신을 활용
<br>

> - 접속 상태 관리 방법
>   - 사용자 접속 시 스터디방에 들어가지 않더라도 Default(전체) 스터디 방 소켓에 연결
>   - 사용자가 스터디방 들어가면 해당 스터디방 소켓에 연결
>   - 따라서 중복 입장을 막고 항상 소켓을 하나에만 연결하여 소켓 연결 여부(웹소켓 세션)를 통해 접속 상태 관리
> - 이유
>   - 접속 상태를 위해 별개의 소켓을 연결하게 되면 스터디방 접속 시 소켓을 두개 연결해야한다.
>   - 10000명이 접속한다고 치면 10000개의 소켓 연결 대신 20000개(채팅 소켓 + 접속 확인용 소켓)의 소켓을 연결해야하는 셈
>   - 그러나, 제시한 방법의 경우에도 스터디방에 접속하지 않더라도 소켓이 Default 스터디방에 연결되어 있음.
  
<br>

## Docs
- [API명세](https://thrilling-detail-a35.notion.site/API-ae7df35b9aaf4c629b1290c9a4f779d0?pvs=4)








