package com.gateway.limoapi.service;

import com.gateway.limoapi.exceptions.CommonException;
import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.DriverTasks;
import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.MiscModel;
import com.gateway.limoapi.model.RepMoveModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DriverTasksService {
    @Value("${dbtype}")
    private String dbType;
    
    @Value("${dbname}")
    private String dbName;

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private ClsCommon objcommon;

    public List<MiscModel> getGlobalData() {
        String strsql="SELECT COALESCE(projectorigin,'') projectorigin FROM my_comp WHERE doc_no=1";
        List<MiscModel> projectorigin = template.query(strsql, new RowMapper<MiscModel>() {
            @Override
            public MiscModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                MiscModel model = new MiscModel();
                model.setErporigin(rs.getString("projectorigin"));
                return model;
            }
        });
        //System.out.println(projectorigin);
        return projectorigin;
    }
    public List<DriverTasks> getDriverTasks(String driverdocno){
        try{
            String sqlfilters="";
            if(!driverdocno.trim().equalsIgnoreCase("") && !driverdocno.trim().equalsIgnoreCase("0")){
                sqlfilters+=" and base.drid="+driverdocno;
            }
            String strsql="";

            if (dbType.trim().equalsIgnoreCase("MySQL")){
                strsql="select base.*,COALESCE(IF(trp.tripstatus=1,date_format(trp.startdate,'%d.%m.%Y'),''),'') tripstartdate,COALESCE(IF(trp.tripstatus=1,trp.starttime,''),'') tripstarttime,COALESCE(IF(trp.tripstatus<2,trp.tripstatus,0),0) tripstatus from ( select 'Delivery' tripmode,r.doc_no,'RAG' rdtype,v.fleet_no,v.reg_no,plate.code_name,v.flname,ac.refname,r.drid,0 repno from gl_ragmt r left join my_acbook ac on (r.cldocno=ac.cldocno and ac.dtype='CRM') left join gl_vehmaster v on r.fleet_no=v.fleet_no left join gl_vehplate plate on v.pltid=plate.doc_no where r.clstatus=0 and r.delstatus=0 and r.delivery=1 union all "+
                        " select 'Delivery' tripmode,c.doc_no,'VCU' rdtype,veh.fleet_no,veh.reg_no,plate.code_name,veh.flname,ac.refname,c.deldrid drid,0 repno from gl_vehcustody c left join my_acbook ac on (c.cldocno=ac.cldocno and ac.dtype='CRM') left join gl_vehmaster veh on c.fleet_no=veh.fleet_no left join gl_vehplate plate on veh.pltid=plate.doc_no where c.rtype='RAG' and c.delivery=1 and c.delstatus is null union all "+
                        " select 'Delivery' tripmode,r.doc_no,'LAG' rdtype,v.fleet_no,v.reg_no,plate.code_name,v.flname,ac.refname,r.drid,0 repno from gl_lagmt r left join my_acbook ac on (r.cldocno=ac.cldocno and ac.dtype='CRM') left join gl_vehmaster v on r.perfleet=v.fleet_no or r.tmpfleet=v.fleet_no left join gl_vehplate plate on v.pltid=plate.doc_no where r.clstatus=0 and r.delstatus=0 and r.delivery=1 union all "+
                        " select case when r.clstatus=0 then 'Replacement-Collection' when r.delstatus=0 then 'Replacement-Delivery' "+
                        " when r.indate is null then 'Replacement-Branch In' else '' end tripmode,r.rdocno doc_no,r.rtype rdtype,v.fleet_no, "+
                        " v.reg_no,plate.code_name,v.flname,ac.refname,coalesce(r.deldrvid,"+driverdocno+") drid,r.doc_no repno from gl_vehreplace r "+
                        " left join gl_vehmaster v on r.rfleetno=v.fleet_no left join gl_vehplate plate on v.pltid=plate.doc_no "+
                        " left join gl_ragmt rg on r.rdocno=rg.doc_no and r.rtype='rag' left join gl_lagmt lg on r.rdocno=lg.doc_no and r.rtype='lag' "+
                        " left join my_acbook ac on (ac.cldocno=case when r.rtype='RAG' then rg.cldocno else lg.cldocno end and ac.dtype='CRM') where "+
                        " r.status=3 and reptype='collection' and r.closestatus=0 union all "+
                        " select case when nrm.movtype='TR' then 'Transfer Branch Close' "+
                        " when nrm.movtype in ('GS','GM','GA') and nrm.delivery=0 then 'Garage Delivery' "+
                        " when nrm.movtype in ('GS','GM','GA') and nrm.delivery=1 and nrm.collection=0 then 'Garage Collection' "+
                        " when nrm.movtype in ('GS','GM','GA') and nrm.collection=1 then 'Garage Branch Close' else '' end tripmode,nrm.doc_no,'MOV' rdtype,veh.fleet_no,veh.reg_no,plate.code_name,veh.flname,'Movement' refname,nrm.drid,0 repno from gl_nrm nrm "+
                        " left join gl_vehmaster veh on nrm.fleet_no=veh.fleet_no "+
                        " left join gl_vehplate plate on veh.pltid=plate.doc_no where nrm.status=3 and nrm.clstatus=0 union all "+
                        " select 'Collection' tripmode,p.agmtno doc_no,p.agmttype rdtype,v.fleet_no,v.reg_no,plate.code_name,v.flname,ac.refname,"+driverdocno+" drid,p.doc_no repno from "+
                        " gl_vehpickup p "+
                        " left join my_acbook ac on (p.cldocno=ac.cldocno and ac.dtype='CRM') "+
                        " left join gl_vehmaster v on p.fleet_no=v.fleet_no "+
                        " left join gl_vehplate plate on v.pltid=plate.doc_no "+
                        " left join gl_ragmt r on p.agmtno=r.doc_no and p.agmttype='RAG' "+
                        " left join gl_lagmt l on p.agmtno=l.doc_no and p.agmttype='LAG' "+
                        " LEFT JOIN an_acollection acol ON p.doc_no=acol.pkupno"+
                        " where p.status=3 and if(p.agmttype='RAG',r.clstatus,l.clstatus)=0 AND (acol.doc_no IS NULL OR acol.bindate IS NULL)) base LEFT JOIN (SELECT MAX(doc_no) maxdocno,rdocno,rdtype,driverid FROM gl_drtrip GROUP BY rdocno,rdtype,driverid) maxtrp ON  "+
                        " base.doc_no=maxtrp.rdocno AND base.rdtype=maxtrp.rdtype AND base.drid=maxtrp.driverid  "+
                        " LEFT JOIN gl_drtrip trp ON maxtrp.maxdocno=trp.doc_no where 1=1 "+sqlfilters;
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                strsql="SELECT base.*,COALESCE(CASE WHEN trp.tripstatus = 1 THEN CONVERT(VARCHAR, trp.startdate, 105) ELSE '' END, '') AS tripstartdate,\n" +
                        " COALESCE(CASE WHEN trp.tripstatus = 1 THEN trp.starttime ELSE '' END, '') AS tripstarttime,\n" +
                        " COALESCE(CASE WHEN trp.tripstatus < 2 THEN trp.tripstatus ELSE 0 END, 0) AS tripstatus\n" +
                        " FROM\n" +
                        " (SELECT 'Delivery' AS tripmode,r.doc_no,'RAG' AS rdtype,v.fleet_no,v.reg_no,plate.code_name,v.flname,ac.refname,r.drid,0 AS repno\n" +
                        " FROM gl_ragmt r\n" +
                        " LEFT JOIN my_acbook ac ON (r.cldocno = ac.cldocno AND ac.dtype = 'CRM')\n" +
                        " LEFT JOIN gl_vehmaster v ON r.fleet_no = v.fleet_no\n" +
                        " LEFT JOIN gl_vehplate plate ON v.pltid = plate.doc_no\n" +
                        " WHERE r.clstatus = 0 AND r.delstatus = 0 AND r.delivery = 1\n" +
                        " UNION ALL\n" +
                        " SELECT 'Delivery' AS tripmode,c.doc_no,'VCU' AS rdtype,veh.fleet_no,veh.reg_no,plate.code_name,veh.flname,ac.refname,c.deldrid AS drid,0 AS repno\n" +
                        " FROM gl_vehcustody c\n" +
                        " LEFT JOIN my_acbook ac ON (c.cldocno = ac.cldocno AND ac.dtype = 'CRM')\n" +
                        " LEFT JOIN gl_vehmaster veh ON c.fleet_no = veh.fleet_no\n" +
                        " LEFT JOIN gl_vehplate plate ON veh.pltid = plate.doc_no\n" +
                        " WHERE c.rtype = 'RAG' AND c.delivery = 1 AND c.delstatus IS NULL\n" +
                        " UNION ALL\n" +
                        " SELECT 'Delivery' AS tripmode,r.doc_no,'LAG' AS rdtype,v.fleet_no,v.reg_no,plate.code_name,v.flname,ac.refname,r.drid,0 AS repno\n" +
                        " FROM gl_lagmt r\n" +
                        " LEFT JOIN my_acbook ac ON (r.cldocno = ac.cldocno AND ac.dtype = 'CRM')\n" +
                        " LEFT JOIN gl_vehmaster v ON r.perfleet = v.fleet_no OR r.tmpfleet = v.fleet_no\n" +
                        " LEFT JOIN gl_vehplate plate ON v.pltid = plate.doc_no\n" +
                        " WHERE r.clstatus = 0 AND r.delstatus = 0 AND r.delivery = 1\n" +
                        " UNION ALL\n" +
                        " SELECT CASE WHEN r.clstatus = 0 THEN 'Replacement-Collection' WHEN r.delstatus = 0 THEN 'Replacement-Delivery' WHEN r.indate IS NULL THEN 'Replacement-Branch In' ELSE '' END AS tripmode,\n" +
                        " r.rdocno AS doc_no,r.rtype AS rdtype,v.fleet_no,v.reg_no,plate.code_name,v.flname,ac.refname,COALESCE(r.deldrvid, "+driverdocno+") AS drid,r.doc_no AS repno\n" +
                        " FROM gl_vehreplace r\n" +
                        " LEFT JOIN gl_vehmaster v ON r.rfleetno = v.fleet_no\n" +
                        " LEFT JOIN gl_vehplate plate ON v.pltid = plate.doc_no\n" +
                        " LEFT JOIN gl_ragmt rg ON r.rdocno = rg.doc_no AND r.rtype = 'rag'\n" +
                        " LEFT JOIN gl_lagmt lg ON r.rdocno = lg.doc_no AND r.rtype = 'lag'\n" +
                        " LEFT JOIN my_acbook ac ON (ac.cldocno = CASE WHEN r.rtype = 'RAG' THEN rg.cldocno ELSE lg.cldocno END AND ac.dtype = 'CRM')\n" +
                        " WHERE r.status = 3 AND reptype = 'collection' AND r.closestatus = 0\n" +
                        " UNION ALL\n" +
                        " SELECT CASE WHEN nrm.movtype = 'TR' THEN 'Transfer Branch Close' \n" +
                        " WHEN nrm.movtype IN ('GS', 'GM', 'GA') AND nrm.delivery = 0 THEN 'Garage Delivery' \n" +
                        " WHEN nrm.movtype IN ('GS', 'GM', 'GA') AND nrm.delivery = 1 AND nrm.collection = 0 THEN 'Garage Collection'\n" +
                        " WHEN nrm.movtype IN ('GS', 'GM', 'GA') AND nrm.collection = 1 THEN 'Garage Branch Close' ELSE '' END AS tripmode,\n" +
                        " nrm.doc_no,'MOV' AS rdtype,veh.fleet_no,veh.reg_no,plate.code_name,veh.flname,'Movement' AS refname,nrm.drid,0 AS repno\n" +
                        " FROM gl_nrm nrm\n" +
                        " LEFT JOIN gl_vehmaster veh ON nrm.fleet_no = veh.fleet_no\n" +
                        " LEFT JOIN gl_vehplate plate ON veh.pltid = plate.doc_no\n" +
                        " WHERE nrm.status = 3 AND nrm.clstatus = 0\n" +
                        " UNION ALL\n" +
                        " SELECT 'Collection' AS tripmode,p.agmtno AS doc_no,p.agmttype AS rdtype,v.fleet_no,v.reg_no,plate.code_name,v.flname,ac.refname,\n" +
                        " "+driverdocno+" AS drid,p.doc_no AS repno FROM gl_vehpickup p \n" +
                        " LEFT JOIN my_acbook ac ON (p.cldocno = ac.cldocno AND ac.dtype = 'CRM') \n" +
                        " LEFT JOIN gl_vehmaster v ON p.fleet_no = v.fleet_no \n" +
                        " LEFT JOIN gl_vehplate plate ON v.pltid = plate.doc_no \n" +
                        " LEFT JOIN gl_ragmt r ON p.agmtno = r.doc_no AND p.agmttype = 'RAG' \n" +
                        " LEFT JOIN gl_lagmt l ON p.agmtno = l.doc_no AND p.agmttype = 'LAG' \n" +
                        " LEFT JOIN an_acollection acol ON p.doc_no=acol.pkupno"+
                        " WHERE p.status=3 and CASE WHEN p.agmttype = 'RAG' THEN r.clstatus ELSE l.clstatus END = 0 AND (acol.doc_no IS NULL OR acol.bindate IS NULL)) base \n" +
                        " LEFT JOIN (SELECT MAX(doc_no) AS maxdocno,rdocno,rdtype,driverid FROM gl_drtrip GROUP BY rdocno,rdtype,driverid) maxtrp ON base.doc_no = maxtrp.rdocno AND base.rdtype = maxtrp.rdtype AND base.drid = maxtrp.driverid \n" +
                        " LEFT JOIN gl_drtrip trp ON maxtrp.maxdocno = trp.doc_no WHERE 1=1 "+sqlfilters;
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
                    return objtemp;
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<DriverTasks> getActiveTask(String rdocno,String rdtype,String tripmode,String repno){
        System.out.println("Recieved Back End:"+rdocno+"::"+rdtype+"::"+tripmode);
        String strsql="";
        try{
            if(rdtype.equalsIgnoreCase("RAG") && (tripmode.equalsIgnoreCase("Delivery"))){
                strsql="select 'Rental #' voctype,agmt.voc_no vocno,plt.code_name platecode,veh.reg_no,veh.fleet_no,0 repno,mov.dout mindate,mov.tout mintime,mov.fout minfuel,round(mov.kmout,0) minkm,mov.obrhid,mov.olocid,agmt.doc_no,'"+tripmode+"' tripmode,ac.refname,veh.flname,'"+rdtype+"' rdtype,0 repstage from gl_ragmt agmt "+
                " left join gl_vehmaster veh on agmt.fleet_no=veh.fleet_no "+
                " left join gl_vehplate plt on veh.pltid=plt.doc_no"+
                " left join my_acbook ac on (agmt.cldocno=ac.cldocno and ac.dtype='CRM') "+
                " left join (select max(doc_no) movdocno,rdocno from gl_vmove where rdtype='RAG' group by rdocno) maxmov on (agmt.doc_no=maxmov.rdocno) "+
                " left join gl_vmove mov on maxmov.movdocno=mov.doc_no where agmt.doc_no="+rdocno+" and agmt.status=3";
            } else if (Integer.parseInt(repno)>0 && !tripmode.equalsIgnoreCase("Collection")) {
                strsql="select base.*,'"+tripmode+"' tripmode from (\n" + " select 'Replacement #' voctype,rep.doc_no vocno,plt.code_name platecode,veh.reg_no,veh.fleet_no,rep.doc_no repno,rep.rdocno doc_no,rep.rtype rdtype,ac.refname,veh.flname,\n" +
                        "  CASE WHEN rep.clstatus=0 THEN rep.rdate WHEN rep.delstatus=0 THEN rep.odate WHEN rep.indate IS NULL THEN rep.cldate ELSE '' END mindate,"+
                " CASE WHEN rep.clstatus=0 THEN rep.rtime WHEN rep.delstatus=0 THEN rep.otime WHEN rep.indate IS NULL THEN rep.cltime ELSE '' END mintime,"+
                " round(CASE WHEN rep.clstatus=0 THEN rep.rkm WHEN rep.delstatus=0 THEN rep.okm WHEN rep.indate IS NULL THEN rep.clkm ELSE 0 END,0) minkm,"+
                " CASE WHEN rep.clstatus=0 THEN 1 WHEN rep.delstatus=0 THEN 2 WHEN rep.indate IS NULL THEN 3 ELSE 0 END repstage,"+
                " CASE WHEN rep.clstatus=0 THEN rep.rfuel WHEN rep.delstatus=0 THEN rep.ofuel WHEN rep.indate IS NULL THEN rep.clfuel ELSE '' END minfuel"+
                " from gl_vehreplace rep left join gl_ragmt rag on rep.rtype='RAG' and rep.rdocno=rag.doc_no\n" +
                        " left join gl_lagmt lag on rep.rtype='LAG' and rep.rdocno=lag.doc_no\n" +
                        " left join my_acbook ac on (ac.dtype='CRM' and ac.cldocno=case when rep.rtype='RAG' then rag.cldocno when rep.rtype='LAG' then lag.cldocno else 0 end)\n" +
                        " left join gl_vehmaster veh on (rep.rfleetno=veh.fleet_no and veh.statu=3)\n" +
                        " left join gl_vehplate plt on veh.pltid=plt.doc_no"+
                        " where rep.closestatus=0 and rep.doc_no="+repno+") base";
            }
            else if(rdtype.equalsIgnoreCase("LAG") && (tripmode.equalsIgnoreCase("Delivery"))){
                strsql="select 'Lease #' voctype,agmt.voc_no vocno,plt.code_name platecode,veh.reg_no,veh.fleet_no,0 repno,mov.dout mindate,mov.tout mintime,mov.fout minfuel,round(mov.kmout,0) minkm,mov.obrhid,mov.olocid,agmt.doc_no,'"+tripmode+"' tripmode,ac.refname,veh.flname,'"+rdtype+"' rdtype,0 repstage from gl_lagmt agmt left join gl_vehmaster veh on (agmt.tmpfleet=veh.fleet_no or agmt.perfleet=veh.fleet_no) left join gl_vehplate plt on veh.pltid=plt.doc_no left join my_acbook ac on (agmt.cldocno=ac.cldocno and ac.dtype='CRM') left join (select max(doc_no) movdocno,rdocno from gl_vmove where rdtype='LAG' group by rdocno) maxmov on (agmt.doc_no=maxmov.rdocno)\n"+
                " left join gl_vmove mov on maxmov.movdocno=mov.doc_no where agmt.doc_no="+rdocno+" and agmt.status=3";
            }
            else if(tripmode.equalsIgnoreCase("Collection")){
                /*strsql="SELECT p.agmtno doc_no,veh.fleet_no,p.doc_no repno,\n" +
                        "CASE WHEN coll.date IS NULL THEN p.pdate ELSE coll.datee END mindate,\n" +
                        "CASE WHEN coll.date IS NULL THEN p.ptime ELSE coll.times END mintime,\n" +
                        "CASE WHEN coll.date IS NULL THEN p.pkm ELSE coll.km END minkm,\n" +
                        "CASE WHEN coll.date IS NULL THEN p.pfuel ELSE coll.fuel END minfuel,\n" +
                        "CASE WHEN COALESCE(coll.pickupdocno,0)=0 THEN 1 ELSE 2 END repstage,'Collection' tripmode,ac.refname,veh.flname,p.agmttype rdtype FROM gl_vehpickup p LEFT JOIN gl_vehmaster veh ON p.fleet_no=veh.FLEET_NO \n" +
                        "LEFT JOIN (SELECT km,fuel,date,times,pkupno pickupdocno FROM an_acollection) coll ON p.doc_no=coll.pickupdocno\n" +
                        "LEFT JOIN my_acbook ac ON ac.cldocno=p.cldocno AND ac.dtype='CRM'\n" +
                        "WHERE p.doc_no="+repno;*/
                if (dbType.trim().equalsIgnoreCase("MySQL")){
                    strsql="SELECT CASE WHEN p.agmttype='RAG' THEN 'Pickup Rental #' WHEN p.agmttype='LAG' THEN 'Pickup Lease #' ELSE '' END voctype,\r\n"
                    		+ "CASE WHEN p.agmttype='RAG' THEN ragmt.voc_no WHEN p.agmttype='LAG' THEN lagmt.voc_no ELSE '' END vocno,p.agmtno doc_no,plt.code_name platecode,veh.reg_no,veh.fleet_no,p.doc_no repno,\n" +
                            " CASE WHEN coll.date IS NULL THEN p.pdate ELSE coll.date END mindate,\n" +
                            " CASE WHEN coll.date IS NULL THEN p.ptime ELSE coll.time END mintime,\n" +
                            " round(CASE WHEN coll.date IS NULL THEN p.pkm ELSE coll.km END,0) minkm,\n" +
                            " CASE WHEN coll.date IS NULL THEN p.pfuel ELSE coll.fuel END minfuel,\n" +
                            " CASE WHEN COALESCE(coll.pickupdocno,0)=0 THEN 1 ELSE 2 END repstage,'Collection' tripmode,ac.refname,veh.flname,p.agmttype rdtype FROM gl_vehpickup p LEFT JOIN gl_vehmaster veh ON p.fleet_no=veh.FLEET_NO left join gl_vehplate plt on veh.pltid=plt.doc_no "+
                            " LEFT JOIN (SELECT km,fuel,datee `date`,times `time`,pkupno pickupdocno FROM an_acollection) coll ON p.doc_no=coll.pickupdocno\n" +
                            " LEFT JOIN my_acbook ac ON ac.cldocno=p.cldocno AND ac.dtype='CRM'\n" +
                            " LEFT JOIN gl_ragmt ragmt ON (p.agmttype='RAG' AND ragmt.doc_no=p.agmtno) "+
                            " LEFT JOIN gl_lagmt lagmt ON (p.agmttype='LAG' AND lagmt.doc_no=p.agmtno) "+
                            " WHERE p.doc_no="+repno;
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    strsql="SELECT CASE WHEN p.agmttype='RAG' THEN 'Pickup Rental #' WHEN p.agmttype='LAG' THEN 'Pickup Lease #' ELSE '' END voctype,\r\n"
                    		+ "CASE WHEN p.agmttype='RAG' THEN ragmt.voc_no WHEN p.agmttype='LAG' THEN lagmt.voc_no ELSE '' END vocno,p.agmtno doc_no,plt.code_name platecode,veh.reg_no,veh.fleet_no,p.doc_no repno,\n" +
                            " CASE WHEN coll.datee IS NULL THEN p.pdate ELSE coll.datee END mindate,\n" +
                            " CASE WHEN coll.datee IS NULL THEN p.ptime ELSE coll.times END mintime,\n" +
                            " round(CASE WHEN coll.datee IS NULL THEN p.pkm ELSE coll.km END,0) minkm,\n" +
                            " CASE WHEN coll.datee IS NULL THEN p.pfuel ELSE coll.fuel END minfuel,\n" +
                            " CASE WHEN COALESCE(coll.pickupdocno,0)=0 THEN 1 ELSE 2 END repstage,'Collection' tripmode,ac.refname,veh.flname,p.agmttype rdtype FROM gl_vehpickup p LEFT JOIN gl_vehmaster veh ON p.fleet_no=veh.FLEET_NO  left join gl_vehplate plt on veh.pltid=plt.doc_no \n"+
                            " LEFT JOIN (SELECT km,fuel,datee,TIMES,pkupno pickupdocno FROM an_acollection) coll ON p.doc_no=coll.pickupdocno\n" +
                            " LEFT JOIN my_acbook ac ON ac.cldocno=p.cldocno AND ac.dtype='CRM'\n" +
                            " LEFT JOIN gl_ragmt ragmt ON (p.agmttype='RAG' AND ragmt.doc_no=p.agmtno) "+
                            " LEFT JOIN gl_lagmt lagmt ON (p.agmttype='LAG' AND lagmt.doc_no=p.agmtno) "+
                            " WHERE p.doc_no="+repno;
                }

            }
            System.out.println(strsql);
            return template.query(strsql, new RowMapper<DriverTasks>() {
                @Override
                public DriverTasks mapRow(ResultSet rs, int rowNum) throws SQLException {
                    DriverTasks objtemp=new DriverTasks();
                    objtemp.setFleetno(rs.getString("fleet_no"));
                    objtemp.setClientname(rs.getString("refname"));
                    objtemp.setFleetname(rs.getString("reg_no")+" - "+rs.getString("platecode")+" "+rs.getString("flname"));
                    objtemp.setRdocno(rs.getInt("doc_no"));
                    objtemp.setRdtype(rs.getString("rdtype"));
                    objtemp.setTripmode(rs.getString("tripmode"));
                    objtemp.setMindate(rs.getString("mindate"));
                    objtemp.setMintime(rs.getString("mintime"));
                    objtemp.setMinkm(rs.getString("minkm"));
                    objtemp.setMinfuel(rs.getString("minfuel"));
                    objtemp.setRepno(rs.getString("repno"));
                    objtemp.setRepstage(rs.getString("repstage"));
                    objtemp.setVoctype(rs.getString("voctype"));
                    objtemp.setVocno(rs.getString("vocno"));
                    objtemp.setRjobtype(rs.getString("jobtype"));
                    return objtemp;
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    @Modifying
    @Transactional(rollbackFor = {CommonException.class})
    public boolean updateRepCollection(java.sql.Date sqldate,String time,String fuel,double km,int repno,int drvdocno){
        try{
            int updatevalue=template.update("update gl_vehreplace set clstatus=1,cldate='"+sqldate+"',cltime='"+time+"',clfuel='"+fuel+"',clkm="+km+",cldrvid="+drvdocno+" where doc_no="+repno);
            System.out.println("Update Value:"+updatevalue);
            if(updatevalue>0){
                return true;
            }
        }
        catch(Exception e){
            throw new CommonException(e.getMessage());
        }
        return false;
    }


    @Modifying
    @Transactional(rollbackFor = {CommonException.class})
    public boolean updateRepDelivery(Date sqldate, String time, String fuel, double km, int repno, int drvdocno, String deliverto) {
        try{

            int updatevalue=template.update("update gl_vehreplace set delstatus=1,deldate='"+sqldate+"',deltime='"+time+"',delfuel='"+fuel+"',delkm="+km+",deldrvid="+drvdocno+",delat='"+deliverto+"' where doc_no="+repno);
            System.out.println("Update Value:"+updatevalue);
            if(updatevalue>0){
                return true;
            }
        }
        catch(Exception e){
            throw new CommonException(e.getMessage());
        }
        return false;
    }

    @Modifying
    @Transactional(rollbackFor = {RuntimeException.class, Error.class, CommonException.class})
    public boolean updateRepClosing(Date sqldate, String time, String fuel, double km, int repno, int drvdocno,int brhid,int locid) {
        try{
            String strsql="update gl_vehreplace set closestatus=1,indate='"+sqldate+"',intime='"+time+"',infuel="+fuel+",inkm="+km+",inbrhid="+brhid+",inloc="+locid+" where doc_no="+repno;
            System.out.println(strsql);
            int updatevalue=template.update(strsql);
            if(updatevalue>0){
            	
                //Getting All Replacement Data
                List<RepMoveModel> repdata=objcommon.getRepData(repno,template);
                
                //Updating IN Vehicle Status
            	int vehupdate=template.update("update gl_vehmaster set status='IN',tran_code='UR',a_br='"+brhid+"',a_loc='"+locid+"',cur_km="+km+" where fleet_no='"+repdata.get(0).getRfleetno()+"'");
            	if(vehupdate<=0) {
            		throw new CommonException("Update Vehicle Master Error");
            	}
                System.out.println("In Branch:"+repdata.get(0).getInbrhid()+"::"+repdata.get(0).getInlocid());
                System.out.println("Out Location:"+repdata.get(0).getObrhid()+"::"+ repdata.get(0).getOlocid());
                //Updating Out Movement with collectdata
                int updatecollectmov=objcommon.updateRepMovData(repdata,template,1,drvdocno);
                if(updatecollectmov<=0){
                    throw new CommonException("Update Movement Error");
                }
                return true;
            }
            else {
                throw new CommonException("Table Update Error");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new CommonException(e.getMessage());
        }

    }


    @Modifying
    @Transactional(rollbackFor = {RuntimeException.class, Error.class, CommonException.class})
    public boolean saveAgmtDelivery(Date sqldate, String time, String fuel, double km, int drvdocno, int rdocno, String rdtype,String fleetno) {
        try{


            int maxparent=0;
            String strtotal="";
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                strtotal="select case when m.rdtype='RAG' then rag.cldocno else lag.cldocno end cldocno,coalesce(rtf.rentaltype,'') rentaltype,case when m.rdtype='RAG' then rag.invtype else lag.inv_type end invtype,(select method from GL_CONFIG WHERE FIELD_NME='monthlycal') monthcalmethod,\n" +
                        "(select value from GL_CONFIG WHERE FIELD_NME='monthlycal') monthcalvalue,obrhid,olocid,(select method from gl_config where field_nme='invOneDayExtra') onedayextraconfig,TIMESTAMPDIFF(SECOND,ts_dout,ts_din)/60 totalmin,("+km+"-kmout) totalkm,("+fuel+"-fout) totalfuel\n" +
                        "    from (select  rdocno,rdtype,obrhid,olocid,cast(concat('"+sqldate+"',' ','"+time+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)\n" +
                        "    ts_dout,kmout,fout from gl_vmove where rdocno="+rdocno+" and rdtype='"+rdtype+"' and status='OUT')m "+
                        " left join gl_ragmt rag on (rag.doc_no="+rdocno+" and m.rdtype='RAG')" +
                        " left join gl_lagmt lag on (lag.doc_no="+rdocno+" and m.rdtype='LAG')"+
                        " left join gl_rtarif rtf on (rag.doc_no=rtf.rdocno and rtf.rstatus=5)";
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                strtotal="select case when m.rdtype='RAG' then rag.cldocno else lag.cldocno end cldocno,coalesce(rtf.rentaltype,'') rentaltype,case when m.rdtype='RAG' then rag.invtype else lag.inv_type end invtype,(select method from GL_CONFIG WHERE FIELD_NME='monthlycal') monthcalmethod,\n" +
                        "(select value from GL_CONFIG WHERE FIELD_NME='monthlycal') monthcalvalue,obrhid,olocid,(select method from gl_config where field_nme='invOneDayExtra') onedayextraconfig,datediff(SECOND,ts_dout,ts_din)/60 totalmin,("+km+"-kmout) totalkm,("+fuel+"-fout) totalfuel\n" +
                        "    from (select  rdocno,rdtype,obrhid,olocid,cast(concat('"+sqldate+"',' ','"+time+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)\n" +
                        "    ts_dout,kmout,fout from gl_vmove where rdocno="+rdocno+" and rdtype='"+rdtype+"' and status='OUT')m "+
                        " left join gl_ragmt rag on (rag.doc_no="+rdocno+" and m.rdtype='RAG')" +
                        " left join gl_lagmt lag on (lag.doc_no="+rdocno+" and m.rdtype='LAG')"+
                        " left join gl_rtarif rtf on (rag.doc_no=rtf.rdocno and rtf.rstatus=5)";
            }
            List<RepMoveModel> totaldata=template.query(strtotal,new RowMapper<RepMoveModel>(){
                @Override
                public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    RepMoveModel objtemp=new RepMoveModel();
                    objtemp.setCldocno(rs.getString("cldocno"));
                    objtemp.setRentaltype(rs.getString("rentaltype"));
                    objtemp.setInvtype(rs.getInt("invtype"));
                    objtemp.setMonthcalmethod(rs.getInt("monthcalmethod"));
                    objtemp.setMonthcalvalue(rs.getInt("monthcalvalue"));
                    objtemp.setObrhid(rs.getString("obrhid"));
                    objtemp.setOlocid(rs.getString("olocid"));
                    objtemp.setOnedayextraconfig(rs.getInt("onedayextraconfig"));
                    objtemp.setTotalmin(rs.getString("totalmin"));
                    objtemp.setTotalkm(rs.getString("totalkm"));
                    objtemp.setTotalfuel(rs.getString("totalfuel"));
                    return objtemp;
                }
            });

            String strupdate="";
            RepMoveModel model=totaldata.get(0);
            strupdate="update gl_vmove set status='IN',din='"+sqldate+"',tin='"+time+"',kmin="+km+",fin="+fuel+",ibrhid="+model.getObrhid()+",ilocid="+model.getOlocid()+",ireason='Agmt Delivery Via App',ttime="+model.getTotalmin()+",tkm="+model.getTotalkm()+",tfuel="+model.getTotalfuel()+" where rdocno="+rdocno+" and rdtype='"+rdtype+"' and status='OUT' and fleet_no="+fleetno;
            System.out.println(strupdate);
            int update=template.update(strupdate);
            if(update<=0){
                throw new CommonException("Movement Update Error");
            }

            int maxmovdoc=0;
            String invtodate=objcommon.getAgmtInvToDate(template,rdocno,rdtype,model.getMonthcalmethod(),model.getMonthcalvalue(),model.getInvtype(),sqldate,model.getOnedayextraconfig());
            List<RepMoveModel> maxparentdata=template.query("select (select max(doc_no) from gl_vmove where fleet_no="+fleetno+" and status='IN') maxparent,(select max(doc_no)+1 from gl_vmove) maxmovdoc",new RowMapper<RepMoveModel>(){
                @Override
                public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    RepMoveModel objtemp=new RepMoveModel();
                    objtemp.setMaxparent(rs.getString("maxparent"));
                    objtemp.setMaxmovdoc(rs.getString("maxmovdoc"));
                    return objtemp;
                }
            });
            maxparent=Integer.parseInt(maxparentdata.get(0).getMaxparent());
            maxmovdoc=Integer.parseInt(maxparentdata.get(0).getMaxmovdoc());

            String agmttrancode="";
            if(model.getRentaltype().equalsIgnoreCase("Monthly")){
                agmttrancode="RM";
            } else if (model.getRentaltype().equalsIgnoreCase("Weekly")) {
                agmttrancode="RW";
            } else if (model.getRentaltype().equalsIgnoreCase("Daily")) {
                agmttrancode="RD";
            }

            //Inserting delivery data to gl_vmove
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                strupdate=" insert into gl_vmove(doc_no,date,rdocno,rdtype,fleet_no,trancode,status,parent,dout,tout,kmout,fout,obrhid,olocid,oreason,tideal,emp_id,emp_type)\n" +
                        "  values("+maxmovdoc+",CURDATE(),"+rdocno+",'"+rdtype+"','"+fleetno+"','"+agmttrancode+"','OUT','"+maxparent+"','"+sqldate+"','"+time+"',"+km+","+fuel+","+model.getObrhid()+","+model.getOlocid()+",'Rental Agmt Delivery via App',0,"+model.getCldocno()+",'CRM')";
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove ON");
                strupdate=" insert into gl_vmove(doc_no,date,rdocno,rdtype,fleet_no,trancode,status,parent,dout,tout,kmout,fout,obrhid,olocid,oreason,tideal,emp_id,emp_type)\n" +
                        "  values("+maxmovdoc+",CONVERT(DATE, GETDATE()),"+rdocno+",'"+rdtype+"','"+fleetno+"','"+agmttrancode+"','OUT','"+maxparent+"','"+sqldate+"','"+time+"',"+km+","+fuel+","+model.getObrhid()+","+model.getOlocid()+",'Rental Agmt Delivery via App',0,"+model.getCldocno()+",'CRM')";
            }

            update=template.update(strupdate);
            if(update<=0){
                throw new CommonException("Movement Insert Error");
            }
            if(dbType.trim().equalsIgnoreCase("MSSQL")){
                int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove OFF");
            }
            //Updating to gl_ragmt
            if(rdtype.equalsIgnoreCase("RAG")){
                strupdate="update  gl_ragmt set delstatus=1,invdate='"+sqldate+"',invtodate='"+invtodate+"' where doc_no="+rdocno;
                update=template.update(strupdate);
                if(update<=0){
                    throw new CommonException("Update Agmt Error");
                }
            }
            return true;

        }
        catch (Exception e){
            e.printStackTrace();
            throw new CommonException(e.getMessage());
        }
    }

    @Modifying
    @Transactional(rollbackFor = {Exception.class,SQLException.class, CommonException.class})
    public boolean saveLeaseAgmtDelivery(Date sqldate, String time, String fuel, double km, int drvdocno, int rdocno, String rdtype, String fleetno) throws SQLException {
        List<RepMoveModel> miscdata=template.query("select agmt.cldocno,mov.obrhid,mov.olocid from gl_lagmt agmt left join gl_vmove mov on (agmt.doc_no=mov.rdocno and mov.rdtype='LAG') where agmt.doc_no=" + rdocno, new RowMapper<RepMoveModel>() {
            @Override
            public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                RepMoveModel model=new RepMoveModel();
                model.setCldocno(rs.getString("cldocno"));
                model.setObrhid(rs.getString("obrhid"));
                model.setOlocid(rs.getString("olocid"));
                return model;
            }
        });
        
        SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
        simplejdbc.withProcedureName("AppLeaseDelUpdateDML");
        simplejdbc.withCatalogName(dbName);
        Map<String,Object> inparams=new HashMap<String,Object>();
        inparams.put("sqlleaseDate", sqldate);
        inparams.put("tempfleet",fleetno);
        inparams.put("perfleet", "");
        inparams.put("sqldelDateout", sqldate);
        inparams.put("delTimeout", time);
        inparams.put("delKmout", km+"");
        inparams.put("branchid", miscdata.get(0).getObrhid());
        inparams.put("delFuelout", fuel);
        inparams.put("docNo", rdocno);
        inparams.put("vehlocationid", miscdata.get(0).getOlocid());
        inparams.put("cientid", miscdata.get(0).getCldocno());
        inparams.put("vmode", "DLY");
        
        SqlParameterSource in=new MapSqlParameterSource(inparams);
        Map<String,Object> simplejdbcresult=simplejdbc.execute(in);
        System.out.println(simplejdbcresult.toString());
        if(simplejdbcresult.get("docno")!=null && !simplejdbcresult.get("docno").toString().equalsIgnoreCase("undefined") && Integer.parseInt(simplejdbcresult.get("docno").toString())>0) {
        	return true;
        }
        else {
        	throw new CommonException("Procedure Error");
        }
        
    }

    @Modifying
    @Transactional(rollbackFor = {CommonException.class})
    public boolean saveAgmtCollection(Date sqldate, String time, String fuel, double km, int drvdocno, int rdocno, String rdtype,String fleetno,String userid,String pickupdocno){
        try{
            List<RepMoveModel> miscdata=template.query("select coalesce(max(doc_no),0)+1 docno from an_acollection", new RowMapper<RepMoveModel>() {
                @Override
                public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    RepMoveModel model=new RepMoveModel();
                    model.setRepno(rs.getInt("docno"));
                    return model;
                }
            });
            int docno=miscdata.get(0).getRepno();
            System.out.println("Android Collection Doc No"+docno);
            //Inserting Collection data to app details
            String strupdate="insert into an_acollection(doc_no,agmtno, agmttype, km, fuel, datee, times, userid, rplno, pkupno, rdtype)"+
                    " values ("+docno+","+rdocno+",'"+rdtype+"',"+km+","+fuel+",'"+sqldate+"','"+time+"',"+userid+",0,"+pickupdocno+",'"+rdtype+"')";
            System.out.println(strupdate);
            int update=template.update(strupdate);
            if(update<=0){
                throw new CommonException("Movement Insert Error");
            }

        }
        catch(Exception e){
            e.printStackTrace();
            throw new CommonException(e.getMessage());
        }
        return true;
    }


    @Modifying
    @Transactional(rollbackFor = {CommonException.class})
    public boolean saveBranchCloseCollection(Date sqldate, String time, String fuel, double km, int drvdocno, int rdocno, String rdtype, String fleetno, String userid, String pickupdocno,String brhid,String locid) {
        List<RepMoveModel> miscdata=template.query("select doc_no docno from an_acollection where pkupno="+pickupdocno, new RowMapper<RepMoveModel>() {
            @Override
            public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                RepMoveModel model=new RepMoveModel();
                model.setRepno(rs.getInt("docno"));
                return model;
            }
        });
        int docno=miscdata.get(0).getRepno();
        System.out.println("Android Collection Docno"+docno);
        if(docno<=0){
            throw new CommonException("No Collection found to close");
        }
        String strupdate="update an_acollection set bindate='"+sqldate+"',bintime='"+time+"',binfuel="+fuel+",binkm="+km+",binbrhid="+brhid+",binlocid="+locid+" where doc_no="+docno;
        int update=template.update(strupdate);
        if(update<=0){
            throw new CommonException("Update Branch Close Error");
        }
        else{
            return true;
        }
    }

    @Modifying
    @Transactional(rollbackFor = {CommonException.class,SQLException.class})
    public boolean startDriverTrip(String rdocno, String rdtype, String driverdocno) {
        String strupdate="";
        if (dbType.trim().equalsIgnoreCase("MySQL")){
            strupdate="insert into gl_drtrip(rdocno,rdtype,driverid,startdate,starttime,tripstatus)values("+rdocno+",'"+rdtype+"',"+driverdocno+",CURDATE(),DATE_FORMAT(NOW(),'%H:%i'),1)";
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            strupdate="insert into gl_drtrip(rdocno,rdtype,driverid,startdate,starttime,tripstatus)values("+rdocno+",'"+rdtype+"',"+driverdocno+",CONVERT(DATE, GETDATE()),CONVERT(VARCHAR(5), GETDATE(), 108),1)";
        }
        int status=template.update(strupdate);
        if(status<=0){
            throw new CommonException("Insert Driver Trip Details Error");
        }
        else{
            return true;
        }
    }

    @Modifying
    @Transactional(rollbackFor = {CommonException.class,SQLException.class})
    public boolean endDriverTrip(String rdocno, String rdtype, String driverdocno) {
        String strupdate="";
        if(dbType.trim().equalsIgnoreCase("MySQL")){
            strupdate="update gl_drtrip set enddate=CURDATE(),endtime=DATE_FORMAT(NOW(),'%H:%i'),tripstatus=2 where rdocno="+rdocno+" and rdtype='"+rdtype+"' and driverid="+driverdocno+" and tripstatus=1";
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            strupdate="update gl_drtrip set enddate=CONVERT(DATE,GETDATE()),endtime=CONVERT(VARCHAR(5),GETDATE(),108),tripstatus=2 where rdocno="+rdocno+" and rdtype='"+rdtype+"' and driverid="+driverdocno+" and tripstatus=1";
        }

        int status=template.update(strupdate);
        if(status<=0){
            throw new CommonException("Insert Driver Trip Details Error");
        }
        else{
            return true;
        }
    }
    
	public List<RepMoveModel> getReplacementData(String rdocno,String rdtype,String tripmode,String repno) {
		// TODO Auto-generated method stub
		try {
			String strsql="SELECT rfleetno,deldate,deltime,round(delkm,2) delkm,delfuel,delat,indate,intime,round(inkm,2) inkm,infuel,inbrhid,inloc inlocid,cldate,cltime,round(clkm,2) clkm,clfuel,reptype,\r\n"
					+ "CASE WHEN rep.clstatus=0 THEN 1 WHEN rep.delstatus=0 THEN 2 WHEN rep.indate IS NULL THEN 3 ELSE 0 END repstage FROM gl_vehreplace rep WHERE doc_no="+repno;
			
			return template.query(strsql, new RowMapper<RepMoveModel>() {
	            @Override
	            public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
	                RepMoveModel objtemp=new RepMoveModel();
	                objtemp.setCollectdate(rs.getString("cldate"));
	                objtemp.setCollecttime(rs.getString("cltime"));
	                objtemp.setCollectkm(rs.getString("clkm"));
	                objtemp.setCollectfuel(rs.getString("clfuel"));
	                
	                objtemp.setDeldate(rs.getString("deldate"));
	                objtemp.setDeltime(rs.getString("deltime"));
	                objtemp.setDelkm(rs.getString("delkm"));
	                objtemp.setDelfuel(rs.getString("delfuel"));
	                objtemp.setAgmttrancode(rs.getString("delat"));
	                
	                objtemp.setIndate(rs.getString("indate"));
	                objtemp.setIntime(rs.getString("intime"));
	                objtemp.setInkm(rs.getString("inkm"));
	                objtemp.setInfuel(rs.getString("infuel"));
	                
	                objtemp.setCldocno(rs.getString("reptype"));
	                objtemp.setRentaltype(rs.getString("repstage"));
	                
	                objtemp.setRfleetno(rs.getString("rfleetno"));
	                return objtemp;
	            }
	        });
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			
		return null;
	}
	
	public List<DropdownModel> getConfigData(String configname) {
		// TODO Auto-generated method stub
		try {
			String strsql="SELECT method,field_nme from gl_config where field_nme='"+configname+"'";
			
			return template.query(strsql, new RowMapper<DropdownModel>() {
	            @Override
	            public DropdownModel mapRow(ResultSet rs, int rowNum) throws SQLException {
	                DropdownModel objtemp=new DropdownModel();
	                objtemp.setDocno(rs.getString("method"));
	                objtemp.setRefname(rs.getString("field_nme"));
	                return objtemp;
	            }
	        });
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			
		return null;
	}
}
