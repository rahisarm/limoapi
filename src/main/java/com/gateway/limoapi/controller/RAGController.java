package com.gateway.limoapi.controller;

import com.gateway.limoapi.model.AgmtModel;
import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.InspectionModel;
import com.gateway.limoapi.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class RAGController {

    @Autowired
    RAGService service;

    @GetMapping("/getRentalReadyFleet")
    @ResponseBody
    public List<DropdownModel> getRentalReadyFleets(@RequestParam String brhid){
        return service.getRentalReadyFleets(brhid);
    }

    @GetMapping("/getClientDriver")
    @ResponseBody
    public List<DropdownModel> getClientDriver(@RequestParam String cldocno){
        return service.getClientDriver(cldocno);
    }

    @GetMapping("/getTariff")
    @ResponseBody
    public List<DropdownModel> getTariff(@RequestParam String fleetno){
        return service.getTariff(fleetno);
    }

    @GetMapping("/getTariffDetails")
    @ResponseBody
    public List<Map<String,Object>> getTariffDetails(@RequestParam Map<String,Object> requestmap){
        return service.getTariffDetails(requestmap);
    }

    @GetMapping("/getCompanyDriver")
    @ResponseBody
    public List<DropdownModel> getCompanyDriver(){
        return service.getCompanyDriver();
    }
    
    @GetMapping("/getFleetLastMovData")
    @ResponseBody
    public Map<String,Object> getFleetLastMovData(@RequestParam String fleetno){
    	return service.getFleetLastMovData(fleetno);
    }

    @PostMapping("/checkFleetAvailable")
    @ResponseBody
    public ResponseEntity<String> checkFleetAvailable(@RequestBody Map<String, String> data){
        boolean status=service.checkFleetAvailable(data);
        HttpHeaders headers=new HttpHeaders();
        if(status){
            return new ResponseEntity<>(status+"",headers, HttpStatus.OK);
        }
        else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fleet Not Available");
        }
    }

    @PostMapping("/saveRentalAgreement")
    @ResponseBody
    public Map<String, String> saveRentalAgreement(@RequestBody Map<String, Object> data){
    	Map<String, String> agmtmap=new HashMap<String,String>();
    	try{
            return service.insertRentalAgreement(data);
            
        }
        catch(Exception e){
            System.out.println("Controller Exception"+e.getMessage());
            return agmtmap;
        }

    }
    
    @GetMapping("/getPickupData")
    @ResponseBody
    public Map<String,Object> getPickupData(@RequestParam String docno){
    	return service.getPickupData(docno);
    }
    
    @GetMapping("/getAppPrintConfig")
    @ResponseBody
    public Map<String,Object> getAppPrintConfig(){
    	return service.getAppPrintConfig();
    }
}
