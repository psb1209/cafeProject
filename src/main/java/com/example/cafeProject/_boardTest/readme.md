# Board 모듈 (`_boardTest`) - 게시판(보드) 관리/노출/권한

> 마지막 업데이트: 2026-01-11 (KST)

이 패키지는 이름만 `_boardTest`일 뿐, **실제로는 게시판(Board) 도메인의 정식 기능**입니다.  
초기 실험/테스트 목적으로 만들었다가 정식 흐름에 편입되며 패키지명이 잔재처럼 남아 있습니다.

또한 `communityBoard`, `informationBoard` 같은 “코드 문자열”이 등장하지만,  
**`communityBoard` 패키지 / `informationBoard` 패키지 같은 별도 패키지와는 무관**합니다.  
(여기서는 단지 “기본 게시판 코드”로만 사용됩니다.)

---

## 빠른 요약 (이 패키지가 하는 일)

- `Board` 엔티티 + `BoardDTO` + `BoardRepository` + `BoardService` 구성
- **운영진용 게시판 관리 화면** 제공 (`/boardManagement/*`)
    - 특정 카페의 게시판 목록/등록/수정/상세
    - 삭제는 차단(대신 enabled 비활성화)
- **일반 사용자용 게시판 목록 화면** 제공 (`/cafe/{cafeCode}/board/list`)
    - 현재 로그인 역할(ROLE)에 따라 **볼 수 있는(enabled=true + readRole 조건)** 게시판만 노출
    - 검색 지원(일반 검색 + 초성 검색)
- **기본 게시판(DefaultBoard) 자동 생성** 지원
    - 카페 생성 시 `DefaultBoardProvisioner.ensureDefaults(cafe)`로 기본 4개 게시판 자동 생성
    - 기본 게시판은 “전체 게시판(public list)”에서는 숨김 처리(제외 코드로 필터링)
    - 운영진 목록에서는 보이되 만든이가 `Auto-Created`로 표시되도록 DTO에서 처리

---

## 라우팅(URL) 치트시트

### 1) 운영진(관리) 영역: `/boardManagement/*`
> 컨트롤러: `BoardAdminController`  
> 접근 제한: `@ManagementOnly` (ADMIN / MANAGER)

관리 페이지는 **쿼리스트링 `c`(cafeCode)가 사실상 필수**입니다.

- `GET /boardManagement/list?c={cafeCode}`
    - 해당 카페의 게시판 목록(기본 게시판 포함)
- `GET /boardManagement/create?c={cafeCode}`
    - 게시판 등록 폼
- `POST /boardManagement/createProc?c={cafeCode}`
    - 게시판 생성 처리
- `GET /boardManagement/view/{id}?c={cafeCode}`
    - 게시판 상세
- `GET /boardManagement/update/{id}?c={cafeCode}`
    - 게시판 수정 폼
- `POST /boardManagement/updateProc?c={cafeCode}`
    - 게시판 수정 처리

삭제 관련 라우팅은 존재하더라도 **의도적으로 404/차단**됩니다.
- `GET /boardManagement/delete/{id}` → 404
- `POST /boardManagement/deleteProc` → 404
- 서비스 레벨에서도 delete 자체를 METHOD_NOT_ALLOWED로 막음

---

### 2) 공개(사용자) 영역: `/cafe/{cafeCode}/board/*`
> 컨트롤러: `BoardPublicController`

- `GET /cafe/{cafeCode}/board/list?keyword=...`
    - “전체 게시판” 화면(카드 형태)
    - 로그인 상태/권한(RoleType 배열)에 따라 **readRole 조건을 만족하는 게시판만** 노출
    - **기본 게시판(DefaultBoard)** 은 여기서 제외됨(숨김)
    - 검색:
        - 일반 문자열: name like
        - 초성 문자열: nameKey like (BaseUtility 기반)
- `GET /cafe/{cafeCode}/board/{boardCode}`
    - 게시판 진입 링크
    - 실제로는 게시글 컨트롤러로 넘김:
        - `redirect:/cafe/{cafeCode}/post/list?b={boardCode}`

---

## 데이터 모델(핵심 필드)

