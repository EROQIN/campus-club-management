package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.message.MarkMessageRequest;
import com.erokin.campusclubmanagement.dto.message.MessageResponse;
import com.erokin.campusclubmanagement.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<MessageResponse>> list() {
        return ResponseEntity.ok(messageService.listMyMessages());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markRead(
            @PathVariable Long id, @Valid @RequestBody MarkMessageRequest request) {
        return ResponseEntity.ok(messageService.markMessage(id, request));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount() {
        return ResponseEntity.ok(messageService.countUnread());
    }
}

