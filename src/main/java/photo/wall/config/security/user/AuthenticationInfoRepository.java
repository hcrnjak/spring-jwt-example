package photo.wall.config.security.user;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import photo.wall.model.Authority;
import photo.wall.model.User;
import photo.wall.model.enums.Role;

@Service
public class AuthenticationInfoRepository implements UserDetailsService {

    @Override
    public AuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {

        if (StringUtils.equals(username, "user")) {
            User user = new User("user", new BCryptPasswordEncoder().encode("user"), "user@user.user",
                    true, new HashSet<Authority>(Arrays.asList(new Authority(1L, Role.ROLE_ADMIN), new Authority(2L, Role.ROLE_USER))));
            user.setId(1L);

            return  AuthenticatedUser.from(user);
        } else {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        }
    }
}
