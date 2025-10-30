package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Message;
import com.erokin.campusclubmanagement.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByRecipientOrderByCreatedAtDesc(User recipient);

    long countByRecipientAndReadIsFalse(User recipient);
}

