package com.gateway.limoapi.controller;

import com.gateway.limoapi.model.DropdownModel;
import com.gateway.limoapi.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ClientController {

    @Autowired
    private ClientService service;

    @GetMapping("/getClient")
    @ResponseBody
    public List<DropdownModel> getClient(@RequestParam Map<String,String> queryparams){
        return service.getClient(queryparams.get("value"));
    }

    @PostMapping("/saveClient")
    @ResponseBody
    public ResponseEntity<String> saveClient(@RequestBody Map<String, String> data){
        int cldocno=service.saveClient(data);
        HttpHeaders headers=new HttpHeaders();
        if(cldocno>0){
            return new ResponseEntity<>(cldocno+"",headers, HttpStatus.OK);
        }
        else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Client Not Saved");
        }

    }
}
