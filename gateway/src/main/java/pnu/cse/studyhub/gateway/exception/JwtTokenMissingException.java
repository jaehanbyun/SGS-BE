package pnu.cse.studyhub.gateway.exception;

import javax.naming.AuthenticationException;
public class JwtTokenMissingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JwtTokenMissingException(String msg) {
        super (msg);
    }
}