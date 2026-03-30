# Member Site

`Spring Boot + Thymeleaf + MySQL + Maven + Java 17`으로 만든 회원 관리 과제 예제입니다.

## 구현 기능

- 회원가입
- 로그인
- 로그아웃
- 내 정보 조회
- 이름 변경
- 암호 변경

회원 정보는 `로그인 아이디`, `암호`, `이름` 세 가지를 사용합니다.

## 기술 설명

### 1. Java

`Java`는 프로그램을 만드는 언어입니다.  
이 프로젝트에서는 서버 로직을 Java로 작성합니다.

예를 들어:

- 회원가입 처리
- 로그인한 사용자 확인
- 데이터베이스에 회원 저장
- 이름/암호 변경

같은 작업을 Java 코드로 구현합니다.

### 2. Spring Boot

`Spring Boot`는 Java로 웹 애플리케이션을 빠르게 만들 수 있게 도와주는 프레임워크입니다.

직접 복잡한 설정을 많이 하지 않아도:

- 웹 서버 실행
- URL 요청 처리
- 데이터베이스 연결
- 로그인 보안 기능

같은 기능을 쉽게 붙일 수 있습니다.

이번 과제에서는:

- `Controller`로 화면 요청 처리
- `Service`로 회원 비즈니스 로직 처리
- `Repository`로 DB 접근
- `Security`로 로그인/로그아웃 처리

구조를 사용합니다.

### 3. Thymeleaf

`Thymeleaf`는 서버에서 HTML을 만들어 주는 템플릿 엔진입니다.

예를 들어 `profile.html`에서:

- 회원 이름 출력
- 로그인 여부에 따라 버튼 다르게 표시
- 폼 입력값과 에러 메시지 출력

같은 일을 할 수 있습니다.

즉, `HTML + 서버 데이터`를 연결해주는 역할입니다.

### 4. MySQL

`MySQL`은 데이터를 저장하는 데이터베이스입니다.

이번 과제에서는 회원 정보를 `members` 테이블에 저장합니다.

예:

- `login_id`
- `password`
- `name`

프로그램을 껐다 켜도 회원 정보가 남아 있으려면 DB가 필요합니다.

### 5. Maven

`Maven`은 Java 프로젝트의 빌드 도구입니다.

역할:

- 필요한 라이브러리 다운로드
- 프로젝트 빌드
- 테스트 실행
- 실행 파일 생성

`pom.xml` 파일에 의존성을 적어두면 Maven이 자동으로 관리합니다.

## 프로젝트 구조

```text
member-site
├─ pom.xml
├─ src
│  ├─ main
│  │  ├─ java/com/example/membersite
│  │  │  ├─ config
│  │  │  ├─ controller
│  │  │  ├─ dto
│  │  │  ├─ entity
│  │  │  ├─ repository
│  │  │  └─ service
│  │  └─ resources
│  │     ├─ static/css
│  │     ├─ templates/auth
│  │     ├─ templates/member
│  │     └─ application.yml
│  └─ test
```

## 핵심 코드 흐름

### 회원가입

1. 사용자가 `/signup` 화면에서 아이디, 암호, 이름 입력
2. `AuthController`가 요청을 받음
3. `MemberService`가 비밀번호를 암호화해서 저장
4. `MemberRepository`가 MySQL에 회원 정보 저장

### 로그인

1. 사용자가 `/login`에서 아이디/암호 입력
2. Spring Security가 `CustomUserDetailsService`를 호출
3. DB에서 아이디를 찾고 암호를 비교
4. 성공하면 세션 로그인 상태 유지

### 회원정보 변경

1. 로그인한 사용자가 `/me`에서 변경 페이지로 이동
2. `MemberController`가 현재 로그인 사용자 정보를 찾음
3. `MemberService`가 이름 또는 암호를 변경
4. JPA가 변경 내용을 DB에 반영

## MySQL 준비

먼저 MySQL에서 데이터베이스를 하나 만들어야 합니다.

```sql
CREATE DATABASE memberdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

같은 내용은 [sql/create_database.sql](sql/create_database.sql) 파일에도 넣어두었습니다.

`src/main/resources/application.yml`의 DB 정보도 본인 환경에 맞게 수정하세요.

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/memberdb?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:Test1234!}
```

즉, 직접 파일을 수정해도 되고 실행 전에 환경변수로 넣어도 됩니다.

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="비밀번호"
```

## 실행 방법

### 1. 준비물 설치

- JDK 17
- Maven
- MySQL

### 2. 프로젝트 이동

```bash
cd member-site
```

### 3. 실행

```bash
mvn spring-boot:run
```

실행 후 브라우저에서 아래 주소로 접속합니다.

```text
http://localhost:8080
```

## 과제 제출 때 설명하면 좋은 포인트

- `Spring Boot`로 웹 프로젝트를 빠르게 구성했다.
- `Thymeleaf`로 서버 렌더링 HTML 화면을 구성했다.
- `Spring Security`로 로그인/로그아웃을 처리했다.
- `JPA`를 사용해 회원 정보를 MySQL에 저장했다.
- 회원가입, 로그인, 로그아웃, 정보 조회, 이름 변경, 암호 변경 기능을 구현했다.
