package pnu.cse.studyhub.room.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pnu.cse.studyhub.room.dto.request.*;
import pnu.cse.studyhub.room.dto.response.*;

import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.service.RoomService;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/room/group")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    // TODO : 나머지는 상태관리 서버랑 통신이후에 개발해야할듯..??

    // 스터디방 생성
    @PostMapping
    public Response<RoomIdResponse> create(@RequestBody RoomCreateRequest request){
        // TODO : 이후에 JWT로부터 userId등 user 정보 받을 예정

        String userId = request.getUserId();

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
    public Response<RoomIdResponse> modify(@RequestBody RoomModifyRequest request){

        // TODO : JWT로부터 userID등 user 정보 받을 예정

        String userId = request.getUserId();

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

        // TODO : JWT로부터 userID등 user 정보 받을 예정

        // size에 따라 가져오는 방의 갯수가 달라짐
        return Response.success("room list successfully",
                roomService.roomList(lastRoomId, 20, title, channel ));
    }

    // 공개 스터디방 상세 정보 조회
    @GetMapping("/{roomId}")
    public Response<DetailResponse> roomInfo(@PathVariable Long roomId){
        // TODO : JWT로부터 userID등 user 정보 받을 예정

        DetailResponse info = roomService.info(roomId);

        return Response.success("Query info of the room Successfully",info);


    }


    // 공개 스터디방 입장 TODO : 입장시에 response로 해당 방의 정보를 다 줘야하나...??
    @PostMapping("/in")
    public Response<Void> in(@RequestBody RoomIdRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        roomService.in(request.getRoomId(),userId);

        return Response.success("Enter Room Successfully");
    }


    // 공개 스터디방 나가기 (방장이면 방 삭제)
//    @DeleteMapping("/out/{roomId}")
//    public Response<Void> out(@PathVariable Long roomId){
//        // TODO : JWT로부터 userID등 user 정보 받을 예정
//        String userId = "donu2";
//
//        roomService.out(userId, roomId);
//
//        return Response.success("Leave Room Successfully");
//    }

    // 공지사항 설정
    @PatchMapping
    public Response<NoticeResponse> notice(@RequestBody NoticeRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        String notice = roomService.notice(request.getRoomType(), request.getRoomId(), userId, request.getRoomNotice());

        return Response.success("Set Notice Successfully", new NoticeResponse(request.getRoomId(),notice));
    }

    // 경고 기능
    @PatchMapping("/alert")
    public Response<AlertResponse> alert(@RequestBody TargetRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        AlertResponse alert = roomService.alert(request.getRoomType(), request.getRoomId(), userId, request.getTargetId());

        return Response.success("Alert the user of the room Successfully",alert);
    }


    // 추방 기능
    @PatchMapping("/kickout")
    public Response<RoomTargetResponse> kickOut(@RequestBody TargetRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        RoomTargetResponse kickOut = roomService.kickout(request.getRoomType(), request.getRoomId(), userId, request.getTargetId());

        return Response.success("Kick out the user of the room Successfully",kickOut);
    }


    @PatchMapping("/delegate")
    public Response<RoomTargetResponse> delegate(@RequestBody TargetRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        RoomTargetResponse delegate = roomService.delegate(request.getRoomType(), request.getRoomId(), userId, request.getTargetId());

        return Response.success("Delegate the chief of room Successfully",delegate);

    }

    // Private 스터디 그룹 가입
    // roomId랑 room_code
    // TODO : roomCode에 UUID말고 다른 값이 들어왔을 때 Exception..?
    @PostMapping("/private")
    public Response<Void> join(@RequestBody JoinRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        roomService.join(userId,request.getRoomCode());

        return Response.success("Join Study Group Successfully");
    }

    // private 스터디 그룹 해체/탈퇴
    @DeleteMapping("/private/{roomId}")
    public Response<Void> withdraw(@PathVariable Long roomId, @RequestBody TestUser request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        roomService.withdraw(userId, roomId);

        return Response.success("Withdraw Room Successfully");
    }

    @PostMapping("/private/in")
    public Response<Void> privateIn(@RequestBody RoomIdRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        roomService.privateIn(request.getRoomId(),userId);

        return Response.success("Enter Study Group Successfully");
    }

    // 코드 생성
    // 일단은 저장되어 있는 UUID 불러오는 방식
    // 이후에 새로운 거 generator
    //
    @GetMapping("/private")
    public Response<RoomCodeResponse> generateCode(@RequestBody RoomIdRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = request.getUserId();

        UUID roomCode = roomService.generateCode(request.getRoomId(), userId);

        return Response.success("Get RoomCode Successfully",new RoomCodeResponse(roomCode));
    }



}