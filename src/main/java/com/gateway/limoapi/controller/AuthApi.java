package com.gateway.limoapi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.limoapi.config.AuthRequest;
import com.gateway.limoapi.config.AuthResponse;
import com.gateway.limoapi.config.JwtTokenUtil;
import com.gateway.limoapi.config.MyUserDetails;
import com.gateway.limoapi.helpers.ClsEncrypt;
import com.gateway.limoapi.model.LoginUser;
import com.gateway.limoapi.repository.LoginRepository;
import com.gateway.limoapi.service.LoginService;

@RestController
public class AuthApi {
	@Autowired 
	AuthenticationManager authManager;
    @Autowired 
    JwtTokenUtil jwtUtil;
    
    @Autowired
	LoginService service;
	
    @Autowired
    private LoginRepository loginrepo;
    ClsEncrypt cryptdao=new ClsEncrypt();
     
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
        	System.out.println(request.getUsername() +"   -   "+ request.getPassword());
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword())
            );
            
            MyUserDetails user = (MyUserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateAccessToken(user);
            AuthResponse response = new AuthResponse(user.getUsername(), accessToken);
            Map<String, Object> responseBody = new HashMap<>();
            String encryptpassword=cryptdao.encrypt(request.getPassword());
            List<LoginUser> userData =  loginrepo.findUserByUsernameAndPassword(request.getUsername(),encryptpassword);
            userData.stream().forEach(System.out::println);            
            responseBody.put("authresponse", response);
            responseBody.put("userdata", userData);
            return ResponseEntity.ok().body(responseBody);
             
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
