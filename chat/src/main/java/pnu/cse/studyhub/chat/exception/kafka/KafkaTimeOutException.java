package pnu.cse.studyhub.chat.exception.kafka;

public class KafkaTimeOutException extends RuntimeException{
    public KafkaTimeOutException(String msg) {
        super(msg);
    }
}
