package pnu.cse.studyhub.room.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pnu.cse.studyhub.room.dto.request.*;
import pnu.cse.studyhub.room.dto.response.*;

import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.service.RoomService;


import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/room/group")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;


    // 스터디방 생성
    @PostMapping
    public Response<RoomIdResponse> create(HttpServletRequest jwt, @RequestBody RoomCreateRequest request){


        String userId = (String) jwt.getAttribute("userId");

        Long roomId = roomService.create(request.isRoomType(), request.getRoomName(),
                userId, request.getMaxUser(), request.getRoomChannel());

        if(request.isRoomType()) {
            return Response.success("Create Open Study Room Successfully", RoomIdResponse.fromRoomId(roomId));
        }else{
            return Response.success("Create Private Study Group Successfully", RoomIdResponse.fromRoomId(roomId));
        }
    }

    // 스터디방 수정
    @PutMapping
    public Response<RoomIdResponse> modify(HttpServletRequest jwt,@RequestBody RoomModifyRequest request){

        String userId = (String) jwt.getAttribute("userId");

        Long Id = roomService.modify(request.isRoomType(), request.getRoomId()
                , userId, request.getRoomName(),request.getMaxUser(), request.getRoomChannel());

        return Response.success("Modify Study Room Successfully",RoomIdResponse.fromRoomId(Id));
    }


    // 스터디방 전체 조회 + 검색 기능
    @GetMapping
    public Response<List<RoomListResponse>> list(
             @RequestParam Long lastRoomId
            ,@RequestParam(required = false) String title
            ,@RequestParam(required = false) RoomChannel channel){

        // size에 따라 가져오는 방의 갯수가 달라짐
        return Response.success("room list successfully",
                roomService.roomList(lastRoomId, 15, title, channel ));
    }

    // 공개 스터디방 상세 정보 조회
    @GetMapping("/{roomId}")
    public Response<OpenDetailResponse> roomInfo(@PathVariable Long roomId){

        OpenDetailResponse info = roomService.info(roomId);

        return Response.success("Query info of the room Successfully",info);

    }


    // 공개 스터디방 입장
    @PostMapping("/in")
    public Response<OpenDetailResponse> in(HttpServletRequest jwt, @RequestBody RoomIdRequest request){

        String userId = (String) jwt.getAttribute("userId");

        OpenDetailResponse info =roomService.in(request.getRoomId(),userId);

        return Response.success("Enter Room Successfully",info);
    }

    // 공지사항 설정
    @PatchMapping
    public Response<NoticeResponse> notice(HttpServletRequest jwt,@RequestBody NoticeRequest request){

        String userId = (String) jwt.getAttribute("userId");

        String notice = roomService.notice(request.getRoomType(), request.getRoomId(), userId, request.getRoomNotice());

        return Response.success("Set Notice Successfully", new NoticeResponse(request.getRoomId(),notice));
    }

    // 경고 기능
    @PatchMapping("/alert")
    public Response<AlertResponse> alert(HttpServletRequest jwt,@RequestBody TargetRequest request){

        String userId = (String) jwt.getAttribute("userId");

        AlertResponse alert = roomService.alert(request.getRoomType(), request.getRoomId(), userId, request.getTargetId());

        return Response.success("Alert the user of the room Successfully",alert);
    }


    // 추방 기능
    @PatchMapping("/kickout")
    public Response<RoomTargetResponse> kickOut(HttpServletRequest jwt, @RequestBody TargetRequest request){

        String userId = (String) jwt.getAttribute("userId");

        RoomTargetResponse kickOut = roomService.kickout(request.getRoomType(), request.getRoomId(), userId, request.getTargetId());

        return Response.success("Kick out the user of the room Successfully",kickOut);
    }


    @PatchMapping("/delegate")
    public Response<RoomTargetResponse> delegate(HttpServletRequest jwt,@RequestBody TargetRequest request){

        String userId = (String) jwt.getAttribute("userId");

        RoomTargetResponse delegate = roomService.delegate(request.getRoomType(), request.getRoomId(), userId, request.getTargetId());

        return Response.success("Delegate the chief of room Successfully",delegate);

    }

    // private 스터디 그룹 조회
    @GetMapping("/private")
    public Response<List<StudyGroupListResponse>> studyGroupList(HttpServletRequest jwt){

        String userId = (String) jwt.getAttribute("userId");

        return Response.success("study group list successfully",roomService.studyGroupList(userId));
    }

    // Private 스터디 그룹 가입
    // roomId랑 room_code

    @PostMapping("/private")
    public Response<Void> join(HttpServletRequest jwt,@RequestBody JoinRequest request){
        String userId = (String) jwt.getAttribute("userId");

        roomService.join(userId,request.getRoomCode());

        return Response.success("Join Study Group Successfully");
    }

    // private 스터디 그룹 해체/탈퇴
    @DeleteMapping("/private/{roomId}")
    public Response<Void> withdraw(HttpServletRequest jwt,@PathVariable Long roomId){
        String userId = (String) jwt.getAttribute("userId");

        roomService.withdraw(userId, roomId);

        return Response.success("Withdraw Room Successfully");
    }

    // 스터디 그룹 방 입장
    @PostMapping("/private/in")
    public Response<PrivateDetailResponse> privateIn(HttpServletRequest jwt,@RequestBody RoomIdRequest request){
        String userId = (String) jwt.getAttribute("userId");

        PrivateDetailResponse info = roomService.privateIn(request.getRoomId(),userId);

        return Response.success("Enter Study Group Successfully", info);
    }

    // 코드 생성
    // 일단은 저장되어 있는 UUID 불러오는 방식
    // 이후에 새로운 거 generator
    //
    @PatchMapping("/private")
    public Response<RoomCodeResponse> generateCode(HttpServletRequest jwt,@RequestBody RoomIdRequest request){
        String userId = (String) jwt.getAttribute("userId");

        UUID roomCode = roomService.generateCode(request.getRoomId(), userId);

        return Response.success("Get RoomCode Successfully",new RoomCodeResponse(roomCode));
    }



}