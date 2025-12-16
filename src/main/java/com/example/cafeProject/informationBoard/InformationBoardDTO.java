package com.example.cafeProject.informationBoard;


import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class InformationBoardDTO {


    private int id;

    private String subject;

    private String content;

    private int cnt;

    private Timestamp createDate;

    private int memberId;

}
