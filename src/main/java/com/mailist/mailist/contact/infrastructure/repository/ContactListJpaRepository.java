package com.mailist.mailist.contact.infrastructure.repository;

import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.domain.model.ListStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ContactListJpaRepository extends JpaRepository<ContactList, Long>, ContactListRepository {

    @Override
    Optional<ContactList> findByName(String name);

    @Override
    boolean existsByName(String name);

    @Override
    List<ContactList> findByIsActive(boolean isActive);

    @Override
    List<ContactList> findByIsDynamic(boolean isDynamic);

    @Query("SELECT cl FROM ContactList cl WHERE cl.id in (:ids)")
    List<ContactList> findAllByIds(Set<Long> ids);

    @Override
    long countByIsActive(boolean isActive);

    @Query("SELECT cl FROM ContactList cl WHERE cl.isDynamic = false")
    List<ContactList> findStaticLists();

    @Query("SELECT cl FROM ContactList cl WHERE cl.isDynamic = true")
    List<ContactList> findDynamicLists();

    @Query("SELECT COUNT(c) FROM ContactList cl JOIN cl.contacts c WHERE cl.id = :listId")
    long countContactsInList(@Param("listId") Long listId);

    @Override
    @Query(value = """
        SELECT
            COUNT(*) as totalLists,
            SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as activeLists,
            COALESCE(SUM(
                (SELECT COUNT(*) FROM contact_list_contacts clc WHERE clc.list_id = cl.id)
            ), 0) as totalSubscribers
        FROM contact_lists cl
        """, nativeQuery = true)
    ListStatistics getGlobalStatistics();
}