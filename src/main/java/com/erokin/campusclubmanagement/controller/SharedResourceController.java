package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.resource.SharedResourceRequest;
import com.erokin.campusclubmanagement.dto.resource.SharedResourceResponse;
import com.erokin.campusclubmanagement.service.SharedResourceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SharedResourceController {

    private final SharedResourceService sharedResourceService;

    @GetMapping("/resources")
    public ResponseEntity<List<SharedResourceResponse>> listAll() {
        return ResponseEntity.ok(sharedResourceService.listAll());
    }

    @GetMapping("/clubs/{clubId}/resources")
    public ResponseEntity<List<SharedResourceResponse>> byClub(@PathVariable Long clubId) {
        return ResponseEntity.ok(sharedResourceService.listByClub(clubId));
    }

    @PostMapping("/clubs/{clubId}/resources")
    public ResponseEntity<SharedResourceResponse> create(
            @PathVariable Long clubId, @Valid @RequestBody SharedResourceRequest request) {
        return ResponseEntity.ok(sharedResourceService.create(clubId, request));
    }

    @PutMapping("/resources/{id}")
    public ResponseEntity<SharedResourceResponse> update(
            @PathVariable Long id, @Valid @RequestBody SharedResourceRequest request) {
        return ResponseEntity.ok(sharedResourceService.update(id, request));
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sharedResourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

