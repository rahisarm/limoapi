package com.gateway.limoapi.service;

import com.gateway.limoapi.dto.InspectionDTO;
import com.gateway.limoapi.exceptions.CommonException;
import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.InspectionModel;
import com.gateway.limoapi.model.MiscModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InspectionService {
    @Value("${dbtype}")
    private String dbType;

    @Value("${dbname}")
    private String dbName;

    @Autowired
    private ClsCommon objcommon;

    @Autowired
    JdbcTemplate template;
    InspectionDTO dto=new InspectionDTO();
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = {SQLException.class, IOException.class,CommonException.class})
    public int saveInspection(List<MultipartFile> multifile, Map<String,String> queryparams) throws SQLException {
        System.out.println("Inside Inspection");
        int inspdocno=0;
        AtomicInteger errorstatus= new AtomicInteger();
        errorstatus.set(0);
        try{
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String time=queryparams.get("time");
            String type=queryparams.get("type");
            String reftype=queryparams.get("reftype");
            String brhid=queryparams.get("brhid");
            String rdocno=queryparams.get("rdocno");
            String fleetno=queryparams.get("fleetno");
            String userid=queryparams.get("userid");
            String refvocno=queryparams.get("refvocno")==null || queryparams.get("refvocno").equalsIgnoreCase("") || queryparams.get("refvocno").equalsIgnoreCase("undefined")?"":queryparams.get("refvocno");
            if(refvocno.equalsIgnoreCase("")){
                List<InspectionModel> refvocmodel=dto.getRefVocno(template,reftype,rdocno);
                refvocno=refvocmodel.get(0).getRefvocno();
            }
            System.out.println(sqldate+"::"+time+"::"+type+"::"+reftype+"::"+brhid+"::"+rdocno+"::"+fleetno+"::"+userid+"::"+refvocno);
            
            if(dbType.trim().equalsIgnoreCase("MySQL")){
            	SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
            	simplejdbc.withProcedureName("AppVehInspectionDML");
            	simplejdbc.withCatalogName(dbName);
            	Map<String,Object> inparams=new HashMap<String,Object>();
            	inparams.put("docNo", java.sql.Types.INTEGER);
            	inparams.put("vdate",sqldate);
                inparams.put("vtype",type);
                inparams.put("vreftype",reftype);
                inparams.put("vrefdocno", Integer.parseInt(rdocno));
                inparams.put("vamount","0.00");
                inparams.put("vaccident","0");
                inparams.put("vpolrep","");
                inparams.put("vacdate",null);
                inparams.put("vcoldate",null);
                inparams.put("vplace","");
                inparams.put("vfine","0");
                inparams.put("vremarks","");
                inparams.put("vclaim","1");
                inparams.put("userId",userid);
                inparams.put("branchid",brhid);
                inparams.put("vmode", "A");
                inparams.put("vdtype", "VIP");
                inparams.put("vrfleet", fleetno);
                inparams.put("vtempround", "0");
                inparams.put("vtime",time);
                inparams.put("refvoucher", refvocno);
                inparams.put("agreementbranch", brhid);
                
                SqlParameterSource in=new MapSqlParameterSource(inparams);
                Map<String,Object> simplejdbcresult=simplejdbc.execute(in);
                System.out.println(simplejdbcresult.toString());
                if(simplejdbcresult.get("docno")!=null && !simplejdbcresult.get("docno").toString().equalsIgnoreCase("undefined") && Integer.parseInt(simplejdbcresult.get("docno").toString())>0) {
                	inspdocno=Integer.parseInt(simplejdbcresult.get("docno").toString());
                }
                else {
                	throw new CommonException("Procedure Error");
                }
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            	SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
            	simplejdbc.withProcedureName("vehInspectionDML");
            	simplejdbc.withCatalogName(dbName);
            	Map<String,Object> inparams=new HashMap<String,Object>();
            	
            	inparams.put("docNo", java.sql.Types.INTEGER);
            	inparams.put("vdate",sqldate);
                inparams.put("vtype",type);
                inparams.put("vreftype",reftype);
                inparams.put("vrefdocno", Integer.parseInt(rdocno));
                inparams.put("vamount","0.00");
                inparams.put("vaccident","0");
                inparams.put("vpolrep","");
                inparams.put("vacdate",null);
                inparams.put("vcoldate",null);
                inparams.put("vplace","");
                inparams.put("vfine","0");
                inparams.put("vremarks","");
                inparams.put("vclaim","1");
                inparams.put("userId",userid);
                inparams.put("branchid",brhid);
                inparams.put("vmode", "A");
                inparams.put("vdtype", "VIP");
                inparams.put("vrfleet", fleetno);
                inparams.put("vtempround", "0");
                inparams.put("vtime",time);
                inparams.put("refvoucher", refvocno);
                inparams.put("agreementbranch", brhid);
                
                SqlParameterSource in=new MapSqlParameterSource(inparams);
                Map<String,Object> simplejdbcresult=simplejdbc.execute(in);
                System.out.println(simplejdbcresult.toString());
                if(simplejdbcresult.get("docno")!=null && !simplejdbcresult.get("docno").toString().equalsIgnoreCase("undefined") && Integer.parseInt(simplejdbcresult.get("docno").toString())>0) {
                	inspdocno=Integer.parseInt(simplejdbcresult.get("docno").toString());
                }
                else {
                	throw new CommonException("Procedure Error");
                }
            }
            int inspectdocno=inspdocno;
            System.out.println("Inspect Docno:"+inspectdocno);
            List<InspectionModel> folderpath=dto.getImagePath(template);
            AtomicInteger filesrno= new AtomicInteger();
            multifile.forEach(file->{
                System.out.println("Inside Each Item");
                byte[] bytes = new byte[1000000];
                try {
                    filesrno.getAndIncrement();
                    String dirname ="VIP";
                    File dir = new File(folderpath.get(0).getImagepath()+ "/attachment/"+dirname);
                    dir.mkdirs();
                    String filename="VIP"+"-"+inspectdocno+"-"+filesrno+".jpg";
                    System.out.println(filename);
                    bytes = file.getBytes();
                    File newfile=new File(folderpath.get(0).getImagepath()+ "/attachment/"+dirname +"/"+ filename);
                    Path path = Paths.get(folderpath.get(0).getImagepath()+ "/attachment/"+dirname +"/"+ filename);
                    Files.write(path, bytes);

                    SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
                    simplejdbc.withProcedureName("AppFileAttach");
                    simplejdbc.withCatalogName(dbName);
                    Map<String,Object> inparams=new HashMap<String,Object>();
                    inparams.put("d_type", "VIP");
                    inparams.put("docno", inspectdocno+"");
                    inparams.put("brhid", brhid);
                    inparams.put("username", "App User");
                    inparams.put("path", String.valueOf(path));
                    inparams.put("filename", filename);
                    inparams.put("descptn", "Attachment for Inspection for App");
                    inparams.put("reftypid", "1");
                    inparams.put("srNo", java.sql.Types.INTEGER);
                    inparams.put("chkclientview", "1");
                    
                    SqlParameterSource in=new MapSqlParameterSource(inparams);
                    Map<String,Object> simplejdbcresult=simplejdbc.execute(in);
                    
                    if(simplejdbcresult.get("srno")!=null && !simplejdbcresult.get("srno").toString().equalsIgnoreCase("undefined") && Integer.parseInt(simplejdbcresult.get("srno").toString())>0) {
                    	
                    }
                    else {
                    	errorstatus.set(1);
                    	throw new CommonException("Procedure Error");
                    }
                    

                } catch (IOException e) {
                    errorstatus.set(1);
                    throw new CommonException(e.getMessage());
                }
                
            });
            if(errorstatus.get()>0){
              throw new CommonException("Raised Exception");
            }
        }
        catch (Exception e){
        	e.printStackTrace();
            throw new CommonException(e.getMessage());
        }
        
        /*catch (SQLException e){
            throw new CommonException(e.getMessage());
        }*/
/*
        Arrays.asList(multifile).stream().forEach(file -> {
            byte[] bytes = new byte[0];
            try {
                bytes = file.getBytes();
                System.out.println(folderpath.get(0).getImagepath() +"\\"+ file.getOriginalFilename());
                File newfile=new File(folderpath.get(0).getImagepath() +"\\"+ file.getOriginalFilename());
                Path path = Paths.get(folderpath.get(0).getImagepath() +"\\"+ file.getOriginalFilename());
                Files.write(path, bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        });
*/
        return inspdocno;
    }

    public List<InspectionModel> getRentalAgmtInspectDetails(String rdtype, String rdocno, String inspecttype) {
        return dto.getRentalAgmtInspectDetails(rdtype,rdocno,inspecttype,template);
    }

    public List<InspectionModel> getLeaseAgmtInspectDetails(String rdtype, String rdocno, String inspecttype) {
        return dto.getLeaseAgmtInspectDetails(rdtype,rdocno,inspecttype,template);
    }

    public List<MiscModel> getInspFleetRefData(String fleetno) {
        return dto.getInspFleetRefData(fleetno,template);
    }

    public List<InspectionModel> getSavedInspectData(String inspdocno) {
        return dto.getSavedInspectData(inspdocno,template);
    }

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = {SQLException.class, IOException.class,CommonException.class})
    public boolean updateInspection(MultipartFile file, String inspectdocno,String brhid) throws SQLException {
        System.out.println("Inside Each Item");
        byte[] bytes = new byte[1000000];

        try {
            List<InspectionModel> folderpath=dto.getImagePath(template);
            List<InspectionModel> filesrnolist=dto.getFileSrno(template,inspectdocno,"VIP");
            int filesrno=filesrnolist.get(0).getFilesrno();
            String dirname ="VIP";
            File dir = new File(folderpath.get(0).getImagepath()+ "/attachment/"+dirname);
            dir.mkdirs();
            String filename="VIP"+"-"+inspectdocno+"-"+filesrno+".jpg";
            System.out.println(filename);
            bytes = file.getBytes();
            File newfile=new File(folderpath.get(0).getImagepath()+ "/attachment/"+dirname +"/"+ filename);
            Path path = Paths.get(folderpath.get(0).getImagepath()+ "/attachment/"+dirname +"/"+ filename);
            Files.write(path, bytes);

            SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
            simplejdbc.withProcedureName("AppFileAttach");
            simplejdbc.withCatalogName(dbName);
            Map<String,Object> inparams=new HashMap<String,Object>();
            inparams.put("d_type", "VIP");
            inparams.put("docno", inspectdocno+"");
            inparams.put("brhid", brhid);
            inparams.put("username", "App User");
            inparams.put("path", String.valueOf(path));
            inparams.put("filename", filename);
            inparams.put("descptn", "Attachment for Inspection for App");
            inparams.put("reftypid", "0");
            inparams.put("srNo", java.sql.Types.INTEGER);
            inparams.put("chkclientview", "1");
            
            SqlParameterSource in=new MapSqlParameterSource(inparams);
            Map<String,Object> simplejdbcresult=simplejdbc.execute(in);
            
            if(simplejdbcresult.get("srno")!=null && !simplejdbcresult.get("srno").toString().equalsIgnoreCase("undefined") && Integer.parseInt(simplejdbcresult.get("srno").toString())>0) {
            	
            }
            else {
            	throw new CommonException("Procedure Error");
            }

            String strinsert="";
            System.out.println("Sign Path:"+String.valueOf(path));
            
            String strsign="";
            if(dbType.trim().equalsIgnoreCase("MySQL")){
            	strsign="INSERT INTO an_signdetails(signature,rdocno,fileatchsrno,userid,location,DATE)VALUES(?,?,?,0,'',CURDATE())";
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            	strsign="INSERT INTO an_signdetails(signature,rdocno,fileatchsrno,userid,location,DATE)VALUES(?,?,?,0,'',convert(date,getdate()))";
            }
            int signinsert=template.update(strsign,String.valueOf(path),inspectdocno,filesrno);
            if(signinsert<=0){
            	throw new CommonException("Sign Update Android Error");
            }
            else {
                return true;
            }
			
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
        
    }

    public List<DropdownModel> getInspRefDoc(String type, String reftype, String brhid, String fleetno) {
        return dto.getInspRefDoc(type,reftype,brhid,fleetno,template,dbType);
    }

    public List<InspectionModel> getLastInspData(String fleetno) {
        return dto.getLastInspData(fleetno,template,dbType);
    }

	public Map<String, Object> getInspLatestFleetData(String fleetno) {
		// TODO Auto-generated method stub
		try {
			String str="SELECT veh.STATUS vehstatus,CASE WHEN mov.repno>0 THEN 'RPL' WHEN mov.rdtype='MOV' THEN 'NRM' ELSE mov.rdtype END refdoctype,veh.a_br brhid,\r\n"
					+ "CASE WHEN mov.repno>0 THEN mov.repno ELSE mov.rdocno END refdocno\r\n"
					+ " FROM gl_vehmaster veh \r\n"
					+ "LEFT JOIN (SELECT MAX(doc_no) maxdocno,fleet_no FROM gl_vmove WHERE rdtype IN ('RAG','LAG','MOV') GROUP BY fleet_no) maxmov ON veh.fleet_no=maxmov.fleet_no \r\n"
					+ "LEFT JOIN gl_vmove mov ON maxmov.maxdocno=mov.doc_no\r\n"
					+ "LEFT JOIN gl_nrm nrm ON mov.rdtype='MOV' AND mov.rdocno=nrm.doc_no\r\n"
					+ "WHERE veh.fleet_no="+fleetno;
			
			return template.queryForMap(str);
		}
		catch(Exception e) {
			throw new CommonException(e.getMessage());
		}
	}
	
	public List<Map<String,Object>> getFileTypes() {
		// TODO Auto-generated method stub
		try {
			String str="SELECT filetype FROM my_filetypes WHERE STATUS=3";
			return template.queryForList(str);
		}
		catch(Exception e) {
			throw new CommonException(e.getMessage());
		}
	}
	
}
