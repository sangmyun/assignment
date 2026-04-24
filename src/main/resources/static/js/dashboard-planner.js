(function () {
    const calendarGrid = document.getElementById("calendarGrid");
    const calendarTitle = document.getElementById("calendarTitle");
    const selectedDateText = document.getElementById("selectedDateText");
    const selectedSummaryDate = document.getElementById("selectedSummaryDate");
    const todayCount = document.getElementById("todayCount");
    const monthCount = document.getElementById("monthCount");
    const dailyScheduleList = document.getElementById("dailyScheduleList");
    const scheduleInput = document.getElementById("scheduleInput");
    const saveScheduleButton = document.getElementById("saveScheduleButton");
    const scheduleStatus = document.getElementById("scheduleStatus");
    const prevMonthButton = document.getElementById("prevMonthButton");
    const nextMonthButton = document.getElementById("nextMonthButton");

    if (!calendarGrid) {
        return;
    }

    const weekdayNames = ["일", "월", "화", "수", "목", "금", "토"];
    const today = new Date();
    const todayDate = formatDate(today);

    let currentYear = today.getFullYear();
    let currentMonth = today.getMonth() + 1;
    let selectedDate = todayDate;
    let monthlySchedules = [];

    // Date 객체를 yyyy-MM-dd 문자열로 변환한다.
    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return year + "-" + month + "-" + day;
    }

    // yyyy-MM-dd 문자열을 Date 객체로 변환한다.
    function parseDateString(value) {
        const parts = value.split("-").map(Number);
        return new Date(parts[0], parts[1] - 1, parts[2]);
    }

    // 날짜 문자열을 화면 표시용 형식으로 바꾼다.
    function formatDisplayDate(value) {
        const date = parseDateString(value);
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const weekday = weekdayNames[date.getDay()];
        return month + "월 " + day + "일 (" + weekday + ")";
    }

    // JSON 요청용 공통 헤더를 만든다.
    function createHeaders() {
        return {
            "Content-Type": "application/json"
        };
    }

    // 인증 만료를 처리하면서 JSON 응답을 가져온다.
    async function fetchJson(url) {
        const response = await fetch(url);
        if (response.status === 401) {
            window.location.href = "/login";
            return [];
        }
        return response.json();
    }

    // 현재 월의 일정 목록을 서버에서 받아온다.
    async function fetchMonthlySchedules() {
        monthlySchedules = await fetchJson("/api/schedules?year=" + currentYear + "&month=" + currentMonth);
    }

    // 선택한 날짜의 일정 목록을 서버에서 받아온다.
    async function fetchDailySchedules(date) {
        return fetchJson("/api/schedules/daily?date=" + date);
    }

    // 상단 요약 카드의 숫자와 날짜를 갱신한다.
    function updateSummary(todaySchedules) {
        if (todayCount) {
            todayCount.textContent = todaySchedules.length + "개";
        }

        if (monthCount) {
            monthCount.textContent = monthlySchedules.length + "개";
        }

        if (selectedSummaryDate) {
            selectedSummaryDate.textContent = selectedDate === todayDate ? "오늘" : formatDisplayDate(selectedDate);
        }
    }

    // 월간 일정 데이터를 기준으로 달력 UI를 다시 그린다.
    function renderCalendar() {
        const firstDay = new Date(currentYear, currentMonth - 1, 1);
        const lastDay = new Date(currentYear, currentMonth, 0);
        const startWeekday = firstDay.getDay(); // 해당 월 1일 무슨요일인지 숫자로 나타낸 값 ex) 0: 일요일, 1: 월요일, 2: 화요일 ~~~~
        const totalDays = lastDay.getDate(); //해당 월의 총 일수

        calendarTitle.textContent = currentYear + "년 " + currentMonth + "월";
        calendarGrid.innerHTML = "";

        for (let blank = 0; blank < startWeekday; blank += 1) {
            const empty = document.createElement("div");
            empty.className = "calendar-cell empty";
            calendarGrid.appendChild(empty);
        }

        for (let day = 1; day <= totalDays; day += 1) {
            const cellDate = currentYear + "-" + String(currentMonth).padStart(2, "0") + "-" + String(day).padStart(2, "0");
            const cell = document.createElement("button");
            const scheduleCount = monthlySchedules.filter(function (item) {
                return item.date === cellDate;
            }).length;

            const classes = ["calendar-cell"];
            if (cellDate === selectedDate) {
                classes.push("selected");
            }
            if (cellDate === todayDate) {
                classes.push("today");
            }

            cell.type = "button";
            cell.className = classes.join(" ");
            cell.innerHTML = "<span class='date-number'>" + day + "</span>"
                + (cellDate === todayDate ? "<span class='today-marker'>TODAY</span>" : "")
                + (scheduleCount > 0 ? "<span class='date-badge'>" + scheduleCount + "</span>" : "");
            cell.addEventListener("click", function () {
                selectedDate = cellDate;
                renderCalendar();
                loadDailySchedules();
            });
            calendarGrid.appendChild(cell);
        }
    }

    // 현재 선택 날짜의 일정 목록을 불러와 화면에 표시한다.
    async function loadDailySchedules() {
        selectedDateText.textContent = formatDisplayDate(selectedDate);
        if (selectedSummaryDate) {
            selectedSummaryDate.textContent = selectedDate === todayDate ? "오늘" : formatDisplayDate(selectedDate);
        }
        const schedules = await fetchDailySchedules(selectedDate);
        renderDailySchedules(schedules);
    }

    // 하루 일정 목록 영역을 다시 렌더링한다.
    function renderDailySchedules(schedules) {
        dailyScheduleList.innerHTML = "";

        if (schedules.length === 0) {
            dailyScheduleList.innerHTML = "<div class='empty-state'>선택한 날짜에 등록된 일정이 없습니다.</div>";
            return;
        }

        schedules.forEach(function (schedule) {
            const item = document.createElement("div");
            item.className = "schedule-item";
            item.innerHTML = "<div><strong>" + escapeHtml(schedule.content) + "</strong><p>" + formatDisplayDate(schedule.date) + "</p></div>";

            const button = document.createElement("button");
            button.type = "button";
            button.className = "mini-button";
            button.textContent = "삭제";
            button.addEventListener("click", function () {
                deleteSchedule(schedule.id);
            });

            item.appendChild(button);
            dailyScheduleList.appendChild(item);
        });
    }

    // 입력창의 내용을 현재 선택 날짜 일정으로 저장한다.
    async function saveSchedule() {
        const content = scheduleInput.value.trim();

        if (!content) {
            scheduleStatus.textContent = "일정 내용을 입력하세요.";
            return;
        }

        const response = await fetch("/api/schedules", {
            method: "POST",
            headers: createHeaders(),
            body: JSON.stringify({
                date: selectedDate,
                content: content
            })
        });

        if (response.status === 401) {
            window.location.href = "/login";
            return;
        }

        if (!response.ok) {
            const message = await response.text();
            scheduleStatus.textContent = message || "일정을 저장하지 못했습니다.";
            return;
        }

        scheduleInput.value = "";
        scheduleStatus.textContent = "일정이 저장되었습니다.";
        await refreshAll();
    }

    // 선택한 일정 하나를 서버에 삭제 요청한다.
    async function deleteSchedule(scheduleId) {
        const response = await fetch("/api/schedules/" + scheduleId + "/delete", {
            method: "POST",
            headers: createHeaders()
        });

        if (response.status === 401) {
            window.location.href = "/login";
            return;
        }

        if (!response.ok) {
            scheduleStatus.textContent = "일정을 삭제하지 못했습니다.";
            return;
        }

        scheduleStatus.textContent = "일정이 삭제되었습니다.";
        await refreshAll();
    }

    // 월간 일정, 일간 일정, 요약 정보를 한 번에 새로고침한다.
    async function refreshAll() {
        // 이번 달 일정 목록 서버에서 가져올때 까지 기다림
        await fetchMonthlySchedules();
        // 달력을 렌더링
        renderCalendar();
        // 현재 선태고딘 날짜의 일정 목록 가져옴
        await loadDailySchedules();
        //
        const todaySchedules = await fetchDailySchedules(todayDate);
        updateSummary(todaySchedules);
    }

    // 일정 내용을 안전하게 출력하기 위해 HTML 특수문자를 이스케이프한다.
    function escapeHtml(value) {
        return value
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }

    prevMonthButton.addEventListener("click", async function () {
        currentMonth -= 1;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear -= 1;
        }
        selectedDate = currentYear + "-" + String(currentMonth).padStart(2, "0") + "-01";
        await refreshAll();
    });

    nextMonthButton.addEventListener("click", async function () {
        currentMonth += 1;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear += 1;
        }
        selectedDate = currentYear + "-" + String(currentMonth).padStart(2, "0") + "-01";
        await refreshAll();
    });

    saveScheduleButton.addEventListener("click", saveSchedule);

    refreshAll();
})();
