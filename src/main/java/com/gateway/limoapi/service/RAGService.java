package com.gateway.limoapi.service;

import com.gateway.limoapi.exceptions.CommonException;
import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.AgmtModel;
import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.InspectionModel;
import com.gateway.limoapi.model.TarifModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class RAGService {

    @Value("${dbtype}")
    private String dbType;

    @Value("${dbname}")
    private String dbName;

    @Autowired
    JdbcTemplate template;
    
    @Autowired
    private ClsCommon objcommon;

    
    public List<DropdownModel> getRentalReadyFleets(String brhid){
        String strconfig="select method from gl_config where field_nme='restrictContractVeh'";
        List<Map<String,Object>> configlist=template.queryForList(strconfig);

        int restrictContractVeh=0;
        if(configlist.size()>0){
            restrictContractVeh=Integer.parseInt(configlist.get(0).get("method").toString());
        }

        String sqljoin="",sqlfilter="",sqltest="";
        if(restrictContractVeh==1) {
            sqljoin+=" left join gl_ragmt rag on (rag.ofleet_no=v.fleet_no and  rag.status=3 and rag.clstatus=0)";
            sqltest+=" and coalesce(rag.doc_no,0)=0";
        }
        else if(restrictContractVeh==2) {
            sqljoin+=" left join gl_lagmt lag on (lag.perfleet=v.fleet_no and  lag.status=3 and lag.clstatus=0)";
            sqltest+=" and coalesce(lag.doc_no,0)=0";
        }
        else if(restrictContractVeh==3) {
            sqljoin+=" left join gl_ragmt rag on (rag.ofleet_no=v.fleet_no and  rag.status=3 and rag.clstatus=0)";
            sqljoin+=" left join gl_lagmt lag on (lag.perfleet=v.fleet_no and  lag.status=3 and lag.clstatus=0)";
            sqltest+=" and coalesce(rag.doc_no,0)=0 and coalesce(lag.doc_no,0)=0";
        }
        String vehsql="";

        if(dbType.trim().equalsIgnoreCase("MySQL")){
            vehsql="select v.fleet_no docno,concat(v.reg_no,' ',plate.code_name,' ',v.flname) refname from gl_vehmaster v "
                    + "	left join gl_vmove m on v.fleet_no=m.fleet_no  left join my_color c "
                    + " on v.clrid=c.doc_no left join gl_vehgroup g on g.doc_no=v.vgrpid left join gl_vehplate plate on v.pltid=plate.doc_no "+sqljoin+""
                    + "	where v.a_br="+brhid+" and ins_exp >=current_date and v.statu <> 7 and   "
                    + "	m.doc_no=(select (max(doc_no)) from gl_vmove where fleet_no=v.fleet_no) and "
                    + " fstatus in ('L','N') and v.status='IN' and v.tran_code='RR' and v.renttype in ('A','R') " +sqltest;
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            vehsql="select v.fleet_no docno,concat(v.reg_no,' ',plate.code_name,' ',v.flname) refname from gl_vehmaster v "
                    + "	left join gl_vmove m on v.fleet_no=m.fleet_no  left join my_color c "
                    + " on v.clrid=c.doc_no left join gl_vehgroup g on g.doc_no=v.vgrpid left join gl_vehplate plate on v.pltid=plate.doc_no "+sqljoin+""
                    + "	where v.a_br="+brhid+" and ins_exp >=convert(date,getdate()) and v.statu <> 7 and   "
                    + "	m.doc_no=(select (max(doc_no)) from gl_vmove where fleet_no=v.fleet_no) and "
                    + " fstatus in ('L','N') and v.status='IN' and v.tran_code='RR' and v.renttype in ('A','R') " +sqltest;
        }


        return template.query(vehsql, new RowMapper<DropdownModel>() {
            @Override
            public DropdownModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                DropdownModel model=new DropdownModel();
                model.setDocno(rs.getString("docno"));
                model.setRefname(rs.getString("refname"));
                return model;
            }
        });
    }

    public List<DropdownModel> getClientDriver(String cldocno) {
        String sql="SELECT d.dr_id docno,d.name refname FROM gl_drdetails d  WHERE d.cldocno='"+cldocno+"' and d.dtype='CRM'";
        return template.query(sql, new RowMapper<DropdownModel>() {
            @Override
            public DropdownModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                DropdownModel model=new DropdownModel();
                model.setDocno(rs.getString("docno"));
                model.setRefname(rs.getString("refname"));
                return model;
            }
        });
    }

    public List<DropdownModel> getTariff(String fleetno) {
        String sqlgetgroup="select vgrpid from gl_vehmaster where fleet_no="+fleetno+" and statu=3";
        List<Map<String,Object>> grouplist=template.queryForList(sqlgetgroup);

        int grpid=Integer.parseInt(grouplist.get(0).get("vgrpid").toString());
        
        String strsql="";

        if(dbType.trim().equalsIgnoreCase("MySQL")){
            strsql="SELECT * FROM (SELECT 1 tarifsrno,m.doc_no tarifdocno,m1.RENTTYPE tarifrenttype,CONCAT(m.doc_no,' ',m1.RENTTYPE) docno,CONCAT(m1.RENTTYPE,' ',ROUND(m1.RATE,2)) refname FROM gl_tarifm  m LEFT JOIN gl_tarifd m1 ON m1.doc_no=m.doc_no and m1.gid="+grpid+" LEFT JOIN  \n" +
                    " gl_tarifexcess t ON t.rdocno=m.doc_no AND t.gid="+grpid+" WHERE    CURRENT_DATE BETWEEN m.validfrm AND m.validto AND m.status=3 AND \n" +
                    " (m1.gid="+grpid+" OR t.gid="+grpid+") AND m.tariftype='Online' GROUP BY m.doc_no,m1.renttype UNION ALL\n" +
                    "SELECT 2 tarifsrno,m.doc_no tarifdocno,m1.RENTTYPE tarifrenttype,CONCAT(m.doc_no,' ',m1.RENTTYPE) docno,CONCAT(m1.RENTTYPE,' ',ROUND(m1.RATE,2)) refname FROM gl_tarifm  m LEFT JOIN gl_tarifd m1 ON m1.doc_no=m.doc_no and m1.gid="+grpid+" LEFT JOIN  \n" +
                    " gl_tarifexcess t ON t.rdocno=m.doc_no AND t.gid="+grpid+" WHERE    CURRENT_DATE BETWEEN m.validfrm AND m.validto AND m.status=3 AND \n" +
                    " (m1.gid="+grpid+" OR t.gid="+grpid+") AND m.tariftype='regular' GROUP BY m.doc_no,m1.renttype ) base GROUP BY base.tarifdocno,base.tarifrenttype ORDER BY base.tarifsrno LIMIT 3";
        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            strsql="SELECT top 3 * FROM (SELECT 1 tarifsrno,m.doc_no tarifdocno,m1.RENTTYPE tarifrenttype,CONCAT(m.doc_no,' ',m1.RENTTYPE) docno,CONCAT(m1.RENTTYPE,' ',ROUND(m1.RATE,2)) refname FROM gl_tarifm  m LEFT JOIN gl_tarifd m1 ON m1.doc_no=m.doc_no and m1.gid="+grpid+" LEFT JOIN  \n" +
                    " gl_tarifexcess t ON t.rdocno=m.doc_no AND t.gid="+grpid+" WHERE    convert(date,getdate()) BETWEEN m.validfrm AND m.validto AND m.status=3 AND \n" +
                    " (m1.gid="+grpid+" OR t.gid="+grpid+") AND m.tariftype='Online'  UNION ALL\n" +
                    "SELECT 2 tarifsrno,m.doc_no tarifdocno,m1.RENTTYPE tarifrenttype,CONCAT(m.doc_no,' ',m1.RENTTYPE) docno,CONCAT(m1.RENTTYPE,' ',ROUND(m1.RATE,2)) refname FROM gl_tarifm  m LEFT JOIN gl_tarifd m1 ON m1.doc_no=m.doc_no and m1.gid="+grpid+" LEFT JOIN  \n" +
                    " gl_tarifexcess t ON t.rdocno=m.doc_no AND t.gid="+grpid+" WHERE    convert(date,getdate()) BETWEEN m.validfrm AND m.validto AND m.status=3 AND \n" +
                    " (m1.gid="+grpid+" OR t.gid="+grpid+") AND m.tariftype='regular'  ) base  ORDER BY base.tarifsrno";
            System.out.println(strsql);
        }


        return template.query(strsql, new RowMapper<DropdownModel>() {
            @Override
            public DropdownModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                DropdownModel model=new DropdownModel();
                model.setDocno(rs.getString("docno"));
                model.setRefname(rs.getString("refname"));
                return model;
            }
        });
    }

    public List<Map<String,Object>> getTariffDetails(Map<String, Object> requestmap) {
        System.out.println("Inside Method");
        System.out.println(requestmap);
        String fleetno=requestmap.get("fleetno").toString();
        String tarifdocno=requestmap.get("tarifdocno").toString();
        String rentaltype=requestmap.get("rentaltype").toString();
        String userid=requestmap.get("userid").toString();
        String sqlgid="select vgrpid from gl_vehmaster where fleet_no="+fleetno;
        List<AgmtModel> grouplist=template.query(sqlgid, new RowMapper<AgmtModel>() {
            @Override
            public AgmtModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                AgmtModel model=new AgmtModel();
                model.setGid(rs.getString("vgrpid"));
                return model;
            }
        });
        String grpid=grouplist.get(0).getGid();

        String struserlevel="select ulevel from my_user where doc_no="+userid;
        Map<String,Object> usermap=template.queryForMap(struserlevel);
        String userlevel=usermap.get("ulevel").toString();

        //Getting Company Corresponding Tariff Detail names
        String sqltarifmap="SELECT text,name,selected FROM gl_apptarif WHERE STATUS=1";
        List<Map<String,Object>> tarifmap=template.queryForList(sqltarifmap);
        System.out.println(tarifmap);
        //Getting all tariff details corresponding to Tarif Docno,Vehicle Grp Id,Rental Type
        String sqltarif="select d.*,case when "+userlevel+"=1 then d.disclevel1 when "+userlevel+"=2 then d.disclevel2 when "+userlevel+"=3 then d.disclevel3 else 0 end discountpercent from gl_tarifd d where d.doc_no="+tarifdocno+" and d.gid="+grpid+" and d.renttype='"+rentaltype+"'";

        Map<String,Object> tempmap=template.queryForMap(sqltarif);

        for (Map<String, Object> row : tarifmap) {
            String text = (String) row.get("text");
            String name = (String) row.get("name");
            int selected = (int) row.get("selected");
            row.put("amount",tempmap.get(name)==null?"0.0":tempmap.get(name));
            row.put("discountpercent", tempmap.get("discountpercent"));
            row.put("netamount", row.get("amount"));
            // Process the field values as needed
        }
        //System.out.println(tarifmap);


        //System.out.println(tarifmap);
        return tarifmap;
    }

    public List<DropdownModel> getCompanyDriver() {
        String sql="SELECT doc_no docno,sal_name refname FROM my_salesman WHERE STATUS=3 AND sal_type='DRV'";
        List<DropdownModel> driverlist=template.query(sql, new RowMapper<DropdownModel>() {
            @Override
            public DropdownModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                DropdownModel model=new DropdownModel();
                model.setDocno(rs.getString("docno"));
                model.setRefname(rs.getString("refname"));
                return model;
            }
        });
        System.out.println(driverlist);
        return driverlist;
    }

    public boolean checkFleetAvailable(Map<String, String> data) {
        boolean status=true;
        String fleetno=data.get("fleetno").toString();
        String outdate=data.get("date").toString();
        String outtime=data.get("time").toString();
        java.sql.Date sqloutdate=null;
        if(outdate!=null && !outdate.equalsIgnoreCase("") && !outdate.equalsIgnoreCase("undefined")){
            sqloutdate=objcommon.changeStringtoSqlDate(outdate);
        }

        String sqltimevalidate="select * from (select cast(concat('"+sqloutdate+"',concat(' ','"+outtime+"')) as datetime) agmtdt ,cast(concat(vm.din,concat(' ',vm.tin)) as datetime) dtin from gl_vmove vm where doc_no=(select max(doc_no) from gl_vmove  where  fleet_no='"+fleetno+"' and status='IN')) base where base.agmtdt<base.dtin";
        //System.out.println(sqltimevalidate);
        List<Map<String,Object>> listtime=template.queryForList(sqltimevalidate);
        String sqlstatusvalidate="select status from gl_vmove vm where doc_no=(select max(doc_no) from gl_vmove where  fleet_no='"+fleetno+"') " ;
        List<Map<String,Object>> liststatus=template.queryForList(sqlstatusvalidate);
        System.out.println(liststatus);
        System.out.println(listtime);
        if(listtime.size()>0 || liststatus.get(0).get("status").toString().equalsIgnoreCase("OUT")){
            return false;
        }
        return true;
    }

    @Modifying
    @Transactional(rollbackFor = {CommonException.class})
    public Map<String, String> insertRentalAgreement(Map<String,Object> data) throws SQLException {

        int agmtvocno=0;
        int agmtdocno=0;
        Map<String, String> agmtmap=new HashMap<String,String>();
        try{
            int errorstatus=0;
            System.out.println("Params Recieved");
            System.out.println(data);
            String cldocno=data.get("cldocno").toString();
            String brhid=data.get("brhid").toString();
            String locid=data.get("locid").toString();
            String fleetno=data.get("fleetno").toString();
            String drvid=data.get("drvid").toString();
            String tarifdocno=data.get("tarifdocno").toString();
            String rentaltype=data.get("rentaltype").toString();
            System.out.println(data.get("tarifaddons"));
            String outdate=data.get("outdate").toString();
            String outtime=data.get("outtime").toString();
            String chkdelivery=data.get("chkdelivery").toString();
            String deldrvid=data.get("deldrvid").toString();
            String delchg=data.get("delchg").toString();
            String basedate=data.get("basedate").toString();
            List<Map<String,Object>> addonlist= (List<Map<String, Object>>) data.get("tarifaddons");
            java.sql.Date sqloutdate=null,sqlbasedate=null,sqlduedate=null;
            if(basedate!=null && !basedate.equalsIgnoreCase("") && !basedate.equalsIgnoreCase("undefined")){
                sqlbasedate=objcommon.changeStringtoSqlDate(basedate);
            }
            if(outdate!=null && !outdate.equalsIgnoreCase("") && !outdate.equalsIgnoreCase("undefined")){
                sqloutdate=objcommon.changeStringtoSqlDate(outdate);
                String strduedate="";
                if(rentaltype.trim().equalsIgnoreCase("Daily")){
                    if(dbType.trim().equalsIgnoreCase("MySQL")){
                        strduedate="select date_add('"+sqloutdate+"',interval 1 day) duedate";
                    } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                        strduedate="select dateadd(day,1,'"+sqloutdate+"') duedate";
                    }
                }
                else if (rentaltype.trim().equalsIgnoreCase("Weekly")){
                    if(dbType.trim().equalsIgnoreCase("MySQL")){
                        strduedate="select date_add('"+sqloutdate+"',interval 7 day) duedate";
                    } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                        strduedate="select dateadd(day,7,'"+sqloutdate+"') duedate";
                    }

                }
                else if (rentaltype.trim().equalsIgnoreCase("Monthly")){
                    if(dbType.trim().equalsIgnoreCase("MySQL")){
                        strduedate="select date_add('"+sqloutdate+"',interval 1 month) duedate";
                    } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                        strduedate="select dateadd(month,1,'"+sqloutdate+"') duedate";
                    }

                }
                Map<String,Object> mapduedate=template.queryForMap(strduedate);
                if(mapduedate.get("duedate")!=null) {
                	sqlduedate=Date.valueOf(mapduedate.get("duedate").toString());
                }
            }
            System.out.println("Delivery Params"+chkdelivery);
            chkdelivery=chkdelivery.equalsIgnoreCase("true") || chkdelivery.equalsIgnoreCase("1") ? "1": "0";
            deldrvid=deldrvid.equalsIgnoreCase("") || chkdelivery.trim().equalsIgnoreCase("0") ? "0": deldrvid.trim();

            //Getting LastIn Km,Last In Fuel
            double outkm=0.0;
            String outfuel="";

            String strgetkm="select kmin,fin from gl_vmove where doc_no=(select max(doc_no) from gl_vmove where fleet_no="+fleetno+" and status='IN')";
            Map<String,Object> mapkm=template.queryForMap(strgetkm);
            if(mapkm.get("kmin")!=null) {
            	outkm=Double.parseDouble(mapkm.get("kmin").toString());
            }
            if(mapkm.get("fin")!=null) {
            	outfuel=mapkm.get("fin").toString();
            }

            String clientcodeno="",clientacno="",salid="",invtype="1",advance="0";
            String strgetclientinfo="select codeno,acno,COALESCE(sal_id,0) salid,coalesce(advance,0) advance,coalesce(invc_method,1) invtype from my_Acbook where cldocno="+cldocno+" and status<>7 and dtype='CRM'";
            Map<String,Object> mapclientinfo=template.queryForMap(strgetclientinfo);
            if(mapclientinfo.get("salid")!=null) {
            	salid=mapclientinfo.get("salid").toString();
            }
            if(mapclientinfo.get("codeno")!=null) {
            	clientcodeno=mapclientinfo.get("codeno").toString();
            }
            if(mapclientinfo.get("acno")!=null) {
            	clientacno=mapclientinfo.get("acno").toString();
            }
            if(mapclientinfo.get("invtype")!=null) {
            	invtype=mapclientinfo.get("invtype").toString();
            }
            if(mapclientinfo.get("advance")!=null) {
            	advance=mapclientinfo.get("advance").toString();
            }
            
            SimpleJdbcCall simplejdbc=new SimpleJdbcCall(template);
            simplejdbc.withProcedureName("AppRentalAgmtDML");
            simplejdbc.withCatalogName(dbName);
            Map<String,Object> inparams=new HashMap<String,Object>();
            
            inparams.put("docNo", java.sql.Types.INTEGER);
            inparams.put("vocNo", java.sql.Types.INTEGER);
            // main
            inparams.put("sqlrentalDate",sqlbasedate);
            inparams.put("fleetno",fleetno);
            inparams.put("clientid",cldocno);
            inparams.put("salesmanid",Integer.parseInt(salid));
            inparams.put("clientcodeno",clientcodeno);
            inparams.put("clientacno",clientacno);
            // drv
            inparams.put("addrvchk",0);
            inparams.put("adddrvcharge","0.0");
            inparams.put("delchk",Integer.parseInt(chkdelivery));
            inparams.put("chfchk",0);
            inparams.put("deldriverid",Integer.parseInt(deldrvid));
            // triffmain
            inparams.put("tasystem","Manual");
            inparams.put("tadocno",tarifdocno);
            inparams.put("invoice",invtype);
            inparams.put("exessinsu","0.0");

            // thariff sub
            inparams.put("inkm",outkm+"");
            inparams.put("infuel",outfuel);
            inparams.put("sqloutDate",sqloutdate);
            inparams.put("outTime",outtime);
            inparams.put("salesagentid",0);
            inparams.put("rentalagentid",0);
            inparams.put("checkoutid",0);
            inparams.put("sqldueDate",sqlduedate);
            inparams.put("dueTime",outtime);

            //payment
            inparams.put("paymentMra","");
            inparams.put("paymentPo","");
            inparams.put("origFleetno",fleetno);
            // ex
            inparams.put("Vehlocationid",locid);

            inparams.put("companyid","1");
            inparams.put("branchid",brhid) ;
            // inparams.put(31,session.getAttribute("BRANCHID").toString().trim());
            inparams.put("userId","1");
            inparams.put("curid","1");

            inparams.put("rentalType",rentaltype);
            inparams.put("advancechk",Integer.parseInt(advance));
            inparams.put("formcode","RAG");
            inparams.put("vmode","A");
            inparams.put("delcharge",delchg);
            inparams.put("rentaldesc","");
            inparams.put("wkday",0);

            SqlParameterSource in=new MapSqlParameterSource(inparams);
            Map<String,Object> simplejdbcresult=simplejdbc.execute(in);
            System.out.println(simplejdbcresult.toString());
            if(simplejdbcresult.get("docno")!=null && !simplejdbcresult.get("docno").toString().equalsIgnoreCase("undefined") && Integer.parseInt(simplejdbcresult.get("docno").toString())>0) {
            	agmtdocno=(int) simplejdbcresult.get("docno");
                agmtvocno=(int) simplejdbcresult.get("vocno");
            }
            else {
            	throw new CommonException("Procedure Error");
            }

            System.out.println("Agmt Created No: "+agmtdocno+"::"+agmtvocno);

            //Getting Group Id from Vehicle
            String grpid="";
            String strgetveh="select vgrpid from gl_vehmaster where fleet_no="+fleetno+" and statu<>7";
            Map<String,Object> mapvehgrp=template.queryForMap(strgetveh);
            if(mapvehgrp.get("vgrpid")!=null) {
            	grpid=mapvehgrp.get("vgrpid").toString();
            }
            
            //Creating Array for Tarif Insertion
            ArrayList<String> tarifarray=new ArrayList<>();
            String fixedaddons[]={"rate","cdw","pai","cdw1","pai1","gps","babyseater","cooler","kmrest","exkmrte","oinschg","exhrchg","chaufchg","chaufexchg","startday","starttime","endday","endtime"};
            String strtarifselected="SELECT 5 rstatus,'"+rentaltype+"' rentaltype";
            String strtarifdiscount="SELECT 6 rstatus,'Discount' rentaltype";
            String strtariftotal="SELECT 7 rstatus,'Net Total' rentaltype";
            System.out.println(addonlist);
            for(int i=0;i<fixedaddons.length;i++){
                int columnfound=0;
                for(int j=0;j<addonlist.size();j++){
                	//System.out.println(addonlist.get(j));
                    if(fixedaddons[i].equalsIgnoreCase(addonlist.get(j).get("name").toString())){
                        columnfound=1;
                        strtarifselected+=","+fixedaddons[i];
                        strtarifdiscount+=","+addonlist.get(j).get("discount")+" "+fixedaddons[i];
                        strtariftotal+=","+addonlist.get(j).get("netamount")+" "+fixedaddons[i];
                    }
                }
                if(columnfound==0){
                    strtarifselected+=", 0.0 "+fixedaddons[i];
                    strtarifdiscount+=", 0.0 "+fixedaddons[i];
                    strtariftotal+=", 0.0 "+fixedaddons[i];
                }
            }
            System.out.println(strtarifselected);
            System.out.println(strtariftotal);
            strtarifselected+=" from gl_tarifd where doc_no="+tarifdocno+" and gid="+grpid+" and renttype='"+rentaltype+"'";
            strtariftotal+=" from gl_tarifd where doc_no="+tarifdocno+" and gid="+grpid+" and renttype='"+rentaltype+"'";
            String strtarif="SELECT CASE WHEN renttype='Daily' THEN 1 WHEN renttype='Weekly' THEN 2 WHEN renttype='Monthly' THEN 3 ELSE 0 END rstatus, \n" +
                    "renttype rentaltype,rate,cdw,pai,cdw1,pai1,gps,babyseater,cooler,kmrest,exkmrte,oinschg,exhrchg,chaufchg,chaufexchg,0.0 startday,0.0 starttime,0.0 endday,0.0 endtime\n" +
                    "FROM gl_tarifd WHERE doc_no="+tarifdocno+" AND gid="+grpid+" UNION ALL "+strtarifselected+" UNION ALL "+strtarifdiscount+" UNION ALL "+strtariftotal;
            System.out.println(strtarif);
            List<Map<String,Object>> listtarif=template.queryForList(strtarif);
            for (Map<String, Object> map : listtarif) {
				tarifarray.add((map.get("rentaltype")==null?"":map.get("rentaltype").toString()) + "::" + (map.get("rate")==null?"0.0":map.get("rate").toString()) + " :: " + (map.get("cdw")==null?"0.0":map.get("cdw").toString()) + " :: " + (map.get("pai")==null?"0.0":map.get("pai")) + " :: " + (map.get("cdw1")==null?"0.0":map.get("cdw1").toString()) + " :: " + (map.get("pai1")==null?"0.0":map.get("pai1").toString()) + " :: " + (map.get("gps")==null?"0.0":map.get("gps").toString()) + " :: " + (map.get("babyseater")==null?"0.0":map.get("babyseater").toString()) + " :: " + (map.get("cooler")==null?"0.0":map.get("cooler").toString()) + " :: " + (map.get("kmrest")==null?"0.0":map.get("kmrest").toString()) + " :: " + (map.get("exkmrte")==null?"0.0":map.get("exkmrte").toString()) + " :: " + (map.get("oinschg")==null?"0.0":map.get("oinschg").toString()) + " :: " + (map.get("exhrchg")==null?"0.0":map.get("exhrchg").toString()) + " :: " + (map.get("chaufchg")==null?"0.0":map.get("chaufchg").toString()) + " :: " + (map.get("chaufexchg")==null?"0.0":map.get("chaufexchg").toString()) + " :: " + (map.get("rstatus")==null?"0":map.get("rstatus").toString()) + " :: " + (map.get("startday")==null?"":map.get("startday").toString()) + " :: " + (map.get("starttime")==null?"":map.get("starttime").toString()) + " :: " + (map.get("endday")==null?"":map.get("endday").toString()) + " :: " + (map.get("endtime")==null?"":map.get("endtime").toString()));
			}
            
            for(int i=0;i< tarifarray.size();i++){

                String[] tariff=tarifarray.get(i).split("::");
                System.out.println(tarifarray.get(i));
                // System.out.println("dasfghjk======="+ragmttariffarray.get(i));
                String strtarifinsert="INSERT INTO gl_rtarif(rentaltype,rate,cdw,pai,cdw1,pai1,gps,babyseater,cooler,kmrest,exkmrte,oinschg,exhrchg,chaufchg,chaufexchg,rstatus,brhid,rdocno,gid)VALUES"
                        + " ('"+(tariff[0].equalsIgnoreCase("undefined") || tariff[0].equalsIgnoreCase("") || tariff[0].trim().equalsIgnoreCase("NaN")|| tariff[0].isEmpty()?0:tariff[0].trim())+"',"
                        + "'"+(tariff[1].trim().equalsIgnoreCase("undefined")  || tariff[1].trim().equalsIgnoreCase("") || tariff[1].trim().equalsIgnoreCase("NaN")|| tariff[1].isEmpty()?0:tariff[1].trim())+"',"
                        + "'"+(tariff[2].trim().equalsIgnoreCase("undefined") || tariff[2].trim().equalsIgnoreCase("") || tariff[2].trim().equalsIgnoreCase("NaN")|| tariff[2].isEmpty()?0:tariff[2].trim())+"',"
                        + "'"+(tariff[3].trim().equalsIgnoreCase("undefined") || tariff[3].trim().equalsIgnoreCase("") || tariff[3].trim().equalsIgnoreCase("NaN")|| tariff[3].isEmpty()?0:tariff[3].trim())+"',"
                        + "'"+(tariff[4].trim().equalsIgnoreCase("undefined") || tariff[4].trim().equalsIgnoreCase("")|| tariff[4].trim().equalsIgnoreCase("NaN") || tariff[4].isEmpty()?0:tariff[4].trim())+"',"
                        + "'"+(tariff[5].trim().equalsIgnoreCase("undefined") || tariff[5].trim().equalsIgnoreCase("") || tariff[5].trim().equalsIgnoreCase("NaN")|| tariff[5].isEmpty()?0:tariff[5].trim())+"',"
                        + "'"+(tariff[6].trim().equalsIgnoreCase("undefined") || tariff[6].trim().equalsIgnoreCase("") || tariff[6].trim().equalsIgnoreCase("NaN")|| tariff[6].isEmpty()?0:tariff[6].trim())+"',"
                        + "'"+(tariff[7].trim().equalsIgnoreCase("undefined") || tariff[7].trim().equalsIgnoreCase("") || tariff[7].trim().equalsIgnoreCase("NaN")|| tariff[7].isEmpty()?0:tariff[7].trim())+"',"
                        + "'"+(tariff[8].trim().equalsIgnoreCase("undefined") || tariff[8].trim().equalsIgnoreCase("") || tariff[8].trim().equalsIgnoreCase("NaN")|| tariff[8].isEmpty()?0:tariff[8].trim())+"',"
                        + "'"+(tariff[9].trim().equalsIgnoreCase("undefined") || tariff[9].trim().equalsIgnoreCase("") || tariff[9].trim().equalsIgnoreCase("NaN")|| tariff[9].isEmpty()?0:tariff[9].trim())+"',"
                        + "'"+(tariff[10].trim().equalsIgnoreCase("undefined") || tariff[10].trim().equalsIgnoreCase("")|| tariff[10].trim().equalsIgnoreCase("NaN")|| tariff[10].isEmpty()?0:tariff[10].trim())+"',"
                        + "'"+(tariff[11].trim().equalsIgnoreCase("undefined") || tariff[11].trim().equalsIgnoreCase("")|| tariff[11].trim().equalsIgnoreCase("NaN")|| tariff[11].isEmpty()?0:tariff[11].trim())+"',"
                        + "'"+(tariff[12].trim().equalsIgnoreCase("undefined") || tariff[12].trim().equalsIgnoreCase("")|| tariff[12].trim().equalsIgnoreCase("NaN")|| tariff[12].isEmpty()?0:tariff[12].trim())+"',"
                        + "'"+(tariff[13].trim().equalsIgnoreCase("undefined") || tariff[13].trim().equalsIgnoreCase("")|| tariff[13].trim().equalsIgnoreCase("NaN")|| tariff[13].isEmpty()?0:tariff[13].trim())+"',"
                        + "'"+(tariff[14].trim().equalsIgnoreCase("undefined") || tariff[14].trim().equalsIgnoreCase("")|| tariff[14].trim().equalsIgnoreCase("NaN")|| tariff[14].isEmpty()?0:tariff[14].trim())+"',"
                        + "'"+(tariff[15].trim().equalsIgnoreCase("undefined") || tariff[15].trim().equalsIgnoreCase("")|| tariff[15].trim().equalsIgnoreCase("NaN")|| tariff[15].isEmpty()?0:tariff[15].trim())+"',"
                        +"'"+brhid+"','"+agmtdocno+"','"+grpid+"' )";
                int tarifinsert = template.update(strtarifinsert);
                if(tarifinsert<=0)
                {
                    errorstatus=1;
                    throw new CommonException("Tarif Insert Error");
                }

            }

            String strinsertdriver="INSERT INTO gl_rdriver(drid,brhid,rdocno,cldocno)VALUES("+drvid+","+brhid+",'"+agmtdocno+"','"+cldocno+"')";
            int insertdriver = template.update(strinsertdriver);
            if(insertdriver<=0){
                errorstatus=1;
                throw new CommonException("Driver Insert Error");
            }

            if(agmtdocno>0 && errorstatus==0){
            	agmtmap.put("agmtdocno", agmtdocno+"");
            	agmtmap.put("agmtvocno", agmtvocno+"");
            }
            else {
            	throw new CommonException("Raised Error");
            }
        }
        catch (Exception e){
        	e.printStackTrace();
            throw new CommonException("Exception "+e.getMessage());
        }
        

        return agmtmap;
    }

	public Map<String, Object> getFleetLastMovData(String fleetno) {
		// TODO Auto-generated method stub
		try {
			
			String strmov="";
			if(dbType.trim().equalsIgnoreCase("MySQL")) {
				strmov="SELECT convert(din,char(10)) mindate,tin mintime,kmin minkm,fin minfuel FROM gl_vmove WHERE doc_no=(SELECT MAX(doc_no) FROM gl_vmove WHERE fleet_no="+fleetno+")";
			}
			else if(dbType.trim().equalsIgnoreCase("MSSQL")) {
				strmov="SELECT CONVERT(varchar, din) mindate,tin mintime,kmin minkm,fin minfuel FROM gl_vmove WHERE doc_no=(SELECT MAX(doc_no) FROM gl_vmove WHERE fleet_no="+fleetno+")";
			}
			
			return template.queryForMap(strmov);
		}
		catch(Exception e) {
			throw new CommonException(e.getMessage());
		}
	}

	public Map<String, Object> getPickupData(String pickupdocno) {
		// TODO Auto-generated method stub
		try {
			String strpickup="";
			if(dbType.trim().equalsIgnoreCase("MySQL")) {
				strpickup="SELECT CONVERT(datee,CHAR(10)) pdate,times ptime,ROUND(km,0) pkm,ROUND(fuel,3) pfuel FROM an_acollection WHERE pkupno="+pickupdocno;
			}
			else if(dbType.trim().equalsIgnoreCase("MSSQL")) {
				strpickup="SELECT CONVERT(varchar, datee) pdate,times ptime,cast(km as decimal(15,0)) pkm,cast(fuel as decimal(15,3)) pfuel FROM an_acollection WHERE pkupno="+pickupdocno;
			}
			return template.queryForMap(strpickup);
		}
		catch(Exception e) {
			throw new CommonException(e.getMessage());
		}
	}

	public Map<String, Object> getAppPrintConfig() {
		// TODO Auto-generated method stub
		
		try {
			String str="select method from gl_config where field_nme='AppPrintRA'";
			return template.queryForMap(str);
		}
		catch(Exception e) {
			throw new CommonException(e.getMessage());
		}
	}
	
}
