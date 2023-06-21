package com.springboot.BasicSpringboot.dao;

import com.springboot.BasicSpringboot.model.Contact;
import com.springboot.BasicSpringboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
    //pagination...
    @Query("from Contact as d where d.user.id = :userId")
    public Page<Contact> findContactsByUserId(@Param("userId") int userId, Pageable pageable);


    @Query("select ")
    public List<Contact> findByNameContaining(String keywords, User user);
}
