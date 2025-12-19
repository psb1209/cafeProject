package com.example.cafeProject._postTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {

    /** 다른 연관관계에 있는 엔티티들을 지연 로딩 방식이 아닌 즉시 가져오는 findById */
    @EntityGraph(attributePaths = {"board", "member"})
    Optional<Post> findDetailById(Integer id);

    @EntityGraph(attributePaths = {"board", "member"})
    Page<Post> findByBoard_Code(String code, Pageable pageable);

    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        join p.board b
        where b.code = :code
          and lower(p.title) like lower(concat('%', :keyword, '%'))
    """)
    Page<Post> searchByTitle(@Param("code") String code, @Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        join p.board b
        where b.code = :code
          and lower(p.titleKey) like lower(concat('%', :keyword, '%'))
    """)
    Page<Post> searchByChosungTitle(@Param("code") String code, @Param("keyword") String keyword, Pageable pageable);
}
