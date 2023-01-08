package com.smartcontact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartcontact.dao.ContactRepo;
import com.smartcontact.dao.UserRepo;
import com.smartcontact.entities.Contact;
import com.smartcontact.entities.User;
import com.smartcontact.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private ContactRepo contactRepo;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	// Method for common data
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String name = principal.getName();
		System.out.println(name);

		User user = userRepo.getUserByUserName(name);
		System.out.println(user);
		model.addAttribute("user", user);

	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "Home Page");
		return "normal/user_dashboard";
	}

	// Add Form Handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";

	}

	// Process Add Contact Handler
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile multipartFile, Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepo.getUserByUserName(name);

			/*
			 * if(3>2) { throw new Exception(); }
			 */

			// Processing and uploading file

			if (multipartFile.isEmpty()) {
				System.out.println("File is empty...");
				contact.setImage("contact.png");

			} else {

				contact.setImage(multipartFile.getOriginalFilename());

				File file = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());

				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");

			}

			contact.setUser(user);
			user.getContacts().add(contact);

			this.userRepo.save(user);
			System.out.println(contact);
			System.out.println("Added to contact");

			session.setAttribute("message", new Message("Your Contact is Added", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong", "danger"));
		}

		return "normal/add_contact_form";

	}

	// Show Contact handler
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal p) {

		model.addAttribute("title", "Show Contacts");
		String name = p.getName();

		Pageable pageable = PageRequest.of(page, 5);

		User user = this.userRepo.getUserByUserName(name);

		Page<Contact> contacts = this.contactRepo.findContactByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// showing details of particular contact
	@RequestMapping("/{cid}/contact")
	public String showContactDetails(@PathVariable("cid") Integer cid, Model model, Principal principal) {
		String username = principal.getName();
		User user = this.userRepo.getUserByUserName(username);
		System.out.println("CID" + cid);
		Optional<Contact> contOptional = this.contactRepo.findById(cid);
		Contact contact = contOptional.get();
		if (user.getId() == contact.getUser().getId())
			model.addAttribute("contact", contact);
		return "normal/show_contact_details";
	}

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, HttpSession session,
			Principal principal) {
		Contact contact = this.contactRepo.findById(cid).get();
		System.out.println("Contact : " + contact.getCid());
		User user = this.userRepo.getUserByUserName(principal.getName());
		// contact.setUser(null);
		user.getContacts().remove(contact);
		this.userRepo.save(user);
		this.contactRepo.delete(contact);
		System.out.println("Deleted");
		session.setAttribute("message", new Message("Contact deleted Sucessfully...", "success"));
		return "redirect:/user/show_contacts/0";
	}

	// Open Update Form Handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		model.addAttribute("title", "Update-Contact");

		Contact contact = this.contactRepo.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// process update form
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("contact.imageURL") MultipartFile file,
			Principal principal, Model model, HttpSession session) {
		try {

			User user = this.userRepo.getUserByUserName(principal.getName());
			Contact oldContactDetail = this.contactRepo.findById(contact.getCid()).get();
			if (!file.isEmpty()) {
				// Delete Old Photo Form Computer
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file2 = new File(deleteFile, oldContactDetail.getImage());
				file2.delete();
				// Update New Photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContactDetail.getImage());
			}
			contact.setUser(user);
			this.contactRepo.save(contact);
			session.setAttribute("message", new Message("Your Contact is Updated....", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Name" + contact.getCname());
		System.out.println("Id" + contact.getCid());
		return "redirect:/user/" + contact.getCid() + "/contact";
	}

	// profile handler
	@GetMapping("/profile")
	public String profileHandler(Model m) {
		m.addAttribute("title", "Profile-Page");
		return "normal/profile";
	}

	// setting handler
	@GetMapping("/settings")
	public String settings() {
		return "normal/setting";
	}

	// changing password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		String userName = principal.getName();
		User currentUser = this.userRepo.getUserByUserName(userName);
		System.out.println("cuurent user" + currentUser.getPassword());

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepo.save(currentUser);
			session.setAttribute("message", new Message("Your Password Successfully Change", "success"));
		} else {
			session.setAttribute("message", new Message("Please Enter correct Old Password !!", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";

	}

}
