package com.example.cafeProject.boardTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    @EntityGraph(attributePaths = "member")
    Page<Board> findAll(Pageable pageable);
}
