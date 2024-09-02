package com.gateway.limoapi.model;

import org.springframework.context.annotation.Bean;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class LoginUser implements Serializable {
    @Id
    private Long docno;
    private String username;
    private String password;
    private String driverdocno;

    public Long getDocno() {
        return docno;
    }

    public void setDoc_no(Long docno) {
        this.docno = docno;
    }

    public LoginUser() {
    }

    public LoginUser(Long docno,String username, String password, String driverdocno) {
        this.docno=docno;
        this.username = username;
        this.password = password;
        this.driverdocno = driverdocno;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverdocno() {
        return driverdocno;
    }

    public void setDriverdocno(String driverdocno) {
        this.driverdocno = driverdocno;
    }

    @Override
    public String toString() {
        return "LoginUser{" +
                ", docno='"+docno+'\''+
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", driverdocno='" + driverdocno + '\'' +
                '}';
    }
}
