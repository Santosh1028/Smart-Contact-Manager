package com.smartcontact.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smartcontact.dao.ContactRepo;
import com.smartcontact.dao.UserRepo;
import com.smartcontact.entities.Contact;
import com.smartcontact.entities.User;


@RestController
public class SearchController {
	@Autowired
	private UserRepo userRepository;
	@Autowired
	private ContactRepo contactRepository;
	// Search Handler
	@GetMapping("/user/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query ,Principal principal){
		System.out.println("Query : " + query);
		User user = this.userRepository.getUserByUserName(principal.getName());
		List<Contact> contacts= this.contactRepository.findByCnameContainingAndUser(query, user);
		return ResponseEntity.ok(contacts);
	}

}