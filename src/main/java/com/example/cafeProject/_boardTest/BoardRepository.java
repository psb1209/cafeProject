package com.example.cafeProject._boardTest;

import com.example.cafeProject.member.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    @EntityGraph(attributePaths = "member")
    Page<Board> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "member")
    @Query("""
        select b
        from Board b
        where b.enabled = true
          and b.readRole in :roles
    """)
    Page<Board> findVisible(@Param("roles") Collection<RoleType> roles, Pageable pageable);

    boolean existsByCode(String code);
    boolean existsByName(String name);
}
