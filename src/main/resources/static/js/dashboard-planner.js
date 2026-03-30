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

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
    const weekdayNames = ["일", "월", "화", "수", "목", "금", "토"];

    const today = new Date();
    const todayDate = formatDate(today);

    let currentYear = today.getFullYear();
    let currentMonth = today.getMonth() + 1;
    let selectedDate = todayDate;
    let monthlySchedules = [];

    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return year + "-" + month + "-" + day;
    }

    function parseDateString(value) {
        const parts = value.split("-").map(Number);
        return new Date(parts[0], parts[1] - 1, parts[2]);
    }

    function formatDisplayDate(value) {
        const date = parseDateString(value);
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const weekday = weekdayNames[date.getDay()];
        return month + "월 " + day + "일 " + weekday + "요일";
    }

    function createHeaders() {
        const headers = {
            "Content-Type": "application/json"
        };

        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        return headers;
    }

    async function fetchMonthlySchedules() {
        const response = await fetch("/api/schedules?year=" + currentYear + "&month=" + currentMonth);
        monthlySchedules = await response.json();
    }

    async function fetchDailySchedules(date) {
        const response = await fetch("/api/schedules/daily?date=" + date);
        return response.json();
    }

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

    function renderCalendar() {
        const firstDay = new Date(currentYear, currentMonth - 1, 1);
        const lastDay = new Date(currentYear, currentMonth, 0);
        const startWeekday = firstDay.getDay();
        const totalDays = lastDay.getDate();

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

    async function loadDailySchedules() {
        selectedDateText.textContent = formatDisplayDate(selectedDate) + " 일정";
        if (selectedSummaryDate) {
            selectedSummaryDate.textContent = selectedDate === todayDate ? "오늘" : formatDisplayDate(selectedDate);
        }
        const schedules = await fetchDailySchedules(selectedDate);
        renderDailySchedules(schedules);
    }

    function renderDailySchedules(schedules) {
        dailyScheduleList.innerHTML = "";

        if (schedules.length === 0) {
            dailyScheduleList.innerHTML = "<div class='empty-state'>아직 등록된 계획이 없어요. 날짜를 선택한 뒤 첫 일정을 가볍게 추가해보세요.</div>";
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

    async function saveSchedule() {
        const content = scheduleInput.value.trim();

        if (!content) {
            scheduleStatus.textContent = "계획 내용을 입력해주세요.";
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

        if (!response.ok) {
            const message = await response.text();
            scheduleStatus.textContent = message || "일정을 저장하지 못했습니다.";
            return;
        }

        scheduleInput.value = "";
        scheduleStatus.textContent = "일정이 저장되었습니다.";
        await refreshAll();
    }

    async function deleteSchedule(scheduleId) {
        const response = await fetch("/api/schedules/" + scheduleId + "/delete", {
            method: "POST",
            headers: createHeaders()
        });

        if (!response.ok) {
            scheduleStatus.textContent = "일정을 삭제하지 못했습니다.";
            return;
        }

        scheduleStatus.textContent = "일정을 삭제했습니다.";
        await refreshAll();
    }

    async function refreshAll() {
        await fetchMonthlySchedules();
        renderCalendar();
        await loadDailySchedules();
        const todaySchedules = await fetchDailySchedules(todayDate);
        updateSummary(todaySchedules);
    }

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
