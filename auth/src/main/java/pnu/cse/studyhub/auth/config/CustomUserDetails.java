package pnu.cse.studyhub.auth.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pnu.cse.studyhub.auth.model.UserAccount;

import java.util.ArrayList;
import java.util.Collection;

//public class CustomUserDetails implements UserDetails {
//
//    private UserAccount account;
//
////    public UserAccount getAccount() {
////        return account;
////    }
////
////    public CustomUserDetails(UserAccount account) {
////        this.account = account;
////    }
////
////    @Override
////    public Collection<? extends GrantedAuthority> getAuthorities() {
////        Collection<GrantedAuthority> collect = new ArrayList<>();
////        collect.add(new SimpleGrantedAuthority(account.getRoleType().toString()));
////        return collect;
////    }
//
//    @Override
//    public String getPassword() {
//        return this.account.getPassword();
//    }
//
//    @Override
//    public String getUsername() {
//        return this.account.getEmail();
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}
