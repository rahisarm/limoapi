package com.gateway.limoapi.service;

import com.gateway.limoapi.helpers.ClsEncrypt;
import com.gateway.limoapi.model.ConfigModel;
import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.LoginUser;
import com.gateway.limoapi.model.MiscModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class LoginService {

    @Autowired
    private JdbcTemplate template;

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    public List<LoginUser> loginUser(LoginUser objuser){
        String username= objuser.getUsername();
        String password=objuser.getPassword();
        String strsql="select doc_no,username,coalesce(driverid,0) driverid,password from usersregistration where username='" + username + "' and password='" + password + "' and status=3";
        System.out.println(strsql);
        return  template.query(strsql, new RowMapper<LoginUser>() {
            @Override
            public LoginUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                LoginUser objtemp=new LoginUser();
                objtemp.setUsername(rs.getString("username"));
                objtemp.setPassword(rs.getString("password"));
                objtemp.setDriverdocno(rs.getString("driverid"));
                return objtemp;
            }
        });
    }
    
	public void testCode() {
		// TODO Auto-generated method stub
		System.out.println("TESTED Sucsessfully");
        
	}

}
