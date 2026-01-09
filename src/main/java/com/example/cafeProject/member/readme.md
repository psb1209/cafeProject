# Member 모듈 (회원/권한/탈퇴 사유 통계)

> 마지막 업데이트: 2026-01-09 (KST)

이 문서는 `Member` 도메인에서 제공하는 기능(회원가입/로그인/내정보/비밀번호 변경/탈퇴(소프트 삭제))과  
관리자(ADMIN/MANAGER)의 기능(회원 목록/회원 상세/권한 변경/탈퇴 사유 통계)을 **프로젝트 현재 코드 기준으로** 정리한 README입니다.


---

## 목차

- [빠른 요약](#빠른-요약)
- [1) 역할(Role) 모델](#1-역할role-모델)
- [2) 접근 제어(누가 어떤 화면을 볼 수 있나)](#2-접근-제어누가-어떤-화면을-볼-수-있나)
- [3) 데이터 모델(핵심 필드)](#3-데이터-모델핵심-필드)
- [4) 주요 기능 흐름(사용자 관점)](#4-주요-기능-흐름사용자-관점)
    - [4.1 회원가입 (익명만)](#41-회원가입-익명만)
    - [4.2 로그인](#42-로그인)
    - [4.3 내 정보 보기 (로그인만)](#43-내-정보-보기-로그인만)
    - [4.4 비밀번호 변경 (로그인만)](#44-비밀번호-변경-로그인만)
    - [4.5 회원 탈퇴 (로그인만, 소프트 삭제)](#45-회원-탈퇴-로그인만-소프트-삭제)
- [5) 관리자 기능](#5-관리자-기능)
    - [5.1 회원 목록 / 상세](#51-회원-목록--상세)
    - [5.2 권한 변경](#52-권한-변경)
    - [5.3 탈퇴 사유 통계 + 탈퇴 회원 목록](#53-탈퇴-사유-통계--탈퇴-회원-목록)
- [6) MemberService 편의 메서드(개발자 참고)](#6-memberservice-편의-메서드개발자-참고)
    - [6.1 로그인/권한 체크](#61-로그인권한-체크)
    - [6.2 "현재 로그인한 Member" 가져오기](#62-현재-로그인한-member-가져오기)
    - [6.3 Member 조회/연관관계 연결](#63-member-조회연관관계-연결)
    - [6.4 Member 목록](#64-member-목록)
    - [6.5 트랜잭션](#65-트랜잭션)
- [7) Validation / 예외 처리(개발자 참고)](#7-validation--예외-처리개발자-참고)
- [8) 파일 배치(프로젝트 적용 체크리스트)](#8-파일-배치프로젝트-적용-체크리스트)

---

## 빠른 요약

- **회원(로그인 사용자)**
    - 내 정보 보기: `GET /member/view`
    - 비밀번호 변경: `GET /member/passwordUpdate` → `POST /member/passwordProc`
    - 탈퇴(소프트 삭제): `GET /member/delete` → `POST /member/deleteProc` (탈퇴 사유 선택 가능)

- **관리(ADMIN/MANAGER)**
    - 회원 목록: `GET /member/list`
    - 회원 상세: `GET /member/view/{id}`
    - 권한 변경: `GET /member/roleUpdate/{id}` → `POST /member/roleProc`
    - **탈퇴 사유 통계 + 탈퇴 회원 목록(필터/페이징)**: `GET /member/withdrawalReason`

- **익명(비로그인)**
    - 회원가입: `GET /member/create` → `POST /member/createProc`
    - 로그인 페이지: `GET /member/login` (로그인 처리 POST는 Spring Security가 담당)

---

## 1) 역할(Role) 모델

`RoleType`:

- `GUEST` : 비로그인(실제 Member 엔티티에 저장되는 역할이라기보다는 “권한 계산용”에 가깝습니다)
- `USER` : 일반 회원 (회원가입 시 기본값)
- `MANAGER` : 운영진
- `ADMIN` : 최고 관리자
- `BANNED` : 차단 계정 (일부 기능 제한)

> 권한 문자열은 Spring Security 권한(`ROLE_USER`, `ROLE_MANAGER`, `ROLE_ADMIN`, `ROLE_BANNED`)을 기준으로 판별합니다.

---

## 2) 접근 제어(누가 어떤 화면을 볼 수 있나)

| 구분 | 화면/기능 | URL | 접근 조건 |
|---|---|---|---|
| 익명만 | 회원가입 | `GET /member/create` | `isAnonymous()` |
| 익명만 | 회원가입 처리 | `POST /member/createProc` | `isAnonymous()` |
| 누구나 | 로그인 페이지 | `GET /member/login` | 공개 |
| 로그인만 | 내 정보 | `GET /member/view` | `isAuthenticated()` |
| 로그인만 | 비밀번호 변경 | `GET /member/passwordUpdate` / `POST /member/passwordProc` | `isAuthenticated()` |
| 로그인만 | 회원 탈퇴 | `GET /member/delete` / `POST /member/deleteProc` | `isAuthenticated()` |
| 관리만 | 회원 목록 | `GET /member/list` | `@ManagementOnly` |
| 관리만 | 회원 상세 | `GET /member/view/{id}` | `@ManagementOnly` |
| 관리만 | 권한 변경 | `GET /member/roleUpdate/{id}` / `POST /member/roleProc` | `@ManagementOnly` |
| 관리만 | 탈퇴 사유 통계 | `GET /member/withdrawalReason` | `@ManagementOnly` |

- `@ManagementOnly`는 “관리자(ADMIN/MANAGER)만 접근”을 의미합니다.
- **BANNED 계정 제한**
    - `POST /member/passwordProc`, `POST /member/deleteProc`에서 `ROLE_BANNED`이면 즉시 차단합니다.

---

## 3) 데이터 모델(핵심 필드)

### Member 엔티티(요약)

- 식별자/기본 정보: `id`, `username(UNIQUE)`, `password`, `email(UNIQUE)`
- 권한/등급: `role(RoleType)`, `grade(Grade)`
- 생성/삭제: `createDate`, `deleted(boolean)`, `deletedDate`, `deleteReason(ReasonType)`
- 카운트: `postCount`, `replyCount`

### ReasonType (탈퇴 사유)

- `NONE` : 선택 안 함
- `PRIVACY` : 개인정보/보안
- `CONTENT` : 콘텐츠 부족
- `SERVICE` : 서비스/사용성
- `ETC` : 기타

### 소프트 삭제(탈퇴)

탈퇴는 DB에서 DELETE 하지 않고 아래처럼 처리합니다.

- `deleted = true`
- `deletedDate = now()`
- `deleteReason = (reason == null ? NONE : reason)`

> 중요한 점: **중복 체크는 소프트 삭제 계정도 포함합니다.**  
> 즉, 탈퇴한 username/email로 재가입하려고 하면 `existsByUsername / existsByEmail`에서 중복으로 막힙니다.

---

## 4) 주요 기능 흐름(사용자 관점)

### 4.1 회원가입 (익명만)

- 화면: `GET /member/create`
- 처리: `POST /member/createProc`
- 검증 DTO: `MemberDTO` + `ValidationGroups.OnCreate`
    - username: 3~20자, 영문/숫자/`_`/`-`만 (`^[a-zA-Z0-9_-]+$`)
    - password: 8자 이상 + 영문/숫자 각각 최소 1자 (`^(?=.*[A-Za-z])(?=.*\d)[A-Za-z0-9~!@#$%^&*_\-+=]+$`)
    - email: `@Email`
- 서비스 처리 요약(`MemberService#setInsert`)
    - `dto.normalize()` (trim, email 소문자화)
    - **금지 username 차단**: 공백/`_`/`-` 제거한 “compact” 문자열로 우회 입력도 막음
    - username/email 중복 체크(소프트 삭제 포함)
    - `role=USER`, `grade=USER`로 강제 세팅
    - 비밀번호 인코딩 후 저장

---

### 4.2 로그인

- 화면: `GET /member/login`
- 처리: `POST /member/login`
    - 로그인 POST는 **컨트롤러가 아니라 Spring Security가 처리**합니다.
    - 실패 시 보통 `/member/login/error`로 리다이렉트(프로젝트 Security 설정에 따름)

---

### 4.3 내 정보 보기 (로그인만)

- `GET /member/view`
- 서버에서 현재 Authentication 기반으로 Member를 조회하여 화면에 표시합니다.
- 참고: 조회는 `deleted=false` 조건이 걸려있어서, 이미 탈퇴한 계정은 “존재하지 않는 회원”처럼 취급될 수 있습니다.

---

### 4.4 비밀번호 변경 (로그인만)

- 화면: `GET /member/passwordUpdate`
- 처리: `POST /member/passwordProc`
- 검증 DTO: `PasswordChangeDTO`
    - `currentPassword` 필수
    - `newPassword` 필수 + 8자 이상 + 패턴
    - `confirmPassword` 필수
    - 서버에서 `newPassword == confirmPassword`를 **추가로 수동 검사**
- 서비스 처리 요약(`MemberService#setUpdate`)
    - 현재 비밀번호 matches 실패 → `WrongPasswordException`
    - 새 비밀번호 인코딩 후 저장

> BANNED 계정은 비밀번호 변경이 차단됩니다.

---

### 4.5 회원 탈퇴 (로그인만, 소프트 삭제)

- 화면: `GET /member/delete`
- 처리: `POST /member/deleteProc`
- 검증 DTO: `MemberDeleteDTO`
    - `password` 필수 + 8자 이상 + 패턴
    - `confirm`은 `@AssertTrue` (동의 체크 필수)
    - `reason(ReasonType)`은 선택(없으면 `NONE`)
- 서비스 처리 요약(`MemberService#setDelete`)
    - 현재 비밀번호 matches 실패 → `WrongPasswordException`
    - `entity.softDelete(reason)` 후 저장

> BANNED 계정은 탈퇴 처리도 차단됩니다.

---

## 5) 관리자 기능

### 5.1 회원 목록 / 상세

- 목록: `GET /member/list` (페이징, `deleted=false`만)
- 상세: `GET /member/view/{id}`

### 5.2 권한 변경

- 화면: `GET /member/roleUpdate/{id}`
- 처리: `POST /member/roleProc` (`MemberDTO` + `ValidationGroups.OnUpdate`)
    - id 필수, role 필수
- **권한 변경 규칙(서비스 기준 요약)**
    - 공통
        - 같은 권한으로 변경 시도 → 실패
        - **자기 자신의 권한 변경 불가**
    - ADMIN이 할 수 없는 것
        - 다른 ADMIN의 권한 변경 불가
        - 누구를 ADMIN으로 올리는 것도 불가(최고 권한은 프로젝트 정책상 별도)
    - MANAGER 제한
        - ADMIN/MANAGER 계정의 권한은 변경 불가
        - 누구에게도 ADMIN/MANAGER 권한을 부여할 수 없음  
          → 사실상 MANAGER는 `USER`/`BANNED` 정도만 토글 가능
    - 그 외(USER 등)
        - 권한 변경 기능 자체 사용 불가

- 화면에서 선택 가능한 RoleType 옵션은 컨트롤러의 `allowedRoleOptions()`로 제한됩니다.
    - ADMIN: `BANNED / USER / MANAGER`
    - MANAGER: `BANNED / USER`

### 5.3 탈퇴 사유 통계 + 탈퇴 회원 목록

- `GET /member/withdrawalReason`
- 제공 내용
    - 사유별 카운트/비율 (`MemberService#withdrawalReasonStats`)
    - 총 탈퇴 회원 수
    - 탈퇴 회원 목록(페이징) + 사유 필터
        - 쿼리 파라미터: `reason` (없으면 전체), `page`

---

## 6) MemberService 편의 메서드(개발자 참고)

다른 컨트롤러/서비스에서 "가져다 쓰기 좋은" 메서드만 모았습니다.

### 6.1 로그인/권한 체크

- `boolean isNotLogin(Authentication auth)`
    - **로그인 안 했으면 true**
    - 보통 컨트롤러에서 가장 먼저 씁니다.

- `boolean isManagement(Authentication auth)`
    - **ADMIN/MANAGER면 true**
    - `auth`가 null이면 NPE가 날 수 있어요 → `isNotLogin()` 먼저 체크하고 호출하세요.

- `RoleType[] getEffectiveRoles(Authentication auth)`
    - “이 사용자가 접근 가능한 역할 범위”를 **RoleType 배열**로 돌려줍니다.
    - 반환 예:
        - 비로그인: `{GUEST}`
        - USER: `{GUEST, USER}`
        - MANAGER: `{GUEST, USER, MANAGER}`
        - ADMIN: `{GUEST, USER, MANAGER, ADMIN}`
        - **BANNED: 빈 배열**
    - **빈 배열**은 `IN (:roles)` 쿼리에서 문제가 될 수 있으니 호출부에서 방어하세요.

- `int roleRank(RoleType role)`
    - 역할 비교용 점수: `GUEST(0) < USER(10) < MANAGER(20) < ADMIN(30)`

**자주 쓰는 패턴**
    if (memberService.isNotLogin(authentication)) {
        return "redirect:/member/login";
    }
    if (memberService.isManagement(authentication)) {
        // 관리 기능
    }

### 6.2 "현재 로그인한 Member" 가져오기

- `Authentication getCurrentMember()`
    - `SecurityContextHolder`에서 현재 Authentication 꺼내오기

- `Member viewCurrentMember(Authentication auth)`
- `Member viewCurrentMember(UserDetails user)`
- `Member viewCurrentMember()`
    - 현재 로그인 사용자를 DB에서 `Member`로 조회해서 반환합니다.
    - 로그인 정보가 없으면 `AccessDeniedException`
    - DB에 없으면 `EntityNotFoundException`

**예시**
    Member me = memberService.viewCurrentMember(authentication);

### 6.3 Member 조회/연관관계 연결

- `Member view(int id)`
- `Member viewByUsername(String username)`
    - 없으면 `EntityNotFoundException`

- `Optional<Member> viewOptional(int id)`
- `Optional<Member> viewOptional(String username)`
    - 예외 대신 Optional로 받고 싶을 때

- `Member getReference(Integer id)`
    - 연관관계 세팅용 "가짜 엔티티"를 얻습니다.
    - 실제 필드를 건드리는 순간 SELECT/지연로딩 예외가 날 수 있으니 트랜잭션 범위에서 사용 권장

### 6.4 Member 목록

- `Page<Member> list(Pageable pageable)`
    - 회원 목록

- `Page<Member> listDeleted(Pageable pageable, ReasonType reason)`
    - 탈퇴 회원 목록
    - `reason == null`이면 전체, 아니면 사유별 필터

- `List<WithdrawalReasonStat> withdrawalReasonStats()`
    - 탈퇴 사유별 집계(요약 박스/차트 등에 사용)

**BANNED 방어 예시(권한 필터 쿼리용)**
    RoleType[] roles = memberService.getEffectiveRoles(authentication);
    if (roles.length == 0) {
        return Page.empty(pageable); // 또는 접근 거부 처리
    }
    return boardRepository.findVisibleBoards(roles, pageable);

### 6.5 트랜잭션

- `Member setInsert(MemberDTO dto)` : 회원가입
- `Member setUpdate(Authentication auth, PasswordChangeDTO dto)` : 비밀번호 변경
- `void setDelete(Authentication auth, MemberDeleteDTO dto)` : 탈퇴(소프트 삭제)
- `void updateRoleType(MemberDTO dto, Member admin)` : 권한 변경(정책 검증 포함)

---

## 7) Validation / 예외 처리(개발자 참고)

### Validation 그룹

- `OnCreate`: 회원가입에서 `username/password/email` 규칙 적용
- `OnUpdate`: 권한 변경에서 `id/role` 필수

### 대표 예외(발생 위치)

- `DuplicateValueException` : username/email 중복
- `ForbiddenUsernameException` : 금지 username
- `WrongPasswordException` : 현재 비밀번호 불일치(비밀번호 변경/탈퇴)
- `EntityNotFoundException` : 대상 회원이 없거나(또는 deleted=true) 조회 불가
- `PermissionDeniedException` : 권한 변경 규칙 위반
- `AccessDeniedException` : 인증 정보가 없거나 익명인데 로그인 전용 기능 접근

---

## 8) 파일 배치(프로젝트 적용 체크리스트)

### Java

- `src/main/java/.../member/`
    - `Member.java`
    - `MemberDTO.java`
    - `MemberDeleteDTO.java`
    - `PasswordChangeDTO.java`
    - `MemberRepository.java`
    - `MemberService.java`
    - `MemberController.java`
    - `RoleType.java`, `Grade.java`, `ReasonType.java`
    - `WithdrawalReasonStat.java`

### Thymeleaf 템플릿

- `src/main/resources/templates/member/`
    - `create.html`
    - `login.html`
    - `view.html`
    - `list.html`
    - `passwordUpdate.html`
    - `roleUpdate.html`
    - `delete.html`
    - `withdrawalReason.html`

> 템플릿은 `layout:decorate="~{main_layout/layout}"` 및 `/css/*.css`를 참조합니다.  
> 프로젝트 레이아웃/리소스 경로가 다르면 템플릿에서 경로만 조정하면 됩니다.