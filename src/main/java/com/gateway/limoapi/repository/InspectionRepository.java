package com.gateway.limoapi.repository;

import com.gateway.limoapi.model.InspectionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InspectionRepository  extends JpaRepository<InspectionModel,Integer> {
    @Query(value = "select '' refdocno,'' refvocno,'' regdetails,1 filesrno,r.brhid,?3 inspecttype,'' imagepath,ac.refname,r.fleet_no fleetno,r.doc_no docno,r.voc_no vocno,r.date,'RAG' reftype,concat(veh.reg_no,' ',plt.code_name,' ',auth.authid,' ',veh.flname) flname   from\n" +
            "gl_ragmt r\n" +
            "left join gl_vehmaster veh on r.fleet_no=veh.fleet_no\n" +
            "left join my_acbook ac on (r.cldocno=ac.cldocno and ac.dtype='CRM')\n" +
            "left join gl_vehplate plt on veh.pltid=plt.doc_no\n" +
            "left join gl_vehauth auth on veh.authid=auth.doc_no where r.status=3 and r.doc_no=?2",nativeQuery = true)
    List<InspectionModel> getRentalAgmtInspectDetails(String rdtype, String rdocno,String inspecttype);

    @Query(value="select '' refdocno,'' refvocno,'' regdetails,1 filesrno,r.brhid,?3 inspecttype'' imagepath,ac.refname,veh.fleet_no fleetno,r.doc_no docno,r.voc_no vocno,r.date,'RAG' reftype,concat(veh.reg_no,' ',plt.code_name,' ',auth.authid,' ',veh.flname) flname   from\n" +
            "gl_lagmt r\n" +
            "left join gl_vehmaster veh on (r.tmpfleet=veh.fleet_no or r.perfleet=veh.fleet_no)\n" +
            "left join my_acbook ac on (r.cldocno=ac.cldocno and ac.dtype='CRM')\n" +
            "left join gl_vehplate plt on veh.pltid=plt.doc_no\n" +
            "left join gl_vehauth auth on veh.authid=auth.doc_no where r.status=3",nativeQuery = true)
    List<InspectionModel> getLeaseAgmtInspectDetails(String rdtype, String rdocno,String inspecttype);


    @Query(value = "select '' refdocno,'' refvocno,'' regdetails,1 filesrno,rep.rbrhid brhid,?2 inspecttype,'' imagepath,ac.refname,veh.fleet_no fleetno,rep.doc_no docno,rep.doc_no vocno,rep.date,'REP' reftype,concat(veh.reg_no,' ',plt.code_name,' ',auth.authid,' ',veh.flname) flname from gl_vehreplace rep\n" +
            "left join gl_ragmt rag on(rep.rdocno=rag.doc_no and rep.rtype='RAG')\n" +
            "left join gl_lagmt lag on (rep.rdocno=lag.doc_no and rep.rtype='LAG')\n" +
            "left join gl_vehmaster veh on (case when ?2='IN' then rep.rfleetno else rep.ofleetno end=veh.fleet_no)\n" +
            "left join my_acbook ac on (case when rep.rtype='RAG' then rag.cldocno else lag.cldocno end =ac.cldocno and ac.dtype='CRM')\n" +
            "left join gl_vehplate plt on veh.pltid=plt.doc_no\n" +
            "left join gl_vehauth auth on veh.authid=auth.doc_no where rep.status=3 and rep.doc_no=?1",nativeQuery = true)
    List<InspectionModel> getRepInspectDetails(String rdocno,String inspecttype);

    @Query(value = "select '' refdocno,'' refvocno,'' regdetails,1 filesrno,nrm.brhid,?2 inspecttype,'' imagepath,coalesce(sal.sal_name,'') refname,veh.fleet_no fleetno,nrm.doc_no docno,nrm.doc_no vocno,nrm.date,'NRM' reftype,\n" +
            "concat(veh.reg_no,' ',plt.code_name,' ',auth.authid,' ',veh.flname) flname from gl_nrm nrm\n" +
            "left join gl_vehmaster veh on nrm.fleet_no=veh.fleet_no\n" +
            "left join gl_vehplate plt on veh.pltid=plt.doc_no\n" +
            "left join gl_vehauth auth on veh.authid=auth.doc_no\n" +
            "left join my_salesman sal on (nrm.drid=sal.doc_no and sal.sal_type='DRV')\n" +
            "where nrm.status=3 and nrm.doc_no=?1",nativeQuery = true)
    List<InspectionModel> getMovInspectDetails(String rdocno,String inspecttype);
}
