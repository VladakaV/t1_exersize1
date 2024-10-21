package ru.t1.java.demo.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.t1.java.demo.service.impl.UserDetailsServiceImpl;
import ru.t1.java.demo.util.AuthEntryPointJwt;
import ru.t1.java.demo.util.AuthTokenFilter;
import ru.t1.java.demo.util.JwtUtils;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig { // extends WebSecurityConfigurerAdapter { // Spring Boot 2
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils;
    private final AuthEntryPointJwt unauthorizedHandler;

    // Spring Boot 2

//  @Override
//  public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
//    authenticationManagerBuilder.userDetailsService(userDetailsService)
//    .passwordEncoder(passwordEncoder());
//  }

    //  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http.cors().and().csrf().disable()
//      .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
//      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//      .authorizeRequests().antMatchers("/api/auth/**").permitAll()
//      .antMatchers("/api/test/**").permitAll()
//      .anyRequest().authenticated();
//
//    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//  }

//  @Bean
//  @Override
//  public AuthenticationManager authenticationManagerBean() throws Exception {
//    return super.authenticationManagerBean();
//  }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                                auth.requestMatchers("/api/auth/**").anonymous()
                                        .requestMatchers("/parse/**").permitAll()
                                        .requestMatchers("/parse").permitAll()
                                        .requestMatchers("/actuator/prometheus").anonymous()
                                        .requestMatchers("/actuator/*").anonymous()
                                        .anyRequest().permitAll()
                );

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
