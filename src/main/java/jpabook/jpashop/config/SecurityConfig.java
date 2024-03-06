package jpabook.jpashop.config;

import jpabook.jpashop.config.auth.PrincipalDetailsService;
import jpabook.jpashop.config.oauth.PrincipalOauth2UserService;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFailureHandler customFailureHandler;
    private final PrincipalOauth2UserService principalOauth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(CsrfConfigurer::disable);
        //접근 권한 설정
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/orders/{orderId}/cancel","/orders").hasAnyRole("ADMIN","MANAGER")
                .anyRequest().permitAll());
        
        // 로그인 설정
        http.formLogin(customizer -> customizer
                .loginPage("/members/login").loginProcessingUrl("/members/login")
                .failureHandler(customFailureHandler).defaultSuccessUrl("/"));

        // 소셜 로그인 설정
        http.oauth2Login(oauth2Customizer ->
                oauth2Customizer.loginPage("/members/login")
                        .userInfoEndpoint(userInfoEndpointCustomize->
                                userInfoEndpointCustomize.userService(principalOauth2UserService)));
        
        // 권한 예외 설정
        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                httpSecurityExceptionHandlingConfigurer.accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/403.html");
                }));
        return http.build();
    }

}
