package com.gateway.limoapi.helpers;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gateway.limoapi.model.MiscModel;
import com.gateway.limoapi.model.MovModel;
import com.gateway.limoapi.model.RepMoveModel;

@Component
public class ClsCommon{

	@Value("${dbtype}")
    private String dbType;

	public  java.sql.Date changeStringtoSqlDate(String startDate){
        java.sql.Date sqlStartDate=null;
        try{
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy");
            java.util.Date date = sdf1.parse(startDate);
            sqlStartDate = new java.sql.Date(date.getTime());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return sqlStartDate;
    }


    public List<RepMoveModel> getRepData(int repno, JdbcTemplate template) {
        return template.query("select case when rep.rtype='RAG' then rag.cldocno else lag.cldocno end cldocno,rep.obrhid,rep.olocid,rep.rbrhid,rep.rlocid,rep.rdocno,rep.rtype rdtype,rep.rfleetno,rep.ofleetno,rep.odate outdate,rep.otime outtime,rep.okm outkm,rep.ofuel outfuel,rep.deldate,rep.deltime,rep.delkm,rep.delfuel,rep.indate,rep.intime,rep.inkm,rep.infuel,rep.inbrhid,rep.inloc inlocid,rep.cldate collectdate,rep.cltime collecttime,rep.clkm collectkm,rep.clfuel collectfuel,rep.infleettrancode from gl_vehreplace rep  left join gl_ragmt rag on (rep.rtype='RAG' and rep.rdocno=rag.doc_no)  left join gl_lagmt lag on (rep.rtype='LAG' and rep.rdocno=lag.doc_no) where rep.doc_no=" + repno, new RowMapper<RepMoveModel>() {
            @Override
            public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                RepMoveModel model=new RepMoveModel();
                model.setCldocno(rs.getString("cldocno"));
                model.setOfleetno(rs.getString("ofleetno"));
                model.setRfleetno(rs.getString("rfleetno"));
                model.setRepno(repno);
                model.setRdocno(rs.getString("rdocno"));
                model.setRdtype(rs.getString("rdtype"));
                model.setOutdate(rs.getString("outdate"));
                model.setOuttime(rs.getString("outtime"));
                model.setOutkm(rs.getString("outkm"));
                model.setOutfuel(rs.getString("outfuel"));
                model.setDeldate(rs.getString("deldate"));
                model.setDeltime(rs.getString("deltime"));
                model.setDelkm(rs.getString("delkm"));
                model.setDelfuel(rs.getString("delfuel"));
                model.setCollectdate(rs.getString("collectdate"));
                model.setCollecttime(rs.getString("collecttime"));
                model.setCollectkm(rs.getString("collectkm"));
                model.setCollectfuel(rs.getString("collectfuel"));
                model.setIndate(rs.getString("indate"));
                model.setIntime(rs.getString("intime"));
                model.setInkm(rs.getString("inkm"));
                model.setInfuel(rs.getString("infuel"));
                model.setInbrhid(rs.getString("inbrhid"));
                model.setInlocid(rs.getString("inlocid"));
                model.setRbrhid(rs.getString("rbrhid"));
                model.setRlocid(rs.getString("rlocid"));
                model.setObrhid(rs.getString("obrhid"));
                model.setOlocid(rs.getString("olocid"));
                return model;
            }
        });
    }

