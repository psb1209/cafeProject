## GitHub 사용하기

### 0. GitHub 회원가입 & 리포지토리 접근 확인

1. GitHub에 회원가입합니다.
2. 가입 후 **본인 GitHub 닉네임**을 저에게 알려주세요.

   * 제가 해당 리포지터리의 **Collaborator**로 추가해야 합니다.
3. 초대를 수락한 후, 아래 주소로 접속해서 소스 파일이 보이는지 확인해 주세요.
   👉 [https://github.com/Anim1048576/cafeProject](https://github.com/Anim1048576/cafeProject)

---

### 1. Git 설치

1. 아래 링크에서 Windows용 Git을 설치합니다.
   👉 [https://git-scm.com/install/windows](https://git-scm.com/install/windows)
   설치 옵션은 웬만하면 **기본값(Next 연타)** 으로 두셔도 됩니다.

2. 설치가 제대로 되었는지 확인하려면,
   **명령 프롬프트(cmd)** 를 열고 아래 명령을 입력해보세요.

   ```bat
   git --version
   ```

   버전 정보가 나오면 정상 설치된 것입니다.

---

### 2. 작업 폴더 지정

프로젝트를 저장할 **작업 폴더**를 하나 정합니다. (예시)

```bat
cd C:\example\path\workspace-boot
```

* `C:\example\path\workspace-boot` 부분은 **각자 원하는 폴더 경로**로 바꿔 주세요.
* 기존에 사용하던 프로젝트가 있다면, **백업**해두는 것을 추천합니다.

---

### 3. GitHub에서 프로젝트 내려받기 (clone)

작업 폴더에서 아래 명령을 실행합니다.

```bat
git clone https://github.com/Anim1048576/cafeProject.git
cd cafeProject
```

* 첫 줄: GitHub에 있는 `cafeProject` 프로젝트를 현재 폴더로 내려받기
* 둘째 줄: 내려받은 `cafeProject` 폴더로 이동

---

### 4. Git 사용자 정보 설정

처음 한 번만, 본인 이름/이메일을 설정해 줍니다.

```bat
git config --global user.name "exampleUsername"
git config --global user.email "example@gmail.com"
```

* `exampleUsername` → 본인 GitHub 닉네임 등
* `example@gmail.com` → 본인 GitHub에 사용하는 이메일

> `--global` : 이 컴퓨터에서 앞으로 사용할 **모든 Git 리포지토리에 공통**으로 적용됩니다.
> 만약 이 컴퓨터를 여러 사람이 같이 쓰거나, 설정을 프로젝트마다 다르게 하고 싶다면
> `--global`을 빼고 아래처럼 **리포지토리마다 한번씩** 설정해도 됩니다.

```bat
git config user.name "exampleUsername"
git config user.email "example@gmail.com"
```

(이 경우, 프롬프트 켤 때마다가 아니라, **그 리포지토리에서 한 번 설정해두면 계속 유지**됩니다.)

---

### 5. 깃허브와 작업 내용 주고받기

#### 5-1. 작업 시작 전에 (다른 사람 변경사항 받아오기)

```bat
git pull
```

* 다른 팀원이 작업한 내용을 **내 컴퓨터로 내려받는** 명령입니다.
* 작업 전마다 한 번씩 해두면 충돌을 줄일 수 있습니다.

---

#### 5-2. 작업 후 내 변경사항 올리기

1. 변경된 파일을 스테이징

```bat
git add .
```

> `.` : 현재 폴더 기준으로 변경된 모든 파일을 한 번에 추가
> (나중에 익숙해지면, 필요한 파일만 골라서 `git add 파일명` 으로 추가해도 됩니다.)

2. 커밋 만들기

```bat
git commit -m "작업 내용"
```

* `"작업 내용"` 부분에 이번에 한 일을 짧게 적어 주세요.
  예: `"회원 가입 폼 검증 추가"`, `"운영게시판 리스트 구현"` 등

3. GitHub로 올리기 (push)

```bat
git push
```

또는

```bat
git push origin main
```

* 로컬의 변경사항을 GitHub `main` 브랜치에 올립니다.
* 올리고 나면 다른 사람들은 `git pull`로 이 내용을 받아갈 수 있습니다.

---

### (선택) Git이 잘 되었는지 확인하기

현재 상태를 확인하고 싶으면 언제든지:

```bat
git status
```

* `nothing to commit, working tree clean`
  → 변경사항 없음, 깔끔한 상태
* `Your branch is up to date with 'origin/main'.`
  → 내 로컬과 GitHub의 main 브랜치 내용이 일치
