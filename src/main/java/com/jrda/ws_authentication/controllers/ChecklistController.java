package com.jrda.ws_authentication.controllers;

import com.jrda.ws_authentication.dao.document.Checklist;
import com.jrda.ws_authentication.dao.document.ChecklistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@RestController
public class ChecklistController {
    private final ChecklistRepository checklistRepository;

    public ChecklistController(ChecklistRepository checklistRepository) {
        this.checklistRepository = checklistRepository;
    }

    @PostMapping(path = "create", consumes = "application/json", produces = "application/json")
    public Checklist createChecklist(@RequestBody String name) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        Checklist checklist = new Checklist();
        checklist.setName(name);

        System.out.println("Checklist " + name + " created for user " + user + "!");
        //create a checklist with given name, set permissions for that user as rw, then return the checklist id
        checklist = checklistRepository.save(checklist);
        return checklist;
    }

    @PatchMapping(path = "update", consumes = "application/json", produces = "application/json")
    public Response updateChecklist(@RequestBody Checklist checklist) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.equals("ramon")) { //todo restrict for write permissions
            System.out.println("Updating " + checklist.getName() + " by " + SecurityContextHolder.getContext().getAuthentication().getName());
            checklistRepository.save(checklist);
            return Response.status(Response.Status.OK).entity(checklist).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("No write permissions on checklist " + checklist.getName() + "!").build();
        }
    }

    @DeleteMapping(path = "delete/{id}", produces = "application/json")
    public Response deleteChecklist(@PathVariable String id) {
        //validate permissions
        checklistRepository.deleteById(id);
        return Response.status(Response.Status.OK).entity(id).build();
    }

    @GetMapping(path = "get/{id}")
    public @ResponseBody ResponseEntity<Checklist> getById(@PathVariable String id) {
        //todo no permissions
        Optional<Checklist> optionalChecklist = checklistRepository.findById(id);
        if (optionalChecklist.isPresent()) {
            Checklist checklist = optionalChecklist.get();
            return new ResponseEntity<>(checklist, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new Checklist(), HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping(path = "getAll")
    public ResponseEntity<List<Checklist>> getAll() {
        //get all checklists where has permissions to read
        //todo this should be called with a findAllById
        List<Checklist> checklistList = checklistRepository.findAll();
        return new ResponseEntity<>(checklistList, HttpStatus.OK);
    }
}
