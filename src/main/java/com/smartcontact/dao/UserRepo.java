package com.smartcontact.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcontact.entities.User;

public interface UserRepo extends JpaRepository<User, Integer> {

	@Query("select u from users u where u.email =:email")
	public User getUserByUserName(@Param("email") String email);

	/*
	 * @Query("select u from User u where u.email =: email") public User
	 * findByEmail(@Param("email") String email);
	 */

}