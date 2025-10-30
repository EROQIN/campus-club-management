package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Club;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByNameIgnoreCase(String name);

    Page<Club> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query(
            "select c from Club c "
                    + "where (:keywords is null or lower(c.name) like lower(concat('%', :keywords, '%')) "
                    + "or lower(c.description) like lower(concat('%', :keywords, '%'))) "
                    + "and (:category is null or lower(c.category) = lower(:category))")
    Page<Club> search(
            @Param("keywords") String keywords,
            @Param("category") String category,
            Pageable pageable);

    @Query(
            "select distinct c from Club c join c.tags t "
                    + "where lower(t.name) in :tagNames")
    Page<Club> findByTagNames(
            @Param("tagNames") Collection<String> tagNames, Pageable pageable);
}

