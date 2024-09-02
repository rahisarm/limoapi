package com.gateway.limoapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.limoapi.model.DriverModel;
import com.gateway.limoapi.repository.DriverRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DriverController {
  
    @Autowired
    private DriverRepository repo;

    @GetMapping("/getDriverName")
    @ResponseBody
    public List<DriverModel> getDriverName(@RequestParam Map<String,String> queryparams){
        return repo.findAllById(Integer.parseInt(queryparams.get("drvdocno")));
    }

    
}
