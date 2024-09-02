package com.gateway.limoapi.controller;

import com.gateway.limoapi.helpers.ClsCommon;
import com.gateway.limoapi.model.MovModel;
import com.gateway.limoapi.repository.MovRepository;
import com.gateway.limoapi.service.MovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MovementController {

    @Autowired
    private MovRepository repo;

    @Autowired
    private MovementService service;

    ClsCommon objcommon=new ClsCommon();
    @GetMapping("getMovFleetMinData")
    @ResponseBody
    public List<MovModel> getMovFleetMinData(@RequestParam Map<String,String> queryparams){
        String fleetno=queryparams.get("fleetno");
        String movstage="1";
        return service.getMovFleetMinData(fleetno,movstage);
    }

    @PostMapping("saveMovInsert")
    @ResponseBody
    public ResponseEntity<?> saveMovInsert(@RequestParam Map<String,String> queryparams){
        try{
            boolean status=service.saveMovInsert(queryparams);
            if(status){
                return new ResponseEntity<>(true,HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
            }
        }
        catch(Exception e){
            return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
        }

    }

    @GetMapping("getMovExistData")
    @ResponseBody
    public List<MovModel> getMovExistData(@RequestParam Map<String,String> queryparams){
        return service.getMovExistData(queryparams.get("rdocno"));
    }

    @PostMapping("closeMovTransfer")
    @ResponseBody
    public ResponseEntity<?> closeMov(@RequestParam Map<String,String> queryparams){
        boolean status=service.closeMovTransfer(queryparams);
        if(status){
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
        }
    }

    @PostMapping("saveMovDelivery")
    @ResponseBody
    public ResponseEntity<?> saveMovDelivery(@RequestParam Map<String,String> queryparams){
        boolean status=service.saveMovDelivery(queryparams);
        if(status){
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
        }
    }

    @PostMapping("saveMovCollection")
    @ResponseBody
    public ResponseEntity<?> saveMovCollection(@RequestParam Map<String,String> queryparams){
        boolean status=service.saveMovCollection(queryparams);
        if(status){
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
        }
    }

    @PostMapping("saveMovGarageBranchClose")
    @ResponseBody
    public ResponseEntity<?> saveMovGarageBranchClose(@RequestParam Map<String,String> queryparams){
        boolean status=service.saveMovGarageBranchClose(queryparams);
        if(status){
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
        }
    }

    @PostMapping("saveRentalReciept")
    @ResponseBody
    public ResponseEntity<?> saveRentalReciept(@RequestParam Map<String,String> queryparams) throws SQLException {
        boolean status=service.saveRentalReciept(queryparams);
        if(status){
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(false,HttpStatus.BAD_GATEWAY);
        }
    }
}
