package com.example.cafeProject.communityBoardLike;


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
@Table(name="likes")
public class Like {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    private int userId;

    private int communityBoardNumber;
}
