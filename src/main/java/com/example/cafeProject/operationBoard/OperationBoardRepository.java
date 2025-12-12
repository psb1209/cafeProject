package com.example.cafeProject.operationBoard;

import com.example.cafeProject.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperationBoardRepository extends JpaRepository<OperationBoard, Integer> {

}
