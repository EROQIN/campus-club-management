package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.message.MarkMessageRequest;
import com.erokin.campusclubmanagement.dto.message.MessageResponse;
import com.erokin.campusclubmanagement.entity.Message;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.MessageRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.MessageService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> listMyMessages() {
        User current = getCurrentUser();
        return messageRepository.findByRecipientOrderByCreatedAtDesc(current).stream()
                .map(dtoMapper::toMessageResponse)
                .toList();
    }

    @Override
    @Transactional
    public MessageResponse markMessage(Long messageId, MarkMessageRequest request) {
        Message message =
                messageRepository
                        .findById(messageId)
                        .orElseThrow(() -> new ResourceNotFoundException("消息不存在"));
        User current = getCurrentUser();
        if (!message.getRecipient().getId().equals(current.getId())) {
            throw new ResourceNotFoundException("消息不存在");
        }
        message.setRead(Boolean.TRUE.equals(request.getRead()));
        message.setReadAt(message.isRead() ? Instant.now() : null);
        return dtoMapper.toMessageResponse(messageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        User current = getCurrentUser();
        return messageRepository.countByRecipientAndReadIsFalse(current);
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }
}

