package com.bpwizard.spring.boot.commons.reactive.demo.services;

import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;

import com.bpwizard.spring.boot.commons.reactive.demo.domain.User;
import com.bpwizard.spring.boot.commons.reactive.service.SpringReactiveService;
import com.bpwizard.spring.boot.commons.security.UserDto;

@Service
public class MyService extends SpringReactiveService<User, ObjectId> {

	public static final String ADMIN_NAME = "Administrator";

	@Override
	public User newUser() {
		return new User();
	}

    @Override
    protected User createAdminUser() {
    	
    	User user = super.createAdminUser(); 
    	user.setName(ADMIN_NAME);
    	return user;
    }
    
	@Override
    protected void updateUserFields(User user, User updatedUser, UserDto currentUser) {

        super.updateUserFields(user, updatedUser, currentUser);
        user.setName(updatedUser.getName());
    }

	@Override
    public void fillAdditionalFields(String registrationId, User user, Map<String, Object> attributes) {
    	
    	String nameKey;
    	
    	switch (registrationId) {
    		
    	case "facebook":
    		nameKey = StandardClaimNames.NAME;
    		break;
    		
    	case "google":
			nameKey = StandardClaimNames.NAME;
			break;
			
		default:
			throw new UnsupportedOperationException("Fetching name from " + registrationId + " login not supprrted");
    	}
    	
    	user.setName((String) attributes.get(nameKey));
    }
	
	@Override
	protected ObjectId toId(String id) {
		return new ObjectId(id);
	}
}
