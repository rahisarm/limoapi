package com.gateway.limoapi.controller;

import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.repository.DropdownRepository;
import com.gateway.limoapi.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DropdownController {

    @Autowired
    InspectionService service;
    @Autowired
    private DropdownRepository listrepo;

    @GetMapping("/getBranch")
    @ResponseBody
    public List<DropdownModel> getBranch(){
        return listrepo.getBranch();
    }

    @GetMapping("/getLocation")
    @ResponseBody
    public List<DropdownModel> getLocation(@RequestParam Map<String,String> queryparams){
        return listrepo.getLocation(queryparams.get("brhid"));
    }

    @GetMapping("/getMovType")
    @ResponseBody
    public List<DropdownModel> getMovType(){
        return listrepo.getMovType();
    }

    @GetMapping("/getMovementFleet")
    @ResponseBody
    public List<DropdownModel> getMovementFleet(){
        return listrepo.getMovementFleet();
    }

    @GetMapping("/getMovementGarage")
    @ResponseBody
    public List<DropdownModel> getMovementGarage(){
        return listrepo.getMovementGarage();
    }

    @GetMapping("/getCardType")
    @ResponseBody
    public List<DropdownModel> getCardType(){
        return listrepo.getCardType();
    }

    @GetMapping("/getInspFleet")
    @ResponseBody
    public List<DropdownModel> getInspFleet(){
        return listrepo.getInspFleet();
    }

    @GetMapping("/getInspRefDoc")
    @ResponseBody
    public List<DropdownModel> getInspRefDoc(@RequestParam Map<String,String> queryparams){
        String type=queryparams.get("type");
        String reftype=queryparams.get("reftype");
        String brhid=queryparams.get("brhid");
        String fleetno=queryparams.get("fleetno");
        return service.getInspRefDoc(type,reftype,brhid,fleetno);
    }
}
