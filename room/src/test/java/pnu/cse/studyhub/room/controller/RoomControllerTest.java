package pnu.cse.studyhub.room.controller;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.studyhub.room.dto.response.DetailResponse;
import pnu.cse.studyhub.room.dto.response.OpenDetailResponse;
import pnu.cse.studyhub.room.dto.response.PrivateDetailResponse;
import pnu.cse.studyhub.room.dto.response.RoomListResponse;
import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.UserRoomId;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;
import pnu.cse.studyhub.room.model.entity.OpenUserRoomEntity;
import pnu.cse.studyhub.room.model.entity.PrivateRoomEntity;
import pnu.cse.studyhub.room.model.entity.PrivateUserRoomEntity;
import pnu.cse.studyhub.room.repository.OpenRoomRepository;
import pnu.cse.studyhub.room.repository.PrivateRoomRepository;
import pnu.cse.studyhub.room.repository.PrivateUserRoomRepository;
import pnu.cse.studyhub.room.repository.UserRoomRepository;
import pnu.cse.studyhub.room.service.RoomService;
import pnu.cse.studyhub.room.service.exception.ApplicationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RoomControllerTest {
    // TODO : 일단 경고, 강퇴, 방장 위임 등은 나중에

    @Autowired
    RoomService roomService;

    @Autowired
    OpenRoomRepository openRoomRepository;

    @Autowired
    PrivateRoomRepository privateRoomRepository;

    @Autowired
    UserRoomRepository userRoomRepository;

    @Autowired
    PrivateUserRoomRepository privateUserRoomRepository;

    @Test
    public void 공개방_생성() {
        // 공개방 생성
        Long newOpenRoomId = roomService.create(true, "test Open Room", "donu", 10, RoomChannel.BUSINESS);

        // 생성한 스터디 그룹
        Optional<OpenRoomEntity> openRoom = openRoomRepository.findById(newOpenRoomId);

        String roomOwnerId = userRoomRepository.findRoomOwnerByRoomId(newOpenRoomId).getUserId();


        assertEquals(openRoom.get().getRoomId(), newOpenRoomId);
        assertEquals(openRoom.get().getRoomName(), "test Open Room");
        assertEquals(roomOwnerId, "donu");
        assertEquals(openRoom.get().getCurUser(), 1);
        assertEquals(openRoom.get().getMaxUser(), 10);
        assertEquals(openRoom.get().getChannel(), RoomChannel.BUSINESS);
    }

    /**
     * strategy = GenerationType.SEQUENCE를 사용할 때
     * allocationSize로 성능 향상 가능
     * allocationSize가 1일 떄 : 9156ms
     * allocationSize가 50일 떄 : 3907ms
     */

    @Test
    public void 공개방_생성_allocationsize_테스트() {

        long startTime = System.currentTimeMillis();

        // 공개방 생성
        for (int i = 0; i < 100; i++) {
            Long newOpenRoomId = roomService.create(true, "test Open Room", "donu", 10, RoomChannel.BUSINESS);
            Long newStudyGroupId = roomService.create(false, "test Study Group", "donu" + i, 10, null);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("실행 시간: " + elapsedTime + "ms");

    }


    @Test
    public void 스터디_그룹_생성() {
        // 스터디 그룹 생성
        Long newStudyGroupId = roomService.create(false, "test Study Group", "donu", 10, null);

        // 생성한 스터디 그룹
        Optional<PrivateRoomEntity> newStudyGroup = privateRoomRepository.findById(newStudyGroupId);

        assertEquals(newStudyGroup.get().getRoomId(), newStudyGroupId);
        assertEquals(newStudyGroup.get().getRoomName(), "test Study Group");
        assertEquals(newStudyGroup.get().getMaxUser(), 10);
    }

    @Test
    public void 공개방_수정() {
        // 스터디 그룹 생성
        Long newOpenRoomId = roomService.create(true, "test Open Room", "donu", 10, RoomChannel.BUSINESS);

        // 생성한 스터디 그룹
        Optional<OpenRoomEntity> openRoom = openRoomRepository.findById(newOpenRoomId);

        assertEquals(openRoom.get().getRoomId(), newOpenRoomId);
        assertEquals(openRoom.get().getRoomName(), "test Open Room");
        assertEquals(openRoom.get().getMaxUser(), 10);
        assertEquals(openRoom.get().getChannel(), RoomChannel.BUSINESS);

        Long modifiedOpenRoomId = roomService.modify(true, newOpenRoomId, "donu", "test Open Room"
                , 10, RoomChannel.BUSINESS, "test room Notice");

        Optional<OpenRoomEntity> modifiedOpenRoom = openRoomRepository.findById(modifiedOpenRoomId);

        assertEquals(modifiedOpenRoom.get().getRoomId(), modifiedOpenRoomId);
        assertEquals(modifiedOpenRoom.get().getRoomName(), "test Open Room");
        assertEquals(modifiedOpenRoom.get().getMaxUser(), 10);
        assertEquals(modifiedOpenRoom.get().getChannel(), RoomChannel.BUSINESS);
        assertEquals(modifiedOpenRoom.get().getRoomNotice(), "test room Notice");


    }

    @Test
    public void 스터디_그룹_수정() {
        // 스터디 그룹 생성
        Long newStudyGroupId = roomService.create(false, "test Study Group", "donu", 10, null);

        // 생성한 스터디 그룹
        Optional<PrivateRoomEntity> newStudyGroup = privateRoomRepository.findById(newStudyGroupId);

        assertEquals(newStudyGroup.get().getRoomId(), newStudyGroupId);
        // TODO : 방장 닉네임 비교!!
        assertEquals(newStudyGroup.get().getRoomName(), "test Study Group");
        assertEquals(newStudyGroup.get().getMaxUser(), 10);

        Long modifiedOpenRoomId = roomService.modify(false, newStudyGroupId, "donu", "test Study Group"
                , 10, null, "test Study Group Notice");

        Optional<PrivateRoomEntity> modifiedStudyGroup = privateRoomRepository.findById(modifiedOpenRoomId);

        assertEquals(modifiedStudyGroup.get().getRoomId(), modifiedOpenRoomId);
        assertEquals(modifiedStudyGroup.get().getRoomName(), "test Study Group");
        assertEquals(modifiedStudyGroup.get().getMaxUser(), 10);
        assertEquals(modifiedStudyGroup.get().getRoomNotice(), "test Study Group Notice");
    }


    @Test
    public void 공개방_조회_검색기능() {

        // 공개방 생성
        for (int i = 0; i < 50; i++) {
            roomService.create(true, "test Open Room1", "donu", 10, RoomChannel.BUSINESS);
            roomService.create(true, "test Open Room2", "donu", 10, RoomChannel.HIGH_SCHOOL);
            roomService.create(true, "test Open Room3", "donu", 10, RoomChannel.ELEMENTARY_SCHOOL);
        }

        List<RoomListResponse> roomListResponses = roomService.roomList(10000L, 200, null, null);
        List<RoomListResponse> roomListResponses1 = roomService.roomList(10000L, 200, "Room1", null);
        List<RoomListResponse> roomListResponses2 = roomService.roomList(10000L, 200, "Room", null);
        List<RoomListResponse> roomListResponses3 = roomService.roomList(10000L, 200, null, RoomChannel.ELEMENTARY_SCHOOL);

        assertEquals(roomListResponses.size(), 150);
        assertEquals(roomListResponses1.size(), 50);
        assertEquals(roomListResponses2.size(), 150);
        assertEquals(roomListResponses3.size(), 50);

    }

    @Test
    public void 공개방_상세정보_조회() {

        Long roomId = roomService.create(true, "test Open Room1", "donu", 10, RoomChannel.BUSINESS);

        Optional<OpenRoomEntity> openRoom = openRoomRepository.findById(roomId);

        //info(String userId, Long roomId , Boolean roomType)
        OpenDetailResponse info = (OpenDetailResponse) roomService.info("donu",roomId,true);

        assertEquals(info.getRoomId(), roomId);
        assertEquals(info.getCurUser(), 1);
        assertEquals(info.getRoomName(), "test Open Room1");
        assertNull(info.getRoomNotice());
        assertEquals(info.getMaxUser(), 10);
        assertEquals(info.getChannel(), RoomChannel.BUSINESS);

        // 공지사항 설정
        roomService.modify(true, roomId, "donu", "test Open Room"
                , 10, RoomChannel.BUSINESS, "test room Notice");

        assertEquals(openRoom.get().getRoomId(), roomId);
        assertEquals(openRoom.get().getCurUser(), 1);
        assertEquals(openRoom.get().getRoomName(), "test Open Room");
        assertEquals(openRoom.get().getRoomNotice(), "test room Notice");
        assertEquals(openRoom.get().getMaxUser(), 10);
        assertEquals(openRoom.get().getChannel(), RoomChannel.BUSINESS);
    }



    @Test
    public void 공개_스터디방_입장() {

        Long roomId = roomService.create(true, "test Open Room1", "donu", 10, RoomChannel.BUSINESS);

        Optional<OpenRoomEntity> openRoom = openRoomRepository.findById(roomId);

//        Optional<OpenUserRoomEntity> userRoom = userRoomRepository.findById(new UserRoomId("donu", roomId));

        roomService.in(roomId, "donu1");

        assertEquals(openRoom.get().getCurUser(), 2);
        assertNotNull(userRoomRepository.findById(new UserRoomId("donu1", roomId)));
        assertTrue(userRoomRepository.findById(new UserRoomId("donu", roomId)).get().isRoomOwner());

    }

    @Test
    public void 스터디그룹_조회() {

        for (int i = 0; i < 4; i++) {
            roomService.create(false, "test Study Group", "donu", 10, null);
        }

        List<PrivateUserRoomEntity> studyGroupList = privateUserRoomRepository.findIsMemberByUserId("donu");

        assertEquals(studyGroupList.size(), 4);

    }

    @Test
    public void 스터디_그룹_상세정보_조회() {
        Long newStudyGroupId = roomService.create(false, "test Study Group", "donu", 10, null);

        Optional<PrivateRoomEntity> privateRoom = privateRoomRepository.findById(newStudyGroupId);

        PrivateDetailResponse info = (PrivateDetailResponse) roomService.info("donu", newStudyGroupId, false);

        assertEquals(info.getRoomId(), newStudyGroupId);
        assertEquals(info.getCurUser(), 1);
        assertEquals(info.getRoomName(), "test Study Group");
        assertNull(info.getRoomNotice());
        assertEquals(info.getMaxUser(), 10);
        assertEquals(info.getRoomOwner(),"donu");

        System.out.println("상세정보 : " + info.toString());

        // 공지사항 설정
        roomService.modify(false, newStudyGroupId, "donu", "test Study Group"
                , 10, null, "test room Notice");

        assertEquals(privateRoom.get().getRoomId(), newStudyGroupId);
        assertEquals(privateRoom.get().getCurUser(), 1);
        assertEquals(privateRoom.get().getRoomName(), "test Study Group");
        assertEquals(privateRoom.get().getRoomNotice(), "test room Notice");
        assertEquals(privateRoom.get().getMaxUser(), 10);

        PrivateDetailResponse info2 = (PrivateDetailResponse) roomService.info("donu", newStudyGroupId, false);



        System.out.println("상세정보 : " + info2.toString());


    }

    @Test
    public void 스터디그룹_코드생성과_가입() {

        // 스터디 그룹 생성
        Long newStudyGroupId = roomService.create(false, "test Study Group", "donu", 10, null);

        // 스터디 그룹 코드 생성
        UUID roomCode = roomService.generateCode(newStudyGroupId, "donu");

        // 해당 코드로 스터디 그룹 가입
        roomService.join("donu2", roomCode);

        System.out.println(newStudyGroupId);
        System.out.println("roomCode는" + roomCode);

    }

    // 에러 발생
    @Test
    public void 스터디그룹_코드생성_이미_가입한_유저() {

        // 스터디 그룹 생성
        Long newStudyGroupId = roomService.create(false, "test Study Group", "donu", 10, RoomChannel.BUSINESS);

        // 스터디 그룹 코드 생성
        UUID roomCode = roomService.generateCode(newStudyGroupId, "donu");

        // 해당 코드로 스터디 그룹 가입
        try {
            roomService.join("donu2", roomCode);
            roomService.join("donu2", roomCode);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(newStudyGroupId);
        System.out.println("roomCode는" + roomCode);
    }


    @Test
    public void 스터디그룹_탈퇴() {
        // 스터디 그룹 생성
        Long newStudyGroupId = roomService.create(false, "test Study Group", "donu", 10, RoomChannel.BUSINESS);

        // 스터디 그룹 코드 생성
        UUID roomCode = roomService.generateCode(newStudyGroupId, "donu");

        // 해당 코드로 스터디 그룹 가입
        roomService.join("donu2", roomCode);

        // 스터디 그룹 탈퇴
        roomService.withdraw("donu2",newStudyGroupId);

        Optional<PrivateUserRoomEntity> userRoom = privateUserRoomRepository.findById(new UserRoomId("donu2", newStudyGroupId));

        assertEquals(privateRoomRepository.findById(newStudyGroupId).get().getCurUser(),1);
        assertFalse(userRoom.get().isMember());


    }

//    @Test
//    public void 스터디그룹_입장() {
//
//
//
//
//
//    }


}