package com.javatican.stock.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "site_user")
public class SiteUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", unique=true, nullable = false)
	private String username;

	@Column(name = "password", nullable = false)
	private String password;

	@JsonIgnore
	@OneToMany(mappedBy = "siteUser")
	private Collection<PortfolioItem> pi;

	public SiteUser() {
	}

	public SiteUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public Collection<PortfolioItem> getPi() {
		return pi;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getId() {
		return id;
	}
}