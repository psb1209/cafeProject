# GitHub 사용하기

## GitHub 회원가입 & 리포지토리 접근 확인
    - GitHub에 회원가입합니다.
    - 가입 후 본인 GitHub 닉네임을 저에게 알려주세요.
      - 제가 해당 리포지터리의 Collaborator로 추가해야 합니다.
    - 초대를 수락한 후 아래 주소로 접속해서 소스 파일이 보이는지 확인해 주세요.
      - https://github.com/Anim1048576/cafeProject


## Git 설치
    - 아래 링크에서 Windows용 Git을 설치합니다.
      - https://git-scm.com/install/windows
    - 설치 옵션은 웬만하면 기본값(Next 연타)으로 두셔도 됩니다.
    
    설치 확인 방법:
    - 명령 프롬프트(cmd)를 열고 아래 명령을 입력해보세요.
    
      git --version
    
    - 버전 정보가 나오면 정상 설치된 것입니다.


## 작업 폴더 지정
    - 프로젝트를 저장할 작업 폴더를 하나 정합니다. (예시)
    
      cd C:\example\path\workspace-boot
    
    - "C:\example\path\workspace-boot" 부분은 각자 원하는 폴더 경로로 바꿔 주세요.
    - 기존에 사용하던 프로젝트가 있다면, 백업해두는 것을 추천합니다.


## GitHub에서 프로젝트 내려받기 (clone)
    작업 폴더에서 아래 명령을 실행합니다.
    
      git clone https://github.com/Anim1048576/cafeProject.git
      cd cafeProject
    
    - 첫 줄: GitHub에 있는 cafeProject 프로젝트를 현재 폴더로 내려받습니다.
    - 둘째 줄: 내려받은 cafeProject 폴더로 이동합니다.


## Git 사용자 정보 설정
    처음 한 번만, 본인 이름/이메일을 설정해 줍니다. (이 리포지토리에서 한 번만 하면 됨)
    
      git config user.name "exampleUsername"
      git config user.email "example@gmail.com"
    
    - exampleUsername  → 본인 GitHub 닉네임 등
    - example@gmail.com → 본인 GitHub에 사용하는 이메일


## 깃허브와 작업 내용 주고받기
    작업 시작 전에 (다른 사람 변경사항 받아오기)
    
      git pull --rebase origin main
    
    - 다른 팀원이 작업한 내용을 내 컴퓨터로 내려받습니다.
    - 작업을 시작하기 전에 한 번 실행해 두면 충돌을 줄일 수 있습니다.


## 작업 후 내 변경사항 올리기
### 변경된 파일들을 추가하기
      git add .
    
    - "." : 현재 폴더 기준으로 변경된 모든 파일을 한 번에 추가합니다.
    - 나중에 익숙해지면 필요한 파일만 골라서 "git add 파일명" 으로 추가해도 됩니다.
    
### 커밋 만들기
      git commit -m "작업 내용"
    
    - "작업 내용" 부분에 이번에 한 일을 짧게 적어 주세요.
      예) "회원 가입 폼 검증 추가", "운영게시판 리스트 구현" 등
    
### GitHub로 올리기 (push)
      git push origin main
    
    - 로컬의 변경사항을 GitHub main 브랜치에 올립니다.
    - 올리고 나면 다른 사람들은 "git pull"로 이 내용을 받아갈 수 있습니다.

### 한 눈에 정리
#### 작업 시작 전 다른 사람 변경사항 받아오기
    git pull --rebase origin main

#### 작업 끝난 후 내 변경사항 올리기
    git add .
    git commit -m "작업 내용"
    git push origin main


## Git 상태 확인 (선택)
    현재 Git 상태를 확인하고 싶을 때:
    
      git status
    
    - "nothing to commit, working tree clean"
      → 변경사항 없음, 깔끔한 상태
    
    - "Your branch is up to date with 'origin/main'."
      → 내 로컬과 GitHub의 main 브랜치 내용이 일치하는 상태


## 에러 발생시 해결
### rejected 에러시 해결
    만약 git push 할 때
    ! [rejected] main -> main (fetch first)
    같은 에러가 나오면, 아래 명령을 다시 실행해주세요.

    git pull --rebase origin main
    git push origin main

    ※ git pull --rebase 실행 시
    "cannot pull with rebase: You have unstaged changes"
    라는 메시지가 나오면, 아직 커밋하지 않은 수정이 있다는 뜻이니
    먼저 git add / git commit을 해주세요.

### 충돌 발생시 해결
    1) 충돌 파일 확인
        git status

    2) 충돌난 파일을 열고
        <<<<<<<, =======, >>>>>>> 표시를 없애도록
        최종 코드를 직접 수정해주세요.

    3) 수정이 끝난 파일을 staging
        src/main/java/com/example/cafeProject/에 위치한 Something.java란 파일을 수정했다면...
            git add src/main/java/com/example/cafeProject/Something.java
        
        여러 개의 파일을 수정했다면...
            git add .

    4) 컨티뉴
        git rebase --continue





