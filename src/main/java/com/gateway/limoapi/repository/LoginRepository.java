package com.gateway.limoapi.repository;

import com.gateway.limoapi.model.DriverTasks;
import com.gateway.limoapi.model.LoginUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<LoginUser,Long> {
    @Query(value="select user_id username,pass password,driverid driverdocno,doc_no docno from my_user where status=3 and user_id=?1 and pass=?2",nativeQuery = true)
    List<LoginUser> findUserByUsernameAndPassword(String username,String password);
    
    @Query(value="select user_id username, pass password,doc_no docno,driverid driverdocno from my_user where status=3 and user_id=?1",nativeQuery=true)
    Optional<LoginUser> findByUsername(String username);
}
