package pnu.cse.studyhub.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> {

    private String result;
    private String message;
    private T data;

    public static Response<Void> success(){
        return new Response<Void>("SUCCESS", "성공!!",null);
    }

    public static <T> Response<T> success(String message,T data){
        return new Response<>("SUCCESS", message ,data);
    }

}

// ex)
//      {
//        "result": "SUCCESS",
//        "message": "Create Room Successfully",
//        "data": {
//          "roomId": //일단 Long
//        }
//      }
