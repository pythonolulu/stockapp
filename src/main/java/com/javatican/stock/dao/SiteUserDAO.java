package com.javatican.stock.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javatican.stock.model.SiteUser;
import com.javatican.stock.repository.SiteUserRepository;

@Repository("siteUserDAO")
public class SiteUserDAO {
	public SiteUserDAO() {
	}

	@Autowired
	SiteUserRepository siteUserRepository;

	public SiteUser findByUsername(String username) {
		return siteUserRepository.findByUsername(username);
	}
}
