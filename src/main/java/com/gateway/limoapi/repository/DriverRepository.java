package com.gateway.limoapi.repository;

import com.gateway.limoapi.model.DriverModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverRepository extends JpaRepository<DriverModel,Integer> {
    @Query(value = "select doc_no drvdocno,sal_name drivername,case when coalesce(mail,'')='' then coalesce(mobile,'') else coalesce(mail,'') end driverdetail from my_salesman where status<>7 and sal_type='DRV' and doc_no=?1",nativeQuery = true)
    List<DriverModel> findAllById(int drvdocno);
}
