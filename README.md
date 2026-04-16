# Member Site

Spring Boot MVC, JDBC, Thymeleaf로 만든 회원/일정 관리 프로젝트입니다.

이 README는 기능 목록보다 `사용자가 무엇을 했을 때 서버가 어떤 순서로 동작하는지`를 중심으로 정리합니다.

## 1. 프로젝트 한 줄 설명

이 프로젝트는 회원가입, 로그인, 세션 관리, 내 정보 수정, 일정 조회/등록/삭제 흐름을 직접 구현한 웹 애플리케이션입니다.

## 2. 기술 스택

- Java 17
- Spring Boot 3.3.0
- Spring MVC
- Thymeleaf
- MySQL
- JDBC
- JavaScript

## 3. 구조

```text
controller
-> 요청을 먼저 받음
-> 화면 이동 또는 API 응답 처리

service
-> 비즈니스 로직 처리

repository
-> SQL 실행, DB 접근

support
-> JDBC 연결, 세션 관리
```

## 4. 가장 먼저 보는 흐름

사용자가 사이트에 처음 들어오면 `/`로 접근합니다.

```text
GET /
-> HomeController.home()
-> SessionManager.getLoginId(request)
-> 로그인 상태면 /dashboard 로 리다이렉트
-> 비로그인 상태면 /login 으로 리다이렉트
```

즉 이 프로젝트는 `/`에서 별도 홈 화면을 보여주지 않고, 로그인 여부만 확인한 뒤 바로 이동시킵니다.

## 5. 로그인 흐름

이 프로젝트에서 가장 설명하기 좋은 흐름은 로그인입니다.

### 5-1. 사용자가 아이디, 비밀번호를 입력했을 때

사용자가 로그인 화면에서 아이디와 비밀번호를 입력하고 로그인 버튼을 누르면 아래 순서로 처리됩니다.

```text
브라우저에서 로그인 폼 제출
-> POST /login 요청 생성
-> loginId, password가 요청 파라미터로 전달됨
-> AuthController.login(loginId, password, response)
-> MemberService.authenticate(loginId, password)
-> MemberRepository.findByLoginId(loginId)
-> DB에서 loginId로 회원 조회
-> 회원이 없으면 false 반환
-> 회원이 있으면 입력한 비밀번호와 DB 비밀번호 비교
-> 비밀번호가 다르면 false 반환
-> 비밀번호가 같으면 true 반환
-> AuthController가 SessionManager.createSession(loginId, response) 호출
-> UUID 기반 sessionId 생성
-> sessionStore(Map)에 sessionId -> loginId 저장
-> memberSessionId 쿠키를 응답에 추가
-> 브라우저가 쿠키 저장
-> /dashboard 로 리다이렉트
```

### 5-2. 로그인 실패했을 때

```text
POST /login
-> MemberService.authenticate() 결과가 false
-> redirect:/login?error
-> login.html 에서 param.error 확인
-> "아이디 또는 비밀번호를 다시 확인해 주세요." 출력
```

즉 `/login?error`는 별도 페이지가 아니라, 로그인 실패 상태를 URL 파라미터로 표현한 것입니다.

### 5-3. 이미 로그인한 사용자가 /login 으로 들어왔을 때

```text
GET /login
-> AuthController.login(request)
-> SessionManager.getLoginId(request)
-> 이미 로그인 상태면 /dashboard 로 리다이렉트
-> 아니면 auth/login 화면 반환
```

즉 로그인한 사용자가 다시 로그인 화면에 들어가지 못하게 막아둔 구조입니다.

## 6. 회원가입 흐름

회원가입도 비슷한 방식으로 처리됩니다.

### 6-1. 회원가입 화면 진입

```text
GET /signup
-> AuthController.signupForm()
-> SessionManager.getLoginId(request)
-> 로그인 상태면 /dashboard 로 이동
-> 비로그인 상태면 signupForm 생성
-> auth/signup 화면 반환
```

### 6-2. 회원가입 제출

```text
POST /signup
-> AuthController.signup(signupForm)
-> loginId 값 검증
-> password 값 검증
-> passwordConfirm 값 검증
-> name 값 검증
-> MemberService.isDuplicatedLoginId(loginId)
-> MemberRepository.existsByLoginId(loginId)
-> 중복 아이디면 에러 메시지와 함께 회원가입 화면 유지
-> 통과하면 MemberService.register(signupForm)
-> new Member(loginId, password, name)
-> MemberRepository.save(member)
-> members 테이블 저장
-> /login?registered 로 리다이렉트
```

즉 회원가입은 컨트롤러에서 입력값을 검증하고, 서비스와 repository를 거쳐 DB 저장까지 이어집니다.

