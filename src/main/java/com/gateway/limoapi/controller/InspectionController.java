package com.gateway.limoapi.controller;

import com.gateway.limoapi.model.DriverTasks;
import com.gateway.limoapi.model.InspectionModel;
import com.gateway.limoapi.model.MiscModel;
import com.gateway.limoapi.repository.InspectionRepository;
import com.gateway.limoapi.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class InspectionController {
    @Autowired
    InspectionService service;
    @Autowired
    InspectionRepository repo;

    @GetMapping("/getInspectionDetails")
    @ResponseBody
    public List<InspectionModel> getInspectionDetails(@RequestParam Map<String,String> queryparams){
        String rdtype=queryparams.get("rdtype");
        String rdocno=queryparams.get("rdocno");
        String repno=queryparams.get("repno");
        String inspecttype=queryparams.get(("inspecttype"));
        System.out.println(rdtype+"::"+rdocno+"::"+repno+"::"+inspecttype);
        if(rdtype.equalsIgnoreCase("RAG")){
            return service.getRentalAgmtInspectDetails(rdtype,rdocno,inspecttype);
        }
        else if(rdtype.equalsIgnoreCase("LAG")){
            return service.getLeaseAgmtInspectDetails(rdtype,rdocno,inspecttype);
        }
        else if(rdtype.equalsIgnoreCase("REP") ){
            return repo.getRepInspectDetails(repno,inspecttype);
        }
        else if (rdtype.equalsIgnoreCase("NRM")){
            return repo.getMovInspectDetails(rdocno,inspecttype);
        }
        return null;
    }


    @PostMapping("/saveInspection")
    @ResponseBody
    public int saveInspection(@RequestParam("image") List<MultipartFile> multifile,@RequestParam() Map<String,String> queryparams){
        System.out.println("Inside Inspection Controller");
        try{
            int inspdocno=service.saveInspection(multifile,queryparams);
            if(inspdocno>0){
                return inspdocno;
            }
            else{
                return 0;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    @GetMapping("/getFleetRefData")
    @ResponseBody
    public List<MiscModel> getFleetRefData(@RequestParam("fleetno") String fleetno){
        return service.getInspFleetRefData(fleetno);
    }

    @GetMapping("/getSavedInspectData")
    @ResponseBody
    public List<InspectionModel> getSavedInspData(@RequestParam("inspectdocno") String inspdocno){
        System.out.println("inside saved insp data action");
        return service.getSavedInspectData(inspdocno);
    }

    @PostMapping("/updateInspection")
    @ResponseBody
    public ResponseEntity<?> updateInspection(@RequestParam("image") MultipartFile multifile,@RequestParam("inspdocno") String inspdocno,@RequestParam("brhid") String brhid){
        System.out.println("Inside Inspection Controller");
        try{
            boolean status=service.updateInspection(multifile,inspdocno,brhid);
            if(status){
                return new ResponseEntity<>(true,HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
        }
    }

    @GetMapping("/getLastInspData")
    @ResponseBody
    public List<InspectionModel> getLastInspData(@RequestParam("fleetno") String fleetno){
        return service.getLastInspData(fleetno);
    }
    
    @GetMapping("/getInspLatestFleetData")
    @ResponseBody
    public Map<String,Object> getInspLatestFleetData(@RequestParam("fleetno") String fleetno){
        return service.getInspLatestFleetData(fleetno);
    }
    
    @GetMapping("/getFileTypes")
    @ResponseBody
    public List<Map<String,Object>> getFileTypes(){
        return service.getFileTypes();
    }
}
