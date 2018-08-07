package com.javatican.stock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.javatican.stock.StockException;
import com.javatican.stock.dao.SiteUserDAO;
import com.javatican.stock.model.SiteUser;

@Service("siteUserService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class SiteUserService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SiteUserDAO siteUserDAO; 

	public SiteUserService() {
	}

	public SiteUser findByUsername(String username) {
		return siteUserDAO.findByUsername(username);
	}
  
}