    @Transactional()
    public int updateRepMovData(List<RepMoveModel> repdata,JdbcTemplate template,int movstage,int drvdocno) {
        try{
            RepMoveModel model=repdata.get(0);
            String fleetno=model.getRfleetno();
            String indate="",intime="",inkm="",infuel="",inbrhid="",inlocid="";
            String strupdate="";
            if(movstage==1){
                indate=model.getCollectdate();
                intime=model.getCollecttime();
                inkm=model.getCollectkm();
                infuel=model.getCollectfuel();
                inbrhid=model.getRbrhid();
                inlocid=model.getRlocid();
            }
            List<RepMoveModel> maxparentdata=template.query("select max(doc_no) maxparent from gl_vmove where fleet_no="+fleetno,new RowMapper<RepMoveModel>(){
                @Override
                public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    RepMoveModel objtemp=new RepMoveModel();
                    objtemp.setMaxparent(rs.getString("maxparent"));
                    return objtemp;
                }
            });
            int maxparent=Integer.parseInt(maxparentdata.get(0).getMaxparent());
            String strtotal="";
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                strtotal="select  TIMESTAMPDIFF(SECOND,ts_dout,ts_din)/60 totalmin,("+inkm+"-kmout) totalkm,("+infuel+"-fout) totalfuel\n" +
                        "    from (select  cast(concat('"+indate+"',' ','"+intime+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)\n" +
                        "    ts_dout,kmout,fout from gl_vmove where doc_no="+maxparent+")m";
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                strtotal="select  dateDIFF(SECOND,ts_dout,ts_din)/60 totalmin,("+inkm+"-kmout) totalkm,("+infuel+"-fout) totalfuel\n" +
                        "    from (select  cast(concat('"+indate+"',' ','"+intime+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)\n" +
                        "    ts_dout,kmout,fout from gl_vmove where doc_no="+maxparent+")m";
            }

            List<RepMoveModel> totaldata=template.query(strtotal,new RowMapper<RepMoveModel>(){
                @Override
                public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    RepMoveModel objtemp=new RepMoveModel();
                    objtemp.setTotalmin(rs.getString("totalmin"));
                    objtemp.setTotalkm(rs.getString("totalkm"));
                    objtemp.setTotalfuel(rs.getString("totalfuel"));
                    return objtemp;
                }
            });

            strupdate="update gl_vmove set status='IN',din='"+indate+"',tin='"+intime+"',kmin='"+inkm+"',fin='"+infuel+"',ibrhid="+inbrhid+",\n" +
                    "  ilocid="+inlocid+",ireason='Replacement Collection via App',ttime="+totaldata.get(0).getTotalmin()+",tkm="+totaldata.get(0).getTotalkm()+",tfuel="+totaldata.get(0).getTotalfuel()+" where fleet_no="+fleetno+" and status='OUT'";
            System.out.println(strupdate);
            int update=template.update(strupdate);
            if(update<=0){
                return 0;
            }
            int maxmovdoc=0;
            if(model.getRepno()>0){
                //Inserting CollectData
                maxparentdata=template.query("select (select max(doc_no) from gl_vmove where fleet_no="+fleetno+") maxparent,(select max(doc_no)+1 from gl_vmove) maxmovdoc",new RowMapper<RepMoveModel>(){
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

                String stridle="";
                if(dbType.trim().equalsIgnoreCase("MySQL")){
                    stridle="select  TIMESTAMPDIFF(SECOND,ts_din,ts_dout)/60 idletime from (select  cast(concat(din,' ',tin) as datetime) ts_din,\n" +
                            "    cast(concat('"+indate+"',' ','"+intime+"')as datetime)ts_dout  from gl_vmove where doc_no="+maxparent+")m";
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    stridle="select  dateDIFF(SECOND,ts_din,ts_dout)/60 idletime from (select  cast(concat(din,' ',tin) as datetime) ts_din,\n" +
                            "    cast(concat('"+indate+"',' ','"+intime+"')as datetime)ts_dout  from gl_vmove where doc_no="+maxparent+")m";
                }

                List<RepMoveModel> idledata=template.query(stridle,new RowMapper<RepMoveModel>(){
                    @Override
                    public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RepMoveModel objtemp=new RepMoveModel();
                        objtemp.setIdletime(rs.getString("idletime"));
                        return objtemp;
                    }
                });

                //Inserting with Collection Data

                if(dbType.trim().equalsIgnoreCase("MySQL")){
                    strupdate="insert into gl_vmove(doc_no,date,rdocno,rdtype,fleet_no,trancode,status,parent,repno,dout,tout,kmout,fout,obrhid,olocid,"+
                            " tideal,emp_id,emp_type)values("+maxmovdoc+",CURDATE(),"+model.getRdocno()+",'"+model.getRdtype()+"',"+fleetno+",'DL','OUT',"+
                            " "+maxparent+","+model.getRepno()+",'"+indate+"','"+intime+"','"+inkm+"','"+infuel+"',"+inbrhid+","+inlocid+","+idledata.get(0).getIdletime()+","+drvdocno+",'DRV')";
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove ON");
                    strupdate="insert into gl_vmove(doc_no,date,rdocno,rdtype,fleet_no,trancode,status,parent,repno,dout,tout,kmout,fout,obrhid,olocid,"+
                            " tideal,emp_id,emp_type)values("+maxmovdoc+",convert(date,getdate()),"+model.getRdocno()+",'"+model.getRdtype()+"',"+fleetno+",'DL','OUT',"+
                            " "+maxparent+","+model.getRepno()+",'"+indate+"','"+intime+"','"+inkm+"','"+infuel+"',"+inbrhid+","+inlocid+","+idledata.get(0).getIdletime()+","+drvdocno+",'DRV')";
                }

                System.out.println(strupdate);
                update=template.update(strupdate);
                if(update<=0){
                    return 0;
                }
                if(dbType.trim().equalsIgnoreCase("MSSQL")){
                    int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove OFF");
                }
                indate=model.getIndate();
                intime=model.getIntime();
                inkm=model.getInkm();
                infuel=model.getInfuel();
                inbrhid=model.getInbrhid();
                inlocid=model.getInlocid();

                maxparentdata=template.query("select max(doc_no) maxparent from gl_vmove where fleet_no="+fleetno,new RowMapper<RepMoveModel>(){
                    @Override
                    public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RepMoveModel objtemp=new RepMoveModel();
                        objtemp.setMaxparent(rs.getString("maxparent"));
                        return objtemp;
                    }
                });
                maxparent=Integer.parseInt(maxparentdata.get(0).getMaxparent());

                if(dbType.trim().equalsIgnoreCase("MySQL")){
                    strtotal="    select  TIMESTAMPDIFF(SECOND,ts_dout,ts_din)/60 totalmin,("+inkm+"-kmout) totalkm,("+infuel+"-fout) totalfuel\n" +
                            "    from (select  cast(concat('"+indate+"',' ','"+intime+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)\n" +
                            "    ts_dout,kmout,fout from gl_vmove where doc_no="+maxparent+")m";
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    strtotal="    select  datediff(SECOND,ts_dout,ts_din)/60 totalmin,("+inkm+"-kmout) totalkm,("+infuel+"-fout) totalfuel\n" +
                            "    from (select  cast(concat('"+indate+"',' ','"+intime+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)\n" +
                            "    ts_dout,kmout,fout from gl_vmove where doc_no="+maxparent+")m";
                }
                totaldata=template.query(strtotal,new RowMapper<RepMoveModel>(){
                    @Override
                    public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RepMoveModel objtemp=new RepMoveModel();
                        objtemp.setTotalmin(rs.getString("totalmin"));
                        objtemp.setTotalkm(rs.getString("totalkm"));
                        objtemp.setTotalfuel(rs.getString("totalfuel"));
                        return objtemp;
                    }
                });

                strupdate="update gl_vmove set status='IN',din='"+indate+"',tin='"+intime+"',kmin='"+inkm+"',fin='"+infuel+"',ibrhid="+inbrhid+",\n" +
                        "  ilocid="+inlocid+",ireason='Replacement Closing via App',ttime="+totaldata.get(0).getTotalmin()+",tkm="+totaldata.get(0).getTotalkm()+",tfuel="+totaldata.get(0).getTotalfuel()+" where fleet_no="+fleetno+" and status='OUT'";
                System.out.println(strupdate);
                update=template.update(strupdate);
                if(update<=0){
                    return 0;
                }

                //Completed Collection and Branch Closing
                //Starting Out and Delivery
                indate=model.getOutdate();
                intime=model.getOuttime();
                inkm=model.getOutkm();
                infuel=model.getOutfuel();
                fleetno=model.getOfleetno();
                inbrhid=model.getObrhid();
                inlocid=model.getOlocid();
                maxparentdata=template.query("select (select max(doc_no) from gl_vmove where fleet_no="+fleetno+") maxparent,(select max(doc_no)+1 from gl_vmove) maxmovdoc",new RowMapper<RepMoveModel>(){
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
                if(dbType.trim().equalsIgnoreCase("MySQL")){
                    stridle="select  TIMESTAMPDIFF(SECOND,ts_din,ts_dout)/60 idletime from (select  cast(concat(din,' ',tin) as datetime) ts_din,\n" +
                            "    cast(concat('"+indate+"',' ','"+intime+"')as datetime)ts_dout  from gl_vmove where doc_no="+maxparent+")m";
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    stridle="select  DATEDIFF(SECOND,ts_din,ts_dout)/60 idletime from (select  cast(concat(din,' ',tin) as datetime) ts_din,\n" +
                            "    cast(concat('"+indate+"',' ','"+intime+"')as datetime)ts_dout  from gl_vmove where doc_no="+maxparent+")m";
                }
                idledata=template.query(stridle,new RowMapper<RepMoveModel>(){
                    @Override
                    public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RepMoveModel objtemp=new RepMoveModel();
                        objtemp.setIdletime(rs.getString("idletime"));
                        return objtemp;
                    }
                });

                if(dbType.trim().equalsIgnoreCase("MySQL")){
                    strupdate="insert into gl_vmove(doc_no,date,rdocno,rdtype,fleet_no,trancode,status,parent,repno,dout,tout,kmout,fout,obrhid,olocid,"+
                            " tideal,emp_id,emp_type)values("+maxmovdoc+",CURDATE(),"+model.getRdocno()+",'"+model.getRdtype()+"',"+fleetno+",'DL','OUT',"+
                            " "+maxparent+","+model.getRepno()+",'"+indate+"','"+intime+"','"+inkm+"','"+infuel+"',"+inbrhid+","+inlocid+","+idledata.get(0).getIdletime()+","+drvdocno+",'DRV')";
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove ON");
                    strupdate="insert into gl_vmove(doc_no,date,rdocno,rdtype,fleet_no,trancode,status,parent,repno,dout,tout,kmout,fout,obrhid,olocid,"+
                            " tideal,emp_id,emp_type)values("+maxmovdoc+",convert(date,getdate()),"+model.getRdocno()+",'"+model.getRdtype()+"',"+fleetno+",'DL','OUT',"+
                            " "+maxparent+","+model.getRepno()+",'"+indate+"','"+intime+"','"+inkm+"','"+infuel+"',"+inbrhid+","+inlocid+","+idledata.get(0).getIdletime()+","+drvdocno+",'DRV')";
                }

                System.out.println(strupdate);
                update=template.update(strupdate);
                if(update<=0){
                    return 0;
                }
                if(dbType.trim().equalsIgnoreCase("MSSQL")){
                    int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove OFF");
                }
                maxparentdata=template.query("select max(doc_no) maxparent from gl_vmove where fleet_no="+fleetno,new RowMapper<RepMoveModel>(){
                    @Override
                    public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RepMoveModel objtemp=new RepMoveModel();
                        objtemp.setMaxparent(rs.getString("maxparent"));
                        return objtemp;
                    }
                });
                maxparent=Integer.parseInt(maxparentdata.get(0).getMaxparent());

                indate=model.getDeldate();
                intime=model.getDeltime();
                inkm=model.getDelkm();
                infuel=model.getDelfuel();

                if(dbType.trim().equalsIgnoreCase("MySQL")){
                    strtotal="    select  TIMESTAMPDIFF(SECOND,ts_dout,ts_din)/60 totalmin,("+inkm+"-kmout) totalkm,("+infuel+"-fout) totalfuel\n" +
                            "    from (select  cast(concat('"+indate+"',' ','"+intime+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)\n" +
                            "    ts_dout,kmout,fout from gl_vmove where doc_no="+maxparent+")m";
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    strtotal="    select  DATEDIFF(SECOND,ts_dout,ts_din)/60 totalmin,("+inkm+"-kmout) totalkm,("+infuel+"-fout) totalfuel\n" +
                            "    from (select  cast(concat('"+indate+"',' ','"+intime+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)\n" +
                            "    ts_dout,kmout,fout from gl_vmove where doc_no="+maxparent+")m";
                }
                totaldata=template.query(strtotal,new RowMapper<RepMoveModel>(){
                    @Override
                    public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RepMoveModel objtemp=new RepMoveModel();
                        objtemp.setTotalmin(rs.getString("totalmin"));
                        objtemp.setTotalkm(rs.getString("totalkm"));
                        objtemp.setTotalfuel(rs.getString("totalfuel"));
                        return objtemp;
                    }
                });

                strupdate="update gl_vmove set status='IN',din='"+indate+"',tin='"+intime+"',kmin='"+inkm+"',fin='"+infuel+"',ibrhid="+inbrhid+",\n" +
                        "  ilocid="+inlocid+",ireason='Replacement Delivery via App',ttime="+totaldata.get(0).getTotalmin()+",tkm="+totaldata.get(0).getTotalkm()+",tfuel="+totaldata.get(0).getTotalfuel()+" where fleet_no="+fleetno+" and status='OUT'";
                System.out.println(strupdate);
                update=template.update(strupdate);
                if(update<=0){
                    return 0;
                }

                maxparentdata=template.query("select (select max(doc_no) from gl_vmove where fleet_no="+fleetno+") maxparent,(select max(doc_no)+1 from gl_vmove) maxmovdoc",new RowMapper<RepMoveModel>(){
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

                if(dbType.trim().equalsIgnoreCase("MySQL")){
                    stridle="select  TIMESTAMPDIFF(SECOND,ts_din,ts_dout)/60 idletime from (select  cast(concat(din,' ',tin) as datetime) ts_din,\n" +
                            "    cast(concat('"+indate+"',' ','"+intime+"')as datetime)ts_dout  from gl_vmove where doc_no="+maxparent+")m";
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    stridle="select  dateDIFF(SECOND,ts_din,ts_dout)/60 idletime from (select  cast(concat(din,' ',tin) as datetime) ts_din,\n" +
                            "    cast(concat('"+indate+"',' ','"+intime+"')as datetime)ts_dout  from gl_vmove where doc_no="+maxparent+")m";
                }
                idledata=template.query(stridle,new RowMapper<RepMoveModel>(){
                    @Override
                    public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RepMoveModel objtemp=new RepMoveModel();
                        objtemp.setIdletime(rs.getString("idletime"));
                        return objtemp;
                    }
                });

                String agmttrancode="";
                if(model.getRdtype().equalsIgnoreCase("RAG")){
                    agmttrancode="RA";
                }
                else{
                    agmttrancode="LA";
                }

                String strinsert="";
                if(dbType.trim().equalsIgnoreCase("MySQL")){
                    strinsert="insert into gl_vmove(doc_no,date,rdocno,rdtype,fleet_no,trancode,status,parent,repno,dout,tout,kmout,fout,obrhid,olocid,"+
                            " tideal,emp_id,emp_type)values("+maxmovdoc+",CURDATE(),"+model.getRdocno()+",'"+model.getRdtype()+"',"+fleetno+",'"+agmttrancode+"','OUT',"+
                            " "+maxparent+","+model.getRepno()+",'"+indate+"','"+intime+"','"+inkm+"','"+infuel+"',"+inbrhid+","+inlocid+","+idledata.get(0).getIdletime()+","+model.getCldocno()+",'CRM')";
                } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                    int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove ON");
                    strinsert="insert into gl_vmove(doc_no,date,rdocno,rdtype,fleet_no,trancode,status,parent,repno,dout,tout,kmout,fout,obrhid,olocid,"+
                            " tideal,emp_id,emp_type)values("+maxmovdoc+",convert(date,getdate()),"+model.getRdocno()+",'"+model.getRdtype()+"',"+fleetno+",'"+agmttrancode+"','OUT',"+
                            " "+maxparent+","+model.getRepno()+",'"+indate+"','"+intime+"','"+inkm+"','"+infuel+"',"+inbrhid+","+inlocid+","+idledata.get(0).getIdletime()+","+model.getCldocno()+",'CRM')";
                }
                update=template.update(strinsert);
                if(update<=0){
                    return 0;
                }
                if(dbType.trim().equalsIgnoreCase("MSSQL")){
                    int identityinsert=template.update("SET IDENTITY_INSERT gl_vmove OFF");
                }
                if(model.getRdtype().equalsIgnoreCase("RAG")){
                    strupdate="update gl_ragmt set fleet_no="+fleetno+" where doc_no="+model.getRdocno();
                }
                else if(model.getRdtype().equalsIgnoreCase("LAG")){
                    strupdate="update gl_lagmt set tmpfleet="+fleetno+" where doc_no="+model.getRdocno();
                }
                update=template.update(strupdate);
                if(update<=0){
                    return 0;
                }
                
                //Updating in gl_vehmaster of Current Out fleetno;
                String temptran="";
    			if(model.getRdtype().equalsIgnoreCase("RAG")){
    				temptran="RA";
    			}
    			else if(model.getRdtype().equalsIgnoreCase("LAG")){
    				temptran="LA";
    			}
    			String strsqlveh="update gl_vehmaster set status='OUT',tran_code='"+temptran+"',a_br="+model.getObrhid()+",a_loc="+model.getOlocid()+" where fleet_no="+model.getOfleetno();
    			int vehoutupdate=template.update(strsqlveh);
    			if(vehoutupdate<=0){
    				System.out.println("Vehicle Out Update Error");
    				return 0;
    			}
            }

        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    public String getAgmtInvToDate(JdbcTemplate template, int rdocno, String rdtype, int monthcalmethod, int monthcalvalue,int invtype,
                                 java.sql.Date sqldate,int onedayextraconfig) {
        String invtodate="";
        try{
            String strupdate="";
            if(rdtype.equalsIgnoreCase("RAG")){
                if(invtype==1){
                    if(dbType.trim().equalsIgnoreCase("MySQL")){
                        strupdate="select case when LAST_DAY('"+sqldate+"')='"+sqldate+"' then LAST_DAY(date_add('"+sqldate+"',interval 1 month)) else LAST_DAY('"+sqldate+"') end invtodate";
                    } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                        strupdate="select case when EOMONTH('"+sqldate+"')='"+sqldate+"' then EOMONTH(dateadd(month,1,'"+sqldate+"')) else EOMONTH('"+sqldate+"') end invtodate";
                    }
                }
                else if(invtype>1){
                    if(dbType.trim().equalsIgnoreCase("MySQL")){
                        strupdate="select case when "+monthcalmethod+"=1 then date_add('"+sqldate+"',interval 1 month) else date_add('"+sqldate+"',interval "+monthcalvalue+" day) end invtodate";
                    } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                        strupdate="select case when "+monthcalmethod+"=1 then dateadd(month,1,'"+sqldate+"') else dateadd(day,"+monthcalvalue+",'"+sqldate+"') end invtodate";
                    }

                }
                if(onedayextraconfig==1){
                    if(dbType.trim().equalsIgnoreCase("MySQL")) {
                		strupdate="select date_sub(base.invtodate,interval 1 day) invtodate from ("+strupdate+") base";
                	}
                	else if(dbType.trim().equalsIgnoreCase("MSSQL")) {
                		strupdate="select dateadd(day,-1,base.invtodate) invtodate from ("+strupdate+") base";
                	}
                }
            }

            List<RepMoveModel> datedata=template.query(strupdate, new RowMapper<RepMoveModel>() {
                @Override
                public RepMoveModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    RepMoveModel objtemp=new RepMoveModel();
                    objtemp.setInvtodate(rs.getString("invtodate"));
                    return objtemp;
                }
            });
            return datedata.get(0).getInvtodate();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return invtodate;
    }

    public List<MovModel> getParentData(String fleetno,JdbcTemplate template) {
        try{
            String strsql="select (select max(doc_no) from gl_vmove where fleet_no="+fleetno+") maxparent,(select max(doc_no)+1 from gl_vmove) maxmovdoc";
            System.out.println(strsql);
            return template.query(strsql,new RowMapper<MovModel>(){
                @Override
                public MovModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    MovModel objtemp=new MovModel();
                    objtemp.setMaxparent(rs.getString("maxparent"));
                    objtemp.setMaxmovdoc(rs.getString("maxmovdoc"));
                    return objtemp;
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<MovModel> getTotalData(Date sqldate, String time, String km, String fuel, String maxparent,JdbcTemplate template) {
        try{
            String strsql="";
            System.out.println("DB IN COMMON:"+dbType);
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                strsql="select  TIMESTAMPDIFF(SECOND,ts_dout,ts_din)/60 totalmin,("+km+"-kmout) totalkm,("+fuel+"-fout) totalfuel" +
                        " from (select  cast(concat('"+sqldate+"',' ','"+time+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)" +
                        " ts_dout,kmout,fout from gl_vmove where doc_no="+maxparent+")m";
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                strsql="select  dateDIFF(SECOND,ts_dout,ts_din)/60 totalmin,("+km+"-kmout) totalkm,("+fuel+"-fout) totalfuel" +
                        " from (select  cast(concat('"+sqldate+"',' ','"+time+"') as datetime) ts_din, cast(concat(dout,' ',tout)as datetime)" +
                        " ts_dout,kmout,fout from gl_vmove where doc_no="+maxparent+")m";
            }
            System.out.println(strsql);
            return template.query(strsql,new RowMapper<MovModel>(){
                @Override
                public MovModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    MovModel objtemp=new MovModel();
                    objtemp.setTotalmin(rs.getString("totalmin"));
                    objtemp.setTotalkm(rs.getString("totalkm"));
                    objtemp.setTotalfuel(rs.getString("totalfuel"));
                    return objtemp;
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<MovModel> getIdleData(Date sqldate, String time, String maxparent,JdbcTemplate template) {
        try{
            String strsql="";
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                strsql="select  TIMESTAMPDIFF(SECOND,ts_din,ts_dout)/60 idletime from (select  cast(concat(din,' ',tin) as datetime) ts_din,\n" +
                "    cast(concat('"+sqldate+"',' ','"+time+"')as datetime)ts_dout  from gl_vmove where doc_no="+maxparent+")m";
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                strsql="select  dateDIFF(SECOND,ts_din,ts_dout)/60 idletime from (select  cast(concat(din,' ',tin) as datetime) ts_din,\n" +
                        "    cast(concat('"+sqldate+"',' ','"+time+"')as datetime)ts_dout  from gl_vmove where doc_no="+maxparent+")m";
            }

            return template.query(strsql,new RowMapper<MovModel>(){
                @Override
                public MovModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                    MovModel objtemp=new MovModel();
                    objtemp.setIdlemin(rs.getString("idletime"));
                    return objtemp;
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public List<MiscModel> getMaxFileSrno(int docno,String code,JdbcTemplate template){
        String strsql="select coalesce(max(sr_no)+1,1) srno from my_fileattach where doc_no="+docno+" and dtype='"+code+"'";
        return template.query(strsql, new RowMapper<MiscModel>() {
            @Override
            public MiscModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                MiscModel model=new MiscModel();
                model.setSrno(rs.getString("srno"));
                return model;
            }
        });
    }
}
