package pnu.cse.studyhub.chat.exception;

public class InvalidMessageFormatException extends RuntimeException{
    public InvalidMessageFormatException(String msg) {
        super(msg);
    }
}
