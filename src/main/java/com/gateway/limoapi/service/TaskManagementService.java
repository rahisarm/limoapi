package com.gateway.limoapi.service;

import java.io.File;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gateway.limoapi.exceptions.CommonException;
import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.helpers.ClsEncrypt;
import com.gateway.limoapi.model.DriverTasks;
import com.gateway.limoapi.model.TaskManagementModel;

@Service
public class TaskManagementService {
	
	@Value("${dbtype}")
    private String dbType;
    
    @Value("${dbname}")
    private String dbName;
	
	@Autowired
    JdbcTemplate template;
    
    @Autowired
    private ClsCommon objcommon;
    

    ClsEncrypt clsEncrypt = new ClsEncrypt();
    
    @Autowired
    private RestTemplate restTemplate;
    
    public boolean addTask(String refType, String refNo, String startDate, String startTime, String hidUser, String desc, String userId, String edcDate) {
    	int val = 0;
    	int update = 0;
		try
		{
			Date  sqledcdate = null;
			if(!edcDate.equals("") && edcDate!=null){
				sqledcdate = objcommon.changeStringtoSqlDate(edcDate);
			}
			
			SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
	        simplejdbc.withProcedureName("an_taskcreationDML");
	        simplejdbc.withCatalogName(dbName);
	        Map<String,Object> inparams=new HashMap<String,Object>();
	        inparams.put("vreftype", refType);
	        inparams.put("vrefno",refNo);
	        inparams.put("vdate", startDate);
	        inparams.put("vtime", startTime);
	        inparams.put("vassuser", hidUser);
	        inparams.put("vdescript", desc);
	        inparams.put("vedc", sqledcdate);
	        inparams.put("vuser", userId);
	        inparams.put("vtype","" );
	        inparams.put("docNo", java.sql.Types.INTEGER);	        
	        SqlParameterSource in=new MapSqlParameterSource(inparams);
	        Map<String,Object> simplejdbcresult=simplejdbc.execute(in);
	        System.out.println("String - "+simplejdbcresult.toString());
	        if(simplejdbcresult.isEmpty()) {
	        	throw new CommonException("Procedure Error");
	        }
	        val = (int) simplejdbcresult.get("docno");
	        
	        System.out.println("val  -  "+val);
	        String sqlinsert = "UPDATE an_schedule SET lastdate = now() WHERE remarks = '"+desc+"'";
	        System.out.println(sqlinsert);
	        template.update(sqlinsert);
	        
	        
	        if(!userId.equals(hidUser)) {
	        	String host="";
	        	String port="";
	        	String userName="";
	        	String password="";
	        	String recipient="";
	        	String subject="";
	        	String mailmessage="";
	        	String mobile="";
	        	String whatsapptemplate="";
	        	String accesstoken="";
	        	String phoneId="";
	        	String whatsappaccountid="";
	        	String appid="";
	        	String whatsappno="";
	        	String path="";
	        	String mediatype="";
	        	String mediaformat="";
	        	String apiurl="";
	        	String cldocnos="";
	        	String msg="";
	        	String attachdoc="";
	        	String extn="";
	        	String msgtype="Template";
	        	String cldocno="0";
	        	String mob="";
	        	String client="";
	        	String rescode="";
	        	String message="";
	        	
	        	File saveFile = null;
	        	
	        	String sqlstr = "SELECT mail, mailpass, smtpserver, smtphostport FROM my_user WHERE status=3 AND user_id='super'";	
				System.out.println("Line 132 - "+sqlstr);
	        	SqlRowSet rssqlstr = template.queryForRowSet(sqlstr);
				while(rssqlstr.next()){
					host = rssqlstr.getString("smtpserver");
					port = rssqlstr.getString("smtphostport");
					userName = rssqlstr.getString("mail");
					password = clsEncrypt.decrypt(rssqlstr.getString("mailpass").trim());  
				}  
	        	
				String sqlstr1 = "SELECT email, mobile FROM my_user WHERE doc_no='"+hidUser+"'";	    
				SqlRowSet rs1 = template.queryForRowSet(sqlstr1);
				while(rs1.next()){
					recipient = rs1.getString("email");    
					mobile = rs1.getString("mobile");        
				}
				
				String mailmsgsql="";
				String sqlstr2 = "select msg, subject from gl_emailmsg where dtype='TMT' and description='Assigned'";	  
				SqlRowSet rs2 = template.queryForRowSet(sqlstr2);
				while(rs2.next()){
					mailmsgsql = rs2.getString("msg").replaceAll("document", val+"");           
					subject = rs2.getString("subject");      
				}
				
				if(!mailmsgsql.equals("")){  
					SqlRowSet rs3 = template.queryForRowSet(mailmsgsql);
					while(rs3.next()){
						mailmessage = rs3.getString("content");        
					}  
				}
				
				if(!mailmessage.equals("") && !recipient.equals("")){
					String successtatus = sendEmailPdf(host,  port, userName,  password,  recipient,  "" , subject,  mailmessage, "", "", saveFile);  
				}
				
				String whatsappmsgsql="";
				String sqlstr4 = "select msg, template, t.mediaid templateid from gl_whatsappmsg g left join wa_template t on t.name=g.template where dtype='TMT' and description='Assigned'";  	  
				System.out.println(sqlstr4);
				SqlRowSet rs4 = template.queryForRowSet(sqlstr4);
				while(rs4.next()){
					whatsappmsgsql = rs4.getString("msg").replaceAll("document", val+"");           
					whatsapptemplate = rs4.getString("templateid");            
				}
				System.out.println(whatsappmsgsql+"  -  "+whatsapptemplate);
				String toUser = "";
				String taskName = "";
				String edc = "";
				String fromUser = "";
				
				if(!whatsappmsgsql.equals("")){  
					SqlRowSet rs5 = template.queryForRowSet(whatsappmsgsql);  
					while(rs5.next()){
						toUser = rs5.getString("touser");
						taskName = rs5.getString("taskname");
						edc = rs5.getString("edcdate");
					}  
				}   
				
				String param1 = "{ \"type\": \"text\", \"text\": \"touser\" }"; 
				String param2 = "{ \"type\": \"text\", \"text\": \"task\" }"; 
				String param3 = "{ \"type\": \"text\", \"text\": \"edc\" }";    
				
				ObjectMapper objectMapper = new ObjectMapper();
			    ObjectNode objectNode = (ObjectNode) objectMapper.readTree(param1);
			    
			    if (objectNode.has("text")) {
		            objectNode.put("text", toUser);
			    }
			    param1 = objectMapper.writeValueAsString(objectNode);
		        System.out.println("UPDATED : "+param1);
		        
		        objectNode = (ObjectNode) objectMapper.readTree(param2);
		    	
			    if (objectNode.has("text")) {
			            objectNode.put("text", taskName);
			    }
			    param2 = objectMapper.writeValueAsString(objectNode);
		        System.out.println("UPDATED : "+param2);
		        
		        objectNode = (ObjectNode) objectMapper.readTree(param3);

			    if (objectNode.has("text")) {
			            objectNode.put("text", edc);
			    }
			    param3 = objectMapper.writeValueAsString(objectNode);
		        System.out.println("UPDATED : "+param3);
		        
		        String paramslist = "[{\"type\": \"body\", \"parameters\": ["+param1+", "+param2+", "+param3+"]}]";
		        
		        if((!mobile.equals("") && mobile!=null) && !toUser.equals("")){
		        	System.out.println("Line 132 - ");
		        	String sql2 = "SELECT phonenoid, accesstoken, whatsappaccountid, appid, whatsappno FROM my_comp WHERE status=3";  
		        	SqlRowSet rs6 = template.queryForRowSet(sql2);
					while(rs6.next()){
						accesstoken = rs6.getString("accesstoken");
						phoneId = rs6.getString("phonenoid");
						whatsappaccountid = rs6.getString("whatsappaccountid");
						appid = rs6.getString("appid");
						whatsappno = rs6.getString("whatsappno");  
					}
		        }
		        
		        String sql3 = "select apiurl from wa_apiurl where type='sendMultimediawithmsg'";         
		        SqlRowSet rs7 = template.queryForRowSet(sql3);
			 	while(rs7.next()) {
			 		apiurl = rs7.getString("apiurl");  
			    }
		        System.out.println(apiurl);
			 	HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_JSON);
	            
	            ObjectMapper objmaper = new ObjectMapper();
	            JsonNode node = objmaper.createObjectNode()
	            		.put("msgtext", msg)
			            .put("path", path)
			            .put("extn", extn)
			            .put("msgtype", msgtype)
			            .put("template", whatsapptemplate)
			            .put("bearer", accesstoken)
			            .put("mediatype", mediatype)
			            .put("mediaformat", mediaformat)
			            .put("whatsappaccountid", whatsappaccountid)
			            .put("appid", appid)
			            .put("phoneId", phoneId)
			            .put("params", paramslist);
			 	
	            JsonNode clientdetails = objmaper.createArrayNode()
			               .add(objmaper.createObjectNode()
			                       .put("clname", client)
			                       .put("mobno", mobile)
			                       .put("cldocno", cldocno));
	            
	            ((com.fasterxml.jackson.databind.node.ObjectNode) node).set("clientdetails", clientdetails);
	            String jsonArr = objmaper.writeValueAsString(node);
	            HttpEntity<String> entity = new HttpEntity<>(jsonArr, headers);
				
	            ResponseEntity<String> response = restTemplate.postForEntity(apiurl, entity, String.class);

	            if (response.getStatusCode() == HttpStatus.OK) {
	            	 String responseBody = response.getBody();
	                 JsonNode JSnode = objmaper.readTree(responseBody);
	                 rescode = JSnode.has("rescode") ? JSnode.get("rescode").asText() : null;
	                 String mediaid = JSnode.has("mediaid") ? JSnode.get("mediaid").asText() : null;
	                 message = JSnode.has("message") ? JSnode.get("message").asText() : null;
	                 if(rescode.equals("1")){
	                	 String updatelog = "Insert Into wa_messagelog(cldocno, brhid, dtype, edate, userid, towano, mediaid, attachdoc, fromwaid, fromphid, appid, fromtext, whatsappno, template) VALUES(?,?,?,now(),?,?,?,?,?,?,?,?,?,?)";
	                	 update = template.update(updatelog,cldocno,"1","WAM",userId,mob,mediaid,attachdoc.equals("")?"0":attachdoc,whatsappaccountid,phoneId,appid,msg,whatsappno,whatsapptemplate);
	                 }
	            }
				
	        }
			 if(update>0) {
				return true; 
			 }
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
    }

