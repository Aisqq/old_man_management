package com.example.filter;

import com.example.dao.UserDao;
import com.example.security.CustomUserDetailsService;
import com.example.utils.JwtTokenUtil;
import com.example.utils.UserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Date;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserDao userDao;

    public JwtAuthorizationFilter(JwtTokenUtil jwtTokenUtil,
                                  CustomUserDetailsService userDetailsService,UserDao userDao) {
        this.userDao = userDao;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        System.out.println("Filter processing1: " + request.getRequestURI());
        Cookie[] cookies = request.getCookies();
        String token = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {

                    token = cookie.getValue();
                    System.out.println("token:"+token);
                    break;
                }
            }
        }

        if (token == null || token.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (jwtTokenUtil.getUsernameFromToken(token) != null) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    UserHolder.saveUser(userDao.findByUsername(username));
                    // 检查 Token 是否需要刷新
                    Date expiration = jwtTokenUtil.getExpirationDateFromToken(token);
                    long remainingTime = expiration.getTime() - System.currentTimeMillis();
                    long refreshThreshold = 15 * 60 * 1000; // 15分钟

                    if (remainingTime < refreshThreshold) {
                        // 生成新 Token
                        String newToken = jwtTokenUtil.generateToken(userDetails);
                        // 通过 Cookie 返回新 Token
                        Cookie newCookie = new Cookie("jwtToken", newToken);
                        newCookie.setHttpOnly(true);
                        newCookie.setSecure(false);
                        newCookie.setPath("/");
                        newCookie.setMaxAge(10);
                        response.addCookie(newCookie);

                    }
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            Cookie cookie = new Cookie("jwtToken", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        chain.doFilter(request, response);
    }
}