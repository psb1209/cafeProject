# Post 모듈 (`_postTest`) - 게시글 + 댓글(대댓글) 기능

> 마지막 업데이트: 2026-01-11 (KST)

이 패키지는 이름만 `_postTest`일 뿐, **실제로는 게시글(Post) 도메인의 정식 기능**입니다.  
초기에 테스트용으로 만들려다 정식 기능으로 편입되면서 패키지명이 잔재처럼 남아 있습니다.

또한, 이 README에서는 **댓글/대댓글 기능(`_commentTest`)까지 한 번에** 설명합니다.  
(댓글 UI/흐름이 `post/view.html` 내부에 붙어 있어 “게시글 상세” 기능과 결합도가 높음)

---

## 빠른 요약 (이 모듈이 하는 일)

### 게시글(Post)
- 게시글 목록/검색/상세/작성/수정
- 공지글(Notice) 기능: 운영진/매니저 또는 게시판 생성자만 공지 변경 가능
- 삭제는 **하드 삭제 금지** → **소프트 삭제(deleted=true)** 로 휴지통 기능 제공
- 제목 초성 검색 지원(`titleKey`)

### 댓글(PostComment) / 대댓글
- 게시글 상세 화면에서 댓글/대댓글 작성/수정/삭제(소프트 삭제)
- 댓글 정렬:
    - 댓글(최상위) = 최신순
    - 대댓글 = 등록순(트리 유지)
- 삭제 댓글은 목록에서 완전히 제거하지 않고 `[삭제된 댓글입니다.]`로 표시하여 **대댓글 구조 유지**

### 연동(다른 모듈 의존)
- `_boardTest`: 게시글은 특정 Board에 귀속(`b=boardCode` 필수)
- `like`: 게시글 좋아요 `/like/createProc` 연동(템플릿에서 사용)
- `board_view`: 조회수 중복 방지 기록(로그인 사용자만 기록)
- `member`: 작성자/등급(Grade) 표시 + 글/댓글 작성 시 등급 상승 체크

---

## 라우팅(URL) 치트시트

### 1) 게시글: `/cafe/{cafeCode}/post/*`
> 컨트롤러: `PostController`  
> 기본 전제: 대부분 요청에 `?b={boardCode}`가 필요(게시판 컨텍스트)

- `GET /cafe/{cafeCode}/post/list?b={boardCode}&keyword=...`
    - 게시글 목록(공지글 + 일반글)
    - 검색:
        - 일반 검색: title like
        - 초성 검색: titleKey like
- `GET /cafe/{cafeCode}/post/view/{id}?b={boardCode}`
    - 게시글 상세 + 댓글 페이징 목록
    - `b`가 없으면 자동으로 `?b={실제 boardCode}` 붙여서 redirect
- `GET /cafe/{cafeCode}/post/create?b={boardCode}`
- `POST /cafe/{cafeCode}/post/createProc?b={boardCode}`
- `GET /cafe/{cafeCode}/post/update/{id}?b={boardCode}`
    - `b`가 없으면 자동으로 `?b=...` 붙여서 redirect
- `POST /cafe/{cafeCode}/post/updateProc?b={boardCode}`
- `POST /cafe/{cafeCode}/post/deleteProc/{id}?b={boardCode}`
    - **소프트 삭제**로 동작

#### 공지 토글(운영진)
- `POST /cafe/{cafeCode}/post/toggleNotice/{id}`
    - 접근: `@ManagementOnly` (ADMIN / MANAGER)

#### 휴지통(삭제된 글)
- `GET /cafe/{cafeCode}/post/trashList?b={boardCode}` (운영진)
    - 접근: `@ManagementOnly`
- `GET /cafe/{cafeCode}/post/trash/{id}?b={boardCode}` (관리자)
    - 접근: `@AdminOnly`

---

### 2) 댓글/대댓글: `/cafe/{cafeCode}/postComment/*`
> 컨트롤러: `PostCommentController` (`_commentTest`)

- `POST /cafe/{cafeCode}/postComment/createProc?b={boardCode}`
    - 댓글 등록(최상위 댓글)
- `POST /cafe/{cafeCode}/postComment/update?b={boardCode}`
    - “수정 모드 진입”용 핸들러
    - flash attribute로 `postCommentUpdate`를 실어 보내고, `post/view.html`에서 수정 UI로 전환
- `POST /cafe/{cafeCode}/postComment/updateProc?b={boardCode}`
    - 댓글 수정 처리
- `POST /cafe/{cafeCode}/postComment/deleteProc?b={boardCode}`
    - 댓글 소프트 삭제 처리
- `POST /cafe/{cafeCode}/postComment/replyProc?b={boardCode}`
    - 대댓글 등록

> `b`가 비어 들어오면, 컨트롤러에서 `postId`로 게시글을 조회해 `boardCode`를 보정합니다.

---

## 핵심 동작 포인트