## 7. 세션 처리 흐름

이 프로젝트는 `HttpSession` 대신 직접 만든 `SessionManager`를 사용합니다.

### 7-1. 세션 생성

```text
로그인 성공
-> SessionManager.createSession(loginId, response)
-> UUID sessionId 생성
-> sessionStore(Map)에 sessionId -> loginId 저장
-> memberSessionId 쿠키 생성
-> 응답에 쿠키 추가
```

### 7-2. 이후 요청 처리

```text
브라우저가 쿠키와 함께 요청 전송
-> SessionManager.getLoginId(request)
-> memberSessionId 쿠키 찾기
-> sessionStore 에서 loginId 조회
-> 로그인 사용자 판별
```

### 7-3. 로그아웃

```text
POST /logout
-> AuthController.logout()
-> SessionManager.expire(request, response)
-> sessionStore 에서 세션 제거
-> 만료 쿠키 생성
-> 브라우저 쿠키 제거
-> /login?logout 로 리다이렉트
```

## 8. 대시보드 진입 흐름

로그인에 성공하면 `/dashboard`로 이동합니다.

```text
GET /dashboard
-> DashboardController.dashboard()
-> SessionManager.getLoginId(request)
-> 로그인 여부 확인
-> 로그인 안 되어 있으면 /login 으로 이동
-> MemberService.findByLoginId(loginId)
-> MemberRepository.findByLoginId(loginId)
-> 회원 정보 조회
-> model.addAttribute("member", member)
-> dashboard/index 반환
```

여기서 `dashboard/index.html`은 서버가 Thymeleaf로 먼저 렌더링하고, 일정 데이터는 이후 JavaScript가 API로 가져옵니다.

## 9. 일정 조회 흐름

일정 기능은 `dashboard-planner.js`와 `ScheduleApiController`가 같이 동작합니다.

### 9-1. 월간 일정 조회

```text
dashboard/index 로드
-> dashboard-planner.js 실행
-> fetchMonthlySchedules()
-> GET /api/schedules?year=2026&month=4
-> ScheduleApiController.monthlySchedules()
-> SessionManager.getLoginId(request)
-> 로그인 확인
-> ScheduleService.findMonthlySchedules(loginId, year, month)
-> MemberService.findByLoginId(loginId)
-> ScheduleRepository.findByMemberIdAndPlanDateBetweenOrderByPlanDateAscIdAsc()
-> JSON 응답
-> JavaScript가 달력에 일정 개수 반영
```

### 9-2. 일간 일정 조회

```text
사용자가 날짜 클릭
-> selectedDate 변경
-> loadDailySchedules()
-> GET /api/schedules/daily?date=2026-04-15
-> ScheduleApiController.dailySchedules()
-> 로그인 확인
-> ScheduleService.findDailySchedules(loginId, date)
-> ScheduleRepository.findByMemberIdAndPlanDateOrderByIdAsc()
-> JSON 응답
-> JavaScript가 오른쪽 일정 목록 렌더링
```

## 10. 일정 등록 흐름

사용자가 대시보드에서 일정 내용을 입력하고 저장 버튼을 누르면:

```text
saveSchedule()
-> scheduleInput.value.trim()
-> 내용이 비어 있으면
-> scheduleStatus.textContent = "일정 내용을 입력하세요."
-> 함수 종료

내용이 있으면
-> POST /api/schedules
-> JSON body에 date, content 전달
-> ScheduleApiController.create()
-> SessionManager.getLoginId(request)
-> 로그인 확인
-> ScheduleService.create(loginId, request)
-> validate(request)
-> 날짜 null 여부 확인
-> 내용 null/공백 여부 확인
-> 내용 길이 100자 이하 확인
-> MemberService.findByLoginId(loginId)
-> new Schedule(memberId, date, trimmedContent)
-> ScheduleRepository.save(schedule)
-> 저장된 일정 JSON 응답
-> 입력창 비우기
-> "일정이 저장되었습니다." 표시
-> refreshAll()
```

즉 프론트엔드에서도 한 번 검증하고, 서버에서도 다시 검증합니다.

## 11. 일정 삭제 흐름

```text
사용자가 삭제 버튼 클릭
-> deleteSchedule(scheduleId)
-> POST /api/schedules/{scheduleId}/delete
-> ScheduleApiController.delete()
-> SessionManager.getLoginId(request)
-> 로그인 확인
-> ScheduleService.delete(loginId, scheduleId)
-> MemberService.findByLoginId(loginId)
-> ScheduleRepository.findByIdAndMemberId(scheduleId, memberId)
-> 본인 일정인지 확인
-> ScheduleRepository.deleteById(scheduleId)
-> 성공 응답
-> "일정이 삭제되었습니다." 표시
-> refreshAll()
```

