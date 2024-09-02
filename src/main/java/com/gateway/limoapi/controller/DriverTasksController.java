package com.gateway.limoapi.controller;


import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.DriverTasks;
import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.model.MiscModel;
import com.gateway.limoapi.model.RepMoveModel;
import com.gateway.limoapi.repository.DriverTasksRepository;
import com.gateway.limoapi.service.DriverTasksService;
import com.gateway.limoapi.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DriverTasksController {
    @Autowired
    private DriverTasksRepository driverTasksRepository;

    ClsCommon objcommon=new ClsCommon();
    @Autowired
    private DriverTasksService driverTasksService;
    @GetMapping("/home")
    public String home(){
        return "This is homepage";
    }

    @GetMapping("/getGlobalData")
    @ResponseBody
    public List<MiscModel> getGlobalData(){
        return driverTasksService.getGlobalData();
    }
    @GetMapping("/allTasks")
    @ResponseBody
    public List<DriverTasks> getAllTasks(@RequestParam Map<String,String> queryparams){
        //return driverTasksRepository.findAllById(queryparams.get("driverdocno"));
        return driverTasksService.getDriverTasks(queryparams.get("driverdocno"));
    }

    @PostMapping("/startDriverTrip")
    @ResponseBody
    public ResponseEntity<?> startDriverTrip(@RequestParam Map<String,String> queryparams){
        boolean savestatus=driverTasksService.startDriverTrip(queryparams.get("rdocno"),queryparams.get("rdtype"),queryparams.get("driverdocno"));
        if(savestatus){
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/endDriverTrip")
    @ResponseBody
    public ResponseEntity<?> endDriverTrip(@RequestParam Map<String,String> queryparams){
        boolean savestatus=driverTasksService.endDriverTrip(queryparams.get("rdocno"),queryparams.get("rdtype"),queryparams.get("driverdocno"));
        if(savestatus){
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(false,HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/getActiveTask")
    @ResponseBody
    public List<DriverTasks> getActiveTask(@RequestParam Map<String,String> queryparams){
        return driverTasksService.getActiveTask(queryparams.get("rdocno"),queryparams.get("rdtype"),queryparams.get("tripmode"),queryparams.get("repno"));
    }

    @GetMapping("/getReplacementData")
    @ResponseBody
    public List<RepMoveModel> getReplacementData(@RequestParam Map<String,String> queryparams){
        return driverTasksService.getReplacementData(queryparams.get("rdocno"),queryparams.get("rdtype"),queryparams.get("tripmode"),queryparams.get("repno"));
    }
    
    @GetMapping("/getConfigData")
    @ResponseBody
    public List<DropdownModel> getConfigData(@RequestParam String configname){
    	return driverTasksService.getConfigData(configname);
    }
    
    
    @PostMapping("/saveAgmtDelivery")
    @ResponseBody
    public ResponseEntity<?> saveDelivery(@RequestParam Map<String,String> queryparams){
        try{
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String time=queryparams.get("time");
            String fuel=queryparams.get("fuel");
            double km=Double.parseDouble(queryparams.get("km"));
            int rdocno=Integer.parseInt(queryparams.get("rdocno"));
            int drvdocno=Integer.parseInt(queryparams.get("drvdocno"));
            String rdtype=queryparams.get("rdtype");
            String fleetno=queryparams.get("fleetno");
            boolean savestatus=false;
            if(rdtype.equalsIgnoreCase("RAG")){
                savestatus=driverTasksService.saveAgmtDelivery(sqldate,time,fuel,km,drvdocno,rdocno,rdtype,fleetno);
            }
            else if(rdtype.equalsIgnoreCase("LAG")){
                savestatus=driverTasksService.saveLeaseAgmtDelivery(sqldate,time,fuel,km,drvdocno,rdocno,rdtype,fleetno);
            }
            if(savestatus){
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
    }

    @PostMapping("/saveAgmtCollection")
    @ResponseBody
    public ResponseEntity<?> saveAgmtCollection(@RequestParam Map<String,String> queryparams){
        try{
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String time=queryparams.get("time");
            String fuel=queryparams.get("fuel");
            double km=Double.parseDouble(queryparams.get("km"));
            int rdocno=Integer.parseInt(queryparams.get("rdocno"));
            int drvdocno=Integer.parseInt(queryparams.get("drvdocno"));
            String rdtype=queryparams.get("rdtype");
            String fleetno=queryparams.get("fleetno");
            String userid=queryparams.get("userid");
            String pickupdocno=queryparams.get("pickupdocno");
            System.out.println("Pickup Docno:"+pickupdocno);
            boolean savestatus=driverTasksService.saveAgmtCollection(sqldate,time,fuel,km,drvdocno,rdocno,rdtype,fleetno,userid,pickupdocno);

            if(savestatus){
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
    }

    @PostMapping("/saveBranchCloseCollection")
    @ResponseBody
    public ResponseEntity<?> saveBranchCloseCollection(@RequestParam Map<String,String> queryparams){
        try{
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String time=queryparams.get("time");
            String fuel=queryparams.get("fuel");
            double km=Double.parseDouble(queryparams.get("km"));
            int rdocno=Integer.parseInt(queryparams.get("rdocno"));
            int drvdocno=Integer.parseInt(queryparams.get("drvdocno"));
            String rdtype=queryparams.get("rdtype");
            String fleetno=queryparams.get("fleetno");
            String userid=queryparams.get("userid");
            String pickupdocno=queryparams.get("pickupdocno");
            String brhid=queryparams.get("brhid");
            String locid=queryparams.get("locid");

            boolean savestatus=driverTasksService.saveBranchCloseCollection(sqldate,time,fuel,km,drvdocno,rdocno,rdtype,fleetno,userid,pickupdocno,brhid,locid);

            if(savestatus){
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
    }

    @PostMapping("/saveRepCollection")
    @ResponseBody
    public ResponseEntity<?> saveRepCollection(@RequestParam Map<String,String> queryparams){
        try{
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String time=queryparams.get("time");
            String fuel=queryparams.get("fuel");
            double km=Double.parseDouble(queryparams.get("km"));
            int repno=Integer.parseInt(queryparams.get("repno"));
            int drvdocno=Integer.parseInt(queryparams.get("drvdocno"));
            boolean savestatus=driverTasksService.updateRepCollection(sqldate,time,fuel,km,repno,drvdocno);
            if(savestatus){
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
        	e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/saveRepDelivery")
    @ResponseBody
    public ResponseEntity<?> saveRepDelivery(@RequestParam Map<String,String> queryparams){
        try{
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String time=queryparams.get("time");
            String fuel=queryparams.get("fuel");
            double km=Double.parseDouble(queryparams.get("km"));
            int repno=Integer.parseInt(queryparams.get("repno"));
            int drvdocno=Integer.parseInt(queryparams.get("drvdocno"));
            String deliverto=queryparams.get("deliverto");
            boolean savestatus=driverTasksService.updateRepDelivery(sqldate,time,fuel,km,repno,drvdocno,deliverto);
            if(savestatus){
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
        	e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/saveRepClosing")
    @ResponseBody
    public ResponseEntity<?> saveRepClosing(@RequestParam Map<String,String> queryparams){
        try{
            System.out.println("Inside Rep Closing Controller");
            java.sql.Date sqldate=objcommon.changeStringtoSqlDate(queryparams.get("date"));
            String time=queryparams.get("time");
            String fuel=queryparams.get("fuel");
            double km=Double.parseDouble(queryparams.get("km"));
            int repno=Integer.parseInt(queryparams.get("repno"));
            int drvdocno=Integer.parseInt(queryparams.get("drvdocno"));
            int brhid=Integer.parseInt(queryparams.get("brhid"));
            int locid=Integer.parseInt(queryparams.get("locid"));
            boolean savestatus=driverTasksService.updateRepClosing(sqldate,time,fuel,km,repno,drvdocno,brhid,locid);
            if(savestatus){
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e){
        	e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
