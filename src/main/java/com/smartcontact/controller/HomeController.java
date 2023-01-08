package com.smartcontact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.smartcontact.dao.UserRepo;
import com.smartcontact.entities.User;
import com.smartcontact.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepo userRepo;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Homepage - Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {

		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// Handler for Registering User
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@ModelAttribute("user") User user, BindingResult bindingResult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,

			 jakarta.servlet.http.HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("You haven't agreed terms and conditions");
				throw new Exception("You haven't agreed terms and conditions");
			}
			
			if(bindingResult.hasErrors()) {
				System.out.println("ERROR: "+bindingResult.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println(agreement);
			System.out.println(user);

			User result = this.userRepo.save(user);

			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered...!", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong...!" + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}
	
	
	//handler for login
	@RequestMapping("/signin")
	public String login(Model model) {
		
		model.addAttribute("title", "Login Page");
		return "login";
	}

}
