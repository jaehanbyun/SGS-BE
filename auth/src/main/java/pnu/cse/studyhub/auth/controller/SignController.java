package pnu.cse.studyhub.auth.controller;


import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pnu.cse.studyhub.auth.dto.*;
import pnu.cse.studyhub.auth.service.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/auth")
public class SignController {

    private final SignService signService;
    private final ResponseService responseService;

    @PostMapping("/check-userid")
    public ResponseCodeDto checkUserid(@RequestParam(value = "id") String userid) {

        return responseService.successResponse(signService.checkUserid(userid));
    }

    @PostMapping("/sign-up")
    public ResponseCodeDto signUp(@RequestBody @Valid AccountDto dto) {

        return responseService.successResponse(signService.signUp(dto));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ResponseDataDto> signIn(@RequestBody @Valid SignInRequestDto request) {

        return responseService.successDataResponse(signService.signIn(request));
    }

    @PostMapping("/send-mail")
    public ResponseCodeDto sendEmail(@RequestParam(value = "email") @Valid String email, @RequestParam(value = "type") String type) {
        return responseService.successResponse(signService.sendEmail(email,type));
    }

    @GetMapping("/get-userid")
    public ResponseCodeDto getUserid(@RequestParam(value = "email") String email) {
        return responseService.successResponse(signService.getUserid(email));
    }

    @PatchMapping("/edit-password")
    public ResponseCodeDto editPassword(@RequestBody @Valid AccountDto dto) {
        return responseService.successResponse(signService.editPassword(dto));
    }

    @GetMapping("/get-profile")
    public ResponseProfileDto getProfile(@RequestHeader(value = "id") String id) {
        return responseService.successProfileResponse(signService.getProfile(id));
    }

    @GetMapping("/get-study-month")
    public ResponseStudyTimeDto getStudyMonth(@RequestHeader(value = "id") String id, @RequestParam(value = "month") String month) {
        return responseService.successStudyTimeResponse(signService.getStudyMonth(id, month));
    }

    @GetMapping("/get-study-month-each")
    public ResponseStudyTimeListDto getStudyMonthEach(@RequestHeader(value = "id") String id, @RequestParam(value = "month") String month) {
        return responseService.successStudyTimeListResponse(signService.getStudyMonthEach(id, month));
    }

    @GetMapping("/get-study-day")
    public ResponseStudyTimeDto getStudyDay(@RequestHeader(value = "id") String id, @RequestParam(value = "day") String day) {
        return responseService.successStudyTimeResponse(signService.getStudyDay(id, day));
    }

    @PatchMapping("/modify-profile")
    public ResponseCodeDto modifyProfile(@RequestBody @Valid ProfileDto dto) {
        return responseService.successResponse(signService.modifyProfile(dto));
    }

    @PostMapping ("/refreshToken")
    public ResponseEntity<ResponseDataDto> checkRefresh(@RequestHeader(value = "refresh_token") String refreshToken) {
        log.info("Check for refresh token in controller = {}", refreshToken);
        return responseService.successDataResponse(signService.checkRefresh(refreshToken));
    }

    @PostMapping ("/logout")
    public ResponseEntity<ResponseDataDto> logout() {

        return responseService.successDataResponse(signService.logout());
    }

    @PostMapping ("/testTime")
    public String testSaveTime() {

        return signService.testSaveStudytime();
    }

}
