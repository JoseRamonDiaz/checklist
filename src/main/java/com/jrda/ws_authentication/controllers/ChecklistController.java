package com.jrda.ws_authentication.controllers;

import com.jrda.ws_authentication.dao.document.Checklist;
import com.jrda.ws_authentication.dao.document.ChecklistRepository;
import com.jrda.ws_authentication.dao.sql.UsersDocuments;
import com.jrda.ws_authentication.dao.sql.UsersDocumentsRepository;
import com.jrda.ws_authentication.dao.sql.UsrDocsId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ChecklistController {
    private final ChecklistRepository checklistRepository;
    private final UsersDocumentsRepository usersDocumentsRepository;

    public ChecklistController(ChecklistRepository checklistRepository, UsersDocumentsRepository usersDocumentsRepository) {
        this.checklistRepository = checklistRepository;
        this.usersDocumentsRepository = usersDocumentsRepository;
    }

    @PostMapping(path = "create", consumes = "application/json", produces = "application/json")
    public Checklist createChecklist(@RequestBody String name) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        Checklist checklist = new Checklist();
        checklist.setName(name);

        System.out.println("Checklist " + name + " created for user " + user + "!");
        //create a checklist with given name, set permissions for that user as rw, then return the checklist id
        checklist = checklistRepository.save(checklist);
        usersDocumentsRepository.save(new UsersDocuments(getUserId(), checklist.getId(), new char[]{'r', 'w'}));
        return checklist;
    }

    @PatchMapping(path = "update", consumes = "application/json", produces = "application/json")
    public Response updateChecklist(@RequestBody Checklist checklist) {
        if (hasWritePermissions(checklist.getId())) {
            System.out.println("Updating " + checklist.getName() + " by " + SecurityContextHolder.getContext().getAuthentication().getName());
            checklistRepository.save(checklist);
            return Response.status(Response.Status.OK).entity(checklist).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("No write permissions on checklist " + checklist.getName() + "!").build();
        }
    }

    @DeleteMapping(path = "delete/{id}", produces = "application/json")
    public Response deleteChecklist(@PathVariable String id) {
        if (hasWritePermissions(id)) {
            checklistRepository.deleteById(id);
            return Response.status(Response.Status.OK).entity(id).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("No write permissions on checklist " + id + "!").build();
    }

    @GetMapping(path = "get/{id}")
    public @ResponseBody
    Response getById(@PathVariable String id) {
        if (hasReadPermissions(id)) {
            Optional<Checklist> optionalChecklist = checklistRepository.findById(id);
            if (optionalChecklist.isPresent()) {
                Checklist checklist = optionalChecklist.get();
                return Response.status(Response.Status.OK).entity(checklist).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Checklist not found").build();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("No read permissions on checklist " + id + "!").build();
    }

    @GetMapping(path = "getAll")
    public ResponseEntity<List<Checklist>> getAll() {
        List<String> ids = getChecklistIdsWithPermissions();
        List<Checklist> checklistList = (List<Checklist>) checklistRepository.findAllById(ids);
        return new ResponseEntity<>(checklistList, HttpStatus.OK);
    }

    private List<String> getChecklistIdsWithPermissions() {
        return usersDocumentsRepository.findByUserId(getUserId()).stream().map(ud -> {
            if (ud.getPermissions()[0] == 'r') {
                return ud.getDocumentId();
            }
            return "";
        }).collect(Collectors.toList());
    }

    private boolean hasWritePermissions(String checklistId) {
        Optional<UsersDocuments> usersDocumentsOptional = usersDocumentsRepository.findById(new UsrDocsId(getUserId(), checklistId));
        if (usersDocumentsOptional.isPresent()) {
            UsersDocuments usersDocuments = usersDocumentsOptional.get();
            return usersDocuments.getPermissions()[1] == 'w';
        }
        return false;
    }

    private boolean hasReadPermissions(String checklistId) {
        Optional<UsersDocuments> usersDocumentsOptional = usersDocumentsRepository.findById(new UsrDocsId(getUserId(), checklistId));
        if (usersDocumentsOptional.isPresent()) {
            UsersDocuments usersDocuments = usersDocumentsOptional.get();
            return usersDocuments.getPermissions()[0] == 'r';
        }
        return false;
    }

    private long getUserId() {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        iterator.next();
        String userId = iterator.next().getAuthority();
        return Long.parseLong(userId);
    }
}
