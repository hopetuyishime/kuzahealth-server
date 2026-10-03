package rw.ac.auca.kuzahealth.controller.parent;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.parent.dto.ParentRequest;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.parent.service.ParentServiceImpl;
import rw.ac.auca.kuzahealth.utils.MessageResponse;
import rw.ac.auca.kuzahealth.utils.paging.PageRequests;
import rw.ac.auca.kuzahealth.utils.paging.PageResponse;

@RestController
@RequestMapping({ "/api/parents", "/api/v1/parents" })
@RequiredArgsConstructor
public class ParentController {

    private final ParentServiceImpl parentService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerParent(@RequestBody @Valid ParentRequest parentRequest) {
        parentService.registerParent(parentRequest);
        return new ResponseEntity<>(
                new MessageResponse("Parent registered successfully.", HttpStatus.CREATED),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<Parent>> getAllParents() {
        List<Parent> parents = parentService.getAllParents();
        return new ResponseEntity<>(parents, HttpStatus.OK);
    }

    /** Paged, filterable alternative to the full list. */
    @GetMapping("/search")
    public PageResponse<Parent> search(@RequestParam(required = false) String q,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Boolean highRisk,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return PageResponse.of(parentService.search(q, district, highRisk, PageRequests.of(page, size, sort)));
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
    public ResponseEntity<MessageResponse> updateParent(@PathVariable UUID id,
            @RequestBody @Valid ParentRequest parent) {
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
