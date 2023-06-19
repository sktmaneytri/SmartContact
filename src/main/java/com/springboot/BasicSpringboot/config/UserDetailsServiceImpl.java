package com.springboot.BasicSpringboot.config;

import com.springboot.BasicSpringboot.dao.UserRepository;
import com.springboot.BasicSpringboot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Configuration
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = repo.getUserByUserName(username);
        if(user == null) {
            throw new UsernameNotFoundException("Could not found user !!!");
        }
            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            return customUserDetails;

    }
}
