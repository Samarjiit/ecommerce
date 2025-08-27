package com.ecommerce.project.security.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


//If you go inside over here, user detail service,
// interface which loads the user specific data okay. whenever
// user attempts to log in spring security calls the implementation of this particular class to look up
// the user in the database or wherever And get their roles authorities and even validate it so that
// it can perform authentication and authorization.So we have our own database.
// We have our own user. So what we want to do is we want to customize this and tell spring security that, hey,
// this is how you get the user information in my application.. So with this
// custom implementation we are telling spring security, okay that hey, this is how you can get the user
// information and we'll have some code written over here so that we can fetch the user information and
// provide it to spring security.

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }


}
//I'm fetching the user. I'm getting the user in my own custom model. I have a user detail implementation
// defined which is a custom implementation. Okay. And I'm returning the object of this type because this
// is expecting the user or type of user detail. Okay. So so yeah this is done. And this class is a part of
// a spring security authentication process. And its job is to fetch user details from the database using the
// username. And that is why it is implementing User Detail service, which Spring Security uses internally as well
// to authenticate users. Okay.

//when a user attempts to log in, spring security will call this class to lookup the user in the database
//and get their roles authority so that it can perform authentication and authorization.