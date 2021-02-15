package com.project2.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project2.demo.beans.Quiz;
import com.project2.demo.beans.User;
import com.project2.demo.entities.Engine;
import com.project2.demo.services.DBService;

@RestController
public class SecondaryController {

	@Autowired
	private DBService services;
  
  //@Autowired
	//private UserRepository userRepo;
	
	public SecondaryController() {
	}
	

	@Autowired
	private Engine engine;
	
	@PostMapping(path="/login", consumes= {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<String> login_page(HttpSession session, @RequestParam MultiValueMap<String,String> paramMap) {

		String username=paramMap.getFirst("username");
		String password=paramMap.getFirst("password");
		HttpHeaders headers=new HttpHeaders();
		
//		return new ResponseEntity<String>(headers,HttpStatus.FOUND);
		if (engine.login(session.getId(),username,password)) {
			headers.setLocation(URI.create("/bar"));
			return new ResponseEntity<String>(headers,HttpStatus.FOUND);
		} else {
			headers.setLocation(URI.create("/baz"));
			return new ResponseEntity<String>(headers,HttpStatus.FOUND);
		}
    }
	
	@GetMapping(value="/s/getQuizzes", produces="application/json")
	public List<Quiz> getQuizzes(@RequestParam String student) {
		List<Quiz> retval = new ArrayList<Quiz>();
		Quiz foo = new Quiz();
		foo.setId(3);
		foo.setName("Quiz A");
		foo.setUser(null);
		Quiz foo2 = new Quiz();
		foo2.setId(3);
		foo2.setName("Quiz B");
		foo2.setUser(null);
		retval.add(foo);
		retval.add(foo2);
		return retval;
	}



}
