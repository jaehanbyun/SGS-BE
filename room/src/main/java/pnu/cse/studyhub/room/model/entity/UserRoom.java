package pnu.cse.studyhub.room.model.entity;

public interface UserRoom {
    String getUserId();
    Long getRoomId();
    int getAlert();
//    Boolean isKickOut();
    int addAlert();
    void kickOut();

}
