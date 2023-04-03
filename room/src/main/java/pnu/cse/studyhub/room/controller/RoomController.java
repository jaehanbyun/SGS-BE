package pnu.cse.studyhub.room.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pnu.cse.studyhub.room.dto.request.NoticeRequest;
import pnu.cse.studyhub.room.dto.request.RoomCreateRequest;
import pnu.cse.studyhub.room.dto.request.RoomInRequest;
import pnu.cse.studyhub.room.dto.request.RoomModifyRequest;
import pnu.cse.studyhub.room.dto.response.*;

import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.service.RoomService;


import java.util.List;

@RestController
@RequestMapping("/room/group")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 스터디방 생성
    @PostMapping
    public Response<RoomIdResponse> create(@RequestBody RoomCreateRequest request){
        // TODO : 이후에 JWT로부터 userId등 user 정보 받을 예정

        String userId = "donu";

        Long roomId = roomService.create(request.isRoomType(), request.getRoomName(),
                userId, request.getMaxUser(), request.getRoomChannel());

        return Response.success("Create Open Study Room Successfully", RoomIdResponse.fromRoomId(roomId));
    }

    // 스터디방 수정
    @PutMapping
    public Response<RoomIdResponse> modify(@RequestBody RoomModifyRequest request){

        // TODO : JWT로부터 userID등 user 정보 받을 예정

        String userId = "donu";

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
                roomService.roomList(lastRoomId, 20, title, channel ));
    }

    // 공개 스터디방 상세 정보 조회
    @GetMapping("/{roomId}")
    public Response<DetailResponse> roomInfo(@PathVariable Long roomId){

        DetailResponse info = roomService.info(roomId);

        return Response.success("Query info of the room Successfully",info);


    }


    // 공개 스터디방 입장 TODO : 입장시에 response로 해당 방의 정보를 다 줘야하나...??
    @PostMapping("/in")
    public Response<Void> in(@RequestBody RoomInRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = "donu2";

        roomService.in(request.getRoomId(),userId);

        return Response.success("Enter Room Successfully");
    }


    // 공개 스터디방 나가기 (방장이면 방 삭제)
    @DeleteMapping("/out/{roomId}")
    public Response<Void> out(@PathVariable Long roomId){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = "donu2";

        roomService.out(userId, roomId);

        return Response.success("Leave Room Successfully");
    }

    // 공지사항 설정
    @PatchMapping
    public Response<NoticeResponse> notice(@RequestBody NoticeRequest request){
        // TODO : JWT로부터 userID등 user 정보 받을 예정
        String userId = "donu";

        String notice = roomService.notice(request.getRoomType(), request.getRoomId(), userId, request.getRoomNotice());

        return Response.success("Set Notice Successfully", new NoticeResponse(request.getRoomId(),notice));

    }




}

