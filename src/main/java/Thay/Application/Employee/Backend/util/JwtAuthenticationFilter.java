package Thay.Application.Employee.Backend.util;

import Thay.Application.Employee.Backend.entity.dto.AuthRequest; // Import your existing AuthRequest class
import Thay.Application.Employee.Backend.service.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;


    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/auth/login"); // Set the login endpoint
    }

    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // Map request JSON to an AuthRequest object
            AuthRequest authRequest = new ObjectMapper().readValue(request.getInputStream(), AuthRequest.class);

            // Create an authentication token
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());

            // Authenticate the user
            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse authentication request", e);
        }
    }



    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        CustomUserDetails customUserDetails  = (CustomUserDetails) authResult.getPrincipal();
        String token = jwtUtil.generateToken(
                customUserDetails.getUsername(),
                customUserDetails.getId(),
                customUserDetails.getRole()
        );
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        new ObjectMapper().writeValue(response.getWriter(), tokenMap);
    }
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Invalid email or password");
        new ObjectMapper().writeValue(response.getWriter(), errorMap);
    }
}
