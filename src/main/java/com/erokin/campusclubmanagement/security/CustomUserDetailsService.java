package com.erokin.campusclubmanagement.security;

import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =
                userRepository
                        .findByEmailIgnoreCase(username)
                        .or(() -> userRepository.findByStudentNumber(username))
                        .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        return UserPrincipal.fromUser(user);
    }
}

