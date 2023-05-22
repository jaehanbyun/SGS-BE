package pnu.cse.studyhub.auth.service;


import org.springframework.stereotype.Service;
import pnu.cse.studyhub.auth.dto.ResponseCodeDto;
import pnu.cse.studyhub.auth.dto.ResponseDataDto;

@Service
public class ResponseService {

    public ResponseCodeDto successResponse(ResponseCodeDto dto) {
        // if (!check) ValidationExceptionProvider.throwValidError(errors);

        return dto;
    }

    public ResponseDataDto successDataResponse(ResponseDataDto dto) {
        // if (!check) ValidationExceptionProvider.throwValidError(errors);

        return dto;
    }
}
