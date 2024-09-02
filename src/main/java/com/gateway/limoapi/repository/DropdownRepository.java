package com.gateway.limoapi.repository;

import com.gateway.limoapi.model.DropdownModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DropdownRepository extends JpaRepository<DropdownModel, String> {
    @Query(value = "select doc_no docno,branchname refname from my_brch where status=3",nativeQuery = true)
    List<DropdownModel> getBranch();

    @Query(value = "select doc_no docno,loc_name refname from my_locm where status=3 and brhid=?1",nativeQuery = true)
    List<DropdownModel> getLocation(String brhid);

    @Query(value = "select veh.fleet_no docno,concat(veh.reg_no,' ',plt.code_name,' ',veh.flname) refname from gl_vehmaster veh left join gl_vehplate plt on veh.pltid=plt.doc_no where veh.statu=3 and veh.fstatus in ('L','N') and veh.status='IN'",nativeQuery = true)
    List<DropdownModel> getMovementFleet();

    @Query(value = "select status docno,st_desc refname from gl_status where mov=1 and status<>'ST'",nativeQuery = true)
    List<DropdownModel> getMovType();

    @Query(value = "select doc_no docno,name refname from gl_garrage where  status<>7",nativeQuery = true)
    List<DropdownModel> getMovementGarage();

    @Query(value = "select doc_no docno,mode refname  from my_cardm where id=1 and dtype='card'",nativeQuery = true)
    List<DropdownModel> getCardType();

    @Query(value = "select veh.fleet_no docno,concat(veh.reg_no,' ',plt.code_name,' ',veh.flname) refname from gl_vehmaster veh left join gl_vehplate plt on veh.pltid=plt.doc_no where veh.statu=3 and veh.fstatus in ('L','N')",nativeQuery = true)
    List<DropdownModel> getInspFleet();

}
