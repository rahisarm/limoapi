package com.gateway.limoapi.service;

import com.gateway.limoapi.exceptions.CommonException;
import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.helpers.MovementHelper;
import com.gateway.limoapi.model.LimoDataModel;
import com.gateway.limoapi.model.MovModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovementService {

    @Value("${dbtype}")
    private String dbType;

    @Value("${dbname}")
    private String dbName;

    @Autowired
    private ClsCommon objcommon;

    @Autowired
    private JdbcTemplate template;
    MovementHelper movhelper=new MovementHelper();
    public List<MovModel> getMovFleetMinData(String fleetno,String movstage) {

        String strsql="";
        if (movstage.equalsIgnoreCase("1")){
            strsql="select m.doc_no movdocno,v.tran_code,v.doc_no,v.date,v.reg_no,v.fleet_no,v.FLNAME,m.fin,round(m.kmin,2) kmin,c.color,\n" +
                    " g.gid,m.din,m.tin,m.ilocid,m.ibrhid from gl_vehmaster v\n" +
                    " left join gl_vmove m on v.fleet_no=m.fleet_no\n" +
                    " left join my_color c  on v.clrid=c.doc_no\n" +
                    " left join gl_vehgroup g on g.doc_no=v.vgrpid\n" +
                    " where v.statu <> 7 and  v.fstatus in ('L','N') and v.status='IN' and\n" +
                    " m.doc_no=(select (max(doc_no)) from gl_vmove where fleet_no=v.fleet_no) and  v.fstatus in ('L','N')  and v.status='IN' and v.fleet_no="+fleetno;
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

    
    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
    public boolean saveMovInsert(Map<String,String> queryparams) throws SQLException {
        boolean status=false;
        try{
            String fleetno=queryparams.get("fleetno");
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            java.sql.Date sqlbasedate=objcommon.changeStringtoSqlDate(queryparams.get("basedate"));
            String trancode=queryparams.get("trancode");
            String time=queryparams.get("time");
            String km=queryparams.get("km");
            String fuel=queryparams.get("fuel");
            String brhid=queryparams.get("brhid");
            String locid=queryparams.get("locid");
            String userid=queryparams.get("userid");
            String drvdocno=queryparams.get("drvdocno");
            String garageid=queryparams.get("garageid");
            garageid=garageid.equalsIgnoreCase("") || garageid.equalsIgnoreCase("undefined") || garageid.isEmpty()?"0":garageid;
            String type=queryparams.get("type");
            String remarks=queryparams.get("remarks");

            System.out.println(dbName+"::"+this.dbName);
            SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
            simplejdbc.withProcedureName("AppMovementDML");
            simplejdbc.withCatalogName(this.dbName);
            Map<String,Object> inparams=new HashMap<String,Object>();
            
            inparams.put("vdate", sqlbasedate);
            inparams.put("vtrancode", type);
            inparams.put("vbranch", brhid);
            inparams.put("vfleetno", fleetno);
            inparams.put("vbrchout", brhid);
            inparams.put("vfout", fuel);
            inparams.put("vtout", time);
            inparams.put("vkmout", (int) Double.parseDouble(km));
            inparams.put("vremarks", remarks);
            inparams.put("vdout", sqldate);
            inparams.put("vstatus", "OUT");
            inparams.put("valoc", locid);
            inparams.put("userId", userid);
            inparams.put("branchid", brhid);
            inparams.put("docNo", Types.INTEGER);
            inparams.put("vmode", "A");
            inparams.put("vdrvid", drvdocno);
            inparams.put("vstaffid", "0");
            inparams.put("vaccd", "0");
            inparams.put("vgarageid", garageid);
            inparams.put("vdelivery", "0");
            inparams.put("vcollection", "0");
            inparams.put("vdtype", "MOV");
            inparams.put("voutbranch", brhid);
            inparams.put("vvehtrancode", trancode);
            
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
        catch (Exception e){
            e.printStackTrace();
            status=false;
        }
        return status;
    }

    public List<MovModel> getMovExistData(String rdocno) {
        String strsql="select plt.code_name platecode,veh.reg_no,veh.flname,nrm.vehtrancode,nrm.doc_no,nrm.fleet_no,nrm.movtype,nrm.delivery,nrm.collection,\n" +
                "case when mov.status='OUT' then dout else din end mindate,\n" +
                "case when mov.status='OUT' then tout else tin end mintime,\n" +
                "case when mov.status='OUT' THEN IF(kmout>COALESCE(an.endkm,0),kmout,an.endkm) ELSE kmin END minkm,\n" +
                "case when mov.status='OUT' then fout else fin end minfuel," +
                "case when mov.status='OUT' then obrhid else ibrhid end brhid,"+
                "case when mov.status='OUT' then olocid else ilocid end locid from gl_nrm nrm left join gl_vehmaster veh on (nrm.fleet_no=veh.fleet_no and veh.statu<>7) left join gl_vehplate plt on veh.pltid=plt.doc_no left join (select max(doc_no) maxmovdocno,rdocno from gl_vmove where rdtype='MOV' group by rdocno) maxmov\n" +
                "on nrm.doc_no=maxmov.rdocno LEFT JOIN an_starttripdet an ON nrm.fleet_no=an.fleet left join gl_vmove mov on maxmov.maxmovdocno=mov.doc_no where 1=1 AND an.rowno=(SELECT (MAX(rowno)) FROM an_starttripdet WHERE fleet=nrm.fleet_no) and nrm.doc_no="+rdocno;
        System.out.println("Mov Exist Data: "+strsql);
        return template.query(strsql, new RowMapper<MovModel>() {
            @Override
            public MovModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                MovModel model=new MovModel();
                model.setTrancode(rs.getString("vehtrancode"));
                model.setFlname(rs.getString("reg_no")+" - "+rs.getString("platecode")+" "+rs.getString("flname"));
                model.setFleetno(rs.getString("fleet_no"));
                model.setMinkm(rs.getString("minkm"));
                model.setMindate(rs.getString("mindate"));
                model.setMintime(rs.getString("mintime"));
                model.setMinfuel(rs.getString("minfuel"));
                model.setDocno(rs.getInt("doc_no"));
                model.setBrhid(rs.getString("brhid"));
                model.setLocid(rs.getString("locid"));
                model.setMovtype(rs.getString("movtype"));
                model.setDelivery(rs.getString("delivery"));
                model.setCollection(rs.getString("collection"));
                return model;
            }
        });
    }
    
    public List<LimoDataModel> getLimoMovExistData(String docno, String driverdocno) {
        String strsql="select veh.fleet_no,veh.reg_no,plt.code_name,veh.flname,gvm.drvid drid,usr.user_name bookuser,bm.bstatus, CONCAT('GW-',b.code,DATE_FORMAT(lb.date,'%Y'),LPAD(bm.`docno`, 6, '0')) bookingno, round(bm.vendoramount,2) vendoramount,round(bm.vendordiscount,2) vendordiscount,round(bm.vendornetamount,2) vendornetamount,bm.assigntype,bm.refno,bm.triptype,bm.otherdetails,bm.pax,concat(coalesce(veh.reg_no,''),' - ',coalesce(plt.code_name,'')) regdetails,coalesce(bm.remarks,'') bookremarks,coalesce(bm.drivername,'') drivername,coalesce(bm.groupname,'') groupname,bm.tarifdocno,bm.tarifdetaildocno,if(date_format(now(),'%Y-%m-%d')=bm.pickupdate,1,0) datval,bm.rowno, coalesce(bm.docno,0) docno, coalesce(lb.voc_no,0) vocno, bm.brhid, bm.job, bm.client, bm.clientid, bm.guest, bm.guestid,"
	    		+ " bm.type, st.name status, bm.blockhrs, bm.pickupdate, bm.pickuptime,bm.plocation pickuplocation,bm.paddress pickupaddress,"
	    		+ " bm.dlocation dropofflocation,bm.daddress dropoffaddress, bm.brand, bm.model, bm.fname, bm.fno, bm.nos, coalesce(bm.tdocno,0) tdocno, bm.remarks "
	    		+ " from gl_limomanagement bm INNER JOIN my_brch b ON bm.`brhid`=b.`BRANCH` INNER JOIN gl_limobookm lb ON lb.`doc_no`=bm.`docno` LEFT JOIN gl_multivehassign gvm ON bm.docno=gvm.bookingno AND bm.job=gvm.jobname AND gvm.drvid="+driverdocno+" LEFT JOIN my_salesman sm ON sm.doc_no=gvm.drvid AND sm.sal_type='DRV' left join gl_limostatusdet st on st.doc_no=bm.bstatus left join gl_vehmaster veh on (gvm.fleetno=veh.fleet_no and statu=3) left join gl_vehplate plt on veh.pltid=plt.doc_no left join my_user usr on bm.bookuserid=usr.doc_no where bm.confirm=0 and bm.docno="+docno;
        System.out.println("Limo Data: "+strsql);
        return template.query(strsql, new RowMapper<LimoDataModel>() {
            @Override
            public LimoDataModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            	LimoDataModel model=new LimoDataModel();
                model.setBranch(rs.getString("brhid"));
                model.setBookingno(rs.getString("bookingno"));
                model.setFleet(rs.getString("fleet_no"));
                model.setReg_no(rs.getString("reg_no"));
                model.setCodename(rs.getString("code_name"));
                model.setVehname(rs.getString("fname"));
                model.setDriverid(rs.getString("drid"));
                model.setClient(rs.getString("client"));
                model.setGuest(rs.getString("guest"));
                return model;
            }
        });
    }

    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
    public boolean closeMovTransfer(Map<String, String> queryparams) {
        System.out.println(queryparams);
            
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String time=queryparams.get("time");
            String km=queryparams.get("km");
            String fuel=queryparams.get("fuel");
            String brhid=queryparams.get("brhid");
            String locid=queryparams.get("locid");
            String fleetno=queryparams.get("fleetno");
            String remarks=queryparams.get("remarks");
            String trancode=queryparams.get("trancode");
            String docno=queryparams.get("docno");
            String drvdocno=queryparams.get("drvdocno");
            String userid=queryparams.get("userid");
            String type=queryparams.get("type");
            List<MovModel> parentdata=objcommon.getParentData(fleetno,template);
            List<MovModel> totaldata=objcommon.getTotalData(sqldate,time,km,fuel,parentdata.get(0).getMaxparent(),template);

            String strupdate="update gl_vmove set status='IN',din='"+sqldate+"',tin='"+time+"',kmin='"+km+"',fin='"+fuel+"',"+
            " ibrhid='"+brhid+"',ilocid='"+locid+"',ireason='"+remarks+"',iaccident=0,ttime='"+totaldata.get(0).getTotalmin()+"',tkm='"+totaldata.get(0).getTotalkm()+"',tfuel='"+totaldata.get(0).getTotalfuel()+"' where rdtype='MOV' and fleet_no='"+fleetno+"' and status='OUT'";
            int update=template.update(strupdate);
            if(update<=0){
                System.out.println(strupdate);
                throw new CommonException("Transfer vmove update error");
            }
            strupdate="update gl_vehmaster set tran_code='"+trancode+"',a_br='"+brhid+"',a_loc='"+locid+"',status='IN' where fleet_no="+fleetno;
            update=template.update(strupdate);
            if(update<=0){
                throw new CommonException("Transfer gl_vehmaster update error");
            }
            strupdate="update gl_nrm set clstatus=1,closedr="+drvdocno+",closeuserid="+userid+",inbranch="+brhid+" where doc_no="+docno;
            System.out.println(strupdate);
            update=template.update(strupdate);
            if(update<=0){
                throw new CommonException("Transfer Master update error");
            }
            return true;
    }

    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class,CommonException.class})
    public boolean saveMovDelivery(Map<String, String> queryparams) {
        System.out.println(queryparams);
        java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
        String time=queryparams.get("time");
        String km=queryparams.get("km");
        String fuel=queryparams.get("fuel");
        String brhid=queryparams.get("brhid");
        String locid=queryparams.get("locid");
        String fleetno=queryparams.get("fleetno");
        String remarks=queryparams.get("remarks");
        String trancode=queryparams.get("trancode");
        String docno=queryparams.get("docno");
        String drvdocno=queryparams.get("drvdocno");
        String userid=queryparams.get("userid");
        String type=queryparams.get("type");
        List<MovModel> parentdata=objcommon.getParentData(fleetno,template);
        List<MovModel> totaldata=objcommon.getTotalData(sqldate,time,km,fuel,parentdata.get(0).getMaxparent(),template);

        String strupdate="update gl_vmove set din='"+sqldate+"',tin='"+time+"',kmin='"+km+"',fin='"+fuel+"',status='IN',trancode='DL',ibrhid='"+brhid+"',ilocid='"+locid+"',"+
        " ttime='"+totaldata.get(0).getTotalmin()+"',tkm='"+totaldata.get(0).getTotalkm()+"',tfuel='"+totaldata.get(0).getTotalfuel()+"' where fleet_no='"+fleetno+"' and rdtype='MOV' and status='OUT'";
        System.out.println(strupdate);
        int update=template.update(strupdate);
        if(update<=0){
            throw new CommonException("Delivery Mov Update Error");
        }
        parentdata=objcommon.getParentData(fleetno,template);
        List<MovModel> idledata=objcommon.getIdleData(sqldate,time,parentdata.get(0).getMaxparent(),template);
        if(dbType.trim().equalsIgnoreCase("MySQL")){
            strupdate="insert into gl_vmove(doc_no,date,fleet_no,rdocno,rdtype,trancode,status,parent,dout,tout,kmout,fout,obrhid,olocid,tideal,emp_id,emp_type,oreason)values('"+parentdata.get(0).getMaxmovdoc()+"',CURDATE(),"+
                    "'"+fleetno+"','"+docno+"','MOV','"+type+"','OUT','"+parentdata.get(0).getMaxparent()+"','"+sqldate+"','"+time+"','"+km+"','"+fuel+"','"+brhid+"','"+locid+"','"+idledata.get(0).getIdlemin()+"',"+drvdocno+",'DRV','Movement Delivery via App')";
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
        	int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove ON");
            strupdate="insert into gl_vmove(doc_no,date,fleet_no,rdocno,rdtype,trancode,status,parent,dout,tout,kmout,fout,obrhid,olocid,tideal,emp_id,emp_type,oreason)values('"+parentdata.get(0).getMaxmovdoc()+"',convert(date,getdate()),"+
                    "'"+fleetno+"','"+docno+"','MOV','"+type+"','OUT','"+parentdata.get(0).getMaxparent()+"','"+sqldate+"','"+time+"','"+km+"','"+fuel+"','"+brhid+"','"+locid+"','"+idledata.get(0).getIdlemin()+"',"+drvdocno+",'DRV','Movement Delivery via App')";
        }

        update=template.update(strupdate);
        if(update<=0){
            throw new CommonException("Delivery Mov Insert Error");
        }
        
        if(dbType.trim().equalsIgnoreCase("MSSQL")){
            int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove OFF");
        }
        
        strupdate="update gl_nrm set delivery=1 where doc_no="+docno;
        update=template.update(strupdate);
        if(update<=0) {
            throw new CommonException("Nrm Delivery update error");
        }
        return true;
    }

    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
    public boolean saveMovCollection(Map<String, String> queryparams) {
        System.out.println(queryparams);
        
        java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
        String time=queryparams.get("time");
        String km=queryparams.get("km");
        String fuel=queryparams.get("fuel");
        String brhid=queryparams.get("brhid");
        String locid=queryparams.get("locid");
        String fleetno=queryparams.get("fleetno");
        String remarks=queryparams.get("remarks");
        String trancode=queryparams.get("trancode");
        String docno=queryparams.get("docno");
        String drvdocno=queryparams.get("drvdocno");
        String userid=queryparams.get("userid");
        String type=queryparams.get("type");
        List<MovModel> parentdata=objcommon.getParentData(fleetno,template);
        List<MovModel> totaldata=objcommon.getTotalData(sqldate,time,km,fuel,parentdata.get(0).getMaxparent(),template);

        String strupdate="update gl_vmove set din='"+sqldate+"',tin='"+time+"',kmin='"+km+"',fin='"+fuel+"',status='IN',ibrhid='"+brhid+"',ilocid='"+locid+"',"+
                " ttime='"+totaldata.get(0).getTotalmin()+"',tkm='"+totaldata.get(0).getTotalkm()+"',tfuel='"+totaldata.get(0).getTotalfuel()+"' where fleet_no='"+fleetno+"' and rdtype='MOV' and status='OUT'";
        int update=template.update(strupdate);
        if(update<=0){
            throw new CommonException("Collection Mov Update Error");
        }
        parentdata=objcommon.getParentData(fleetno,template);
        List<MovModel> idledata=objcommon.getIdleData(sqldate,time,parentdata.get(0).getMaxparent(),template);
        if(dbType.trim().equalsIgnoreCase("MySQL")){
            strupdate="insert into gl_vmove(doc_no,date,fleet_no,rdocno,rdtype,trancode,status,parent,dout,tout,kmout,fout,obrhid,olocid,tideal,emp_id,emp_type,oreason)values('"+parentdata.get(0).getMaxmovdoc()+"',CURDATE(),"+
                    "'"+fleetno+"','"+docno+"','MOV','DL','OUT','"+parentdata.get(0).getMaxparent()+"','"+sqldate+"','"+time+"','"+km+"','"+fuel+"','"+brhid+"','"+locid+"','"+idledata.get(0).getIdlemin()+"',"+drvdocno+",'DRV','Movement Delivery via App')";
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove on");
            strupdate="insert into gl_vmove(doc_no,date,fleet_no,rdocno,rdtype,trancode,status,parent,dout,tout,kmout,fout,obrhid,olocid,tideal,emp_id,emp_type,oreason)values('"+parentdata.get(0).getMaxmovdoc()+"',convert(date,getdate()),"+
                    "'"+fleetno+"','"+docno+"','MOV','DL','OUT','"+parentdata.get(0).getMaxparent()+"','"+sqldate+"','"+time+"','"+km+"','"+fuel+"','"+brhid+"','"+locid+"','"+idledata.get(0).getIdlemin()+"',"+drvdocno+",'DRV','Movement Delivery via App')";
        }

        update=template.update(strupdate);
        if(update<=0){
            throw new CommonException("Collection Mov Insert Error");
        }
        if(dbType.trim().equalsIgnoreCase("MSSQL")){
            int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove OFF");
        }
        strupdate="update gl_nrm set collection=1 where doc_no="+docno;
        update=template.update(strupdate);
        if(update<=0) {
            throw new CommonException("Nrm Collection update error");
        }
        return true;
    }

    @Modifying
    @Transactional(rollbackOn = {Exception.class, SQLException.class, CommonException.class})
    public boolean saveMovGarageBranchClose(Map<String, String> queryparams) {
        System.out.println(queryparams);
        
        java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
        String time=queryparams.get("time");
        String km=queryparams.get("km");
        String fuel=queryparams.get("fuel");
        String brhid=queryparams.get("brhid");
        String locid=queryparams.get("locid");
        String fleetno=queryparams.get("fleetno");
        String remarks=queryparams.get("remarks");
        String trancode=queryparams.get("trancode");
        String docno=queryparams.get("docno");
        String drvdocno=queryparams.get("drvdocno");
        String userid=queryparams.get("userid");
        String type=queryparams.get("type");
        List<MovModel> parentdata=objcommon.getParentData(fleetno,template);
        List<MovModel> totaldata=objcommon.getTotalData(sqldate,time,km,fuel,parentdata.get(0).getMaxparent(),template);

        String strupdate="update gl_vmove set din='"+sqldate+"',tin='"+time+"',kmin='"+km+"',fin='"+fuel+"',status='IN',trancode='DL',ibrhid='"+brhid+"',ilocid='"+locid+"',"+
                " ttime='"+totaldata.get(0).getTotalmin()+"',tkm='"+totaldata.get(0).getTotalkm()+"',tfuel='"+totaldata.get(0).getTotalfuel()+"' where fleet_no='"+fleetno+"' and rdtype='MOV' and status='OUT'";
        int update=template.update(strupdate);
        if(update<=0){
            throw new CommonException("Garage Branch Close Mov Update Error");
        }
        strupdate="update gl_vehmaster set tran_code='"+trancode+"',a_br='"+brhid+"',a_loc='"+locid+"',status='IN' where fleet_no="+fleetno;
        update=template.update(strupdate);
        if(update<=0){
            throw new CommonException("vehicle master Update Error");
        }

        strupdate="update gl_nrm set clstatus=1,closedr="+drvdocno+",closeuserid="+userid+",inbranch="+brhid+" where doc_no="+docno;
        update=template.update(strupdate);
        if(update<=0){
            throw new CommonException("master Update Error");
        }
        return true;
    }

    @Modifying
    @Transactional(rollbackOn = {SQLException.class, Exception.class, CommonException.class})
    public boolean saveRentalReciept(Map<String, String> queryparams) {
        try{
            
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

            if(paytype==null || paytype.trim().equalsIgnoreCase("") || paytype.trim().equalsIgnoreCase("undefined")) {
            	paytype="0";
            }
            if(cardtype==null || cardtype.trim().equalsIgnoreCase("") || cardtype.trim().equalsIgnoreCase("undefined")) {
            	cardtype="0";
            }
            
            
            //Getting clientname,paytype,cardtype
			/*
			 * List<MovModel> misclist=template.query("select\n" +
			 * "(select refname from my_acbook where dtype='CRM' and cldocno="
			 * +cldocno+") refname,\n" +
			 * "(select mode from my_cardm where id=1 and dtype='card' and doc_no="+(
			 * cardtype.equalsIgnoreCase("")?"0":cardtype)+") cardtype,\n" +
			 * "(select case when "+paytype+"=1 then 'CASH' when "
			 * +paytype+"=2 then 'CARD' when "
			 * +paytype+"=3 then 'CHEQUE/ONLINE' else '' end ) paytype", new
			 * RowMapper<MovModel>() {
			 * 
			 * @Override public MovModel mapRow(ResultSet rs, int rowNum) throws
			 * SQLException { MovModel model=new MovModel();
			 * model.setClientname(rs.getString("refname"));
			 * model.setCardtype(rs.getString("cardtype"));
			 * model.setPaytype(rs.getString("paytype")); return model; } });
			 */
            String numerics="";
            if(paytype.trim().equalsIgnoreCase("2")) {
            	numerics=cardno;
            }
            else if(paytype.trim().equalsIgnoreCase("3")) {
            	numerics=chequeno;
            }
            
            SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
            simplejdbc.withProcedureName("appRentalRecieptDML");
            simplejdbc.withCatalogName(dbName);
            Map<String,Object> inparams=new HashMap<String,Object>();
            
            inparams.put("rbranch_no", brhid);
            inparams.put("rcldocno", cldocno);
            inparams.put("rpaytype", paytype);
            inparams.put("rcardtype", cardtype);
            inparams.put("rcheckno", numerics);
            inparams.put("rrdate", sqlchequedate);
            inparams.put("ramount", amount);
            inparams.put("rdescription", desc);
            inparams.put("docNo", Types.INTEGER);
            inparams.put("rusrid", userid);
            
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
        catch(Exception e) {
        	e.printStackTrace();
        }
        return false;
    }
}
