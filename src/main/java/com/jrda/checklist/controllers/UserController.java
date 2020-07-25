package com.jrda.checklist.controllers;

import com.jrda.checklist.dao.sql.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class UserController {
    public final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private final UserRepository userRepository;
    private final UsersDocumentsRepository usersDocumentsRepository;

    public UserController(UserRepository userRepository, UsersDocumentsRepository usersDocumentsRepository) {
        this.userRepository = userRepository;
        this.usersDocumentsRepository = usersDocumentsRepository;
    }

    //RequestParam
    @PostMapping("api/login")
    public @ResponseBody
    ResponseEntity<String> login(@RequestBody AppUser user) {
        long id = authenticateUser(user);
        if (id != -1) {
            String token = getJWTToken(user.getName(), id);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Wrong user or password!", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(path = "api/users", consumes = "application/json")
    public @ResponseBody
    ResponseEntity<String> createUser(@RequestBody AppUser user) {
        try {
            validateEmailFormat(user.getEmail());
            validateEmailIsNotUsed(user.getEmail());
            validateNameIsNotUsed(user.getName());
        } catch (AppUserException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPass(passwordEncoder.encode(user.getPass()));
        userRepository.save(user);
        return new ResponseEntity<>("User " + user.getName() + " successfully created!", HttpStatus.OK);
    }

    @PutMapping(path = "api/users", consumes = "application/json")
    public @ResponseBody
    ResponseEntity<String> replaceUser(@RequestBody AppUser newUser) {
        try {
            validateUserId(newUser.getId());
            validateEmailFormat(newUser.getEmail());
            validateEmailIsNotUsed(newUser.getEmail());
            validateNameIsNotUsed(newUser.getName());
        } catch (AppUserException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AppUser savedUser = userRepository.findById(newUser.getId()).map(u -> {
            u.setName(newUser.getName());
            u.setEmail(newUser.getEmail());
            return userRepository.save(u);
        }).orElseGet(() -> {
            newUser.setId(newUser.getId());
            newUser.setPass(passwordEncoder.encode(newUser.getPass()));
            return userRepository.save(newUser);
        });
        return new ResponseEntity<>("User " + savedUser.getName() + " successfully updated!", HttpStatus.OK);
    }

    @PostMapping(path = "api/users/{id}/checklists", consumes = "application/json")
    public void addAccess(@PathVariable Long id, @RequestBody UsersDocuments usersDocuments) {
        usersDocuments.setUserId(id);
        usersDocumentsRepository.save(usersDocuments);
    }

    @PutMapping(path = "api/users/{uid}/{did}")
    public void replacePermissions(@PathVariable long uid, @PathVariable String did, @RequestBody UsersDocuments newUsersDocuments) {
        usersDocumentsRepository.findById(new UsrDocsId(uid, did)).map(ud -> {
            ud.setPermissions(newUsersDocuments.getPermissions());
            return usersDocumentsRepository.save(ud);
        }).orElseGet(() -> {
            newUsersDocuments.setUserId(uid);
            newUsersDocuments.setDocumentId(did);
            return usersDocumentsRepository.save(newUsersDocuments);
        });
    }

    private String getJWTToken(String username, long id) {
        String secretKey = "mySecretKey";
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("USER," + id);

        String token = Jwts
                .builder()
                .setId(id + "")
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

    private long authenticateUser(AppUser user) {
        List<AppUser> byName = userRepository.findByName(user.getName());

        if (byName.isEmpty()) {
            return -1;
        }

        AppUser appUser = byName.get(0);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (appUser != null && passwordEncoder.matches(user.getPass(), appUser.getPass())) {
            return appUser.getId();
        } else {
            return -1;
        }
    }

    private void validateNameIsNotUsed(String name) throws AppUserException {
        if (!userRepository.findByName(name).isEmpty()) {
            throw new AppUserException("Name is already taken");
        }
    }

    private void validateEmailIsNotUsed(String email) throws AppUserException {
        if (!userRepository.findByEmail(email).isEmpty()) {
            throw new AppUserException("Email is already taken");
        }
    }

    private void validateEmailFormat(String email) throws AppUserException {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        if (!matcher.find()) {
            throw new AppUserException("Invalid email");
        }
    }

    private void validateUserId(long id) throws AppUserException {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().noneMatch(a -> a.getAuthority().equals(id + ""))) {
            throw new AppUserException("No permissions to modify this user");
        }
    }

}
