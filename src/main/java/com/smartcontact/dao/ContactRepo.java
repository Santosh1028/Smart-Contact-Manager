package com.smartcontact.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcontact.entities.Contact;
import com.smartcontact.entities.User;

public interface ContactRepo extends JpaRepository<Contact, Integer> {
	
	@Query("from contact as c where c.user.id =:userId")
	public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pageable);
	
	//Search Method
	public List<Contact> findByCnameContainingAndUser(String name,User user); 
	

}