	public String sendEmailPdf(String host, String port, String userName, String password, String recipient,
			String CC, String subject, String message, String Filepath, String BCC, File attachfile) {
		Properties properties = new Properties();
        properties.setProperty("mail.smtp.protocol", "smtps");                  
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.debug", "true");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.user", userName);
        properties.put("mail.password", password);
        //java.net.preferIPv4Stack=true;
        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        Session session = Session.getInstance(properties, auth);
        
        Message msg = new MimeMessage(session);
        
        try{		
            msg.setFrom(new InternetAddress(userName));
            InternetAddress[] toAddresses = { new InternetAddress(recipient) };
            msg.setRecipients(Message.RecipientType.TO, toAddresses);
           
            String[] cc = null;
            String[] bcc = null;
            if(CC.length() != 0){
                cc = CC.trim().split(",");
            } 
            if(BCC.length() != 0){
                bcc = BCC.trim().split(",");
            }


            if(!(CC.equals(""))){
            for(int i = 0; i < cc.length; i++) {
                if(!cc[i].isEmpty())
                    msg.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i]));
            }
            }
            
            if(!(BCC.equals(""))){
            for(int i = 0; i < bcc.length; i++) {
                if(!bcc[i].isEmpty())
                    msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc[i]));
            }
            }      
            if(!(subject.equals(""))){                        
            msg.setSubject(subject);
            }
            msg.setSentDate(new java.util.Date());
            // creates message part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(message, "text/html");
            // creates multi-part
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            // adds attachments
            int i=0;  
            //System.out.println("Filepath===="+Filepath);  
            if(!Filepath.equalsIgnoreCase("")){                   
            java.util.List<java.io.File> files = new ArrayList<>();                      
            String[] urlarray = Filepath.split(",");          
            File saveFile = null;
        	for (i = 0; i < urlarray.length; i++) {     
        		String tranno=urlarray[i];	             
        		if(!(tranno.equalsIgnoreCase(""))){   
        			//System.out.println("tranno===="+tranno);            
        			saveFile=new File(tranno);       
        			files.add(saveFile);
        		 	//System.out.println("out test===="+files);  
        		}
        	} 
        	for(File s:files){      
        		//System.out.println("out test===="+s);  
        		MimeBodyPart attachPart = new MimeBodyPart();    
        		attachPart.attachFile(s); 
        		multipart.addBodyPart(attachPart); 
        	}         
            }
            MimeBodyPart attachPart = new MimeBodyPart();    
    		if(attachfile!=null){
            	attachPart.attachFile(attachfile);     
        		multipart.addBodyPart(attachPart);  
            }
    		
            msg.setContent(multipart);
            Transport.send(msg);
            }catch (Exception ex) {
            	ex.printStackTrace();
            	return "fail";
            }
            return "success";
        
	}

	public List<TaskManagementModel> pendingTask(String userid) {
		try {
		String sqltest=" and (t.userid='"+userid+"' or t.ass_user='"+userid+"')";
		String strsql="select u1.user_name crtuser,u.user_name user,t.userid,ass_user,t.doc_no,tt.reftype ref_type,ref_no,strt_date,strt_time,description,act_status status,t.edcdate from an_taskcreation t "
				+ "left join an_taskcreationdets a on t.doc_no=a.rdocno left join my_user u on u.doc_no=t.ass_user left join my_user u1 on u1.doc_no=t.userid  left join an_tasktype tt on tt.doc_no=t.ref_type "
				+ "where t.close_status=0 and t.utype!='app' "+sqltest+" group by doc_no"; 
		System.out.println("pendingGrid--->>>"+strsql);
		return template.query(strsql, new RowMapper<TaskManagementModel>() {
            @Override
            public TaskManagementModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TaskManagementModel objtemp=new TaskManagementModel();
                objtemp.setCrtuser(rs.getString("crtuser"));
                objtemp.setUser(rs.getString("user"));
                objtemp.setAss_user(rs.getString("ass_user"));
                objtemp.setDoc_no(rs.getString("doc_no"));
                objtemp.setRefType(rs.getString("ref_type"));
                objtemp.setRefNo(rs.getString("ref_no"));
                objtemp.setStartDate(rs.getString("strt_date"));
                objtemp.setStartTime(rs.getString("strt_time"));
                objtemp.setDesc(rs.getString("description"));
                objtemp.setStatus(rs.getString("status"));
                objtemp.setEdcDate(rs.getString("edcdate"));
                return objtemp;
            }
        });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    

}
