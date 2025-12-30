package com.example.cafeProject.board_view;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="board_view")
public class Board_view {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;

    @Transient
    private int board_viewCnt;

    private Integer communityBoardNumber;

    private Integer operationBoardNumber;

    private Integer noticeBoardNumber;

    private Integer informationBoardNumber;
}
