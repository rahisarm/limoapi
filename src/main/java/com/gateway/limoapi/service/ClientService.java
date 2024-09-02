package com.gateway.limoapi.service;


import com.gateway.limoapi.dto.ClientDTO;
import com.gateway.limoapi.exceptions.CommonException;
import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.ConfigModel;
import com.gateway.limoapi.model.DriverModel;
import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.MiscModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class ClientService {

    @Value("${dbtype}")
    private String dbType;

    @Autowired
    private JdbcTemplate template;


    @Autowired
    private ClsCommon objcommon;
    
    public List<DropdownModel> getClient(String value){
        String sqltest="";
        if(value!=null && !value.trim().equalsIgnoreCase("undefined") && !value.trim().equalsIgnoreCase("")){
            sqltest+=" and refname like '%"+value+"%'";
        }
        String strsql="select cldocno docno,coalesce(refname,'') refname from my_acbook where  status<>7 and dtype='CRM' "+sqltest;
        return template.query(strsql, new RowMapper<DropdownModel>() {
            @Override
            public DropdownModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                DropdownModel model=new DropdownModel();
                model.setRefname(rs.getString("refname"));
                model.setDocno(rs.getString("docno"));
                return model;
            }
        });
    }
    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
    public int saveClient(Map<String, String> data){
        ClientDTO dto=new ClientDTO();
        System.out.println(data);
        //Getting Account Group
        List<MiscModel> acgrouplist=dto.getClientAccountGroup(template);
        List<MiscModel> cldocnolist=dto.getMaxCldocno(template);
        List<MiscModel> acnolist=dto.getMaxAcno(template);

        String acgroup=acgrouplist.get(0).getAccountgroup();
        String catid=acgrouplist.get(0).getDocno();
        String cldocno=cldocnolist.get(0).getCldocno();
        String acno=acnolist.get(0).getCldocno();

        String clientname=data.get("clientname").toString();
        String mobileno=data.get("mobileno").toString();
        String email=data.get("email").toString();
        java.sql.Date sqldob= objcommon.changeStringtoSqlDate(data.get("dob").toString());
        java.sql.Date sqllicenseexpiry= objcommon.changeStringtoSqlDate(data.get("licenseexpiry"));
        java.sql.Date sqlvisaexpiry= objcommon.changeStringtoSqlDate(data.get("visaexpiry"));
        String licenseno=data.get("licenseno").toString();
        String licenseissued=data.get("licenseissued").toString();
        String visano=data.get("visano").toString();
        String visaissued=data.get("visaissued").toString();
        String invtype=data.get("invtype").toString();
        String advance=data.get("advance").toString();
        String passportno=data.get("passportno").toString();
        java.sql.Date sqlpassportexpiry= objcommon.changeStringtoSqlDate(data.get("passportexpiry"));
        System.out.println("Recieved VAlues:"+clientname+"::"+mobileno);

        int errorstatus=0;
        //Inserting to my_head

        String strinserthead="";
        if(dbType.trim().equalsIgnoreCase("MySQL")){
            strinserthead="insert into my_head (description,curid,rate,dr,date,atype,dtype,grpno,alevel,grplevel,gr_type,agroup,lapply,tr_no,cflow,m_s,den,cmpid,brhid,account,cldocno,doc_no) values"+
            " ('"+clientname+"',1,1.0,1,CURDATE(),'AR','CRM','"+acgroup+"',(select concat('.','"+acgroup+"','.','"+acno+"')),2,0,1,1,0,0,0,340,1,0,"+acno+","+cldocno+","+acno+")";
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            int identityinsert=template.update("SET IDENTITY_INSERT my_head ON");
            strinserthead="insert into my_head (description,curid,rate,dr,date,atype,dtype,grpno,alevel,grplevel,gr_type,agroup,lapply,tr_no,cflow,m_s,den,cmpid,brhid,account,cldocno,doc_no) values"+
                    " ('"+clientname+"',1,1.0,1,CONVERT(DATE,GETDATE()),'AR','CRM','"+acgroup+"',(select concat('.','"+acgroup+"','.','"+acno+"')),2,0,1,1,0,0,0,340,1,0,"+acno+","+cldocno+","+acno+")";
        }
        int inserthead=template.update(strinserthead);
        if(inserthead<=0){
            errorstatus=1;
            throw new CommonException("Insert Account Error");
        }
        if(dbType.trim().equalsIgnoreCase("MSSQL")){
            int identityinsert=template.update("SET IDENTITY_INSERT my_head OFF");
        }
        if(advance.trim().equalsIgnoreCase("true")) {
        	advance="1";
        }
        else {
        	advance="0";
        }
        double saliksrvDXB=0.0,saliksrvAUH=0.0,trafficsrv=0.0;
        String strsrvrate="SELECT field_nme,ROUND(VALUE,2) VALUE FROM gl_config WHERE method=1 AND field_nme IN ('saliksrv','trafficsrv','saliksrvAUH')";
        List<ConfigModel> srvlist=template.query(strsrvrate, new RowMapper<ConfigModel>() {

			@Override
			public ConfigModel mapRow(ResultSet rs, int rowNum) throws SQLException {
				// TODO Auto-generated method stub
				ConfigModel model=new ConfigModel();
				model.setField_nme(rs.getString("field_nme"));
				model.setValue(rs.getString("value"));
				return model;
			}        	
        });
        for(int i=0;i<srvlist.size();i++) {
        	ConfigModel model=srvlist.get(i);
        	if(model.getField_nme().trim().equalsIgnoreCase("saliksrv")) {
        		saliksrvDXB=Double.parseDouble(model.getValue().toString());
        	}
        	else if(model.getField_nme().trim().equalsIgnoreCase("trafficsrv")) {
        		trafficsrv=Double.parseDouble(model.getValue().toString());
        	}
        	else if(model.getField_nme().trim().equalsIgnoreCase("saliksrvAUH")) {
        		saliksrvAUH=Double.parseDouble(model.getValue().toString());
        	}
        }
        String strinsertclient="";
        if(dbType.trim().equalsIgnoreCase("MySQL")){
            strinsertclient="insert into my_acbook(DATE,RefName,curId,sal_id,catid,tax,advance,invc_method,taxid,trnnumber,acc_group,acno,period,period2,credit,per_mob,mail1,codeno,dtype,brhid,cmpid,cldocno,DOC_NO,STATUS,ser_default,per_salikrate,per_trafficharge)values("+
                    "CURDATE(),'"+clientname+"',1,0,"+catid+",1,"+advance+","+invtype+",1,0,"+acgroup+","+acno+",0,0,0,'"+mobileno+"','"+email+"',"+cldocno+",'CRM',1,1,"+cldocno+","+cldocno+",3,1,"+saliksrvDXB+","+trafficsrv+")";
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            strinsertclient="insert into my_acbook(DATE,RefName,curId,sal_id,catid,tax,advance,invc_method,taxid,trnnumber,acc_group,acno,period,period2,credit,per_mob,mail1,codeno,dtype,brhid,cmpid,cldocno,DOC_NO,STATUS,ser_default,per_salikrate,per_trafficharge)values("+
                    "convert(date,getdate()),'"+clientname+"',1,0,"+catid+",1,"+advance+","+invtype+",1,0,"+acgroup+","+acno+",0,0,0,'"+mobileno+"','"+email+"',"+cldocno+",'CRM',1,1,"+cldocno+","+cldocno+",3,1,"+saliksrvDXB+","+trafficsrv+")";
        }
        System.out.println(strinsertclient);
        int insertclient=template.update(strinsertclient);
        if(insertclient<=0){
            errorstatus=1;
            throw new CommonException("Insert Client Error");
        }

        String strinsertdriver="insert into gl_drdetails(branch,name,mobno,dlno,issfrm,dob,sr_no,doc_no,led,cldocno,visano,visa_exp,dtype,passport_no,pass_exp)values("+
                "1,'"+clientname+"','"+mobileno+"','"+licenseno+"','"+licenseissued+"','"+sqldob+"',1,"+cldocno+",'"+sqllicenseexpiry+"',"+cldocno+",'"+visano+"','"+sqlvisaexpiry+"','CRM','"+passportno+"','"+sqlpassportexpiry+"')";
        int insertdriver=template.update(strinsertdriver);
        if(insertdriver<=0){
            errorstatus=1;
            throw new CommonException("Insert Driver Error");
        }

        if(errorstatus==0){
            return Integer.parseInt(cldocno);
        }
        else{
            return 0;
        }
    }
}
