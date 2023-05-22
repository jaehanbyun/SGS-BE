package pnu.cse.studyhub.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.auth.model.UserAccount;
import pnu.cse.studyhub.auth.repository.AccountRepository;

//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailService implements UserDetailsService {
//
//    private final AccountRepository accountRepository;
//
////    @Override
////    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
////        UserAccount account = accountRepository.findByUserid(userid);
////        if (account == null) throw new RuntimeException("아이디를 찾을수 없습니다.");
////        return new CustomUserDetails(account.getAccount());
////    }
//}