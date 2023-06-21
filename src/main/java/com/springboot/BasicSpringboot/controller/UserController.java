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
import java.util.Optional;

import com.springboot.BasicSpringboot.helper.message;

import javax.swing.text.html.Option;

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
                contact.setImage("contact.png");
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

    //showing contact detail
    @GetMapping("{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        addCommonData(model, principal);

        Optional<Contact> contactOptional = conRepo.findById(cId);
        Contact contact = contactOptional.get();

        String username = principal.getName();
        User user = repo.getUserByUserName(username);
        if(user.getId()==contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title", contact.getName());
        }

        return "normal/contact_detail";
    }
    //delete contact handler
    @GetMapping("/delete/{cId}")
    public String deleteContactById(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        addCommonData(model, principal);
        Optional<Contact> contactOptional = conRepo.findById(cId);
        User user = repo.getUserByUserName(principal.getName());
        Contact contact = contactOptional.get();
        //check .. assignment
        try {

            user.getContacts().remove(contact);
            message message = new message("Contact delete successfully...", "alert-success");
            model.addAttribute("message", message);
        } catch ( Exception e) {
            message message = new message("Something were wrong: " + e.getMessage(), "alert-danger");
            model.addAttribute("message", message);
        }
        model.addAttribute("title", "Contacts Page");
        return "redirect:/user/contacts/0";
    }

    //open update form handler
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        addCommonData(model, principal);

        Optional<Contact> optionalContact = this.conRepo.findById(cId);
        Contact contact = optionalContact.get();
        model.addAttribute("title", "Update Contact");

        model.addAttribute("contact", contact);
        return "normal/update_form";
    }

    // update contact handler
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact,
                                @RequestParam("profileImage") MultipartFile file,
                                Model model,
                                Principal principal) {
        try {
            Contact oldContactDetail = conRepo.findById(contact.getcId()).get();
                if(!file.isEmpty()) {

                    //delete old photo
                    File deleteFile = new ClassPathResource("static/img").getFile();
                    File file1 = new File(deleteFile, oldContactDetail.getImage());
                    file1.delete();

                    //save
                    File savedFile =  new ClassPathResource("static/img").getFile();
                    Path path =   Paths.get(savedFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                    Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                    contact.setImage(file.getOriginalFilename());
                }
                 else {
                     contact.setImage(oldContactDetail.getImage());
                }

                User user =  this.repo.getUserByUserName(principal.getName());
                 contact.setUser(user);

                this.conRepo.save(contact);
                message message = new message("Your contact is update successfully!", "alert-success");
                model.addAttribute("message", message);


        } catch (Exception e) {
            e.printStackTrace();
            message message = new message("Something were wrong: " + e.getMessage(), "alert-danger");
            model.addAttribute("message", message);
        }
        System.out.println("CONTACT NAME: " +contact.getName());
        System.out.println("CONTACT ID: "+contact.getcId());

        return "redirect:/user/"+contact.getcId()+"/contact";
    }
}
