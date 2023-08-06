package kr.co.mgv.user.service;

import kr.co.mgv.user.mapper.AuthenticationDao;
import kr.co.mgv.user.mapper.UserRoleDao;
import kr.co.mgv.user.vo.User;
import kr.co.mgv.user.vo.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationService implements UserDetailsService {

    private final AuthenticationDao authenticationDao;
    private final UserRoleDao userRoleDao;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        User user = authenticationDao.getUserById(id);
        return user;
    }

    public User getUserByEmail(String email) {
        return authenticationDao.getUserByEmail(email);
    }

}
