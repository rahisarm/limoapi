package com.gateway.limoapi.dto;

import com.gateway.limoapi.model.MiscModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClientDTO {

    public List<MiscModel> getClientAccountGroup(JdbcTemplate template) {
        String strsql="SELECT acc_group,doc_no FROM my_clcatm WHERE webappcategory=1";
        return template.query(strsql, new RowMapper<MiscModel>() {
            @Override
            public MiscModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                MiscModel bean=new MiscModel();
                bean.setAccountgroup(rs.getString("acc_group"));
                bean.setDocno(rs.getString("doc_no"));
                return bean;
            }
        });
    }
 
    public List<MiscModel> getMaxCldocno(JdbcTemplate template) {
        String strsql="select coalesce(max(doc_no),0)+1 maxdocno from my_acbook where dtype='CRM'";
        return template.query(strsql, new RowMapper<MiscModel>() {
            @Override
            public MiscModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                MiscModel bean=new MiscModel();
                bean.setCldocno(rs.getString("maxdocno"));
                return bean;
            }
        });
    }

    public List<MiscModel> getMaxAcno(JdbcTemplate template) {
        String strsql="select coalesce(max(doc_no),0)+1 maxdocno from my_head";
        return template.query(strsql, new RowMapper<MiscModel>() {
            @Override
            public MiscModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                MiscModel bean=new MiscModel();
                bean.setCldocno(rs.getString("maxdocno"));
                return bean;
            }
        });
    }
}
