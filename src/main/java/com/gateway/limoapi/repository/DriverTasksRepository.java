package com.gateway.limoapi.repository;

import com.gateway.limoapi.model.DriverTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface DriverTasksRepository extends JpaRepository<DriverTasks,Long> {

}
