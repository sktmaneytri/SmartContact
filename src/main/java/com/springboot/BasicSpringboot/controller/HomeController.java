package com.springboot.BasicSpringboot.controller;

import com.springboot.BasicSpringboot.dao.UserRepository;
import com.springboot.BasicSpringboot.helper.message;
import com.springboot.BasicSpringboot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final UserRepository repo;

    public HomeController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping(value = {"/home", "/"})
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }
    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title","Register - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    //handler for register user
    @RequestMapping(value = "/do_register", method = RequestMethod.POST)
    public String registerUser (@ModelAttribute("user") User user, @RequestParam(value ="agreement", defaultValue = "false")
                                boolean agreement, Model model) {
        try {
            if(!agreement) {
                throw new Exception("You have not agreed the terms and conditions");
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");
           user.setPassword(passwordEncoder.encode(user.getPassword()));
            User result = this.repo.save(user);;

            model.addAttribute("user", new User());
            message message = new message("Successfully Registered !!", "alert-success");
            model.addAttribute("message", message);
            return "signup";

        }catch (Exception e) {
            message message = new message("Something went wrong!! " + e.getMessage(), "alert-danger");
            model.addAttribute("message", message);
            model.addAttribute("user",user);
            return "signup";
        }
    }


    //handler for custom login

    @GetMapping("/signin")
    public String customLogin(Model model) {
        model.addAttribute("title", "Login Page");
        return "login";
    }


}
