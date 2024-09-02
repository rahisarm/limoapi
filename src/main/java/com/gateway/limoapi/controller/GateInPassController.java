package com.gateway.limoapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.limoapi.model.GateInPassModel;
import com.gateway.limoapi.service.GateInPassService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class GateInPassController {

	@Autowired 
	private GateInPassService gateInPassService;
	 
	
	@PostMapping("/getDatas")
	@ResponseBody
	public List<GateInPassModel> getData(@RequestBody Map<String, Object> data) {
		List<GateInPassModel> result =  gateInPassService.getDetails(data);
		return result;
	}
	
	@PostMapping("/setDatas")
	@ResponseBody
	public Integer updateData(@RequestBody Map<String, Object> data) {
		System.out.println("Successfully hit!");
		Integer result = gateInPassService.setDetails(data); 
		return result;
	}
}