즉 삭제도 단순히 ID만 보고 지우는 것이 아니라, 로그인한 사용자의 일정인지 먼저 확인합니다.

## 12. 내 정보 조회 / 수정 흐름

### 12-1. 내 정보 조회

```text
GET /me
-> MemberController.profile()
-> SessionManager.getLoginId(request)
-> 로그인 확인
-> MemberService.findByLoginId(loginId)
-> model.addAttribute("member", member)
-> member/account 반환
```

### 12-2. 이름 수정

```text
POST /me/name
-> MemberController.editName()
-> 로그인 확인
-> 이름 null / 공백 검증
-> 이름 길이 검증
-> MemberService.updateName(loginId, name)
-> MemberRepository.updateName(memberId, name)
-> /me 로 리다이렉트
```

### 12-3. 비밀번호 수정

```text
POST /me/password
-> MemberController.editPassword()
-> 로그인 확인
-> 현재 비밀번호 입력 여부 확인
-> 새 비밀번호 입력 여부 확인
-> 새 비밀번호 길이 확인
-> 새 비밀번호 확인 일치 여부 확인
-> MemberService.matchesPassword(loginId, currentPassword)
-> 현재 비밀번호 실제 일치 여부 확인
-> MemberService.updatePassword(loginId, newPassword)
-> MemberRepository.updatePassword(memberId, newPassword)
-> /me 로 리다이렉트
```

## 13. 인증 실패 처리

대시보드와 일정 API에서는 로그인 상태가 아니면 바로 로그인 화면으로 보내거나 `401`을 반환합니다.

```text
페이지 요청
-> 로그인 안 되어 있으면 redirect:/login

API 요청
-> 로그인 안 되어 있으면 401 Unauthorized
-> 프론트엔드가 이를 받으면 /login 으로 이동
```

즉 화면 요청과 API 요청의 처리가 조금 다릅니다.

## 14. 보안상 현재 상태

- 비밀번호는 현재 평문 비교입니다.
- 세션 저장소는 서버 메모리 기반입니다.
- 일정 내용은 `escapeHtml()`을 거쳐 출력합니다.
- 즉 사용자가 입력한 문자열을 그대로 HTML로 해석하지 않게 막고 있습니다.

## 15. DB 구조

### members

- `id`
- `login_id`
- `password`
- `name`

### schedules

- `id`
- `member_id`
- `plan_date`
- `content`
- `created_at`

관계는 아래와 같습니다.

```text
members 1
-> N schedules
```

## 16. 실행 정보

- 포트: `8081`
- DB URL: `jdbc:mysql://localhost:3306/memberdb`
- DB 계정: `root`
- DB 비밀번호: `1234`

실행 순서:

1. MySQL 실행
2. 프로젝트 실행
3. 애플리케이션 시작 시 `schema.sql` 실행
4. `http://localhost:8081` 접속

## 17. 발표할 때 짧게 말하면

`사용자가 로그인 화면에서 아이디와 비밀번호를 입력하면 컨트롤러가 요청을 받고, 서비스가 인증을 처리하고, repository가 DB를 조회합니다. 인증이 성공하면 SessionManager가 세션 쿠키를 만들고, 이후 대시보드에서는 JavaScript가 일정 API를 호출해서 조회, 등록, 삭제를 처리합니다.`

## 18. 참고 파일

1. [HomeController.java](/C:/Users/USER/Downloads/java/member-site/src/main/java/com/example/membersite/controller/HomeController.java)
2. [AuthController.java](/C:/Users/USER/Downloads/java/member-site/src/main/java/com/example/membersite/controller/AuthController.java)
3. [DashboardController.java](/C:/Users/USER/Downloads/java/member-site/src/main/java/com/example/membersite/controller/DashboardController.java)
4. [MemberController.java](/C:/Users/USER/Downloads/java/member-site/src/main/java/com/example/membersite/controller/MemberController.java)
5. [ScheduleApiController.java](/C:/Users/USER/Downloads/java/member-site/src/main/java/com/example/membersite/controller/ScheduleApiController.java)
6. [MemberService.java](/C:/Users/USER/Downloads/java/member-site/src/main/java/com/example/membersite/service/MemberService.java)
7. [ScheduleService.java](/C:/Users/USER/Downloads/java/member-site/src/main/java/com/example/membersite/service/ScheduleService.java)
8. [SessionManager.java](/C:/Users/USER/Downloads/java/member-site/src/main/java/com/example/membersite/support/SessionManager.java)
9. [dashboard-planner.js](/C:/Users/USER/Downloads/java/member-site/src/main/resources/static/js/dashboard-planner.js)
