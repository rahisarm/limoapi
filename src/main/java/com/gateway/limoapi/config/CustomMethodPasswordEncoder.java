package com.gateway.limoapi.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.gateway.limoapi.helpers.ClsEncrypt;

public class CustomMethodPasswordEncoder implements PasswordEncoder {

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		ClsEncrypt ClsEncrypt=new ClsEncrypt();
		String password=ClsEncrypt.getInstance().encrypt(rawPassword.toString());
		System.out.println(rawPassword+"  -  "+encodedPassword);
		return password.toString().equals(encodedPassword);
	}

	@Override
	public String encode(CharSequence rawPassword) {
		// TODO Auto-generated method stub
		return null;
	}

}
