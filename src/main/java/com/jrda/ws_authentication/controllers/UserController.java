package com.jrda.ws_authentication.controllers;

import com.jrda.ws_authentication.dao.sql.AppUser;
import com.jrda.ws_authentication.dao.sql.UserRepository;
import com.jrda.ws_authentication.dto.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    private boolean authenticateUser(User user) {
        List<AppUser> byName = userRepository.findByName(user.getUser());
        AppUser appUser = byName.get(0);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return appUser != null && passwordEncoder.matches(user.getPwd(), appUser.getPass());
    }

    @PostMapping(path = "user/create", consumes = "application/json")
    public @ResponseBody
    ResponseEntity<String> createUser(@RequestBody User user) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AppUser appUser = new AppUser(user.getUser(), passwordEncoder.encode(user.getPwd()));
        AppUser createdUser = userRepository.save(appUser);
        return new ResponseEntity<>("User " + createdUser.getName() + " successfully created!", HttpStatus.OK);
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

}
