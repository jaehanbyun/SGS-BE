package pnu.cse.studyhub.auth.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.studyhub.auth.config.JwtTokenProvider;
import pnu.cse.studyhub.auth.config.TCPMessageService;
import pnu.cse.studyhub.auth.dto.*;
import pnu.cse.studyhub.auth.exception.CustomException;
import pnu.cse.studyhub.auth.exception.CustomExceptionStatus;
import pnu.cse.studyhub.auth.model.User;
import pnu.cse.studyhub.auth.model.UserAccount;
import pnu.cse.studyhub.auth.repository.AccountRepository;
import pnu.cse.studyhub.auth.repository.StudyTimeRepository;
import pnu.cse.studyhub.auth.util.ByteArrayToStringConverter;
import pnu.cse.studyhub.auth.util.JsonConverter;

import javax.servlet.http.Cookie;
import javax.validation.constraints.Email;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SignService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountRepository accountRepository;
    private final StudyTimeRepository studyTimeRepository;
    private final EmailService emailService;
    private final TCPMessageService tcpMessageService;
    private final ByteArrayToStringConverter byteArrayToStringConverter;
    private final S3Uploader s3Uploader;
    private final JsonConverter jsonConverter;

    private long refreshTime = 14 * 24 * 60 * 60 * 1000L;

    @Transactional
    public ResponseCodeDto checkUserid(String userid) {
        UserAccount exist = accountRepository.findByUserid(userid);

        if (exist != null)
            throw new CustomException(CustomExceptionStatus.DUPLICATED_USERID, "AUTH-004", "이미 존재하는 아이디입니다.");

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage("회원가입 가능한 아이디 입니다.");
        response.setResult("SUCCESS");
        response.setMessage("Valid Userid");
        response.setData(successCode);
        return response;
    }

    @Transactional
    public ResponseCodeDto signUp(AccountDto dto) {
        UserAccount exist = accountRepository.findByUserid(dto.getId());

        if (exist != null)
            throw new CustomException(CustomExceptionStatus.DUPLICATED_USERID, "AUTH-001", "이미 존재하는 아이디입니다.");

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        UserAccount userAccount = UserAccount.createAccount(dto);
        accountRepository.save(userAccount);

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage("회원가입에 성공하였습니다.");
        response.setResult("SUCCESS");
        response.setMessage("Create Account Successfully");
        response.setData(successCode);
        return response;
    }

    @Transactional
    public ResponseEntity<ResponseDataDto> signIn(SignInRequestDto request) {
        UserAccount account = accountRepository.findByUserid(request.getId());

        if (account == null)
            throw new CustomException(CustomExceptionStatus.USERID_NOT_FOUND, "AUTH-002", "존재하지 않는 아이디 입니다.");
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new CustomException(CustomExceptionStatus.WRONG_PASSWORD, "AUTH-002", "잘못된 비밀번호 입니다.");
        }

        String refreshToken = jwtTokenProvider.CreateRefreshToken(account.getEmail(),account.getUserid());
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        // expires in 1 day
            .maxAge(24*60*60)
            .secure(true)
            .httpOnly(true)
            .path("/")
            .build();

        ResponseDataDto<Object> response = new ResponseDataDto<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("refreshToken", cookie.toString());
        response.setResult("SUCCESS");
        response.setMessage("SignIn Successfully");
        SignInResponseDto res = SignInResponseDto.builder()
                .id(account.getUserid())
                .email(account.getEmail())
                .accessToken(jwtTokenProvider.createToken(account.getEmail(),account.getUserid()))
                .build();
        response.setData(res);


        return new ResponseEntity<ResponseDataDto>(response, headers, HttpStatus.valueOf(200));
    }

    @Transactional
    public ResponseCodeDto sendEmail(@Email String email, String type) {
        if (email == null) {
            throw new CustomException(CustomExceptionStatus.EMPTY_EMAIL, "AUTH-003", "이메일이 입력되지 않았습니다.");
        }
        if (!type.equals("sign") && !type.equals("edit")) {
            throw new CustomException(CustomExceptionStatus.INVALID_PARAM, "AUTH-003", "올바르지 않은 인자입니다..");
        }
        UserAccount exist = accountRepository.findByEmail(email);

        if (exist != null && type.equals("sign")) {
            throw new CustomException(CustomExceptionStatus.DUPLICATED_EMAIL, "AUTH-003", "이미 등록된 이메일 입니다.");
        }
        else if (exist == null && type.equals("edit")) {
            throw new CustomException(CustomExceptionStatus.EMAIL_NOT_FOUND, "AUTH-003", "등록되지 않은 이메일 입니다.");
        }

        String regexPattern = "^(.+)@(\\S+)$";
        if(!Pattern.compile(regexPattern).matcher(email).matches()) {
            throw new CustomException(CustomExceptionStatus.INVALID_EMAIL,"AUTH-003", "잘못된 이메일 입니다.");
        }

        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 6;
        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        emailService.sendMail(email,"회웝가입 인증이메일", "다음 인증코드를 입력해주세요: " + generatedString);

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage(generatedString);
        response.setResult("SUCCESS");
        response.setMessage("Email Send Successfully");
        response.setData(successCode);

        return response;
    }

    @Transactional
    public ResponseCodeDto getUserid(String email) {
        UserAccount exist = accountRepository.findByEmail(email);

        if (exist == null)
            throw new CustomException(CustomExceptionStatus.ACCOUNT_NOT_FOUND,"AUTH-006", "아이디를 찾을 수 없습니다.");

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage(exist.getUserid());
        response.setResult("SUCCESS");
        response.setMessage("Get Userid Successfully");
        response.setData(successCode);
        return response;
    }

    @Transactional
    public ResponseCodeDto editPassword(AccountDto dto) {
        UserAccount exist = accountRepository.findByEmail(dto.getEmail());

        if (exist == null)
            throw new CustomException(CustomExceptionStatus.ACCOUNT_NOT_FOUND,"AUTH-007", "아이디를 찾을 수 없습니다.");
        if (!dto.getId().equals(exist.getUserid()))
            throw new CustomException(CustomExceptionStatus.WRONG_ID, "AUTH-007", "아이디가 일치하지 않습니다.");

        exist.setPassword(passwordEncoder.encode(dto.getPassword()));
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        accountRepository.save(exist);

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage("Edit Password Successfully");
        response.setResult("SUCCESS");
        response.setMessage("Request Done Successfully");
        response.setData(successCode);

        return response;
    }

    @Transactional
    public ResponseProfileDto getProfile(String id) {
        UserAccount exist = accountRepository.findByUserid(id);

        if (exist == null)
            throw new CustomException(CustomExceptionStatus.ACCOUNT_NOT_FOUND,"AUTH-008", "아이디를 찾을 수 없습니다.");

        ResponseProfileDto response = new ResponseProfileDto();
        ProfileDto profile = new ProfileDto();

        profile.setId(id);
        profile.setName(exist.getName());
        profile.setEmail(exist.getEmail());
        profile.setProfileImage(exist.getProfileImage());
        profile.setDescription(exist.getDescription());
//        profile.setStudyTime("01:00:00");
        profile.setUrl(exist.getUrl());
        profile.setStudyTime(userStudyTimeFromTCP(id));
        response.setResult("SUCCESS");
        response.setMessage("Get Profile Successfully");
        response.setData(profile);
        return response;
    }

    @Transactional
    public ResponseStudyTimeDto getStudyMonth(String id, String month) {
        List<User> exist = studyTimeRepository.findByUseridAndMonth(id, month);

        if (exist == null)
            throw new CustomException(CustomExceptionStatus.ACCOUNT_NOT_FOUND,"AUTH-008", "아이디를 찾을 수 없습니다.");

        ResponseStudyTimeDto response = new ResponseStudyTimeDto();
        StudyTimeDto studyTime = new StudyTimeDto();

        int sec = 0;
        int minute = 0;
        int hour = 0;

        for (int i=0; i<exist.size();i++) {
            String temp = exist.get(i).getStudyTime();
            hour += Integer.parseInt(temp.substring(0,3));
            minute += Integer.parseInt(temp.substring(4,6));
            sec += Integer.parseInt(temp.substring(6,8));
        }
        if (sec > 59) {
            minute = minute + (sec / 60);
            sec = sec % 60;
        }
        if (minute > 59) {
            hour = hour + (minute / 60);
            minute = minute % 60;
        }
        String studyTimeStr = String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(sec);

        studyTime.setUserid(id);
        studyTime.setDate(month);
        studyTime.setStudyTime(studyTimeStr);
        response.setResult("SUCCESS");
        response.setMessage("Get StudyTime Successfully");
        response.setData(studyTime);
        return response;
    }

    @Transactional
    public ResponseStudyTimeDto getStudyDay(String id, String day) {
        String month = day.substring(0, 7);
        String daySubStr = day.substring(8,10);
        List<User> exist = studyTimeRepository.findByUseridAndMonthAndDay(id, month, daySubStr);

        if (exist == null)
            throw new CustomException(CustomExceptionStatus.ACCOUNT_NOT_FOUND,"AUTH-008", "아이디를 찾을 수 없습니다.");

        ResponseStudyTimeDto response = new ResponseStudyTimeDto();
        StudyTimeDto studyTime = new StudyTimeDto();

        int sec = 0;
        int minute = 0;
        int hour = 0;

        for (int i=0; i<exist.size();i++) {
            String temp = exist.get(i).getStudyTime();
            hour += Integer.parseInt(temp.substring(0,3));
            minute += Integer.parseInt(temp.substring(4,6));
            sec += Integer.parseInt(temp.substring(6,8));
        }
        if (sec > 59) {
            minute = minute + (sec / 60);
            sec = sec % 60;
        }
        if (minute > 59) {
            hour = hour + (minute / 60);
            minute = minute % 60;
        }
        String studyTimeStr = String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(sec);

        studyTime.setUserid(id);
        studyTime.setDate(day);
        studyTime.setStudyTime(studyTimeStr);
        response.setResult("SUCCESS");
        response.setMessage("Get StudyTime Successfully");
        response.setData(studyTime);
        return response;
    }

    @Transactional
    public ResponseCodeDto modifyProfile(ProfileDto dto) {
        UserAccount exist = accountRepository.findByUserid(dto.getId());
        String msg = "Edit ";

        if (exist == null)
            throw new CustomException(CustomExceptionStatus.ACCOUNT_NOT_FOUND,"AUTH-009", "아이디를 찾을 수 없습니다.");
        if (dto.getName() != null) {
            exist.editName(dto.getName());
            msg += "name ";
        }
        if (dto.getProfileImage() != null) {
            try {
                String base64Profile = dto.getProfileImage();
                String profileUri = s3Uploader.base64ImageUpload(base64Profile,dto.getId());
                exist.editProfileImage(profileUri);
            } catch (IOException e) {
                throw new CustomException(CustomExceptionStatus.INCORRECT_IMAGE_FORMAT,"AUTH-011", "프로필 이미지 형식이 올바르지 않습니다.");
            }
            msg += "profileImage ";
        }
        if (dto.getDescription() != null) {
            exist.editDescription(dto.getDescription());
            msg += "description ";
        }
        if (dto.getUrl() != null) {
            exist.editUrl(dto.getUrl());
            msg += "url ";
        }
        msg += "Successfully";

        accountRepository.save(exist);

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage(msg);
        response.setResult("SUCCESS");
        response.setMessage("Modify User Profile Successfully");
        response.setData(successCode);

        return response;
    }

    private String userStudyTimeFromTCP(String userId) {
        String tcpMessage;
        // TCP 서버로 userId보내고 userId랑 studyTime을 response로 받음
        tcpMessage = TCPUserRequest.builder()
                .server("auth")
                .type("STUDY_TIME_FROM_TCP")
                .userId(userId)
                .build().toString();
        String response = tcpMessageService.sendMessage(tcpMessage);
        String stringObj = byteArrayToStringConverter.convert(response);
        TCPUserResponse obj = jsonConverter.convertFromJson(stringObj,TCPUserResponse.class);
        log.debug(obj.toString());
        log.info("[tcp from state] request {} user's studyTime {}",userId, obj.getStudyTime());
        return obj.getStudyTime();

    }

    @Transactional
    public ResponseEntity<ResponseDataDto> checkRefresh(String refreshToken) {

        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(CustomExceptionStatus.USERID_NOT_FOUND, "AUTH-002", "존재하지 않는 아이디 입니다.");
        }

        String userid = jwtTokenProvider.getUserid(refreshToken);

        UserAccount account = accountRepository.findByUserid(userid);

        if (account == null)
            throw new CustomException(CustomExceptionStatus.USERID_NOT_FOUND, "AUTH-002", "존재하지 않는 아이디 입니다.");


        String newRefreshToken = jwtTokenProvider.CreateRefreshToken(account.getEmail(), account.getUserid());
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                // expires in 1 day
                .maxAge(24*60*60)
                .secure(true)
                .httpOnly(true)
                .path("/")
                .build();

        log.error("check header  {}", cookie.toString());

        ResponseDataDto<Object> response = new ResponseDataDto<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("refreshToken", cookie.toString());
        response.setResult("SUCCESS");
        response.setMessage("Update Access Token Successfully");
        SignInResponseDto res = SignInResponseDto.builder()
                .id(account.getUserid())
                .email(account.getEmail())
                .accessToken(jwtTokenProvider.createToken(account.getEmail(),account.getUserid()))
                .build();
        response.setData(res);
        log.error("check header sec  {}", headers.toString());
        log.error("check header sec  {}", new ResponseEntity<ResponseDataDto>(response, headers, HttpStatus.valueOf(200)).toString());

        return new ResponseEntity<ResponseDataDto>(response, headers, HttpStatus.valueOf(200));
    }

    @Transactional
    public String testSaveStudytime() {
        LocalDateTime localDateTime = LocalDateTime.now(); // Or any other LocalDateTime
        Date now = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
        Date date = new Date(now.getTime() - (1000 * 60 * 60 * 24));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatedDate = dateFormat.format(date);
        log.info("time test month ={} day = {}",formatedDate.substring(0,7), formatedDate.substring(8,10));
        String month = formatedDate.substring(0,7);
        String day = formatedDate.substring(8,10);
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setId("testId");
        userInfoDto.setStudyTime("test time");

        userInfoDto.setMonth(month);
        userInfoDto.setDay(day);

        User user = User.createInfo(userInfoDto);
        studyTimeRepository.save(user);
        return "Good";
    }

}