### 1) 전역 모델 주입(@ModelAttribute)
`PostController`는 모든 요청 전에 아래 값을 모델에 자동 주입합니다.

- `imageUrlPrefix`: 업로드 이미지 URL prefix(`/` 보정)
- `board`: 요청 파라미터 `b`가 있으면 해당 Board DTO 조회
- `cafe`: `{cafeCode}`로 Cafe DTO 조회

템플릿(`post/*.html`)은 위 모델을 전제로 작성되어 있습니다.

---

### 2) 변조 방지(바인딩 제한 + 서버에서 강제 세팅)
`PostDTO`에 대해 아래 필드는 바인딩에서 무시(disallowed)됩니다.

- `memberId`, `username`, `createDate`, `deleted`, `deletedAt`, `titleKey`, `boardId`, `boardCode`

또한 create/update 처리 시 컨트롤러에서 아래를 **항상 재세팅**합니다.
- `dto.boardId`, `dto.boardCode` ← 현재 `board` 모델에서 강제로 주입

→ 즉, 클라이언트가 hidden 값을 변조해도 서버에서 덮어써서 방어합니다.

---

### 3) 권한 정책 요약

#### 게시글 작성
- 보드가 `enabled=true`여야 함
- 현재 로그인 사용자의 effective role 배열에 `board.writeRole`이 포함돼야 함

#### 게시글 수정/삭제
- 기본: 작성자 본인
- 예외: `MANAGER` 이상이면 가능

#### 공지글(Notice) 변경
- `MANAGER` 이상이면 가능
- 또는 “해당 게시판 생성자(운영자)”면 가능

#### 댓글/대댓글 수정/삭제
- 기본: 작성자 본인
- 예외: `ADMIN` / `MANAGER`이면 가능
- `BANNED`(effective roles empty)면 차단

---

### 4) 삭제 정책(소프트 삭제)

#### 게시글(Post)
- 하드 삭제 없음
- `deleted=true`, `deletedAt=now`
- 휴지통 목록/상세로 접근 가능(권한 제한 있음)

#### 댓글(PostComment)
- 하드 삭제 없음(소프트 삭제)
- `deleted=true`면 화면에서 `[삭제된 댓글입니다.]`로 표시
- 삭제 댓글도 목록에는 남겨서 “대댓글 구조”를 유지합니다.

---

## 정렬/검색/페이징

### 게시글 목록
- 공지글(notice=true)은 별도 리스트로 상단 노출
- 일반글은 `id desc`(기본 pageable)
- 검색:
    - 일반 검색: `title like %keyword%`
    - 초성 검색: `titleKey like %chosungKey%` 또는 `title like %raw%`

### 댓글 목록(페이징)
- Repository: `findByPostIdOrderByRefDescLevelAsc(postId, pageable)`
    - 댓글(최상위): `ref desc` → 최신 댓글 묶음이 먼저
    - 대댓글: `level asc` → 등록 순서 유지
- 화면 들여쓰기: `step * 30px` (`step`은 reply 시 부모+1)

> 참고: 템플릿(`post/view.html`)에 `commentCount` 출력이 있으나, 현재 `PostController.view()`에서 `commentCount`를 모델에 넣지 않습니다.  
> 필요하면 `postCommentService.countActiveComments(postId)`를 추가로 model에 주입하는 방식으로 맞추면 됩니다.

---

## 이미지 업로드 (Summernote)
> 컨트롤러: `PostImageController`

- `POST /post/uploadImage` (`multipart/form-data`, field: `file`)
- 응답(JSON): `{ "url": "...", "fileName": "..." }`
- 템플릿(create/update)에서 Summernote `onImageUpload` 콜백으로 호출합니다.

---

## 관련 파일 위치

### Java
- `src/main/java/com/example/cafeProject/_postTest/`
    - `Post.java`, `PostDTO.java`
    - `PostRepository.java`
    - `PostService.java`
    - `PostController.java`
    - `PostImageController.java`

- `src/main/java/com/example/cafeProject/_commentTest/`
    - `PostComment.java`, `PostCommentDTO.java`
    - `PostCommentRepository.java`
    - `PostCommentService.java`
    - `PostCommentController.java`

### Thymeleaf
- `src/main/resources/templates/post/`
    - `list.html`, `view.html`, `create.html`, `update.html`
    - 댓글 UI는 `view.html` 내부에 포함

---

## 운영/개선 메모(선택)

- 댓글/대댓글 목록은 엔티티 연관(member/post)이 LAZY라서, 상황에 따라 N+1이 생길 수 있습니다.
    - 필요하면 댓글 조회에 EntityGraph/FetchJoin 도입 고려
- 대댓글 등록(`replyProc`)은 현재 등급(ReplyCount) 증가 로직이 호출되지 않습니다.
    - “대댓글도 댓글로 카운트할지” 정책에 따라 `updateGrade()` 호출을 추가할지 결정하면 됩니다.
