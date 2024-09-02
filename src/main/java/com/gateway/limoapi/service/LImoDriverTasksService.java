package com.gateway.limoapi.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.relational.core.sql.IsNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gateway.limoapi.dto.InspectionDTO;
import com.gateway.limoapi.exceptions.CommonException;
import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.DriverTasks;
import com.gateway.limoapi.model.InspectionModel;
import com.gateway.limoapi.model.LimoDataModel;
import com.gateway.limoapi.model.MovModel;

@Service
public class LImoDriverTasksService {
	@Value("${dbtype}")
    private String dbType;
    
    @Value("${dbname}")
    private String dbName;

    @Autowired
    private JdbcTemplate template;
    
    @Autowired
    private DriverTasksService driverTaskService;
    
    @Autowired
    private PrintService printService;
    
    InspectionDTO dto=new InspectionDTO();

    @Autowired
    private ClsCommon objcommon;
    
    public List<LimoDataModel> getJobs(String driverdocno){
    	String sql = "SELECT CONCAT(m.docno,'-',m.job) docno,COALESCE(CONCAT(m.guest,' - ',m.client,' - ',m.fno,' - ',m.brand,' ',model,' - ',m.job,'-',m.docno),'') jobno  FROM gl_limomanagement m LEFT JOIN gl_multivehassign gvm ON m.docno=gvm.bookingno AND m.job=gvm.jobname AND gvm.drvid="+driverdocno+"  LEFT JOIN `my_salesman` s ON s.srno=gvm.drvid WHERE m.bstatus>=2 and m.bstatus<7 and gvm.bookingstatus<=6 AND gvm.drvid="+driverdocno;
    	System.out.println(sql);
    	return template.query(sql, new RowMapper<LimoDataModel>() {
             @Override
             public LimoDataModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            	 LimoDataModel objtemp=new LimoDataModel();
                 objtemp.setJobno(rs.getString("jobno"));
                 objtemp.setBookingno(rs.getString("docno"));
                 return objtemp;
             }
         });
    }
    
    public List<DriverTasks> getDriverTasks(String driverdocno){
        try{
            String sqlfilters="";
            if(!driverdocno.trim().equalsIgnoreCase("") && !driverdocno.trim().equalsIgnoreCase("0")){
                sqlfilters+=" and base.drid="+driverdocno;
            }
            String strsql="";

            if (dbType.trim().equalsIgnoreCase("MySQL")){
            	strsql="SELECT base.*,COALESCE(IF(trp.tripstatus=1,DATE_FORMAT(trp.startdate,'%d.%m.%Y'),''),'') tripstartdate,COALESCE(IF(trp.tripstatus=1,trp.starttime,''),'') tripstarttime,COALESCE(IF(trp.tripstatus<2,trp.tripstatus,0),0) tripstatus FROM ("
            			+ " SELECT ''jobname,CASE WHEN nrm.movtype='TR' THEN 'Transfer Branch Close'  WHEN nrm.movtype IN ('GS','GM','GA') AND nrm.delivery=0 THEN 'Garage Delivery'  WHEN nrm.movtype IN ('GS','GM','GA') AND nrm.delivery=1 AND nrm.collection=0 THEN 'Garage Collection'  WHEN nrm.movtype IN ('GS','GM','GA') AND nrm.collection=1 THEN 'Garage Branch Close' ELSE '' END tripmode,nrm.doc_no,'MOV' rdtype,veh.fleet_no,veh.reg_no,plate.code_name,veh.flname,'Movement' refname,nrm.drid,0 repno FROM gl_nrm nrm  LEFT JOIN gl_vehmaster veh ON nrm.fleet_no=veh.fleet_no  LEFT JOIN gl_vehplate plate ON veh.pltid=plate.doc_no WHERE nrm.status=3 AND nrm.clstatus=0 UNION ALL SELECT '' jobname,'Collection' AS tripmode,p.agmtno AS doc_no,p.agmttype AS rdtype,v.fleet_no,v.reg_no,plate.code_name,v.flname,ac.refname,"
            			+ "  "+driverdocno+" AS drid,p.doc_no AS repno FROM gl_vehpickup p "
            			+ "  LEFT JOIN my_acbook ac ON (p.cldocno = ac.cldocno AND ac.dtype = 'CRM') "
            			+ "  LEFT JOIN gl_vehmaster v ON p.fleet_no = v.fleet_no "
            			+ "  LEFT JOIN gl_vehplate plate ON v.pltid = plate.doc_no "
            			+ "  LEFT JOIN gl_ragmt r ON p.agmtno = r.doc_no AND p.agmttype = 'RAG' "
            			+ "  LEFT JOIN gl_lagmt l ON p.agmtno = l.doc_no AND p.agmttype = 'LAG' "
            			+ "  LEFT JOIN an_acollection acol ON p.doc_no=acol.pkupno "
            			+ "  WHERE p.status=3 and CASE WHEN p.agmttype = 'RAG' THEN r.clstatus ELSE l.clstatus END = 0 AND (acol.doc_no IS NULL OR acol.bindate IS NULL) UNION ALL"
            			+ " SELECT gvm.jobname jobname,CASE WHEN gvm.bookingstatus=3 THEN 'Job Accepted' WHEN gvm.bookingstatus=5 THEN 'Waiting For Guest'  WHEN gvm.bookingstatus=6 THEN 'Trip Started' ELSE 'Job Assigned' END AS tripmode, bm.docno doc_no, 'JOB' rdtype,veh.fleet_no,veh.reg_no,plt.code_name,veh.flname,ac.refname,gvm.drvid drid,'' repno FROM gl_limomanagement bm left join gl_multivehassign gvm ON bm.docno=gvm.bookingno AND bm.job=gvm.jobname and gvm.drvid="+driverdocno+" LEFT JOIN gl_vehmaster veh ON (gvm.fleetno=veh.fleet_no AND statu=3) LEFT JOIN gl_vehplate plt ON veh.pltid=plt.doc_no  INNER JOIN gl_limobookm lb ON lb.`doc_no`=bm.`docno` LEFT JOIN my_acbook ac ON (bm.clientid=ac.cldocno AND ac.dtype='CRM')  WHERE gvm.drvid="+driverdocno+"  AND bm.bstatus>=2 and bm.bstatus<=6 and gvm.bookingstatus<=6"
            			+ ") base LEFT JOIN (SELECT MAX(doc_no) maxdocno,rdocno,rdtype,driverid FROM gl_drtrip GROUP BY rdocno,rdtype,driverid) maxtrp ON   base.doc_no=maxtrp.rdocno AND base.rdtype=maxtrp.rdtype AND base.drid=maxtrp.driverid   LEFT JOIN gl_drtrip trp ON maxtrp.maxdocno=trp.doc_no WHERE 1=1 "+sqlfilters;
            }
            System.out.println("SQL:"+strsql);
            return template.query(strsql, new RowMapper<DriverTasks>() {
                @Override
                public DriverTasks mapRow(ResultSet rs, int rowNum) throws SQLException {
                    DriverTasks objtemp=new DriverTasks();
                    objtemp.setTripstartdate(rs.getString("tripstartdate"));
                    objtemp.setTripstarttime(rs.getString("tripstarttime"));
                    objtemp.setDrvtripstatus(rs.getString("tripstatus"));
                    objtemp.setId(Long.parseLong(rowNum+""));
                    objtemp.setClientname(rs.getString("refname"));
                    objtemp.setFleetname(rs.getString("reg_no")+" "+rs.getString("code_name")+" "+rs.getString("flname"));
                    objtemp.setRdocno(rs.getInt("doc_no"));
                    objtemp.setRdtype(rs.getString("rdtype"));
                    objtemp.setTripmode(rs.getString("tripmode"));
                    objtemp.setRepno(rs.getString("repno"));
                    objtemp.setFleetno(rs.getString("fleet_no"));
                    objtemp.setRjobtype(rs.getString("jobname"));
                    return objtemp;
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
    public boolean saveStartTripInsert(Map<String,String> queryparams) {
        boolean status=false;
        try{
            String fleetno=queryparams.get("fleetno");
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String bookingno=queryparams.get("bookingno");
            String rjobtype=queryparams.get("rjobtype");
            String time=queryparams.get("time");
            String km=queryparams.get("km");
            String brhid=queryparams.get("brhid");
            String userid=queryparams.get("userid");
            //tring username=queryparams.get("username");
            String drvdocno=queryparams.get("drvdocno");
            String remarks=queryparams.get("remarks");
            
            String starttripdetails = "SELECT veh.reg_no,veh.fleet_no,veh.flname vehname,COALESCE(sm.sal_name,'') drivername,bm.plocation pickuplocation, bm.dlocation dropofflocation, bm.job, bm.type, COALESCE(bm.tdocno,0) tdocno,bm.tarifdocno,bm.tarifdetaildocno, COALESCE(bm.docno,0) bookingno,COALESCE(CONCAT(bm.docno,'-',bm.job),'') jobno, bm.bstatus jobstatus FROM gl_limomanagement bm INNER JOIN my_brch b ON bm.`brhid`=b.`BRANCH` INNER JOIN gl_limobookm lb ON lb.`doc_no`=bm.`docno` LEFT JOIN gl_multivehassign gvm ON bm.docno=gvm.bookingno AND bm.job=gvm.jobname AND gvm.drvid="+drvdocno+" LEFT JOIN my_salesman sm ON sm.doc_no=gvm.drvid LEFT JOIN gl_limostatusdet st ON st.doc_no=bm.bstatus LEFT JOIN gl_vehmaster veh ON (gvm.fleetno=veh.fleet_no AND statu=3) LEFT JOIN gl_vehplate plt ON veh.pltid=plt.doc_no LEFT JOIN my_user usr ON bm.bookuserid=usr.doc_no WHERE bm.confirm=0 and bm.job='"+rjobtype+"' AND bm.docno="+bookingno;
            List<Map<String, Object>> result = template.queryForList(starttripdetails);
            System.out.println(result.get(0).get("drivername"));
            
            System.out.println("Start Trip details - "+starttripdetails);
            
            String sql="insert into an_starttripdet( jobno,userid,regno, vehname, drivername, location, startkm, startdate, starttime,endlocation,fleet,startremarks,driverid,brhid,jobdoc,jobtype,tarifdoc,tarifdetdoc) values"
            + " ('"+result.get(0).get("jobno")+"',"+userid+",'"+result.get(0).get("reg_no")+"','"+result.get(0).get("vehname")+"','"+result.get(0).get("drivername")+"','"+result.get(0).get("pickuplocation")+"',"+km+",'"+sqldate+"','"+time+"','"+result.get(0).get("dropofflocation")+"','"+result.get(0).get("fleet_no")+"','"+remarks+"','"+drvdocno+"','"+brhid+"','"+result.get(0).get("tdocno")+"','"+result.get(0).get("type")+"','"+result.get(0).get("tarifdocno")+"','"+result.get(0).get("tarifdetaildocno")+"')";
            int isInserted = template.update(sql);
            if(!(isInserted>=1)) {
            	throw new CommonException("Insertion in an_starttripdet Error");
            }
            if(!result.get(0).get("drivername").toString().equalsIgnoreCase("6")) {
            String sql2="update gl_limomanagement set bstatus=6 where docno="+bookingno+" and job='"+result.get(0).get("job")+"'";
            isInserted=template.update(sql2);
            if(!(isInserted>=1)) {
            	throw new CommonException("Updation in gl_multivehassignment Error");
            }
            }
            
            String sqlupdatemulti="update gl_multivehassign set bookingstatus=6 where bookingno="+bookingno+" and jobname='"+result.get(0).get("job")+"' and drvid="+drvdocno;
            int isUpdated=template.update(sqlupdatemulti);
            if(!(isUpdated>=1)) {
            	throw new CommonException("Updation in gl_ Error");
            }
            
            status=driverTaskService.startDriverTrip(bookingno,"JOB",drvdocno);
            if(!status) {
            	throw new CommonException("Updation in startDriverTrip Error");
            }
            String systemnote="Started Trip of "+ bookingno +" - "+result.get(0).get("job")+" - "+ sqldate+" - "+time+" - "+km+" - "+result.get(0).get("pickuplocation")+" - by "+result.get(0).get("drivername");
			String sqllog = "INSERT INTO gl_limomgmtlog (bookdocno, jobname, brhid, userid, logdate, remarks, systemremarks) VALUES ('"+bookingno+"','"+result.get(0).get("job")+"','"+brhid+"','"+userid+"', now(), '"+remarks+"','"+systemnote+"')";
			isInserted=template.update(sqllog);
            if(isInserted>=1) {
            	status = true;
            }
            
        }
        catch (Exception e){
            e.printStackTrace();
            status=false;
            throw new CommonException("Updation in startDriverTrip Error");
        }
        return status;
    }

    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
	public boolean saveEndTripInsert(List<MultipartFile> multifile,List<MultipartFile> smultifile, Map<String, String> queryparams) {
    	 boolean status=false;
         try{
        	 String grpid="";
        	 double vat=0.0;
        	 double vals=0.0;
        	 double extrahr=0.0;
        	 double minkm=0.0;
        	 double extrakmrate=0.0;
        	 double mincharge=0.0;
        	 double excesshrsrate=0.0;
        	 double nightmincharge=0.0;
        	 double nightminkmcharge=0.0;
        	 double nightextrakmrate=0.0;
        	 double nightexcesshrsrate=0.0;
        	 double ridechrg=0.0;
        	 double totalkm=0.0;
        	 double extrahrcharges=0.0;
        	 double taxamount=0.0;
        	 double total=0.0;
        	 double totalchrg=0.0;
        	 double totkm=0;
        	 double tothrs=0;
        	 int doc=0;
        	 String stime="";
        	 String etime="";
        	 String timevalue="";
             String fleetno=queryparams.get("fleetno");
             java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
             String bookingno=queryparams.get("bookingno");
             String rjobtype=queryparams.get("rjobtype");
             String time=queryparams.get("time");
             String km=queryparams.get("km");
             String brhid=queryparams.get("brhid");
             String userid=queryparams.get("userid");
             String drvdocno=queryparams.get("drvdocno");
             String remarks=queryparams.get("remarks");
             
             String starttripdetails = "SELECT veh.reg_no,veh.fleet_no,veh.flname vehname,COALESCE(bm.drivername,'') drivername,bm.plocation pickuplocation, bm.dlocation dropofflocation, bm.job, bm.type, COALESCE(bm.tdocno,0) tdocno,bm.tarifdocno,bm.tarifdetaildocno, COALESCE(bm.docno,0) bookingno,COALESCE(CONCAT(bm.docno,'-',bm.job),'') jobno FROM gl_limomanagement bm INNER JOIN my_brch b ON bm.`brhid`=b.`BRANCH` INNER JOIN gl_limobookm lb ON lb.`doc_no`=bm.`docno` LEFT JOIN gl_limostatusdet st ON st.doc_no=bm.bstatus LEFT JOIN gl_vehmaster veh ON (bm.fno=veh.fleet_no AND statu=3) LEFT JOIN gl_vehplate plt ON veh.pltid=plt.doc_no LEFT JOIN my_user usr ON bm.bookuserid=usr.doc_no LEFT JOIN gl_jobassign gj ON gj.bookdocno=bm.docno  WHERE bm.confirm=0 AND bm.job='"+rjobtype+"' and bm.docno="+bookingno;
             System.out.println(starttripdetails);
             List<Map<String, Object>> result = template.queryForList(starttripdetails);
             System.out.println(result.get(0).get("drivername"));
             
             String sqlr = "SELECT startkm,date_format(startdate,'%d.%m.%Y') startdate,rowno FROM an_starttripdet WHERE jobno ='"+result.get(0).get("jobno")+"'";
             System.out.println(sqlr);
             List<Map<String, Object>> resultsqlr = template.queryForList(sqlr);
             System.out.println(resultsqlr.get(0).get("startkm"));
             
             boolean endDriverTrip = driverTaskService.endDriverTrip(bookingno, "JOB", drvdocno);
             if(!endDriverTrip) {
             	throw new CommonException("Updation in endDriverTrip Error");
             }
             
			 String sql="update an_starttripdet set endkm="+km+",enddate='"+sqldate+"',endtime='"+time+"',endlocation='"+result.get(0).get("dropofflocation")+"',endremarks='"+remarks+"' where rowno="+resultsqlr.get(0).get("rowno")+""; 
			 status = template.update(sql)>=1?false:true;
			 if(status) throw new CommonException("Updation in an_starttripdet Error");
             
			 List<InspectionModel> folderpath = dto.getImagePath(template);
				AtomicInteger filesrno = new AtomicInteger();
				
				if(multifile!=null) {
					System.out.println("MultiFile Length - "+multifile.size());
					multifile.forEach(file -> {
						System.out.println("Inside Each Item");
						byte[] bytes = new byte[1000000];
						try {
							filesrno.getAndIncrement();
							String dirname = "LIMO";
							File dir = new File(folderpath.get(0).getImagepath() + "/attachment/" + dirname);
							dir.mkdirs();
							String filename = "LIMO-"+bookingno+"-"+result.get(0).get("job")+"-EndTrip-" + filesrno + ".jpg";
							System.out.println(filename);
							bytes = file.getBytes();
							File newfile = new File(
									folderpath.get(0).getImagepath() + "/attachment/" + dirname + "/" + filename);
							Path path = Paths.get(folderpath.get(0).getImagepath() + "/attachment/" + dirname + "/" + filename);
							Files.write(path, bytes);

							SimpleJdbcCall simplejdbc = new SimpleJdbcCall(template);
							simplejdbc.withProcedureName("AppFileAttach");
							simplejdbc.withCatalogName(dbName);
							Map<String, Object> inparams = new HashMap<String, Object>();
							inparams.put("d_type", "BMG");
							inparams.put("docno", bookingno + "");
							inparams.put("brhid", brhid);
							inparams.put("username", "App User");
							inparams.put("path", String.valueOf(path));
							inparams.put("filename", filename);
							inparams.put("descptn","Attachment for End Trip for App");
							inparams.put("reftypid", "1");
							inparams.put("srNo", java.sql.Types.INTEGER);
							inparams.put("chkclientview", "1");

							SqlParameterSource in = new MapSqlParameterSource(inparams);
							Map<String, Object> simplejdbcresult = simplejdbc.execute(in);

							if (simplejdbcresult.get("srno") != null
									&& !simplejdbcresult.get("srno").toString().equalsIgnoreCase("undefined")
									&& Integer.parseInt(simplejdbcresult.get("srno").toString()) > 0) {
								String sqlUp = "update my_fileattach set jobname='"+result.get(0).get("job")+"' where doc_no="+bookingno+" and brhid="+brhid+" and sr_no="+simplejdbcresult.get("srno").toString()+"AND filename='"+filename+"'";
								int ress = template.update(sqlUp);
								if(ress<=0) throw new CommonException("Error in Files attach");
							} else {
								throw new CommonException("Procedure Error");
							}
						} catch (IOException e) {
							throw new CommonException(e.getMessage());
						}
					});
				}
				
				
				smultifile.forEach(file -> {
					System.out.println("Inside Each Item");
					byte[] bytes = new byte[1000000];
					try {
						filesrno.getAndIncrement();
						String dirname = "LIMO";
						File dir = new File(folderpath.get(0).getImagepath() + "/attachment/" + dirname);
						dir.mkdirs();
						String filename = "LIMO-"+bookingno+"-"+result.get(0).get("job")+"-SIGN-" + "-EndTrip-" + filesrno + ".jpg";
						System.out.println(filename);
						bytes = file.getBytes();
						File newfile = new File(
								folderpath.get(0).getImagepath() + "/attachment/" + dirname + "/" + filename);
						Path path = Paths.get(folderpath.get(0).getImagepath() + "/attachment/" + dirname + "/" + filename);
						Files.write(path, bytes);
						SimpleJdbcCall simplejdbc = new SimpleJdbcCall(template);
						simplejdbc.withProcedureName("AppFileAttach");
						simplejdbc.withCatalogName(dbName);
						Map<String, Object> inparams = new HashMap<String, Object>();
						inparams.put("d_type", "BMG");
						inparams.put("docno", bookingno + "");
						inparams.put("brhid", brhid);
						inparams.put("username", "App User");
						inparams.put("path", String.valueOf(path));
						inparams.put("filename", filename);
						inparams.put("descptn","Attachment for End Trip for App");
						inparams.put("reftypid", "1");
						inparams.put("srNo", java.sql.Types.INTEGER);
						inparams.put("chkclientview", "0");

						SqlParameterSource in = new MapSqlParameterSource(inparams);
						Map<String, Object> simplejdbcresult = simplejdbc.execute(in);

						if (simplejdbcresult.get("srno") != null
								&& !simplejdbcresult.get("srno").toString().equalsIgnoreCase("undefined")
								&& Integer.parseInt(simplejdbcresult.get("srno").toString()) > 0) {
							String updateStatus = "update my_fileattach SET STATUS=7,jobname='"+result.get(0).get("job")+"' WHERE sr_no="+simplejdbcresult.get("srno").toString()+" AND doc_no="+bookingno+" AND filename='"+filename+"'";
							boolean ustatus = template.update(updateStatus)>=1?false:true;
							 if(ustatus) throw new CommonException("updateStatus Error");
							 String sqlupdatemulti="update gl_multivehassign set bookingstatus=7,signpath='"+String.valueOf(path)+"' where bookingno="+bookingno+" and jobname='"+result.get(0).get("job")+"' and drvid="+drvdocno;
						     int isUpdated=template.update(sqlupdatemulti);
						     if(!(isUpdated>=1)) {
						    	 throw new CommonException("Updation in gl_multivehassign Error");
						     }
						} else {
							throw new CommonException("Procedure Error");
						}
					} catch (IOException e) {
						throw new CommonException(e.getMessage());
					}
				});
			 
		     int endCheck=0;
		     String checkAllTripsCompleted = "SELECT (COUNT(*)-(SELECT COUNT(*) COUNT FROM gl_multivehassign WHERE  bookingno="+bookingno+" and jobname='"+result.get(0).get("job")+"' AND bookingstatus>=7)) ended FROM gl_multivehassign where bookingno="+bookingno+" and jobname='"+result.get(0).get("job")+"'";
		     System.out.println(checkAllTripsCompleted);
		     SqlRowSet rscheck = template.queryForRowSet(checkAllTripsCompleted);
             if(rscheck.next()) endCheck = rscheck.getInt("ended");
             
		     if(endCheck>=1) {
		    	 	String usersql = "SELECT user_name user FROM my_user WHERE doc_no="+userid;
					SqlRowSet usersqlRowSet = template.queryForRowSet(usersql);
					String username="";
					if(usersqlRowSet.next()) {
						username=usersqlRowSet.getString("user");
					}
					
					String systemnote="Ended Trip of " + bookingno + " - "+result.get(0).get("job")+" - "+ sqldate+" - "+time+" - "+km+" - "+result.get(0).get("dropofflocation")+" - by "+username;
					String sqllog = "INSERT INTO gl_limomgmtlog (bookdocno, jobname, brhid, userid, logdate, remarks, systemremarks) VALUES ('"+bookingno+"','"+result.get(0).get("job")+"','"+brhid+"','"+userid+"', now(), '"+remarks+"','"+systemnote+"')";
					status = template.update(sqllog)>=1?false:true;
					if(status) throw new CommonException("Insertion in gl_limomgmtlog failed");
					return true;
		     }
		     
		     String sqlg = "SELECT gid FROM (SELECT CONCAT(bookdocno,'-',docname) jobname, gid FROM gl_limobookhours UNION ALL SELECT CONCAT(bookdocno,'-',docname) jobname, gid FROM gl_limobooktransfer) a  WHERE a.jobname='"+result.get(0).get("jobno")+"'";
             SqlRowSet rssqlg = template.queryForRowSet(sqlg);
             if(rssqlg.next()) grpid = rssqlg.getString("gid");
             
             String sqls11="select cstper value from gl_taxmaster where curDate() between fromdate and todate and type=2 and cstper!=0";
             SqlRowSet rssqls11 = template.queryForRowSet(sqls11);
             if(rssqls11.next()) vat = rssqls11.getDouble("value");
             
             String sql1="select starttime,endtime,(endkm-startkm) totkm,TIMESTAMPDIFF(hour,concat(startdate,' ',starttime),concat(enddate,' ',endtime)) tothrs from an_starttripdet where rowno="+resultsqlr.get(0).get("rowno")+"";
             SqlRowSet rssql1 = template.queryForRowSet(sql1);
             if(rssql1.next()) {
            	 totkm=rssql1.getDouble("totkm");
				 tothrs=rssql1.getDouble("tothrs");
				 stime=rssql1.getString("starttime");
				 etime=rssql1.getString("endtime");
             }
             
             String sqlss="select mincharge,minkmcharge, extrakmrate, excesshrsrate, nightmincharge, nightminkmcharge, nightextrakmrate, nightexcesshrsrate from gl_limotarifm m left join gl_limotariftaxi lt on lt.tarifdocno=m.doc_no where curdate() between validfrom and validto and lt.gid="+grpid+"";
             System.out.println(sqlss);
             SqlRowSet rssqlss = template.queryForRowSet(sqlss);
             if(rssqlss.next()) {
            	 minkm=rssqlss.getDouble("minkmcharge");
            	 extrakmrate=rssqlss.getDouble("extrakmrate");
            	 mincharge=rssqlss.getDouble("mincharge");
            	 excesshrsrate=rssqlss.getDouble("excesshrsrate");
            	 nightmincharge=rssqlss.getDouble("nightmincharge");
            	 nightminkmcharge=rssqlss.getDouble("nightminkmcharge");
            	 nightextrakmrate=rssqlss.getDouble("nightextrakmrate");
            	 nightexcesshrsrate=rssqlss.getDouble("nightexcesshrsrate");
				 
             }
             
             String sqls1="select value from gl_config where field_nme='standardspeed'";
             System.out.println(sqls1);
             SqlRowSet rssqls1 = template.queryForRowSet(sqls1);
				while(rssqls1.next()){
					vals=rssqls1.getDouble("value");
				}
				
				extrahr=tothrs - (totkm/vals);
				double extrahour=0;
				if(extrahr>0){
					 extrahour=extrahr;
				}
				else
				{
					 extrahour=0;
				}
				String sqls="select description from gl_config where field_nme='nightstarttime'";
				System.out.println(sqls);
				SqlRowSet rssqls = template.queryForRowSet(sqls);
				while(rssqls.next()){
					timevalue=rssqls.getString("description");
				}
				
				String[] ntime=timevalue.split(":");
				int nighthrs=Integer.parseInt(ntime[0]);
				int nightmin=Integer.parseInt(ntime[1]);
				
				String[] starttime=stime.split(":");
				int shrs=Integer.parseInt(starttime[0]);
				int smin=Integer.parseInt(starttime[1]);
				
				String[] entime=etime.split(":");
				int ehrs=Integer.parseInt(entime[0]);
				int emin=Integer.parseInt(entime[1]);
				
				if(shrs>=nighthrs)
				{
					if(shrs==nighthrs)
					{
						if(smin>=nightmin)
						{
							if(totkm<=nightminkmcharge){
								
								ridechrg=nightminkmcharge;
	 							System.out.println("nightridech=="+ridechrg);
							}
							else
							{
								totalkm=totkm-nightminkmcharge;
								ridechrg=(totalkm*nightextrakmrate)+nightmincharge;
								System.out.println("nightcase==tokm="+totalkm+"ridech=="+ridechrg);
								
							}
							extrahrcharges=extrahour*nightexcesshrsrate;
							totalchrg=ridechrg+extrahrcharges;
							taxamount=(ridechrg+extrahrcharges)*(vat/100);
							total=taxamount+ridechrg+extrahrcharges;
							
							
						}
					}
					else{
						if(totkm<=nightminkmcharge){
							
							ridechrg=nightminkmcharge;
 							System.out.println("nightridech=="+ridechrg);
						}
						else
						{
							totalkm=totkm-nightminkmcharge;
							ridechrg=(totalkm*nightextrakmrate)+nightmincharge;
							System.out.println("nightcase==tokm="+totalkm+"ridech=="+ridechrg);
							
						}
						extrahrcharges=extrahour*nightexcesshrsrate;
						totalchrg=ridechrg+extrahrcharges;
						taxamount=(ridechrg+extrahrcharges)*(vat/100);
						total=taxamount+ridechrg+extrahrcharges;
					}
					
				}
				else if(ehrs>=nighthrs){
					if(ehrs==nighthrs)
					{
						if(emin>=nightmin)
						{
							if(totkm<=nightminkmcharge){
								
								ridechrg=nightminkmcharge;
	 							System.out.println("End **nightridech=="+ridechrg);
							}
							else
							{
								totalkm=totkm-nightminkmcharge;
								ridechrg=(totalkm*nightextrakmrate)+nightmincharge;
								System.out.println("End nightcase==tokm="+totalkm+"ridech=="+ridechrg);
								
							}
							extrahrcharges=extrahour*nightexcesshrsrate;
							totalchrg=ridechrg+extrahrcharges;
							taxamount=(ridechrg+extrahrcharges)*(vat/100);
							total=taxamount+ridechrg+extrahrcharges;
						}
					}
					else{
						if(totkm<=nightminkmcharge){
							
							ridechrg=nightminkmcharge;
 							System.out.println("End nightridech=="+ridechrg);
						}
						else
						{
							totalkm=totkm-nightminkmcharge;
							ridechrg=(totalkm*nightextrakmrate)+nightmincharge;
							System.out.println("End nightcase==tokm="+totalkm+"ridech=="+ridechrg);
							
						}
						extrahrcharges=extrahour*nightexcesshrsrate;
						totalchrg=ridechrg+extrahrcharges;
						taxamount=(ridechrg+extrahrcharges)*(vat/100);
						total=taxamount+ridechrg+extrahrcharges;
					}
				}
				else{
						if(totkm<=minkm){
							
							ridechrg=minkm;
						}
						else
						{
							totalkm=totkm-minkm;
							ridechrg=(totalkm*extrakmrate)+mincharge;
						}
						extrahrcharges=extrahour*excesshrsrate;
						totalchrg=ridechrg+extrahrcharges;
						taxamount=(ridechrg+extrahrcharges)*(vat/100);
						total=taxamount+ridechrg+extrahrcharges;
						
				}
				String sqlss1="insert into my_trno(USERNO, TRTYPE, brhId, edate, transid) values("+userid+",'CINV',1,now(),0)";
				status = template.update(sqlss1)>=1?false:true;
				if(status) throw new CommonException("Insertion in my_trno failed");
				
				String sql12="insert into an_cashinvm(doc_no,jobid, tripcharge, extrahrschrg, totalchrg, vat, nettotal, userid,bookdocno,jobname)values("+doc+",'"+result.get(0).get("jobno")+"',"+ridechrg+","+extrahrcharges+","+totalchrg+","+taxamount+","+total+","+userid+","+bookingno+",'"+result.get(0).get("job")+"')";
				status = template.update(sql12)>=1?false:true;
				if(status) throw new CommonException("Insertion in an_cashinvm failed");
				
				String sql13="update gl_limomanagement set bstatus=7 where docno="+bookingno+" and job='"+result.get(0).get("job")+"' ";
				System.out.println(sql13);
				status = template.update(sql13)>=1?false:true;
				if(status) throw new CommonException("Insertion in gl_limomanagement failed");
				
				int maxdoc=0;
				String strmaxdoc="select coalesce(max(doc_no)+1,1) maxdoc from gl_limojobclose";
				SqlRowSet rsstrmaxdoc = template.queryForRowSet(strmaxdoc);
				while(rsstrmaxdoc.next()){
					maxdoc=rsstrmaxdoc.getInt("maxdoc");
				}
				
				String usersql = "SELECT user_name user FROM my_user WHERE doc_no="+userid;
				SqlRowSet usersqlRowSet = template.queryForRowSet(usersql);
				String username="";
				if(usersqlRowSet.next()) {
					username=usersqlRowSet.getString("user");
				}
				
				String systemnote="Ended Trip of " + bookingno + " - "+result.get(0).get("job")+" - "+ sqldate+" - "+time+" - "+km+" - "+result.get(0).get("dropofflocation")+" - by "+username;
				String sqllog = "INSERT INTO gl_limomgmtlog (bookdocno, jobname, brhid, userid, logdate, remarks, systemremarks) VALUES ('"+bookingno+"','"+result.get(0).get("job")+"','"+brhid+"','"+userid+"', now(), '"+remarks+"','"+systemnote+"')";
				status = template.update(sqllog)>=1?false:true;
				if(status) throw new CommonException("Insertion in gl_limomgmtlog failed");
				
				System.out.println(bookingno);
				System.out.println(result.get(0).get("tdocno").toString());
				System.out.println(result.get(0).get("job").toString());
				System.out.println(result.get(0).get("tarifdocno").toString());
				System.out.println(resultsqlr.get(0).get("startkm").toString());
				System.out.println(stime.toString());
				System.out.println(resultsqlr.get(0).get("startkm").toString());
				System.out.println(sqldate.toString());
				
				/*CalculateData(bookingno, result.get(0).get("tdocno").toString(), result.get(0).get("job").toString(), result.get(0).get("tarifdocno").toString(), result.get(0).get("tarifdetaildocno").toString(), resultsqlr.get(0).get("startdate").toString(), stime.toString(), resultsqlr.get(0).get("startkm").toString(),queryparams.get("date"), time,km, "0", "0", "0", "0", "0", "0");
				
				SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
		        simplejdbc.withProcedureName("limoJobCloseAppDML");
		        simplejdbc.withCatalogName(dbName);
		        Map<String,Object> inparams=new HashMap<String,Object>();
		        inparams.put("vbookdocno", bookingno);
		        inparams.put("vjobdocno",result.get(0).get("tdocno"));
		        inparams.put("vjobtype", result.get(0).get("type"));
		        inparams.put("vmode", "A");
		        inparams.put("vdtype", "BLJC");
		        inparams.put("vbranchid", queryparams.get("brhid"));
		        inparams.put("vuserid", userid);
		        inparams.put("vcompanyid", "1");
		        inparams.put("docNo", maxdoc);
		        inparams.put("vtype", "APP");
		        
		        SqlParameterSource in=new MapSqlParameterSource(inparams);
		        Map<String,Object> simplejdbcresult=simplejdbc.execute(in);
		        System.out.println("String - "+simplejdbcresult.toString());
		        if(simplejdbcresult.isEmpty()) {
		        	throw new CommonException("Procedure Error");
		        }
		        else {*/
					
		        	boolean tripReportGeneratePrint = printService.tripReportGeneratePrint(result.get(0).get("job").toString(), bookingno);
		        	//boolean tripReportGeneratePrint=true;
		        	System.out.println("Trip Generation - "+tripReportGeneratePrint);
		        	if(!tripReportGeneratePrint) {
		        		throw new CommonException("Print/WhatsApp Error");
		        	}
		        	return tripReportGeneratePrint;
                 //}
         }
         catch (Exception e){
             e.printStackTrace();
             throw new CommonException(e.getMessage());
         }
	}

    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
	private int CalculateData(String bookdocno,String jobdocno, String jobname , String tarifdocno , String tarifdetaildocno, String startdate , 
			String starttime , String startkm , String enddate , String endtime, String endkm, String fuelchg,String parkingchg , String otherchg , 
			String greetchg , String vipchg , String boquechg) {
		int insertval=0;
		try{
			System.out.println("Booking Doc No:"+bookdocno);
			System.out.println("Job Doc No:"+jobdocno);
			System.out.println("Job Name:"+jobname);
			System.out.println("Tarif Master Doc No:"+tarifdocno);
			System.out.println("Tarif Detail Doc No:"+tarifdetaildocno);
			System.out.println("endkm:"+endkm);
			System.out.println("startkm:"+startkm);
			
			//Getting tarif details.
			int guestno=0;
			int cldocno=0;
			int pickuplocid=0,dropofflocid=0;
			double kmrestrict=0.0,tarif=0.0,exkmrate=0.0,extimerate=0.0,exhourrate=0.0,nighttarif=0.0,nightexhourrate=0.0,totalhours=0.0,
					transferextrahours=0.0,totalkm=0.0,extrakm=0.0,extrakmamt=0.0,extrahouramt=0.0,extranighthouramt=0.0,limoexcesshours=0.0,limoexcessdayhours=0.0,limoexcessnighthours=0.0,limonighttarifamt=0.0;
			String timerestrict="",nightstarttime="",nightendtime="",timeafteraddhours="",timeafteraddmins="",jobtype="";
			int blockhrs=0,nightstartmethod=0,nightendmethod=0;
			String strtarif="",strnighttarif="",strtotalhours="",strtransferaddhours="",strtransferaddmins="",strtransferextrahours="",strmaster="",strinsert="",
					strlimoaddhours="",limotimeafteraddhours="",strlimocheckexcesshrs="",strlimo="",strnightlimohours="";
			java.sql.Date sqlstartdate=null,sqlenddate=null;
			ArrayList<String> queryarray=new ArrayList<String>();
			jobtype=jobname.charAt(0)=='T'?"Transfer":"Limo";
			if(!startdate.equalsIgnoreCase("undefined") && !startdate.equalsIgnoreCase("")){
				System.out.println("Date ="+startdate );
				sqlstartdate=objcommon.changeStringtoSqlDate(startdate);
			}
			if(!enddate.equalsIgnoreCase("undefined") && !enddate.equalsIgnoreCase("")){
				sqlenddate=objcommon.changeStringtoSqlDate(enddate);
			}
			strmaster="select guestno,cldocno from gl_limobookm where status=3 and doc_no="+bookdocno;
			System.out.println("Master Query: "+strmaster);
			SqlRowSet rsmaster = template.queryForRowSet(strmaster);
			while(rsmaster.next()){
				guestno=rsmaster.getInt("guestno");
				cldocno=rsmaster.getInt("cldocno");
			}
			
			if(jobtype.equalsIgnoreCase("Transfer")){
				//Getting Pickup Location and Dropoff Location
				String strgetlocation="select pickuplocid pickuplocid,dropfflocid dropofflocid,triptype from gl_limobooktransfer where bookdocno="+bookdocno+" and doc_no="+jobdocno;
				String triptype="";
				SqlRowSet rslocation = template.queryForRowSet(strgetlocation);
				while(rslocation.next()){
					pickuplocid=rslocation.getInt("pickuplocid");
					dropofflocid=rslocation.getInt("dropofflocid");
					triptype=rslocation.getString("triptype");
				}
				if(triptype.equalsIgnoreCase("Arrival")){
					String strgetparkingfee="select round(coalesce(amount,0.0),2) amount from gl_limoparkingfee where status=3 and cldocno="+cldocno+" and locationdocno="+pickuplocid;
					SqlRowSet rsgetparkingfee = template.queryForRowSet(strgetparkingfee);
					while(rsgetparkingfee.next()){
						parkingchg=rsgetparkingfee.getString("amount");
					}
				}
				strtarif="select estdist kmrestrict,esttime timerestrict,tarif,exdistrate exkmrate,extimerate from gl_limobooktransfer where bookdocno="+bookdocno+" and doc_no="+jobdocno;
				System.out.println(strtarif);
			}
			else if(jobtype.equalsIgnoreCase("Limo")){
				strtarif="select blockhrs,tarif,exhrrate,nighttarif,nightexhrrate from gl_limobookhours where bookdocno="+bookdocno+" and doc_no="+jobdocno;
			}
			SqlRowSet rstarif = template.queryForRowSet(strtarif);
			while(rstarif.next()){
				if(jobtype.equalsIgnoreCase("Transfer")){
					kmrestrict=rstarif.getDouble("kmrestrict");
					timerestrict=rstarif.getString("timerestrict");
					tarif=rstarif.getDouble("tarif");
					exkmrate=rstarif.getDouble("exkmrate");
					extimerate=rstarif.getDouble("extimerate");
				}
				else if(jobtype.equalsIgnoreCase("Limo")){
					blockhrs=rstarif.getInt("blockhrs");
					tarif=rstarif.getDouble("tarif");
					exhourrate=rstarif.getDouble("exhrrate");
					nighttarif=rstarif.getDouble("nighttarif");
					nightexhourrate=rstarif.getDouble("nightexhrrate");
				}
			}

			//Getting Night start and end from gl_config

			strnighttarif="select (select method from gl_config where field_nme='nightendtime') nightendmethod,"+
				" (select description from gl_config where field_nme='nightendtime') nightendvalue,"+
				" (select method from gl_config where field_nme='nightstarttime') nightstartmethod,"+
				" (select description from gl_config where field_nme='nightstarttime')nightstartvalue";
			SqlRowSet rsnighttarif = template.queryForRowSet(strnighttarif);
			while(rsnighttarif.next()){
				nightstartmethod=rsnighttarif.getInt("nightstartmethod");
				nightstarttime=rsnighttarif.getString("nightstartvalue");
				nightendmethod=rsnighttarif.getInt("nightendmethod");
				nightendtime=rsnighttarif.getString("nightendvalue");
			}
			
			strtotalhours="select timestampdiff(minute,concat('"+sqlstartdate+"',' ','"+starttime+"'),concat('"+sqlenddate+"',' ','"+endtime+"'))/60 totalhours";
			SqlRowSet rstotalhours = template.queryForRowSet(strtotalhours);
			while(rstotalhours.next()){
				totalhours=rstotalhours.getDouble("totalhours");
			}
			
			if(jobtype.equalsIgnoreCase("Transfer")){
				
				//To subtract the time restrict;here we add the time restrict with the startdate and time
				
				strtransferaddhours="select DATE_FORMAT(TIMESTAMPADD(HOUR,"+timerestrict.split(":")[0]+",concat('"+sqlstartdate+"',' ','"+starttime+"')), '%H:%i') timeafteraddhours";
				System.out.println(strtransferaddhours);
				SqlRowSet rstimeafteraddhours = template.queryForRowSet(strtransferaddhours);
				while(rstimeafteraddhours.next()){
					timeafteraddhours=rstimeafteraddhours.getString("timeafteraddhours");
				}
				
				strtransferaddmins="select DATE_FORMAT(TIMESTAMPADD(MINUTE,"+timerestrict.split(":")[1]+",concat('"+sqlstartdate+"',' ','"+timeafteraddhours+"')), '%H:%i') timeafteraddmins";
				System.out.println(strtransferaddmins);
				SqlRowSet rstimeafteraddmins = template.queryForRowSet(strtransferaddmins);
				while(rstimeafteraddmins.next()){
					timeafteraddmins=rstimeafteraddmins.getString("timeafteraddmins");
				}
				
				strtransferextrahours="select TIMESTAMPDIFF(MINUTE,concat('"+sqlstartdate+"',' ','"+timeafteraddmins+"'),concat('"+sqlenddate+"',' ','"+endtime+"'))/60 transferextrahours";
				System.out.println(strtransferextrahours);
				SqlRowSet rstransferextrahours = template.queryForRowSet(strtransferextrahours);
				while(rstransferextrahours.next()){
					transferextrahours=rstransferextrahours.getDouble("transferextrahours");
				}
			}
			else if(jobtype.equalsIgnoreCase("Limo")){
				
				//To subtract the time restrict,here we add the block hours whith the start date and time.
				
				strlimoaddhours="select DATE_FORMAT(TIMESTAMPADD(HOUR,"+blockhrs+",concat('"+sqlstartdate+"',' ','"+starttime+"')), '%H:%i') limotimeafteraddhours";
				
				SqlRowSet rslimoafteraddhours = template.queryForRowSet(strlimoaddhours);
				while(rslimoafteraddhours.next()){
					limotimeafteraddhours=rslimoafteraddhours.getString("limotimeafteraddhours");
				}
				
				//Checking end time is less than time after adding block hours and therby finding excess hours.
				
				strlimocheckexcesshrs="select if(endtime<blocktime,0,timestampdiff(minute,blocktime,endtime)/60) limoexcesshrs from ("+
				" select timestamp(concat('"+sqlenddate+"',' ','"+endtime+"')) endtime,timestamp(concat('"+sqlstartdate+"',' ','"+limotimeafteraddhours+"')) blocktime)m";
				SqlRowSet rslimocheckblockhrs = template.queryForRowSet(strlimocheckexcesshrs);
				System.out.println("Total Limo Excess Hours Query: "+strlimocheckexcesshrs);
				while(rslimocheckblockhrs.next()){
					limoexcesshours=rslimocheckblockhrs.getDouble("limoexcesshrs");
				}
				//Getting Night Hours
				//1.Check End time is greater than night starttime then nighthours=endtime-nightstarttime
				//2.Check end time is greater than night nightendtime then nighthours=nightendtime-nightstarttime
				//3.Check end time is less than night starttime then nighthours=0;
				//4.Day hours=totalexcesshours-nighthours.
				if(sqlstartdate.compareTo(sqlenddate)==0){
					strnightlimohours="select case when (endtime>=nightstarttime and endtime<=nightendtime) then timestampdiff(minute,nightstarttime,endtime)/60"+
					" when (endtime>=nightstarttime and endtime>nightendtime) then timestampdiff(minute,nightstarttime,nightendtime)/60"+
					" when endtime<nightstarttime then 0 else 0 end nighthours from (select concat('"+sqlenddate+"',' ','"+endtime+"') endtime,"+
					" concat('"+sqlstartdate+"',' ','"+nightstarttime+"') nightstarttime,concat(date_add('"+sqlenddate+"',interval 1 day),' ','"+nightendtime+"') nightendtime)m";
				}
				else if(sqlstartdate.compareTo(sqlenddate)>0){
					strnightlimohours="select case when (endtime>=nightstarttime and endtime<=nightendtime) then timestampdiff(minute,nightstarttime,endtime)/60"+
					" when (endtime>=nightstarttime and endtime>nightendtime) then timestampdiff(minute,nightstarttime,nightendtime)/60"+
					" when endtime<nightstarttime then 0 else 0 end nighthours from (select concat('"+sqlenddate+"',' ','"+endtime+"') endtime,"+
					" concat('"+sqlstartdate+"',' ','"+nightstarttime+"') nightstarttime,concat('"+sqlenddate+"',' ','"+nightendtime+"') nightendtime)m";
				}
				else{
					System.out.println("else condition");

				}
					System.out.println("Night Checking"+strnightlimohours);
					SqlRowSet rsnightlimohours = template.queryForRowSet(strnightlimohours);
					while(rsnightlimohours.next()){
						limoexcessnighthours=rsnightlimohours.getDouble("nighthours");
					}	
					limoexcessnighthours=limoexcessnighthours<0?limoexcessnighthours*-1:limoexcessnighthours;
				
				limoexcessdayhours=limoexcesshours-limoexcessnighthours;
				
			}
			
			//Calculating Total kms and charges
			totalkm=Double.parseDouble(endkm)-Double.parseDouble(startkm);
			extrakm=totalkm-kmrestrict;
			if(extrakm>0){
				extrakmamt=extrakm*exkmrate;
			}
				
			//Calculating extra hour charge.
			if(jobtype.equalsIgnoreCase("Transfer")){
				if(transferextrahours>0){
					extrahouramt=transferextrahours*extimerate;
				}
			}
			else if(jobtype.equalsIgnoreCase("Limo")){
				if(limoexcessdayhours>0){
					extrahouramt=limoexcessdayhours*exhourrate;
				}
				if(limoexcessnighthours>0){
					extranighthouramt=limoexcessnighthours*nightexhourrate;
					limonighttarifamt=nighttarif;
				}
			}
			

			if(jobtype.equalsIgnoreCase("Transfer")){
				double tot=0.0;
				String strtot="select "+tarif+"+"+extrakmamt+"+"+extrahouramt+"+"+fuelchg+"+"+parkingchg+"+"+otherchg+"+"+greetchg+"+"+vipchg+"+"+boquechg+" total";
				SqlRowSet rstotal = template.queryForRowSet(strtot);
				while(rstotal.next()){
					tot=rstotal.getDouble("total");
				}
				System.out.println("Tot: "+tot);
				strinsert="insert into gl_limojobclosecalc(bookdocno,jobdocno,jobtype,jobname,guestno,total,tarif,exkmchg,exhrchg,fuelchg,parkingchg,otherchg,greetchg,vipchg,boquechg,status,startdate,starttime,startkm,closedate,closetime,closekm)values("+bookdocno+","+jobdocno+",'"+jobtype+"','"+jobname+"',"+guestno+","+tot+","+tarif+","+extrakmamt+","+extrahouramt+","+fuelchg+","+parkingchg+","+otherchg+","+greetchg+","+vipchg+","+boquechg+",3,'"+sqlstartdate+"','"+starttime+"',"+startkm+",'"+sqlenddate+"','"+endtime+"',"+endkm+")";
			}
			else if(jobtype.equalsIgnoreCase("Limo")){
				double tot=0.0;
				String strtot="select "+tarif+"+"+extrahouramt+"+"+extranighthouramt+"+"+limonighttarifamt+"+"+fuelchg+"+"+parkingchg+"+"+otherchg+"+"+greetchg+"+"+vipchg+"+"+boquechg+" total";
				SqlRowSet rstotal = template.queryForRowSet(strtot);
				while(rstotal.next()){
					tot=rstotal.getDouble("total");
				}
				strinsert="insert into gl_limojobclosecalc(bookdocno,jobdocno,jobtype,jobname,guestno,total,tarif,nighttarif,exhrchg,exnighthrchg,fuelchg,parkingchg,otherchg,greetchg,vipchg,boquechg,status,startdate,starttime,startkm,closedate,closetime,closekm)values("+bookdocno+","+jobdocno+",'"+jobtype+"','"+jobname+"',"+guestno+","+tot+","+tarif+","+limonighttarifamt+","+extrahouramt+","+extranighthouramt+","+fuelchg+","+parkingchg+","+otherchg+","+greetchg+","+vipchg+","+boquechg+",3,'"+sqlstartdate+"','"+starttime+"',"+startkm+",'"+sqlenddate+"','"+endtime+"',"+endkm+")";
				
			}
			String strdelete="delete from gl_limojobclosecalc where bookdocno="+bookdocno+" and jobdocno="+jobdocno+" and jobtype='"+jobtype+"'";
			int deleteval=template.update(strdelete);
			insertval=template.update(strinsert);
			if(insertval>0){
			}
		}
		catch(Exception e){
			//return 0;
			e.printStackTrace();
		}
		finally{
		}
		return insertval;
		
	}

	public List<LimoDataModel> getclient(String docno) {
		String dno = docno.split("-")[0];
		String job = docno.split("-")[1];
		String sql = "SELECT COALESCE(CONCAT(guest,' - ',CLIENT),'') client , clientid, brhid FROM gl_limomanagement WHERE job='"+job+"' and docno="+dno;
		System.out.println(sql);
   	 return template.query(sql, new RowMapper<LimoDataModel>() {
            @Override
            public LimoDataModel mapRow(ResultSet rs, int rowNum) throws SQLException {
           	 LimoDataModel objtemp=new LimoDataModel();
                objtemp.setClient(rs.getString("client"));
                objtemp.setClientdocno(rs.getString("clientid"));
                objtemp.setBranch(rs.getString("brhid"));
                return objtemp;
            }
        });
	}

    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
	public boolean saveExpenses(List<MultipartFile> multifile, Map<String, String> queryparams) {
    	int jobdocno=0;
    	try {
    		String bookno=queryparams.get("bookno");
    		String job=queryparams.get("job");
    		String drvdocno=queryparams.get("drvdocno");
            String userid=queryparams.get("userid");
            String brhid=queryparams.get("brhid");
            String cldocno=queryparams.get("cldocno");
            String paytype=queryparams.get("paytype");
            String cardtype=queryparams.get("cardtype");
            String chequeno=queryparams.get("chequeno");
            String cardno=queryparams.get("cardno");
            java.sql.Date sqlchequedate=objcommon.changeStringtoSqlDate(queryparams.get("chequedate"));
            String amount=queryparams.get("amount");
            String desc=queryparams.get("description");
            String numericscard="";
            String numericscheque="";
            if(paytype.trim().equalsIgnoreCase("2")) {
            	numericscard=cardno;
            }
            else if(paytype.trim().equalsIgnoreCase("3")) {
            	numericscheque=chequeno;
            }
            if(paytype==null || paytype.trim().equalsIgnoreCase("") || paytype.trim().equalsIgnoreCase("undefined")) {
            	paytype="0";
            }
            if(cardtype==null || cardtype.trim().equalsIgnoreCase("") || cardtype.trim().equalsIgnoreCase("undefined")) {
            	cardtype="0";
            }
			AtomicInteger errorstatus = new AtomicInteger();
			String sqldocno = "Select coalesce(max(doc_no),0)+1 docno from gl_jobexp";
			SqlRowSet rssqldocno = template.queryForRowSet(sqldocno);
            if(rssqldocno.next()) jobdocno = rssqldocno.getInt("docno");
            final int jbdocno=jobdocno;
            String sqlInsert = "INSERT INTO `gl_jobexp` (`doc_no`,`bookno`,`job`,`paytype`,`cardtype`,`cardno`,`chqdate`,`chqno`,`amount`,`desc`,`date`) VALUES ("+jobdocno+","+bookno+",'"+job+"',"+paytype+",'"+cardtype+"','"+numericscard+"','"+sqlchequedate+"','"+numericscheque+"','"+amount+"','"+desc+"',NOW());";
            boolean status = template.update(sqlInsert)>=1?false:true;
			if(status) {
				throw new CommonException("Inserton at gl_jobexp Error");
			}
			if(multifile!=null) {
				int bookingno = jobdocno;
				List<InspectionModel> folderpath = dto.getImagePath(template);
				AtomicInteger filesrno = new AtomicInteger();
				System.out.println("MultiFile Length - "+multifile.size());
				multifile.forEach(file -> {
					System.out.println("Inside Each Item");
					byte[] bytes = new byte[1000000];
					try {
						filesrno.getAndIncrement();
						String dirname = "LIMO";
						File dir = new File(folderpath.get(0).getImagepath() + "/attachment/" + dirname);
						dir.mkdirs();
						String filename = "LIMO" + "-" + job + "-" + filesrno + ".jpg";
						System.out.println(filename);
						bytes = file.getBytes();
						File newfile = new File(
								folderpath.get(0).getImagepath() + "/attachment/" + dirname + "/" + filename);
						Path path = Paths.get(folderpath.get(0).getImagepath() + "/attachment/" + dirname + "/" + filename);
						Files.write(path, bytes);

						SimpleJdbcCall simplejdbc = new SimpleJdbcCall(template);
						simplejdbc.withProcedureName("AppFileAttach");
						simplejdbc.withCatalogName(dbName);
						Map<String, Object> inparams = new HashMap<String, Object>();
						inparams.put("d_type", "BMG");    // changed it to show the file in erp application before value 'LIMO'
						inparams.put("docno", bookno + "");
						inparams.put("brhid", brhid);
						inparams.put("username", "App User");
						inparams.put("path", String.valueOf(path));
						inparams.put("filename", filename);
						inparams.put("descptn", job+"- Attachment for Expense for App");
						inparams.put("reftypid", "1");
						inparams.put("srNo", java.sql.Types.INTEGER);
						inparams.put("chkclientview", "1");

						SqlParameterSource in = new MapSqlParameterSource(inparams);
						Map<String, Object> simplejdbcresult = simplejdbc.execute(in);

						if (simplejdbcresult.get("srno") != null
								&& !simplejdbcresult.get("srno").toString().equalsIgnoreCase("undefined")
								&& Integer.parseInt(simplejdbcresult.get("srno").toString()) > 0) {
							String sqlUp = "update my_fileattach set jobname='"+job+"' where doc_no="+bookingno+" and brhid="+brhid+" and sr_no="+simplejdbcresult.get("srno").toString()+"AND filename='"+filename+"'";
							int ress = template.update(sqlUp);
							if(ress<=0) throw new CommonException("Error in Files attach");
						} else {
							errorstatus.set(1);
							throw new CommonException("Procedure Error");
						}
					} catch (IOException e) {
						errorstatus.set(1);
						throw new CommonException(e.getMessage());
					}
				});
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommonException(e.getMessage());
		}

		return true;
	}

	public List<LimoDataModel> getjobstoassign() {
		String sql = "SELECT CONCAT('GW-',b.code,DATE_FORMAT(lb.date,'%Y'),LPAD(bm.`docno`, 6, '0'),' - ',veh.reg_no,' - ',veh.fleet_no,' - ',veh.flname) jobno, COALESCE(bm.docno,0) docno   FROM gl_limomanagement bm INNER JOIN my_brch b ON bm.`brhid`=b.`BRANCH` INNER JOIN gl_limobookm lb ON lb.`doc_no`=bm.`docno` LEFT JOIN gl_limostatusdet st ON st.doc_no=bm.bstatus LEFT JOIN gl_vehmaster veh ON (bm.fno=veh.fleet_no AND statu=3) LEFT JOIN gl_vehplate plt ON veh.pltid=plt.doc_no LEFT JOIN my_user usr ON bm.bookuserid=usr.doc_no WHERE bm.confirm=0 AND bm.bstatus<=1 ORDER BY pickupdate,pickuptime ASC";
   	 	System.out.println("Job Assignment - "+sql);
		return template.query(sql, new RowMapper<LimoDataModel>() {
            @Override
            public LimoDataModel mapRow(ResultSet rs, int rowNum) throws SQLException {
           	 LimoDataModel objtemp=new LimoDataModel();
                objtemp.setJobno(rs.getString("jobno"));
                objtemp.setBookingno(rs.getString("docno"));
                return objtemp;
            }
        });
	}
	
	 public List<MovModel> getStartFleetMinData(String fleetno,String movstage) {

	        String strsql="";
	        if (movstage.equalsIgnoreCase("1")){
	            strsql="select m.doc_no movdocno,v.tran_code,v.doc_no,v.date,v.reg_no,v.fleet_no,v.FLNAME,m.fout fin,round(m.kmout,2) kmin,c.color,\n" +
	                    " g.gid,m.dout din,m.tout tin,m.olocid ilocid,m.obrhid ibrhid from gl_vehmaster v\n" +
	                    " left join gl_vmove m on v.fleet_no=m.fleet_no\n" +
	                    " left join my_color c  on v.clrid=c.doc_no\n" +
	                    " left join gl_vehgroup g on g.doc_no=v.vgrpid\n" +
	                    " where v.statu <> 7 and  v.fstatus in ('L','N') and v.status='OUT' and\n" +
	                    " m.doc_no=(select (max(doc_no)) from gl_vmove where fleet_no=v.fleet_no) and  v.fstatus in ('L','N')  and v.status='OUT' and v.fleet_no="+fleetno;
	        }
	        System.out.println(strsql);
	        return template.query(strsql, new RowMapper<MovModel>() {
	            @Override
	            public MovModel mapRow(ResultSet rs, int rowNum) throws SQLException {
	                MovModel model=new MovModel();
	                model.setDocno(rs.getInt("movdocno"));
	                model.setBrhid(rs.getString("ibrhid"));
	                model.setLocid(rs.getString("ilocid"));
	                model.setMindate(rs.getString("din"));
	                model.setMintime(rs.getString("tin"));
	                model.setMinfuel(rs.getString("fin"));
	                model.setMinkm(rs.getString("kmin"));
	                model.setFleetno(rs.getString("fleet_no"));
	                model.setTrancode(rs.getString("tran_code"));
	                return model;
	            }
	        });
	    }
	 
	 public List<MovModel> getEndFleetMinData(String fleetno,String movstage) {

	        String strsql="";
	        if (movstage.equalsIgnoreCase("1")){
	            strsql="select m.doc_no movdocno,v.tran_code,v.doc_no,v.date,v.reg_no,v.fleet_no,v.FLNAME,m.fout fin,ROUND(an.startkm,2) kmin,c.color,\n" +
	                    " g.gid,an.startdate din,an.starttime tin,m.ilocid,an.brhid ibrhid from gl_vehmaster v\n" +
	                    " left join gl_vmove m on v.fleet_no=m.fleet_no\n" +
	                    " left join my_color c  on v.clrid=c.doc_no\n" +
	                    " left join gl_vehgroup g on g.doc_no=v.vgrpid LEFT JOIN an_starttripdet an ON v.fleet_no=an.fleet \n" +
	                    " where v.statu <> 7 and  v.fstatus in ('L','N') and v.status='OUT' and\n" +
	                    " m.doc_no=(select (max(doc_no)) from gl_vmove where fleet_no=v.fleet_no) AND an.rowno=(SELECT (MAX(rowno)) FROM an_starttripdet WHERE fleet=v.fleet_no) and  v.fstatus in ('L','N')  and v.status='OUT' and v.fleet_no="+fleetno;
	        }
	        System.out.println(strsql);
	        return template.query(strsql, new RowMapper<MovModel>() {
	            @Override
	            public MovModel mapRow(ResultSet rs, int rowNum) throws SQLException {
	                MovModel model=new MovModel();
	                model.setDocno(rs.getInt("movdocno"));
	                model.setBrhid(rs.getString("ibrhid"));
	                model.setLocid(rs.getString("ilocid"));
	                model.setMindate(rs.getString("din"));
	                model.setMintime(rs.getString("tin"));
	                model.setMinfuel(rs.getString("fin"));
	                model.setMinkm(rs.getString("kmin"));
	                model.setFleetno(rs.getString("fleet_no"));
	                model.setTrancode(rs.getString("tran_code"));
	                return model;
	            }
	        });
	    }
	 
	 public List<LimoDataModel> getStatus(){
		 String sql = "SELECT doc_no srno,NAME PROCESS FROM gl_limostatusdet WHERE STATUS=3 AND manual=1 ORDER BY seqno";
	   	 	System.out.println("Job Assignment - "+sql);
			return template.query(sql, new RowMapper<LimoDataModel>() {
	            @Override
	            public LimoDataModel mapRow(ResultSet rs, int rowNum) throws SQLException {
	           	 LimoDataModel objtemp=new LimoDataModel();
	                objtemp.setJobstatusid(rs.getString("srno"));
	                objtemp.setJobstatus(rs.getString("PROCESS"));
	                return objtemp;
	            }
	        });
	 }

	 @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
	public boolean setStatus(Map<String, String> queryparams) {
	        try{
	            String fleetno=queryparams.get("fleetno");
	            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
	            String bookingno=queryparams.get("bookingno");
	            String brhid=queryparams.get("brhid");
	            String userid=queryparams.get("userid");
	            String drvdocno=queryparams.get("drvdocno");
	            String remarks=queryparams.get("remarks");
	            String statusid=queryparams.get("jobstatusid");
	            
	            String starttripdetails = "SELECT bm.brhid,veh.reg_no,veh.fleet_no,veh.flname vehname,COALESCE(sm.sal_name,'') drivername,bm.plocation pickuplocation, bm.dlocation dropofflocation, bm.job, bm.type, COALESCE(bm.tdocno,0) tdocno,bm.tarifdocno,bm.tarifdetaildocno, COALESCE(bm.docno,0) bookingno,COALESCE(CONCAT(bm.docno,'-',bm.job),'') jobno, bm.bstatus jobstatus FROM gl_limomanagement bm INNER JOIN my_brch b ON bm.`brhid`=b.`BRANCH` INNER JOIN gl_limobookm lb ON lb.`doc_no`=bm.`docno` LEFT JOIN gl_multivehassign gvm ON bm.docno=gvm.bookingno AND bm.job=gvm.jobname AND gvm.drvid="+drvdocno+" LEFT JOIN my_salesman sm ON sm.doc_no=gvm.drvid LEFT JOIN gl_limostatusdet st ON st.doc_no=bm.bstatus LEFT JOIN gl_vehmaster veh ON (gvm.fleetno=veh.fleet_no AND statu=3) LEFT JOIN gl_vehplate plt ON veh.pltid=plt.doc_no LEFT JOIN my_user usr ON bm.bookuserid=usr.doc_no WHERE bm.confirm=0 AND bm.docno="+bookingno;
	            List<Map<String, Object>> result = template.queryForList(starttripdetails);
	            System.out.println(result.get(0).get("drivername"));
	            
	            String strgetstatus="select name from gl_limostatusdet where doc_no="+statusid;
				SqlRowSet rsstatus=template.queryForRowSet(strgetstatus);
				String statusname="";
				while(rsstatus.next()){
					statusname=rsstatus.getString("name");
				}
				
				String updatemultivehstatus="UPDATE gl_multivehassign SET bookingstatus="+statusid+" WHERE bookingno="+bookingno+" AND jobname='"+result.get(0).get("job")+"' AND drvid="+drvdocno;
				int updatemvs = template.update(updatemultivehstatus); 
				if(updatemvs<=0){
					throw new CommonException("gl_multivehassign update error");
				}
				int endCheck=0;
				String checkAllTripsCompleted = "SELECT (COUNT(*)-(SELECT COUNT(*) COUNT FROM gl_multivehassign WHERE  bookingno="+bookingno+" and jobname='"+result.get(0).get("job")+"' AND bookingstatus>=7)) ended FROM gl_multivehassign where bookingno="+bookingno+" and jobname='"+result.get(0).get("job")+"'";
			    System.out.println(checkAllTripsCompleted);
			    SqlRowSet rscheck = template.queryForRowSet(checkAllTripsCompleted);
	            if(rscheck.next()) endCheck = rscheck.getInt("ended");
	             
			    if(endCheck>=1) {
			    	String usersql = "SELECT user_name user FROM my_user WHERE doc_no="+userid;
					SqlRowSet usersqlRowSet = template.queryForRowSet(usersql);
					String username="";
					if(usersqlRowSet.next()) {
						username=usersqlRowSet.getString("user");
					}
					String systemnote="Status Updation to "+statusname+" of "+bookingno+" - "+result.get(0).get("job")+" by "+username;
					String strinsertlog="insert into gl_limomgmtlog(bookdocno, jobname, brhid, userid, logdate,remarks, systemremarks)values("+bookingno+",'"+result.get(0).get("job")+"',"+result.get(0).get("brhid")+","+userid+",now(),'"+remarks+"','"+systemnote+"')";
					int insertlog=template.update(strinsertlog);
					if(insertlog<=0){
						throw new CommonException("Error Entering the log"); 
					} else {
						return true;
					}
			    }
				
				String strsql="update gl_limomanagement set bstatus='"+statusid+"',remarks='"+remarks+"' where docno='"+bookingno+"' and job='"+result.get(0).get("job")+"'";	
			 	int sqlupdate = template.update(strsql);     
				if(sqlupdate<=0){
					throw new CommonException("Limomanagement update error");
				}
				
				String usersql = "SELECT user_name user FROM my_user WHERE doc_no="+userid;
				SqlRowSet usersqlRowSet = template.queryForRowSet(usersql);
				String username="";
				if(usersqlRowSet.next()) {
					username=usersqlRowSet.getString("user");
				}
				String systemnote="Status Updation to "+statusname+" of "+bookingno+" - "+result.get(0).get("job")+" by "+username;
				String strinsertlog="insert into gl_limomgmtlog(bookdocno, jobname, brhid, userid, logdate,remarks, systemremarks)values("+bookingno+",'"+result.get(0).get("job")+"',"+result.get(0).get("brhid")+","+userid+",now(),'"+remarks+"','"+systemnote+"')";
				int insertlog=template.update(strinsertlog);
				if(insertlog<=0){
					throw new CommonException("Error Entering the log"); 
				} else {
					return true;
				}
	            
	        } catch (Exception e) {
	        	e.printStackTrace();
				throw new CommonException(e.getMessage());
	        }
	}
    
	
}
