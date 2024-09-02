package com.gateway.limoapi.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gateway.limoapi.model.LoginUser;
import com.gateway.limoapi.repository.LoginRepository;

@Service
public class MyUserDetailService implements UserDetailsService{
	
	@Autowired 
	LoginRepository loginRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println(username);
		Optional<LoginUser> user = loginRepository.findByUsername(username);
		user.orElseThrow(() -> new UsernameNotFoundException(username));
		return user.map(MyUserDetails::new).get();
	}

}
