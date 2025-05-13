package com.example.config;

import com.example.dao.UserDao;
import com.example.filter.JwtAuthenticationFilter;
import com.example.filter.JwtAuthorizationFilter;
import com.example.filter.UserHolderCleanupFilter;
import com.example.security.CustomUserDetailsService;
import com.example.service.CaptchaService;
import com.example.utils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDao userDao;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CaptchaService captchaService;
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtTokenUtil jwtTokenUtil,
                          AuthenticationConfiguration authenticationConfiguration,UserDao userDao,CaptchaService captchaService) {
        this.captchaService = captchaService;
        this.userDao = userDao;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationConfiguration = authenticationConfiguration;
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://10.168.82.114:800"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/test/*",
                        "/favicon.ico",
                        "/login.html",
                        "/captcha/pic",
                        "/register.html",
                        "/api/user/public/**",
                        "/forgetPassword.html"
                );
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                authenticationManager(), jwtTokenUtil,captchaService);
        JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(
                jwtTokenUtil, userDetailsService,userDao);
        UserHolderCleanupFilter userHolderCleanupFilter =
                new UserHolderCleanupFilter();

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        // 未登录时的处理（返回401或重定向）
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (isAjaxRequest(request)) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
                            } else {
                                response.sendRedirect("/login.html?redirect=" + request.getRequestURI());
                            }
                        })
                        // 已登录但无权限的处理（返回403或重定向）
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (isAjaxRequest(request)) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问");
                            } else {
                                response.sendRedirect("/login.html?denied=true");
                            }
                        })
                )
                // 过滤器的顺序
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthorizationFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(userHolderCleanupFilter, ExceptionTranslationFilter.class) ;

        return http.build();
    }

    // 判断是否是AJAX请求
    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || "application/json".equalsIgnoreCase(request.getHeader("Content-Type"));
    }
}
