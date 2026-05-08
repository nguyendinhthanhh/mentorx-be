package com.mentorx.api.common.security;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isEnabled = user.getStatus() == com.mentorx.api.common.enums.UserStatus.ACTIVE || 
                           user.getStatus() == com.mentorx.api.common.enums.UserStatus.PENDING;

        List<SimpleGrantedAuthority> authorities = userRoleRepository.findByUserIdWithRole(user.getId())
                .stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleName()))
                .toList();

        if (authorities.isEmpty()) {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!isEnabled)
                .build();
    }
}
