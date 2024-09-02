package com.gateway.limoapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.limoapi.model.TaskManagementModel;
import com.gateway.limoapi.service.TaskManagementService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TaskManagementController {

	@Autowired
	TaskManagementService taskManagementService;
	
	@PostMapping("/reAssignTask")
	public boolean reAssignTask(@RequestParam Map<String,String> queryparams ) {
		return taskManagementService.assignTask(queryparams.get("refType"), queryparams.get("refNo"), queryparams.get("startDate"), queryparams.get("startTime"), queryparams.get("hidUser"), queryparams.get("desc"), queryparams.get("userId"), queryparams.get("edcDate"),queryparams.get("docno"),queryparams.get("taskstatus"));
	}
	
	@PostMapping("/addtask")
	public boolean addTask(@RequestParam Map<String,String> queryparams ) {
		return taskManagementService.addTask(queryparams.get("refType"), queryparams.get("refNo"), queryparams.get("startDate"), queryparams.get("startTime"), queryparams.get("hidUser"), queryparams.get("desc"), queryparams.get("userId"), queryparams.get("edcDate"));
	}
	
	@GetMapping("/pendingtask")
	public List<TaskManagementModel> pendingTask(@RequestParam Map<String,String> queryparams) {
		return taskManagementService.pendingTask(queryparams.get("userid"));
	}
	
	
}
