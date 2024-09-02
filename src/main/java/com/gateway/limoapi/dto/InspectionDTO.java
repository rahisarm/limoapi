package com.gateway.limoapi.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.InspectionModel;
import com.gateway.limoapi.model.MiscModel;

public class InspectionDTO {

    public List<InspectionModel> getSavedInspectData(String inspdocno,JdbcTemplate template) {
        System.out.println("Inside Saved Inspect Data");
        String strsql="SELECT m.brhid,m.rfleet,m.doc_no inspdocno,m.voc_no inspvocno,m.reftype,m.refdocno,m.REFVOUCHERNO refvocno,CONCAT(veh.REG_NO,' ',auth.AUTHNAME,' ',plt.CODE_NAME) regno,veh.FLNAME FROM gl_vinspm m \n" +
                "LEFT JOIN gl_vehmaster veh ON m.rfleet=veh.FLEET_NO \n" +
                "LEFT JOIN gl_vehauth auth ON veh.AUTHID=auth.DOC_NO\n" +
                "LEFT JOIN gl_vehplate plt ON veh.PLTID=plt.DOC_NO WHERE m.doc_no="+inspdocno;
        return template.query(strsql, new RowMapper<InspectionModel>() {
            @Override
            public InspectionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                InspectionModel model=new InspectionModel();
                model.setDocno(rs.getInt("inspdocno"));
                model.setFleetno(rs.getString("rfleet"));
                model.setFlname(rs.getString("flname"));
                model.setReftype(rs.getString("reftype"));
                model.setRefdocno(rs.getString("refdocno"));
                model.setRefvocno(rs.getString("refvocno"));
                model.setRegdetails(rs.getString("regno"));
                model.setFlname(rs.getString("flname"));
                model.setBrhid(rs.getString("brhid"));
                return model;
            }
        });
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<InspectionModel> getImagePath(JdbcTemplate template) {
        String strsql="select 1 docno,imgpath from my_comp where doc_no=1";
        return template.query(strsql, new RowMapper<InspectionModel>() {
            @Override
            public InspectionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                InspectionModel model=new InspectionModel();
                model.setDocno(rs.getInt("docno"));
                model.setImagepath(rs.getString("imgpath"));
                return model;
            }
        });
    }

    public List<InspectionModel> getRentalAgmtInspectDetails(String rdtype, String rdocno, String inspecttype, JdbcTemplate template) {
        String strsql="select r.brhid,'"+inspecttype+"' inspecttype,ac.refname,r.fleet_no fleetno,r.doc_no docno,r.voc_no vocno,r.date,'RAG' reftype,concat(veh.reg_no,' ',plt.code_name,' ',auth.authid,' ',veh.flname) flname   from"+
                " gl_ragmt r"+
                " left join gl_vehmaster veh on r.fleet_no=veh.fleet_no"+
                " left join my_acbook ac on (r.cldocno=ac.cldocno and ac.dtype='CRM')"+
                " left join gl_vehplate plt on veh.pltid=plt.doc_no"+
                " left join gl_vehauth auth on veh.authid=auth.doc_no where r.status=3 and r.doc_no="+rdocno;
        return template.query(strsql, new RowMapper<InspectionModel>() {
            @Override
            public InspectionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                InspectionModel model=new InspectionModel();
                model.setBrhid(rs.getString("brhid"));
                model.setInspecttype(rs.getString("inspecttype"));
                model.setRefname(rs.getString("refname"));
                model.setFleetno(rs.getString("fleetno"));
                model.setDocno(rs.getInt("docno"));
                model.setVocno(rs.getString("vocno"));
                model.setReftype(rs.getString("reftype"));
                model.setFlname(rs.getString("flname"));
                return model;
            }
        });
    }

    public List<InspectionModel> getLeaseAgmtInspectDetails(String rdtype, String rdocno, String inspecttype, JdbcTemplate template) {
        String strsql="select r.brhid,'"+inspecttype+"' inspecttype,ac.refname,veh.fleet_no fleetno,r.doc_no docno,r.voc_no vocno,r.date,'LAG' reftype,concat(veh.reg_no,' ',plt.code_name,' ',auth.authid,' ',veh.flname) flname   from"+
                " gl_lagmt r"+
                " left join gl_vehmaster veh on (r.tmpfleet=veh.fleet_no or r.perfleet=veh.fleet_no)"+
                " left join my_acbook ac on (r.cldocno=ac.cldocno and ac.dtype='CRM')"+
                " left join gl_vehplate plt on veh.pltid=plt.doc_no"+
                " left join gl_vehauth auth on veh.authid=auth.doc_no where r.status=3 and r.doc_no="+rdocno;
        return template.query(strsql, new RowMapper<InspectionModel>() {
            @Override
            public InspectionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                InspectionModel model=new InspectionModel();
                model.setBrhid(rs.getString("brhid"));
                model.setInspecttype(rs.getString("inspecttype"));
                model.setRefname(rs.getString("refname"));
                model.setFleetno(rs.getString("fleetno"));
                model.setDocno(rs.getInt("docno"));
                model.setVocno(rs.getString("vocno"));
                model.setReftype(rs.getString("reftype"));
                model.setFlname(rs.getString("flname"));
                return model;
            }
        });
    }

    public List<MiscModel> getInspFleetRefData(String fleetno, JdbcTemplate template) {
        String strsql="select case when rep.doc_no>0 then 'REP' else mov.rdtype end rdtype,\n" +
                "case when rep.doc_no>0 then 'REP' else mov.rdocno end rdocno,mov.fleet_no fleetno,mov.status movtype from gl_vmove mov\n" +
                "left join gl_ragmt rag on (mov.rdtype='RAG' and mov.rdocno=rag.doc_no)\n" +
                "left join gl_lagmt lag on (mov.rdtype='LAG' and mov.rdocno=lag.doc_no)\n" +
                "left join gl_vehreplace rep on mov.repno=rep.doc_no\n" +
                "where mov.doc_no=(select max(doc_no) from gl_vmove where fleet_no="+fleetno+")";
        return template.query(strsql, new RowMapper<MiscModel>() {
            @Override
            public MiscModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                MiscModel model=new MiscModel();
                model.setRdtype(rs.getString("rdtype"));
                model.setRdocno(rs.getString("rdocno"));
                model.setType(rs.getString("movtype"));
                model.setInspecttype(rs.getString("movtype"));
                model.setFleetno(rs.getString("fleetno"));
                return model;
            }
        });
    }

    public List<InspectionModel> getFileSrno(JdbcTemplate template,String inspectdocno,String dtype) {
        String strsql="SELECT MAX(sr_no)+1 filesrno FROM my_fileattach WHERE dtype='VIP' AND doc_no="+inspectdocno;
        return template.query(strsql, new RowMapper<InspectionModel>() {
            @Override
            public InspectionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                InspectionModel model=new InspectionModel();
                model.setFilesrno(rs.getInt("filesrno"));
                return model;
            }
        });
    }

    public List<DropdownModel> getInspRefDoc(String type, String reftype, String branch, String fleetno, JdbcTemplate template,String dbType) {
        String strsql="",sqltest="",sqlbranch="",table="";
        System.out.println("DB Type"+dbType);
        if(!fleetno.equalsIgnoreCase("")){
            sqltest+=" and veh.fleet_no like '%"+fleetno+"%'";
        }
        if(reftype.equalsIgnoreCase("RAG")){
            if(!branch.equalsIgnoreCase("") && !branch.equalsIgnoreCase("a")){
                sqlbranch+=" and r.brhid="+branch;
            }
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                table="select concat(r.voc_no,' ',date_format(r.date,'%d.%m.%Y'),' ',ac.refname) refname,r.doc_no docno from"+
                        " gl_ragmt r inner join gl_vehmaster veh on r.fleet_no=veh.fleet_no inner join my_acbook ac on (r.cldocno=ac.cldocno and ac.dtype='CRM')"+
                        " where r.status=3 "+sqlbranch+sqltest;
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                table="select concat(r.voc_no,' ',convert(varchar,r.date,104),' ',ac.refname) refname,r.doc_no docno from"+
                        " gl_ragmt r inner join gl_vehmaster veh on r.fleet_no=veh.fleet_no inner join my_acbook ac on (r.cldocno=ac.cldocno and ac.dtype='CRM')"+
                        " where r.status=3 "+sqlbranch+sqltest;
            }

        }
        if(reftype.equalsIgnoreCase("LAG")){
/*				table="select concat(l.voc_no,' ',date_format(l.date,'%d.%m.%Y'),' ',ac.refname) refname,l.doc_no docno from gl_lagmt l left join gl_vehmaster veh on (l.perfleet=veh.fleet_no or l.tmpfleet=veh.fleet_no) left join my_acbook ac"+
						" on (l.cldocno=ac.cldocno and ac.dtype='CRM') where l.status=3 and l.brhid="+branch+sqltest;*/
            if(!branch.equalsIgnoreCase("") && !branch.equalsIgnoreCase("a")){
                sqlbranch+=" and l.brhid="+branch;
            }
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                table="select concat(l.voc_no,' ',date_format(l.date,'%d.%m.%Y'),' ',ac.refname) refname,l.doc_no docno from gl_lagmt l left join gl_vmove mov on (mov.doc_no=(select max(doc_no) from gl_vmove where rdocno=l.doc_no and rdtype='LAG' group "+
                        " by rdtype,rdocno)) left join gl_vehmaster veh on mov.fleet_no=veh.fleet_no left join my_acbook ac on (l.cldocno=ac.cldocno and ac.dtype='CRM')  "+
                        " where l.status=3 "+sqlbranch+sqltest+" group by mov.rdtype,mov.rdocno";
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                table="select concat(l.voc_no,' ',convert(varchar,l.date,104),' ',ac.refname) refname,l.doc_no docno from gl_lagmt l left join gl_vmove mov on (mov.doc_no=(select max(doc_no) from gl_vmove where rdocno=l.doc_no and rdtype='LAG' group "+
                        " by rdtype,rdocno)) left join gl_vehmaster veh on mov.fleet_no=veh.fleet_no left join my_acbook ac on (l.cldocno=ac.cldocno and ac.dtype='CRM')  "+
                        " where l.status=3 "+sqlbranch+sqltest;
            }
        }
        if(reftype.equalsIgnoreCase("RPL")){
            String sqlfleet="";
            if(type.equalsIgnoreCase("IN")){
                sqlfleet="rep.rfleetno";
            }
            else if(type.equalsIgnoreCase("OUT")){
                sqlfleet="rep.ofleetno";
            }
            else{
                sqlfleet="rep.rfleetno";
            }

            if(!branch.equalsIgnoreCase("") && !branch.equalsIgnoreCase("a")){
                sqlbranch+=" and rep.inbrhid="+branch;
            }
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                table="select distinct rep.doc_no,concat(rep.doc_no,' ',date_format(rep.date,'%d.%m.%Y'),' ',ac.refname) refname,rep.doc_no docno from gl_vehreplace rep left join gl_ragmt rag on"+
                        " (rep.rdocno=rag.doc_no and rep.rtype='RAG') left join gl_lagmt lag on (rep.rdocno=lag.doc_no and rep.rtype='LAG') left join"+
                        " gl_vehmaster veh on ("+sqlfleet+"=veh.fleet_no) left join my_acbook ac on (if(rep.rtype='RAG',rag.cldocno=ac.cldocno,lag.cldocno=ac.cldocno)"+
                        " and ac.dtype='CRM') where rep.status=3 "+sqlbranch+sqltest;
            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                table="select rep.doc_no,concat(rep.doc_no,' ',convert(varchar,rep.date,104),' ',ac.refname) refname,rep.doc_no docno from gl_vehreplace rep left join gl_ragmt rag on"+
                        " (rep.rdocno=rag.doc_no and rep.rtype='RAG') left join gl_lagmt lag on (rep.rdocno=lag.doc_no and rep.rtype='LAG') left join"+
                        " gl_vehmaster veh on ("+sqlfleet+"=veh.fleet_no) left join my_acbook ac on (case when rep.rtype='RAG' then rag.cldocno else lag.cldocno end=ac.cldocno"+
                        " and ac.dtype='CRM') where rep.status=3 "+sqlbranch+sqltest;
            }

        }
        if(reftype.equalsIgnoreCase("NRM")){

            if(!branch.equalsIgnoreCase("") && !branch.equalsIgnoreCase("a")){
                sqlbranch+=" and nrm.brhid="+branch;
            }
            if(dbType.trim().equalsIgnoreCase("MySQL")){
                table="select distinct concat(nrm.doc_no,' ',date_format(nrm.date,'%d.%m.%Y'),' ',sal.sal_name) refname,nrm.doc_no docno from gl_nrm nrm "+
                        " left join gl_vehmaster veh  on  (nrm.fleet_no=veh.fleet_no) left join my_salesman sal on (if(nrm.movtype='ST',(nrm.staffid=sal.doc_no and "+
                        " sal.sal_type='STF'), (nrm.drid=sal.doc_no and sal.sal_type='DRV'))) where nrm.status=3 "+sqlbranch+sqltest;

            } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
                table="select concat(nrm.doc_no,' ',convert(varchar,nrm.date,104),' ',sal.sal_name) refname,nrm.doc_no docno from gl_nrm nrm "+
                        " left join gl_vehmaster veh  on  (nrm.fleet_no=veh.fleet_no) left join my_salesman sal on (case when nrm.movtype='ST' then (nrm.staffid=sal.doc_no and "+
                        " sal.sal_type='STF') else (nrm.drid=sal.doc_no and sal.sal_type='DRV')) where nrm.status=3 "+sqlbranch+sqltest;
            }

        }
        System.out.println(table);
        return template.query(table, new RowMapper<DropdownModel>() {
            @Override
            public DropdownModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                DropdownModel model=new DropdownModel();
                model.setDocno(rs.getString("docno"));
                model.setRefname(rs.getString("refname"));
                return model;
            }
        });
    }

    public List<InspectionModel> getRefVocno(JdbcTemplate template,String reftype,String refdocno) {
        String strsql="";
        if(reftype.equalsIgnoreCase("RAG")){
            strsql="select voc_no refvocno from gl_ragmt where doc_no="+refdocno;
        }
        else if(reftype.equalsIgnoreCase("LAG")){
            strsql="select voc_no refvocno from gl_lagmt where doc_no="+refdocno;
        }
        else if(reftype.equalsIgnoreCase("RPL")){
            strsql="select doc_no refvocno from gl_vehreplace where doc_no="+refdocno;
        }
        else if(reftype.equalsIgnoreCase("NRM")){
            strsql="select doc_no refvocno from gl_nrm where doc_no="+refdocno;
        }
        return template.query(strsql, new RowMapper<InspectionModel>() {
            @Override
            public InspectionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                InspectionModel model=new InspectionModel();
                model.setRefvocno(rs.getString("refvocno"));
                return model;
            }
        });
    }

    public List<InspectionModel> getLastInspData(String fleetno, JdbcTemplate template,String dbType) {
        String strsql="";
        if(dbType.trim().equalsIgnoreCase("MySQL")){
            strsql="SELECT (select coalesce(projectorigin,'') from my_comp where doc_no=1 ) erporigin,coalesce(f.path,'') imagepath,insp.doc_no inspdocno,insp.refdocno,insp.voc_no inspvocno,DATE_FORMAT(insp.date,'%d.%m.%Y') inspdate,\n" +
                    "CASE WHEN insp.reftype='RAG' THEN 'Rental Agreement' WHEN insp.reftype='LAG' THEN 'Lease Agreement' WHEN insp.reftype='RPL' THEN 'Replacement' WHEN insp.reftype='NRM' THEN 'Non Revenue Movement' ELSE '' END reftype,\n" +
                    "CASE WHEN insp.reftype='RAG' THEN rag.voc_no WHEN insp.reftype='LAG' THEN lag.voc_no WHEN insp.reftype='RPL' THEN rep.doc_no WHEN insp.reftype='NRM' THEN nrm.doc_no ELSE '' END refvocno FROM (SELECT MAX(doc_no) maxdocno,rfleet FROM gl_vinspm WHERE STATUS=3 GROUP BY rfleet) base \n" +
                    "LEFT JOIN gl_vinspm insp ON insp.doc_no=base.maxdocno AND insp.rfleet=base.rfleet\n" +
                    "LEFT JOIN gl_ragmt rag ON insp.reftype='RAG' AND insp.refdocno=rag.doc_no\n" +
                    "LEFT JOIN gl_lagmt lag ON insp.reftype='LAG' AND insp.refdocno=lag.doc_no\n" +
                    "LEFT JOIN gl_vehreplace rep ON insp.reftype='RPL' AND insp.refdocno=rep.doc_no\n" +
                    "LEFT JOIN gl_nrm nrm ON insp.reftype='NRM' AND insp.refdocno=nrm.doc_no "+
                    " left join my_fileattach f on (f.doc_no=insp.doc_no and f.dtype='VIP' and f.ref_id=1 and f.clientview=1) WHERE insp.rfleet="+fleetno;

        } else if (dbType.trim().equalsIgnoreCase("MSSQL")) {
            strsql="SELECT (select coalesce(projectorigin,'') from my_comp where doc_no=1 ) erporigin,coalesce(f.path,'') imagepath,insp.doc_no inspdocno,insp.refdocno,insp.voc_no inspvocno,convert(varchar,insp.date,104) inspdate,\n" +
                    "CASE WHEN insp.reftype='RAG' THEN 'Rental Agreement' WHEN insp.reftype='LAG' THEN 'Lease Agreement' WHEN insp.reftype='RPL' THEN 'Replacement' WHEN insp.reftype='NRM' THEN 'Non Revenue Movement' ELSE '' END reftype,\n" +
                    "CASE WHEN insp.reftype='RAG' THEN rag.voc_no WHEN insp.reftype='LAG' THEN lag.voc_no WHEN insp.reftype='RPL' THEN rep.doc_no WHEN insp.reftype='NRM' THEN nrm.doc_no ELSE '' END refvocno FROM (SELECT MAX(doc_no) maxdocno,rfleet FROM gl_vinspm WHERE STATUS=3 GROUP BY rfleet) base \n" +
                    "LEFT JOIN gl_vinspm insp ON insp.doc_no=base.maxdocno AND insp.rfleet=base.rfleet\n" +
                    "LEFT JOIN gl_ragmt rag ON insp.reftype='RAG' AND insp.refdocno=rag.doc_no\n" +
                    "LEFT JOIN gl_lagmt lag ON insp.reftype='LAG' AND insp.refdocno=lag.doc_no\n" +
                    "LEFT JOIN gl_vehreplace rep ON insp.reftype='RPL' AND insp.refdocno=rep.doc_no\n" +
                    "LEFT JOIN gl_nrm nrm ON insp.reftype='NRM' AND insp.refdocno=nrm.doc_no "+
                    " left join my_fileattach f on (f.doc_no=insp.doc_no and f.dtype='VIP' and f.ref_id=1 and f.clientview=1) WHERE insp.rfleet="+fleetno;

        }

        return template.query(strsql, new RowMapper<InspectionModel>() {
            @Override
            public InspectionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
                InspectionModel model=new InspectionModel();
                model.setRefvocno(rs.getString("refvocno"));
                model.setRefdocno(rs.getString("refdocno"));
                model.setReftype(rs.getString("reftype"));
                model.setDocno(rs.getInt("inspdocno"));
                model.setDate(rs.getString("inspdate"));
                model.setImagepath(rs.getString("imagepath"));
                model.setRegdetails(rs.getString("erporigin"));
                return model;
            }
        });
    }
}
