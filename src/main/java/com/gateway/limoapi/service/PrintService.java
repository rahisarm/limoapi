package com.gateway.limoapi.service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.limoapi.dto.InspectionDTO;
import com.gateway.limoapi.exceptions.CommonException;
import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.InspectionModel;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service
public class PrintService {

	@Autowired
    JdbcTemplate template;
    
    @Autowired
    private ClsCommon objcommon;
    
    @Autowired
    private RestTemplate restTemplate;
    
    InspectionDTO dto=new InspectionDTO();
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @Value("${whatsApp}")
    private boolean whatsApp;
    
    @Value("${dbname}")
    private String dbName;
    
    public boolean tripReportGeneratePrint(String jobname,String bookdocno) {
		try {
			System.out.println("Entered Print Generation");
			String outputPath="";
			String mobno="";
			Map<String, Object> parameters = new HashMap();
			String templatePath = "src/main/resources/templates/tripreport.jrxml";
			JasperReport jasperReport = JasperCompileManager.compileReport(templatePath);
			String sql = "SELECT *,now() date FROM (SELECT lg.guestcontactno whatsappno,ma.address clientaddress,ma.com_mob clienttelephone,bt.bookdocno,bt.docname,COALESCE(CONCAT('GW-',b.code,DATE_FORMAT(bm.date,'%Y'),LPAD(bm.`doc_no`, 6, '0')),'') bookingno,COALESCE(CONCAT(ploc.location,' - ',dloc.location),'') limorouteinfo, COALESCE(CONCAT(bt.pickupdate,',',bt.pickuptime),'') datentime, lg.guest limoguest, ma.refname clientname FROM gl_limobooktransfer bt LEFT JOIN gl_limobookm bm ON bt.bookdocno=bm.doc_no LEFT JOIN gl_cordinates ploc ON ploc.doc_no=bt.pickuplocid  AND ploc.status=3 LEFT JOIN gl_cordinates dloc ON dloc.doc_no=bt.dropfflocid AND dloc.status=3 LEFT JOIN  my_brch b ON bm.`brhid`=b.branch LEFT JOIN gl_limoguest lg ON lg.doc_no=bm.guestno LEFT JOIN my_acbook ma ON ma.cldocno=bm.cldocno  AND ma.dtype='CRM' UNION ALL SELECT lg.guestcontactno whatsappno,ma.address clientaddress,ma.com_mob clienttelephone,bt.bookdocno,bt.docname,COALESCE(CONCAT('GW-',b.code,DATE_FORMAT(bm.date,'%Y'),LPAD(bm.`doc_no`, 6, '0')),'') bookingno,COALESCE(CONCAT(ploc.location),'') limorouteinfo, COALESCE(CONCAT(bt.pickupdate,',',bt.pickuptime),'') datentime, lg.guest limoguest, ma.refname clientname FROM gl_limobookhours bt LEFT JOIN gl_limobookm bm ON bt.bookdocno=bm.doc_no LEFT JOIN gl_cordinates ploc ON ploc.doc_no=bt.pickuplocid  AND ploc.status=3 LEFT JOIN  my_brch b ON bm.`brhid`=b.branch LEFT JOIN gl_limoguest lg ON lg.doc_no=bm.guestno LEFT JOIN my_acbook ma ON ma.cldocno=bm.cldocno  AND ma.dtype='CRM') d WHERE d.bookdocno="+bookdocno+" AND d.docname='"+jobname+"'";
			System.out.println(sql);
			// add the real path
			Resource resource = resourceLoader.getResource("classpath:/static/headerlogoprint.jpg");
		    File file = resource.getFile();
		    String imgPath = file.getAbsolutePath().replace("\\", "\\\\"); // If you need to escape backslashes
		    parameters.put("headerlogo",imgPath);
		    // end to add the header path
		    SqlRowSet rssql = template.queryForRowSet(sql);
			while(rssql.next()){
				parameters.put("clientaddress", rssql.getString("clientaddress"));
				parameters.put("clienttelephone", rssql.getString("clienttelephone"));
				parameters.put("clientname", rssql.getString("clientname"));
				parameters.put("bookdocno", rssql.getString("bookdocno"));
				parameters.put("jobname", rssql.getString("docname"));
				parameters.put("bookingno", rssql.getString("bookingno"));
				parameters.put("date", rssql.getString("date"));
				mobno = rssql.getString("whatsappno");
			}	
		
			String sqlcomp = " SELECT company companyname, address companyaddress, tel companyph,email companyemail,imgPath path  FROM my_comp";
			SqlRowSet rssqlcomp = template.queryForRowSet(sqlcomp);
			while(rssqlcomp.next()){
				parameters.put("companyname", rssqlcomp.getString("companyname"));
				parameters.put("companyaddress", rssqlcomp.getString("companyaddress"));
				parameters.put("companyph", rssqlcomp.getString("companyph"));
				parameters.put("companyemail", rssqlcomp.getString("companyemail"));
				//outputPath = rssqlcomp.getString("path");
			}	
			//outputPath+="\\"+bookdocno+"-"+jobname+".pdf";
			
			
			
			
			
			List<InspectionModel> folderpath = dto.getImagePath(template);
			AtomicInteger filesrno = new AtomicInteger();
			
			filesrno.getAndIncrement();
			String dirname = "LIMO";
			File dir = new File(folderpath.get(0).getImagepath() + "/attachment/" + dirname);
			dir.mkdirs();
			String filename = "LIMO-"+bookdocno+"-"+jobname+"-TR.pdf";
			Path path = Paths.get(folderpath.get(0).getImagepath() + "/attachment/" + dirname + "/" + filename);
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, template.getDataSource().getConnection());
			JasperExportManager.exportReportToPdfFile(jasperPrint, String.valueOf(path));
			
			SimpleJdbcCall simplejdbc = new SimpleJdbcCall(template);
			simplejdbc.withProcedureName("AppFileAttach");
			simplejdbc.withCatalogName(dbName);
			Map<String, Object> inparams = new HashMap<String, Object>();
			inparams.put("d_type", "BMG");
			inparams.put("docno", bookdocno + "");
			inparams.put("brhid", "1");
			inparams.put("username", "App User");
			inparams.put("path", String.valueOf(path));
			inparams.put("filename", filename);
			inparams.put("descptn","Trip Report for App");
			inparams.put("reftypid", "1");
			inparams.put("srNo", java.sql.Types.INTEGER);
			inparams.put("chkclientview", "1");

			SqlParameterSource in = new MapSqlParameterSource(inparams);
			Map<String, Object> simplejdbcresult = simplejdbc.execute(in);
			if (simplejdbcresult.get("srno") != null
					&& !simplejdbcresult.get("srno").toString().equalsIgnoreCase("undefined")
					&& Integer.parseInt(simplejdbcresult.get("srno").toString()) > 0) {
				String sqlUp = "update my_fileattach set jobname='"+jobname+"' where doc_no="+bookdocno+" and brhid="+1+" and sr_no="+simplejdbcresult.get("srno").toString()+"AND filename='"+filename+"'";
				int ress = template.update(sqlUp);
				if(ress<=0) throw new CommonException("Error in Files attach");
			} else {
				throw new CommonException("Procedure Error");
			}
			System.out.println("For Each");
			simplejdbcresult.values().stream().forEach(System.out::println);
			
			if(whatsApp) {
				String mediaid = "";
				
				String sql4 = "select mediaid from wa_template where name='pdftemplate'";           
			    SqlRowSet rs8 = template.queryForRowSet(sql4);
			 	while(rs8.next()) {
			 		mediaid = rs8.getString("mediaid");    
			    }
			 	String accesstoken="";
			 	String phoneId="";
			 	String whatsappaccountid="";
			 	String appid="";
			 	String whatsappno="";
			 	
			 	String sql2 = "SELECT phonenoid, accesstoken, whatsappaccountid, appid, whatsappno FROM my_comp WHERE status=3";  
			 	SqlRowSet rs6 = template.queryForRowSet(sql2);
				while(rs6.next()){
					accesstoken = rs6.getString("accesstoken");
					phoneId = rs6.getString("phonenoid");
					whatsappaccountid = rs6.getString("whatsappaccountid");
					appid = rs6.getString("appid");
					whatsappno = rs6.getString("whatsappno");  
				}
			 	
				String apiurl="";
				
				String sql3 = "select apiurl from wa_apiurl where type='sendMultimediawithmsg'";  
				SqlRowSet rs7 = template.queryForRowSet(sql3);
			 	while(rs7.next()) {
			 		apiurl = rs7.getString("apiurl");  
			    }
			 	
			 	String msg="";
			 	String extn="pdf";
			 	String msgtype="Template";
			 	String mediatype="application/pdf";
			 	String mediaformat="pdf";
			 	String client="";
			 	String cldocno="";
			 	
			 	HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_JSON);
	            
	            ObjectMapper objmaper = new ObjectMapper();
	            JsonNode node = objmaper.createObjectNode()
	                    .put("msgtext", msg)
	                    .put("path",  String.valueOf(path))
	                    .put("extn", extn)
	                    .put("msgtype", msgtype)
	                    .put("template", mediaid)
	                    .put("bearer", accesstoken)
	                    .put("mediatype", mediatype)
	                    .put("mediaformat", mediaformat)
	                    .put("whatsappaccountid", whatsappaccountid)
	                    .put("appid", appid)
	                    .put("phoneId", phoneId);

	            JsonNode clientdetails = objmaper.createArrayNode()
	                    .add(objmaper.createObjectNode()
	                            .put("clname", client)
	                            .put("mobno", mobno)
	                            .put("cldocno", cldocno));

	            ((com.fasterxml.jackson.databind.node.ObjectNode) node).set("clientdetails", clientdetails);
	            String jsonArr = objmaper.writeValueAsString(node);
	            HttpEntity<String> entity = new HttpEntity<>(jsonArr, headers);

	            restTemplate.postForEntity(apiurl, entity, String.class);
			 	
				
			}
			System.out.println("PDF generated successfully at: " +  String.valueOf(path));
			
			return true;
		} catch(Exception e) {
			System.out.println("Print Not Genereted");
			e.printStackTrace();
			return false;
		}
		
	}
}
