package com.gateway.limoapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.DriverTasks;
import com.gateway.limoapi.model.LimoDataModel;
import com.gateway.limoapi.model.MovModel;
import com.gateway.limoapi.service.LImoDriverTasksService;
import com.gateway.limoapi.service.MovementService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class LimoDriverTasksController {

	    ClsCommon objcommon=new ClsCommon();
	    @Autowired
	    private LImoDriverTasksService limodriverTasksService;
	    
	    @Autowired
	    private MovementService service;
	   
	    @GetMapping("/allTasksLimo")
	    @ResponseBody
	    public List<DriverTasks> getAllTasks(@RequestParam Map<String,String> queryparams){
	        //return driverTasksRepository.findAllById(queryparams.get("driverdocno"));
	        return limodriverTasksService.getDriverTasks(queryparams.get("driverdocno"));
	    }
	    
	    @PostMapping("/setStartTrip")
	    @ResponseBody
	    public boolean setStartTrip(@RequestParam Map<String,String> queryparams){
	        return limodriverTasksService.saveStartTripInsert(queryparams);
	    }
	    
	    @PostMapping("/setEndTrip")
	    @ResponseBody
	    public boolean setEndTrip(@RequestParam(value = "image",required = false) List<MultipartFile> multifile, @RequestParam("signature") List<MultipartFile> smultifile,@RequestParam Map<String,String> queryparams){
	        System.out.println("Entered End Trip");
	    	return limodriverTasksService.saveEndTripInsert(multifile,smultifile,queryparams);
	    }
	    
	    @GetMapping("/getStartTripData")
	    @ResponseBody
	    public List<LimoDataModel> getMovExistData(@RequestParam Map<String,String> queryparams){
	        return service.getLimoMovExistData(queryparams.get("rdocno"),queryparams.get("driverdocno"));
	    }
	    
	    @GetMapping("/getjobs")
	    @ResponseBody
	    public List<LimoDataModel> getJobs(@RequestParam Map<String,String> queryparams){
	    	return limodriverTasksService.getJobs(queryparams.get("driverdocno"));
	    }
	    
	    @GetMapping("/getclientbyjob")
	    @ResponseBody
	    public List<LimoDataModel> getclient(@RequestParam Map<String,String> queryparams){
	    	return limodriverTasksService.getclient(queryparams.get("rdocno"));
	    }
	    
	    @PostMapping("/saveExpenses")
	    @ResponseBody
	    public boolean saveExpenses(@RequestParam(value = "image",required = false) List<MultipartFile> multifile,@RequestParam() Map<String,String> queryparams){
	       // System.out.println(multifile.toString());
	    	return limodriverTasksService.saveExpenses(multifile,queryparams);
	    }
	    
	    @PostMapping("/saveJobAssignment")
	    @ResponseBody
	    public boolean saveJobAssignment(@RequestParam("image") List<MultipartFile> multifile,@RequestParam() Map<String,String> queryparams){
	        return limodriverTasksService.saveExpenses(multifile,queryparams);
	    }
	   
	    @GetMapping("/getjobstoassign")
	    @ResponseBody
	    public List<LimoDataModel> getjobstoassign(){
	    	return limodriverTasksService.getjobstoassign();
	    }
	    
	    @GetMapping("getStartFleetMinData")
	    @ResponseBody
	    public List<MovModel> getStartFleetMinData(@RequestParam Map<String,String> queryparams){
	        String fleetno=queryparams.get("fleetno");
	        String movstage="1";
	        return limodriverTasksService.getStartFleetMinData(fleetno,movstage);
	    }
	    
	    @GetMapping("getEndFleetMinData")
	    @ResponseBody
	    public List<MovModel> getEndFleetMinData(@RequestParam Map<String,String> queryparams){
	        String fleetno=queryparams.get("fleetno");
	        String movstage="1";
	        return limodriverTasksService.getEndFleetMinData(fleetno,movstage);
	    }
}
