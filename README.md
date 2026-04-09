# ☕ cafeProject

카페 커뮤니티 서비스를 주제로 제작한 Spring Boot 기반 웹 프로젝트입니다.  
회원 관리, 게시판, 댓글, 좋아요 등의 기능을 구현하며 웹 서비스의 전반적인 흐름을 학습하고 적용하는 것을 목표로 개발했습니다.

---

## 1. 프로젝트 소개

**cafeProject**는 사용자가 카페 관련 정보를 공유하고, 게시글과 댓글을 작성하며, 커뮤니티 형태로 소통할 수 있도록 구현한 웹 애플리케이션입니다.  
Spring Boot를 기반으로 MVC 구조를 적용했으며, JPA를 사용해 데이터베이스를 연동하고, Thymeleaf를 통해 화면을 구성했습니다.

---

## 2. 개발 목적

- Spring Boot 기반 웹 애플리케이션 구조 이해
- 회원가입 / 로그인 / 게시판 / 댓글 / 좋아요 기능 구현 경험
- JPA를 활용한 데이터 처리 및 엔티티 관계 설계 학습
- Thymeleaf를 이용한 서버 사이드 렌더링 방식 학습
- Spring Security를 활용한 인증 / 인가 처리 경험

---

## 3. 기술 스택

### Backend
- Java 17
- Spring Boot 3
- Spring Data JPA
- Spring Security
- Spring Validation

### Frontend
- Thymeleaf
- HTML
- CSS
- JavaScript

### Database
- MariaDB

### Build Tool
- Gradle

### Library
- Lombok
- ModelMapper
- Jsoup
- Thymeleaf Layout Dialect
- Thymeleaf Extras Java8Time
- Thymeleaf Extras Spring Security6

---

## 4. 주요 기능

- 회원가입 / 로그인 / 로그아웃
- 사용자 인증 및 권한 처리
- 카페 메인 화면 구성
- 게시판 CRUD
- 댓글 CRUD
- 좋아요 기능
- 게시판 유형별 분리 운영
  - 공지 게시판
  - 정보 게시판
  - 운영 게시판
  - 커뮤니티 게시판

---

## 5. 프로젝트 구조

```bash
src/main/java/com/example/cafeProject
├── member
├── mainCafe
├── communityBoard
├── communityBoardComment
├── informationBoard
├── informationBoardComment
├── noticeBoard
├── noticeBoardComment
├── operationBoard
├── operationBoardComment
├── like
├── config
├── security
└── CafeProjectApplication.java
