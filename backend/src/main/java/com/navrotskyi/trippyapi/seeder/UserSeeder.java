package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.Role;
import com.navrotskyi.trippyapi.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class UserSeeder {
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getSampleUsers() {
        String pass = passwordEncoder.encode("password");
        return List.of(
                new User("Admin Trippy", "admin@trippy.pl", pass, Role.ADMIN,null, null, true, false),
                new User("Jan Kowalski", "jan@trippy.pl", pass, Role.USER, null , null, true, false),
                new User("Anna Nowak", "anna@trippy.pl", pass, Role.USER, null , null, true, false),
                new User("Marek Mostowiak", "marek@trippy.pl", pass, Role.USER, null , null, true, false),
                new User("Zofia Kruk", "zofia@trippy.pl", pass, Role.USER, null , null, true, false),
                new User("Piotr Fronczewski", "piotr@trippy.pl", pass, Role.USER, null , null, true, false)
        );
    }
}