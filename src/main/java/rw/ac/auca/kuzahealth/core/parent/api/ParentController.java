package rw.ac.auca.kuzahealth.core.parent.api;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.parent.service.ParentServiceImpl;
import rw.ac.auca.kuzahealth.utils.MessageResponse;

@RestController
@RequestMapping("/api/parents")
public class ParentController {

    private final ParentServiceImpl parentService;

    @Autowired
    public ParentController(ParentServiceImpl parentService) {
        this.parentService = parentService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> registerParent(@RequestBody Parent parent) {
        Parent createdParent = parentService.registerParent(parent);
        MessageResponse response = new MessageResponse("Parent registered successfully.", HttpStatus.CREATED);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Parent>> getAllParents() {
        List<Parent> parents = parentService.getAllParents();
        return new ResponseEntity<>(parents, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Parent> getParentById(@PathVariable UUID id) {
        Parent parent = parentService.getParentById(id);
        if (parent != null) {
            return new ResponseEntity<>(parent, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateParent(@PathVariable UUID id, @RequestBody Parent parent) {
        Parent updatedParent = parentService.updateParent(id, parent);
        if (updatedParent != null) {
            MessageResponse response = new MessageResponse("Parent updated successfully.", HttpStatus.OK);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new MessageResponse("Parent not found.", HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteParent(@PathVariable UUID id) {
        boolean isDeleted = parentService.deleteParent(id);
        if (isDeleted) {
            MessageResponse response = new MessageResponse("Parent deleted successfully.", HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(new MessageResponse("Parent not found.", HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
    }
}
