package com.javatican.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javatican.stock.model.SiteUser;

public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {
	
	SiteUser findByUsername(String username); 
 
}
