package pnu.cse.studyhub.chat.exception.kafka;

public class KafkaSerializationException extends RuntimeException{
    public KafkaSerializationException(String msg) {
        super(msg);
    }
}
