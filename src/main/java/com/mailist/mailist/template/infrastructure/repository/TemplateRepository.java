package com.mailist.mailist.template.infrastructure.repository;

import com.mailist.mailist.template.domain.aggregate.Template;
import com.mailist.mailist.template.domain.valueobject.TemplateCategory;
import com.mailist.mailist.template.domain.valueobject.TemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    // Hibernate automatically filters by tenantId using @TenantId annotation

    Optional<Template> findByName(String name);

    Page<Template> findAll(Pageable pageable);

    List<Template> findByStatus(TemplateStatus status);

    Page<Template> findByStatus(TemplateStatus status, Pageable pageable);

    List<Template> findByCategory(TemplateCategory category);

    Page<Template> findByCategory(TemplateCategory category, Pageable pageable);

    boolean existsByName(String name);

    long countByStatus(TemplateStatus status);

    long countByCategory(TemplateCategory category);

    @Query("SELECT t FROM Template t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Template> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT t FROM Template t JOIN t.tags tag WHERE tag = :tag")
    List<Template> findByTag(@Param("tag") String tag);

    List<Template> findByIsDefault(Boolean isDefault);
}
