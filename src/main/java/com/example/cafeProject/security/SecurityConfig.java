package com.example.cafeProject.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .httpBasic(AbstractHttpConfigurer::disable)

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/logout")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )

                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스
                        .requestMatchers(
                                "/",
                                "/css/**", "/js/**", "/assets/**",
                                "/attach/summernote/**", "/favicon.ico",
                                "/noLogin/login",
                                "/member/login", "/member/login/error"
                        ).permitAll()

                        // ✅ ADMIN만 접근 가능
                        // .requestMatchers("/operationBoard/**").hasAnyRole("ADMIN", "MANAGER")

                        // ✅ 로그인한 사용자만
//                        .requestMatchers("/operationBoard/**").authenticated()
                        .requestMatchers("/noticeBoard/update/**").authenticated()
                        .requestMatchers("/noticeBoard/create/**").authenticated()


                        // 나머지는 전부 허용
                        .anyRequest().permitAll()
                )

                .exceptionHandling(e -> e
                        // 🔥 USER가 접근하면 메인으로
                        .accessDeniedHandler((request, response, ex) -> {
                            response.sendRedirect("/healthCafe");
                        })
                )

                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/member/login/error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }


    //    //import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//    //import org.springframework.security.crypto.password.PasswordEncoder;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
