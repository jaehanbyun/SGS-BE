package pnu.cse.studyhub.chat.exception.kafka;

public class KafkaInterruptException extends RuntimeException{
    public KafkaInterruptException(String msg) {
        super(msg);
    }
}
