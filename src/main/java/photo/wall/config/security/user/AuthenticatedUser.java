package photo.wall.config.security.user;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import photo.wall.model.Authority;
import photo.wall.model.User;

public class AuthenticatedUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Boolean enabled;

    public AuthenticatedUser(Long id, String username, String password, String email,
            Collection<? extends GrantedAuthority> authorities, Boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                mapToGrantedAuthorities(user.getAuthorities()),
                user.getEnabled()
        );
    }

    private static Set<GrantedAuthority> mapToGrantedAuthorities(Set<Authority> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getRole().name()))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
