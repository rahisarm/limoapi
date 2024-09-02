package com.gateway.limoapi.config;

import com.gateway.limoapi.dto.SharedData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;


public class DBConfig {
    @Value("${dbtype}")
    private String dbType;

    @Value("${dbname}")
    private String dbName;

    @Primary
    @Bean
    public DataSource dataSource(){
       // SharedData.dbType=this.dbType;
        if(dbType.trim().equalsIgnoreCase("MySQL")){
            //MySQL Configuration
            DriverManagerDataSource dataSource=new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://localhost:3306/"+dbName+"?useSSL=false");
            dataSource.setUsername("root");
            dataSource.setPassword("gateway");
            return dataSource;
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            //MSSQL Configuration
            DriverManagerDataSource dataSource=new DriverManagerDataSource();
            dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName="+dbName+";integratedSecurity=true;encrypt=false;useSSL=false;");
            return dataSource;
        }
        else{
            throw new RuntimeException("DB Configuration Error");
        }
    }
}
