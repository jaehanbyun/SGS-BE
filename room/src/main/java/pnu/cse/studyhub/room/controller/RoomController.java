package pnu.cse.studyhub.room.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pnu.cse.studyhub.room.dto.request.OpenRoomListRequest;
import pnu.cse.studyhub.room.dto.request.RoomCreateRequest;
import pnu.cse.studyhub.room.dto.response.Response;
import pnu.cse.studyhub.room.dto.response.RoomIdResponse;
import pnu.cse.studyhub.room.dto.response.RoomListResponse;

import pnu.cse.studyhub.room.exception.ApplicationException;
import pnu.cse.studyhub.room.exception.ErrorCode;
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

//        if(userId.equals("donu")){
//            throw new ApplicationException(ErrorCode.EXCEPTION_TEST,String.format("exception test : %s user not founded", userId));
//        }

        Long roomId = roomService.create(request.isRoomType(), request.getRoomName(),
                userId, request.getMaxUser(), request.getRoomChannel());

        return Response.success("Create Open Study Room Successfully", RoomIdResponse.fromRoomId(roomId));
    }

    // 스터디방 전체 조회 + 검색 기능
    @GetMapping
    public Response<List<RoomListResponse>> list(
            @RequestParam(required = false) String title
            ,@RequestParam(required = false) RoomChannel channel
            ,@RequestBody OpenRoomListRequest request){

        return Response.success("room list successfully~",
                roomService.roomList(request.getLastRoomId(), request.getSize(),title,channel ));
    }




}

