package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.message.MarkMessageRequest;
import com.erokin.campusclubmanagement.dto.message.MessageResponse;
import java.util.List;

public interface MessageService {

    List<MessageResponse> listMyMessages();

    MessageResponse markMessage(Long messageId, MarkMessageRequest request);

    long countUnread();
}

