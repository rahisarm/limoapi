package com.gateway.limoapi.helpers;

import java.sql.*;

import javax.naming.*;
import javax.sql.*;

import org.springframework.beans.factory.annotation.Value;

public class ClsConnection {

	@Value("${dbname}")
	private String dbName;

	@Value("${spring.datasource.url}")
	private String dburl;

	@Value("${spring.datasource.username}")
	private String user;

	@Value("${spring.datasource.password}")
	private String pass;

	@Value("${spring.datasource.driver-class-name}")
	private String driverclassname;

	// Skip connection
	public java.sql.Connection getConnection() {
		java.sql.Connection con = null;
		System.out.println(dburl + "   " + user + "  " + pass+"  "+dbName);
		String url = dburl;
		String username = user;
		String password = pass;
		try {
			Class.forName(driverclassname);
			con = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}
}
