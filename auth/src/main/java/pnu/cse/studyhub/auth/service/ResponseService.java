package pnu.cse.studyhub.auth.service;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.auth.dto.ResponseCodeDto;
import pnu.cse.studyhub.auth.dto.ResponseDataDto;
import pnu.cse.studyhub.auth.dto.ResponseProfileDto;
import pnu.cse.studyhub.auth.dto.ResponseStudyTimeDto;

@Service
public class ResponseService {

    public ResponseCodeDto successResponse(ResponseCodeDto dto) {

        return dto;
    }

    public ResponseEntity<ResponseDataDto> successDataResponse(ResponseEntity<ResponseDataDto> dto) {

        return dto;
    }

    public ResponseProfileDto successProfileResponse(ResponseProfileDto dto) {

        return dto;
    }

    public ResponseStudyTimeDto successStudyTimeResponse(ResponseStudyTimeDto dto) {

        return dto;
    }
}
