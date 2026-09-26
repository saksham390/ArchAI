package com.archai.knowledge.repository;

import com.archai.knowledge.entity.KnowledgeDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
    List<KnowledgeDocument> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId, Pageable pageable);
    Optional<KnowledgeDocument> findByIdAndOwnerId(Long id, String ownerId);

    @Query("select document from KnowledgeDocument document where document.ownerId = :ownerId "
        + "and (lower(document.title) like lower(concat('%', :query, '%')) "
        + "or lower(document.content) like lower(concat('%', :query, '%'))) "
        + "order by document.createdAt desc")
    List<KnowledgeDocument> searchByOwnerIdAndText(
        @Param("ownerId") String ownerId,
        @Param("query") String query,
        Pageable pageable
    );
}
