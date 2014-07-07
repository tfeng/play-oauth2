package security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SimpleUserDetailsService implements UserDetailsService {

  private static class SimpleUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private String password;
    private String username;

    public SimpleUserDetails(String username, String password) {
      this.username = username;
      this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
      return password;
    }

    @Override
    public String getUsername() {
      return username;
    }

    @Override
    public boolean isAccountNonExpired() {
      return true;
    }

    @Override
    public boolean isAccountNonLocked() {
      return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }
  }

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Value("${security.user.password}")
  private String password;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return new SimpleUserDetails(username, passwordEncoder.encode(password));
  }
}
