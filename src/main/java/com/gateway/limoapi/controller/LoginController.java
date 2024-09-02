package com.gateway.limoapi.controller;

import com.gateway.limoapi.helpers.ClsEncrypt;
import com.gateway.limoapi.model.LoginUser;
import com.gateway.limoapi.model.MiscModel;
import com.gateway.limoapi.repository.LoginRepository;
import com.gateway.limoapi.service.LoginService;
import com.gateway.limoapi.service.PrintService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api")
public class LoginController {

	@Autowired
	LoginService service;
	
	@Autowired 
	PrintService printService;
	
    @Autowired
    private LoginRepository loginrepo;
    ClsEncrypt cryptdao=new ClsEncrypt();
    @PostMapping("/loginUser")
    @ResponseBody
    public List<LoginUser> loginUser(@RequestParam Map<String,String> queryparams){
        System.out.println(queryparams.get("username"));
        String encryptpassword=cryptdao.encrypt(queryparams.get("password"));
        return loginrepo.findUserByUsernameAndPassword(queryparams.get("username"),encryptpassword);
    }

    @GetMapping("/testCode")
    @ResponseBody
    public void testCode() {
    	System.out.println("Test");
    }
}
