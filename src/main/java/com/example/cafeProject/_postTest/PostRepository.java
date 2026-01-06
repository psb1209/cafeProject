package com.example.cafeProject._postTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {


    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.id = :id
          and p.deleted = false
    """)
    Optional<Post> findDetailById(@Param("id") Integer id);

    /** boardId 기준(일반글) */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.board.id = :boardId
          and p.deleted = false
          and p.notice = false
    """)
    Page<Post> findByBoard_Id(@Param("boardId") Integer boardId, Pageable pageable);

    /** 소프트 삭제된 게시글 상세 보기 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.id = :id
          and p.deleted = true
    """)
    Optional<Post> findTrashById(@Param("id") Integer id);

    /** boardId 기준(휴지통) */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.board.id = :boardId
          and p.deleted = true
    """)
    Page<Post> findTrashByBoard_Id(@Param("boardId") Integer boardId, Pageable pageable);

    /** 게시글 검색 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.board.id = :boardId
          and p.deleted = false
          and p.notice = false
          and lower(p.title) like lower(concat('%', :keyword, '%'))
    """)
    Page<Post> searchByTitle(@Param("boardId") Integer boardId,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    /** 게시글 초성 검색 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.board.id = :boardId
          and p.deleted = false
          and p.notice = false
          and (
               p.titleKey like concat('%', :key, '%')
            or p.title like concat('%', :raw, '%')
          )
    """)
    Page<Post> searchByChosungTitle(@Param("boardId") Integer boardId,
                                    @Param("key") String key,
                                    @Param("raw") String raw,
                                    Pageable pageable);

    /** 공지글 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.board.id = :boardId
          and p.deleted = false
          and p.notice = true
    """)
    List<Post> findByNotice(@Param("boardId") Integer boardId);
}
