package com.bpwizard.spring.boot.commons.service.dto;

import java.util.Arrays;

import com.bpwizard.spring.boot.commons.service.domain.Role;
import com.bpwizard.spring.boot.commons.service.domain.User;

public class UserProfile {
	
	private String id;
	private String email;
	private String password;
	private String name;
	private String firstName;
	private String lastName;
	private String imageUrl;
	private String roles[];
    private String accessToken;
    private String tokenType = "Bearer";
    private String sessionId = "n/a";
    private long expireIn;
    public static UserProfile fromUserAndToken(User<Long> user) {
    	return UserProfile.fromUserAndToken(user, null, 0, null);
    }
    public static UserProfile fromUserAndToken(User<Long> user, String accessToken, long expireIn, String sessionId) {
    	UserProfile userProfile = new UserProfile();
    	userProfile.setAccessToken(accessToken);
    	userProfile.setId(user.getId().toString());
    	userProfile.setEmail(user.getEmail());
    	userProfile.setImageUrl(user.getImageUrl());
    	userProfile.setName(user.getName());
    	userProfile.setFirstName(user.getFirstName());
    	userProfile.setLastName(user.getLastName());
    	userProfile.setExpireIn(expireIn);
    	userProfile.setSessionId(sessionId);
    	userProfile.setRoles(user.getRoles().stream().toArray(String[]::new));
    	return userProfile;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public long getExpireIn() {
		return this.expireIn;
	}

	public void setExpireIn(long expireIn) {
		this.expireIn = expireIn;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	@Override
	public String toString() {
		return "UserProfile [id=" + id + ", email=" + email + ", password=" + password + ", name=" + name
				+ ", firstName=" + firstName + ", lastName=" + lastName + ", imageUrl=" + imageUrl + ", roles="
				+ Arrays.toString(roles) + ", accessToken=" + accessToken + ", tokenType=" + tokenType + ", sessionId="
				+ sessionId + ", expireIn=" + expireIn + "]";
	}
}
