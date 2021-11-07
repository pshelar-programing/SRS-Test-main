package com.srs.controller;


import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.srs.crud.UserRepository;
import com.srs.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.srs.pojo.SrsInputBean;
import com.srs.service.SrsService;

@RestController
public class SrsController {
	@Autowired
	private UserRepository userRepository;
	private SrsService service;
	
	
	@RequestMapping("/")
	public String hello() {
		return "Hello SRS";
	}
	
	@PostMapping(value = "/srs/post", consumes = "application/json", produces = "application/json")
	public ResponseEntity<SrsInputBean> postSRSTest(@RequestBody SrsInputBean input){
		service = new SrsService();
		SrsInputBean output = service.getData(input);
		return new ResponseEntity<SrsInputBean>(output, HttpStatus.OK);
	}

	@PostMapping(path="/add")
	public @ResponseBody String addNewUser (@RequestParam String name
			, @RequestParam String email) {

		User n = new User();
		n.setName(name);
		n.setEmail(email);
		userRepository.save(n);
		return "Saved";
	}

}
