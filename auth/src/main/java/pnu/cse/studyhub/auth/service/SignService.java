package pnu.cse.studyhub.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.studyhub.auth.config.JwtTokenProvider;
import pnu.cse.studyhub.auth.dto.*;
import pnu.cse.studyhub.auth.exception.CustomException;
import pnu.cse.studyhub.auth.exception.CustomExceptionStatus;
import pnu.cse.studyhub.auth.model.UserAccount;
import pnu.cse.studyhub.auth.repository.AccountRepository;

import javax.servlet.http.Cookie;
import javax.validation.constraints.Email;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    private long refreshTime = 14 * 24 * 60 * 60 * 1000L;

    @Transactional
    public ResponseCodeDto checkUserid(String userid) {
        UserAccount exist = accountRepository.findByUserid(userid);

        if (exist != null)
            throw new CustomException(CustomExceptionStatus.DUPLICATED_USERID, "이미 존재하는 아이디입니다.");

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage("회원가입 가능한 아이디 입니다.");
        response.setSuccess("SUCCESS");
        response.setMessage("Valid Userid");
        response.setSuccessCode(successCode);
        return response;
    }

    @Transactional
    public ResponseCodeDto signUp(AccountDto dto) {
        UserAccount exist = accountRepository.findByUserid(dto.getId());

        if (exist != null)
            throw new CustomException(CustomExceptionStatus.DUPLICATED_USERID, "이미 존재하는 아이디입니다.");

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        UserAccount userAccount = UserAccount.createAccount(dto);
        accountRepository.save(userAccount);

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage("회원가입에 성공하였습니다.");
        response.setSuccess("SUCCESS");
        response.setMessage("Create Account Successfully");
        response.setSuccessCode(successCode);
        return response;
    }

    @Transactional
    public ResponseEntity<ResponseDataDto> signIn(SignInRequestDto request) {
        UserAccount account = accountRepository.findByUserid(request.getId());

        if (account == null)
            throw new CustomException(CustomExceptionStatus.USERID_NOT_FOUND, "존재하지 않는 아이디 입니다.");
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new CustomException(CustomExceptionStatus.WRONG_PASSWORD, "잘못된 비밀번호 입니다.");
        }

        String refreshToken = jwtTokenProvider.CreateRefreshToken(account.getUserid());
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
        response.setSuccess("SUCCESS");
        response.setMessage("SignIn Successfully");
        SignInResponseDto res = SignInResponseDto.builder()
                .id(account.getUserid())
                .email(account.getEmail())
                .accessToken(jwtTokenProvider.createToken(account.getEmail(),account.getUserid()))
                .build();
        response.setAccountInfo(res);



        return new ResponseEntity<ResponseDataDto>(response, headers, HttpStatus.valueOf(200));
    }

    @Transactional
    public ResponseCodeDto sendEmail(@Email String email) {
        UserAccount exist = accountRepository.findByEmail(email);

        if (exist != null) {
            throw new CustomException(CustomExceptionStatus.DUPLICATED_EMAIL, "이미 등록된 이메일 입니다.");
        }

        if (email == null) {
            throw new CustomException(CustomExceptionStatus.EMPTY_EMAIL, "이메일이 입력되지 않았습니다.");
        }

        String regexPattern = "^(.+)@(\\S+)$";
        if(!Pattern.compile(regexPattern).matcher(email).matches()) {
            throw new CustomException(CustomExceptionStatus.INVALID_EMAIL, "잘못된 이메일 입니다.");
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
        response.setSuccess("SUCCESS");
        response.setMessage("Email Send Successfully");
        response.setSuccessCode(successCode);

        return response;
    }

    @Transactional
    public ResponseCodeDto getUserid(String email) {
        UserAccount exist = accountRepository.findByEmail(email);

        if (exist == null)
            throw new CustomException(CustomExceptionStatus.ACCOUNT_NOT_FOUND, "아이디를 찾을 수 없습니다.");

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage(exist.getUserid());
        response.setSuccess("SUCCESS");
        response.setMessage("Get Userid Successfully");
        response.setSuccessCode(successCode);
        return response;
    }

    @Transactional
    public ResponseCodeDto editPassword(AccountDto dto) {
        UserAccount exist = accountRepository.findByEmail(dto.getEmail());

        if (exist == null)
            throw new CustomException(CustomExceptionStatus.ACCOUNT_NOT_FOUND, "아이디를 찾을 수 없습니다.");
//        if (!dto.getId().equals(exist.getUserid()))
//            throw new CustomException(CustomExceptionStatus.INVALID_ID, "아이디가 일치하지 않습니다.");

        exist.setPassword(passwordEncoder.encode(dto.getPassword()));
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        accountRepository.save(exist);

        ResponseCodeDto<Object> response = new ResponseCodeDto<>();
        SuccessCodeDto successCode = new SuccessCodeDto();

        successCode.setIsSuccess(true);
        successCode.setCode("1000");
        successCode.setMessage("Edit Password Successfully");
        response.setSuccess("SUCCESS");
        response.setMessage("Request Done Successfully");
        response.setSuccessCode(successCode);

        return response;
    }

}
