package com.gateway.limoapi.repository;

import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.MovModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MovRepository extends JpaRepository<MovModel,Integer> {

}
