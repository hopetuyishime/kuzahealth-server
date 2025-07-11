package rw.ac.auca.kuzahealth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.user.entity.User;
import rw.ac.auca.kuzahealth.core.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // @Override
    // public UserDetails loadUserByUsername(String email) throws
    // UsernameNotFoundException {
    // User user = userRepository.findByEmail(email).orElseThrow(() -> new
    // UsernameNotFoundException("User not found"));;
    // // System.out.println("user"+user);
    // if(Objects.isNull(user)) {
    // System.out.println("User not available");
    // throw new UsernameNotFoundException("User not found");
    // }
    // return new org.springframework.security.core.userdetails.User(
    // user.getEmail(),
    // user.getPassword(),
    // user.getAuthorities()
    // );
    // }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole().name());
    }

    // private Collection<? extends GrantedAuthority> getAuthorities(String role) {
    //     return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    // }
}
