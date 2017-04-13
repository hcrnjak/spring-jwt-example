package com.hcrnjak.config.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hcrnjak.config.security.authentication.AuthenticatedUser;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final Logger logger = LogManager.getLogger(JwtAuthenticationTokenFilter.class);

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private JwtTokenHandler jwtTokenHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader(this.tokenHeader);
        logger.info("Checking JWT : " + jwt);

        // User not already authenticated, try to authenticate him through JWT
        if (SecurityContextHolder.getContext().getAuthentication() == null && StringUtils.isNotEmpty(jwt)) {
            if (jwtTokenHandler.isTokenValid(jwt)) {

                // Parse JWT
                AuthenticatedUser user = jwtTokenHandler.parseToken(jwt);

                // If JWT valid, populate SecurityContext with the data
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }


        filterChain.doFilter(request, response);
    }
}
