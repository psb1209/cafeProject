package com.example.cafeProject.operationBoard;

import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "operation")
@Entity
public class OperationBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String content;


    private int cnt;

    @CreationTimestamp
    private Timestamp createDate;

/*    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId")
    private Member member;*/

    @OneToMany(mappedBy = "operationBoard", fetch = FetchType.EAGER)
    @OrderBy("id desc")
    private List<OperationBoardComment> commentList;
}
