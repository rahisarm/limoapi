package com.gateway.limoapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.GateInPassModel;

@Service
public class GateInPassService {
	
	@Autowired
    JdbcTemplate template;
	
	@Autowired
	private ClsCommon objcommon;
	
	public List<GateInPassModel> getDetails(Map<String, Object> data){
		System.out.println("Entered");
		String strsql="",sqltest="",sqljoin="";
		java.sql.Date sqlfromdate=null,sqltodate=null;
		if(!data.get("fromdate").toString().equalsIgnoreCase("")){
			sqlfromdate=objcommon.changeStringtoSqlDate(data.get("fromdate").toString());
			sqltest+=" and nrm.date>='"+sqlfromdate+"'";
		}
		if(!data.get("todate").toString().equalsIgnoreCase("")){
			sqltodate=objcommon.changeStringtoSqlDate(data.get("todate").toString());
			sqltest+=" and nrm.date<='"+sqltodate+"'";
		}
		if(!data.get("brhid").toString().equalsIgnoreCase("a") && !data.get("brhid").toString().equalsIgnoreCase("")){
			sqltest+=" and nrm.brhid="+data.get("brhid").toString();
		}
		
		/*
		 * String
		 * strGatepath="SELECT coalesce(func,'') gatepath FROM my_menu WHERE menu_name='Gate In Pass' AND gate='E'"
		 * ;
		 */
		String gatepath = "com/workshop/gateinpassmaster/gateInPass.jsp";
		/*
		 * try { gatepath = template.queryForObject(strGatepath, String.class); }
		 * catch(EmptyResultDataAccessException e) { return null; }
		 */
        
		String sql = "select '"+gatepath+"' gatepath,cp.company,1 compno,veh.ch_no chassisno,yom.doc_no yomid,'Create' btnview,coalesce(mov1.oreason,'') description,veh.flname,nrm.doc_no,nrm.date,nrm.fleet_no,nrm.brhid,br.branchname,nrm.userid,usr.user_name user," 
        			 +" if(nrm.drid>0,nrm.drid,nrm.staffid) driverid,drv.sal_name driver,veh.reg_no,plate.code_name platecode,brd.brand_name brand,brd.doc_no "
        		     +" brandid,model.vtype model,model.doc_no modelid,yom.yom,mov.dout outdate,mov.tout outtime,mov.fout,CASE WHEN mov.fout=0.000 THEN "
        		     +" 'Level 0/8' WHEN mov.fout=0.125 THEN 'Level 1/8' WHEN mov.fout=0.250 THEN 'Level 2/8' WHEN mov.fout=0.375 THEN 'Level 3/8' "
        		     +" WHEN mov.fout=0.500 THEN 'Level 4/8' WHEN mov.fout=0.625 THEN 'Level 5/8'  WHEN mov.fout=0.750 THEN 'Level 6/8' WHEN "
        		     +" mov.fout=0.875 THEN 'Level 7/8' WHEN mov.fout=1.000 THEN 'Level 8/8'  END as outfuel,mov.kmout outkm from gl_nrm nrm "
        		     +" left join gl_vehmaster veh on nrm.fleet_no=veh.fleet_no left join gl_vehbrand brd on veh.brdid=brd.doc_no "
        		     +" left join gl_vehmodel model on veh.vmodid=model.doc_no left join gl_vehplate plate on veh.pltid=plate.doc_no "
        		     +" left join my_brch  br on nrm.brhid=br.doc_no left join my_comp cp on br.cmpid=cp.doc_no left join my_user usr on nrm.userid=usr.doc_no left join "
        		     +" my_salesman drv on (if(nrm.drid>0,(nrm.drid=drv.doc_no and drv.sal_type='DRV'),(nrm.staffid=drv.doc_no and "
        		     +" drv.sal_type='STF'))) left join gl_yom yom on veh.yom=yom.doc_no left join (select max(doc_no) maxdoc,rdocno,min(doc_no) mindoc from "
        		     +" gl_vmove where rdtype='MOV' group by rdocno) a on (a.rdocno=nrm.doc_no) left join gl_vmove mov on "
        		     +" (mov.rdocno=nrm.doc_no and mov.rdtype='MOV' and mov.doc_no=a.maxdoc) left join gl_vmove mov1 on "
        		     +" (mov1.rdocno=nrm.doc_no and mov1.rdtype='MOV' and mov1.doc_no=a.mindoc) where nrm.clstatus=0 and nrm.movtype in ('GA','GS','GM') and nrm.status=3 and nrm.gipno=0 "+sqltest+" "+sqljoin+"";
        System.out.println(sql);
        return template.query(sql, new RowMapper<GateInPassModel>() {
            @Override
            public GateInPassModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            	GateInPassModel model=new GateInPassModel();
                model.setGatepath(rs.getString("gatepath"));
                model.setCompany(rs.getString("company"));
                model.setCompno(rs.getInt("compno"));
                model.setChassisno(rs.getString("chassisno"));
                model.setYomid(rs.getInt("yomid"));
                model.setBtnview(rs.getString("btnview"));
                model.setDescription(rs.getString("description"));
                model.setFlname(rs.getString("flname"));
                model.setDoc_no(rs.getInt("doc_no"));
                model.setDate(rs.getString("date"));
                model.setFleet_no(rs.getString("fleet_no"));
                model.setBrhid(rs.getString("brhid"));
                model.setBranchname(rs.getString("branchname"));
                model.setUserid(rs.getString("userid"));
                model.setUser(rs.getString("user"));
                model.setDriverid(rs.getInt("driverid"));
                model.setDriver(rs.getString("driver"));
                model.setReg_no(rs.getString("reg_no"));
                model.setPlatecode(rs.getString("platecode"));
                model.setBrand(rs.getString("brand"));
                model.setBrandid(rs.getInt("brandid"));
                model.setModel(rs.getString("model"));
                model.setModelid(rs.getInt("modelid"));
                model.setYom(rs.getString("yom"));
                model.setOutdate(rs.getString("outdate"));
                model.setOuttime(rs.getString("outtime"));
                model.setFout(rs.getString("fout"));
                model.setOutfuel(rs.getString("outfuel"));
                model.setOutkm(rs.getString("outkm"));
                return model;
            }
        });
	}

	public Integer setDetails(Map<String, Object> data) {
		if(data.get("nrmdocno").toString()==null&&data.get("gipno").toString()==null) {
			return 0;
		}
		String updatesql = "update gl_nrm set gipno = '"+data.get("gipno").toString()+"' where doc_no="+data.get("nrmdocno").toString(); 
		Integer tarifinsert = template.update(updatesql);
		return tarifinsert;
	}
}
