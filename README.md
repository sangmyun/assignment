# Member Site

Spring Boot + Thymeleaf + JDBC/MyBatis 기반 회원/일정 관리 프로젝트입니다.  
이 문서는 기능 나열보다, 요청이 들어왔을 때 서버가 어떤 순서로 동작하는지(전체 흐름)에 집중해 설명합니다.

## 1. 기술 스택

- Java 17
- Spring Boot 3.3.0
- Spring MVC
- Thymeleaf
- JDBC (`JdbcTemplate`)
- MyBatis
- MySQL

## 2. 레이어 구조

```text
Client (Browser / JS)
-> Controller
-> Service
-> Repository (JdbcTemplate, MyBatis Mapper)
-> MySQL
```

- `controller`: HTTP 요청/응답 처리
- `service`: 비즈니스 로직, 검증, 트랜잭션 경계
- `repository`: SQL 실행, DB 접근

## 3. 프로젝트 전체 흐름

### 3.1 첫 진입

```text
GET /
-> HomeController
-> 로그인 여부 확인
-> 로그인 상태면 /dashboard
-> 비로그인 상태면 /login
```

### 3.2 로그인

```text
POST /login
-> AuthController
-> MemberService.authenticate(loginId, password)
-> MemberRepository.findByLoginId(loginId)
-> 인증 성공 시 토큰/쿠키 발급
-> /dashboard 리다이렉트
```

### 3.3 회원가입

```text
POST /signup
-> AuthController
-> 입력값 검증
-> MemberService.register(form)
-> MemberRepository.save(member)
-> /login?registered 리다이렉트
```

### 3.4 대시보드 진입

```text
GET /dashboard
-> DashboardController
-> 로그인 사용자 확인
-> MemberService.findByLoginId(loginId)
-> dashboard/index 렌더링
```

이후 일정 데이터는 서버 렌더링 후, 프론트 JS가 API 호출로 가져옵니다.

### 3.5 일정 조회

```text
GET /api/schedules?year=YYYY&month=MM
-> ScheduleApiController.monthlySchedules
-> ScheduleService.findMonthlySchedules
-> ScheduleRepository.findByMemberIdAndPlanDateBetween...
-> JSON 응답
```

```text
GET /api/schedules/daily?date=YYYY-MM-DD
-> ScheduleApiController.dailySchedules
-> ScheduleService.findDailySchedules
-> ScheduleRepository.findByMemberIdAndPlanDateOrderByDisplayOrderAscIdAsc
-> JSON 응답
```

### 3.6 일정 생성

```text
POST /api/schedules
Body: { "date": "...", "content": "..." }
-> ScheduleApiController.create
-> ScheduleService.create
-> 입력값 검증(date/content)
-> 다음 display_order 계산
-> ScheduleRepository.save
-> 생성 결과 JSON
```

### 3.7 일정 삭제

```text
POST /api/schedules/{scheduleId}/delete
-> ScheduleApiController.delete
-> ScheduleService.delete
-> 본인 소유 일정인지 확인
-> ScheduleRepository.deleteById
-> 성공 응답
```

### 3.8 일정 순서 변경 (트랜잭션)

```text
POST /api/schedules/reorder
Body: { "date": "YYYY-MM-DD", "scheduleIds": [5, 2, 9] }
-> ScheduleApiController.reorder
-> ScheduleService.reorder (@Transactional)
-> 선택 날짜의 전체 일정 ID 검증
-> 전달 순서대로 display_order 업데이트
-> 하나라도 실패하면 전체 롤백
-> 모두 성공하면 전체 커밋
```

### 3.9 내 정보 수정

```text
POST /me/name
-> MemberController
-> 입력 검증
-> MemberService.updateName
-> MemberRepository.updateName
```

```text
POST /me/password
-> MemberController
-> 현재 비밀번호 검증
-> MemberService.updatePassword
-> MemberRepository.updatePassword
```

## 4. 인증/인가 흐름

- 로그인 체크 인터셉터에서 보호 경로 접근을 검사합니다.
- 인증이 필요한 페이지 요청은 미인증 시 `/login`으로 리다이렉트합니다.
- 인증이 필요한 API 요청은 미인증 시 `401`을 반환합니다.

## 5. 데이터 모델

### `members`

- `id` (PK)
- `login_id` (UNIQUE)
- `password`
- `name`

### `schedules`

- `id` (PK)
- `member_id` (FK -> members.id)
- `plan_date`
- `content`
- `display_order`
- `created_at`

관계:

```text
members (1) -> schedules (N)
```

## 6. 실행 정보

- 기본 포트: `8081`
- 기본 DB URL: `jdbc:mysql://localhost:3306/memberdb`
- 스키마: 앱 시작 시 `schema.sql` 실행

실행 순서:

1. MySQL 실행
2. 애플리케이션 실행 (`./mvnw spring-boot:run` 또는 IDE 실행)
3. `http://localhost:8081` 접속
