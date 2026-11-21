package com.recoverx.conversation.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.recoverx.conversation.entity.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);
    Optional<Conversation> findByIdAndBusinessId(Long id, Long businessId);
    List<Conversation> findAllByBusinessIdAndInvoiceIdOrderByCreatedAtDesc(Long businessId, Long invoiceId);
    boolean existsByNexconnChannelId(String nexconnChannelId);
    java.util.Optional<Conversation> findByBusinessIdAndOwnerUserIdAndCustomerUserId(
            Long businessId, Long ownerUserId, Long customerUserId);
    List<Conversation> findAllByOwnerUserIdAndCustomerUserId(Long ownerUserId, Long customerUserId);
    List<Conversation> findAllByCustomerUserIdOrderByCreatedAtDesc(Long customerUserId);
}
