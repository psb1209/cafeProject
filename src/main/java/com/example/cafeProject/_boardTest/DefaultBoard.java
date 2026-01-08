package com.example.cafeProject._boardTest;

import com.example.cafeProject.member.RoleType;
import lombok.Getter;

@Getter
public enum DefaultBoard {
    COMMUNITY("communityBoard", "커뮤니티", "자유로운 이야기", RoleType.GUEST, RoleType.USER),
    INFORMATION("informationBoard", "정보", "유용한 정보 공유", RoleType.GUEST, RoleType.USER),
    NOTICE("noticeBoard", "공지", "카페 공지", RoleType.GUEST, RoleType.MANAGER),
    OPERATION("operationBoard", "운영", "운영진 게시판", RoleType.MANAGER, RoleType.MANAGER);

    private final String code;
    private final String name;
    private final String description;
    private final RoleType readRole;
    private final RoleType writeRole;

    DefaultBoard(String code, String name, String description, RoleType readRole, RoleType writeRole) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.readRole = readRole;
        this.writeRole = writeRole;
    }
}