### `Board` (Entity) - `boards` 테이블
- `name` : 게시판 이름 (생성 후 변경 불가)
- `nameKey` : 초성 검색용 키
- `description` : 게시판 설명
- `imgName` : 썸네일 이미지 파일명(선택)
- `code` : 링크용 게시판 코드 (생성 후 변경 불가)
- `enabled` : 활성화 여부 (삭제 대신 비활성화)
- `readRole` : 읽기 권한 (RoleType)
- `writeRole` : 쓰기 권한 (RoleType)
- `cafe` : 소속 카페 (ManyToOne)

> 참고: `Board`는 `BaseEntity`를 상속하므로 (프로젝트 공통 규칙에 따라)
> 작성자(member), createDate 등의 공통 필드도 함께 관리됩니다.

### 유니크 제약(중요)
- DB 레벨: `unique(cafeid, code)`
- 서비스 레벨 중복 체크: `existsByCode(code)` (전역 중복 체크) + `existsByCafe_IdAndName(cafeId, name)`

즉, “DB는 카페 단위 code 유니크”인데 “서비스는 전역 code 유니크로 막는” 구조입니다.  
의도라면 OK, 아니라면 정책을 한 쪽으로 맞추는 리팩토링 후보입니다.

---

## 서비스 로직 포인트 (`BoardService`)

### 1) “보이는 게시판” 규칙(공개 영역)
- 일반 사용자/비로그인:
    - `enabled=true` 이고
    - `readRole in (현재 사용자의 effective roles)` 인 것만 조회
- 매니저/관리자:
    - 운영/점검 목적 → 비활성(enabled=false)도 접근 가능(viewVisibleByCode가 우회 허용)

### 2) 기본 게시판(DefaultBoard) 처리
- `DefaultBoard`:
    - COMMUNITY / INFORMATION / NOTICE / OPERATION
- `DefaultBoardProvisioner.ensureDefaults(cafe)`:
    - 카페 생성 시 기본 4개 게시판을 자동 생성(이미 존재하면 스킵)
- “전체 게시판(public list)”에서는 기본 게시판 코드를 제외하고 노출:
    - Repository 쿼리에서 `b.code not in :excludedCodes` 로 필터링
- 운영진 목록에서는 기본 게시판도 보이도록 되어 있고,
    - DTO 변환 시 기본 게시판의 만든이를 `"Auto-Created"`로 강제 표시

### 3) 권한 값 검증(생성/수정 공통)
- `BANNED`는 read/write 둘 다 금지
- `읽기 권한(readRole)`은 `쓰기 권한(writeRole)`보다 높을 수 없음  
  (roleRank 비교로 검증)

### 4) 삭제 정책
- delete는 완전 차단(METHOD_NOT_ALLOWED)
- 게시판을 숨기려면 `enabled=false`로 “비활성화”하는 방식을 사용

### 5) 검색(공개 목록)
- 키워드가 초성 쿼리이면 `nameKey` 기반 검색
- 아니면 `name like` 검색
- 키워드가 없으면 기본 visible 목록 조회

---

## 관련 파일 위치

### Java
- `src/main/java/com/example/cafeProject/_boardTest/`
    - `Board.java`
    - `BoardDTO.java`
    - `BoardRepository.java`
    - `BoardService.java`
    - `BoardAdminController.java` (운영진 관리)
    - `BoardPublicController.java` (공개 목록)
    - `DefaultBoard.java` / `DefaultBoardProvisioner.java`

### Thymeleaf 템플릿
- `src/main/resources/templates/board/`
    - `publicList.html` : 전체 게시판(공개)
- `src/main/resources/templates/boardManagement/`
    - `list.html`, `create.html`, `update.html`, `view.html` : 운영진 관리 UI

---

## 운영 팁 / 주의사항

- 관리 URL은 `?c={cafeCode}`가 없으면 404/NotFound로 떨어지도록 설계됨
- public list는 “전체 게시판”이지만 기본 게시판은 의도적으로 숨김 처리됨
- 게시판 코드는 post 기능과 직접 연결되므로, 코드 변경이 불가능(updatable=false)하게 막혀 있음
    - 진입 링크: `/cafe/{cafeCode}/post/list?b={boardCode}`