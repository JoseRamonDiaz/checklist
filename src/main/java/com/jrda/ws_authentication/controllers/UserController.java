package com.jrda.ws_authentication.controllers;

import com.jrda.ws_authentication.dao.sql.AppUser;
import com.jrda.ws_authentication.dao.sql.UserRepository;
import com.jrda.ws_authentication.dao.sql.UsersDocuments;
import com.jrda.ws_authentication.dao.sql.UsersDocumentsRepository;
import com.jrda.ws_authentication.dto.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final UsersDocumentsRepository usersDocumentsRepository;

    public UserController(UserRepository userRepository, UsersDocumentsRepository usersDocumentsRepository) {
        this.userRepository = userRepository;
        this.usersDocumentsRepository = usersDocumentsRepository;
    }

    //requestparam
    @PostMapping("user/login")
    public @ResponseBody
    ResponseEntity<String> login(@RequestBody User user) {
        if (authenticateUser(user)) {
            String token = getJWTToken(user.getUser());
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Wrong user or password!", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(path = "user", consumes = "application/json")
    public @ResponseBody
    ResponseEntity<String> createUser(@RequestBody AppUser user) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPass(passwordEncoder.encode(user.getPass()));
        userRepository.save(user);
        return new ResponseEntity<>("User " + user.getName() + " successfully created!", HttpStatus.OK);
    }

    @PutMapping(path = "user/{id}", consumes = "application/json")
    public @ResponseBody
    ResponseEntity<String> replaceUser(@RequestBody AppUser newUser, @PathVariable long id) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AppUser savedUser = userRepository.findById(id).map(u -> {
            u.setName(newUser.getName());
            return userRepository.save(u);
        })
                .orElseGet(() -> {
                    newUser.setId(id);
                    newUser.setPass(passwordEncoder.encode(newUser.getPass()));
                    return userRepository.save(newUser);
                });
        return new ResponseEntity<>("User " + savedUser.getName() + " successfully created!", HttpStatus.OK);
    }

    @PostMapping(path = "user/{id}/add", consumes = "application/json")

    public void addAccess(@PathVariable Long id, @RequestBody UsersDocuments usersDocuments) {
        usersDocuments.setUserId(id);
        usersDocumentsRepository.save(usersDocuments);
    }


    private String getJWTToken(String username) {
        String secretKey = "mySecretKey";
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_USER");

        String token = Jwts
                .builder()
                .setId("softtekJWT")
                .setSubject(username)
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 9000000))
                .signWith(SignatureAlgorithm.HS512,
                        secretKey.getBytes()).compact();

        return "Bearer " + token;
    }

    private boolean authenticateUser(User user) {
        List<AppUser> byName = userRepository.findByName(user.getUser());
        AppUser appUser = byName.get(0);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return appUser != null && passwordEncoder.matches(user.getPwd(), appUser.getPass());
    }

}
