# DB 설정
    서버 포트 : 9006
    DB명 : cafeProject
    DB 사용자 : cafeProjectUser
    비밀번호 : 1234

## MariaDB에서 root로 로그인한 후 다음 작업 실행:
    create database cafeProject;
    create user 'cafeProjectUser'@'localhost' identified by '1234';
    grant all privileges on cafeProject.* to 'cafeProjectUser'@'localhost';
    flush privileges;

## application.properties 설정을 다음과 같이 세팅:
    server.port=9006

    # mariaDB
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
    spring.datasource.url=jdbc:mariadb://localhost:3306/cafeProject
    spring.datasource.username=cafeProjectUser
    spring.datasource.password=1234