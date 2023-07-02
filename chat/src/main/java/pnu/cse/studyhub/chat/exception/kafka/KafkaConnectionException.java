package pnu.cse.studyhub.chat.exception.kafka;

public class KafkaConnectionException extends RuntimeException{
    public KafkaConnectionException(String msg) {
        super(msg);
    }
}
