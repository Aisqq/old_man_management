package com.example.filter;

import com.example.utils.UserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UserHolderCleanupFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            if(UserHolder.getUser()!=null){
                System.out.println("请求方法: " + request.getMethod() + ", 路径: " + request.getRequestURI());
                System.out.println(UserHolder.getUser().getId());
                UserHolder.removeUser();
            }
        }
    }
}