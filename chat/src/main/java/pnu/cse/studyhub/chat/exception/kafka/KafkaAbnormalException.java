package pnu.cse.studyhub.chat.exception.kafka;

public class KafkaAbnormalException extends RuntimeException{
    public KafkaAbnormalException(String msg) {
        super(msg);
    }
}
