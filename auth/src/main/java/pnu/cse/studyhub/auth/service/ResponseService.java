package pnu.cse.studyhub.auth.service;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.auth.dto.ResponseCodeDto;
import pnu.cse.studyhub.auth.dto.ResponseDataDto;

@Service
public class ResponseService {

    public ResponseCodeDto successResponse(ResponseCodeDto dto) {
        // if (!check) ValidationExceptionProvider.throwValidError(errors);

        return dto;
    }

    public ResponseEntity<ResponseDataDto> successDataResponse(ResponseEntity<ResponseDataDto> dto) {
        // if (!check) ValidationExceptionProvider.throwValidError(errors);

        return dto;
    }
}
