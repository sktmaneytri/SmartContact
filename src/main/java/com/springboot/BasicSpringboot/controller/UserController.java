package com.springboot.BasicSpringboot.controller;

import com.springboot.BasicSpringboot.dao.ContactRepository;
import com.springboot.BasicSpringboot.dao.UserRepository;
import com.springboot.BasicSpringboot.model.Contact;
import com.springboot.BasicSpringboot.model.User;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import com.springboot.BasicSpringboot.helper.message;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository repo;

    @Autowired
    private ContactRepository conRepo;
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String username = principal.getName();
        User user = repo.getUserByUserName(username);
        model.addAttribute("USER", user);
    }
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal)
    {
        addCommonData(model, principal);
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }
    //open add form controler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model)
    {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());

        return "normal/add_contact_form";
    }

    //processing add contact form
    @PostMapping("/process-contact")
    public String processContact(
            @ModelAttribute Contact contact,
            @RequestParam("profileImage") MultipartFile file,
            Principal principal,
            Model model) {
        try {


            String name = principal.getName();
            User user = this.repo.getUserByUserName(name);

            //processing and upload file
            if(file.isEmpty()) {

            } else {
                contact.setImage(file.getOriginalFilename());

               File savedFile =  new ClassPathResource("static/img").getFile();
             Path path =   Paths.get(savedFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded!");
            }

            contact.setUser(user);

            user.getContacts().add(contact);

            this.repo.save(user);

            System.out.println("DATA: " + contact);

            //message sucess
             message message = new message("Add contact successfully","alert-success");
             model.addAttribute("message", message);

        } catch (Exception e) {
            message message = new message("Somethings is wrong: " + e.getMessage(),"alert-danger");
            model.addAttribute("message", message);
        }
        return "normal/add_contact_form";
    }

    //show contact handler
    //perpage = 5
    //currentpage = 0 [page]

    @GetMapping("/contacts/{page}")
    public String showContact (@PathVariable("page") Integer page, Model model, Principal principal) {
        addCommonData(model, principal);
        model.addAttribute("title", "Contacts Page");

        String username = principal.getName();
        User user = repo.getUserByUserName(username);

        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = conRepo.findContactsByUserId(user.getId(), pageable);
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/show_contacts";
    }
}
