(function () {
    const calendarGrid = document.getElementById("calendarGrid");
    const calendarTitle = document.getElementById("calendarTitle");
    const selectedDateText = document.getElementById("selectedDateText");
    const selectedListDate = document.getElementById("selectedListDate");
    const selectedSummaryDate = document.getElementById("selectedSummaryDate");
    const todayCount = document.getElementById("todayCount");
    const monthCount = document.getElementById("monthCount");
    const dailyScheduleList = document.getElementById("dailyScheduleList");
    const scheduleInput = document.getElementById("scheduleInput");
    const saveScheduleButton = document.getElementById("saveScheduleButton");
    const saveOrderButton = document.getElementById("saveOrderButton");
    const scheduleStatus = document.getElementById("scheduleStatus");
    const prevMonthButton = document.getElementById("prevMonthButton");
    const nextMonthButton = document.getElementById("nextMonthButton");

    if (!calendarGrid) {
        return;
    }

    const API = {
        monthly: "/api/schedules",
        daily: "/api/schedules/daily",
        reorder: "/api/schedules/reorder",
        deleteById: function (scheduleId) {
            return "/api/schedules/" + scheduleId + "/delete";
        }
    };

    const MESSAGES = {
        today: "Today",
        noSchedulesForDate: "No schedules on this date.",
        orderChanged: "Order changed. Click Save Order to persist.",
        enterSchedule: "Enter schedule content.",
        noSchedulesToSave: "No schedules to save.",
        saveFailed: "Failed to save schedule.",
        orderSaveFailed: "Failed to save order.",
        deleteFailed: "Failed to delete schedule.",
        scheduleSaved: "Schedule saved.",
        orderSaved: "Order saved.",
        scheduleDeleted: "Schedule deleted.",
        emptyApiResponse: "Schedule API response is empty. Check login status or server logs."
    };

    const weekdayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    const today = new Date();
    const todayDate = formatDate(today);

    let currentYear = today.getFullYear();
    let currentMonth = today.getMonth() + 1;
    let selectedDate = todayDate;
    let monthlySchedules = [];
    let dailySchedules = [];

    function formatDate(date) {
        return formatDateParts(date.getFullYear(), date.getMonth() + 1, date.getDate());
    }

    function formatDateParts(year, month, day) {
        return year + "-" + String(month).padStart(2, "0") + "-" + String(day).padStart(2, "0");
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
        return month + "/" + day + " (" + weekday + ")";
    }

    function createHeaders() {
        return {
            "Content-Type": "application/json"
        };
    }

    function redirectIfUnauthorized(response) {
        if (response.status === 401) {
            window.location.href = "/login";
            return true;
        }
        return false;
    }

    function setStatus(message) {
        if (scheduleStatus) {
            scheduleStatus.textContent = message;
        }
    }

    function selectedSummaryLabel() {
        return selectedDate === todayDate ? MESSAGES.today : formatDisplayDate(selectedDate);
    }

    function updateSelectedSummaryDate() {
        if (selectedSummaryDate) {
            selectedSummaryDate.textContent = selectedSummaryLabel();
        }
    }

    async function fetchJson(url) {
        try {
            const response = await fetch(url);
            if (redirectIfUnauthorized(response)) {
                return [];
            }
            if (!response.ok) {
                return [];
            }

            const contentType = response.headers.get("content-type") || "";
            if (!contentType.includes("application/json")) {
                return [];
            }
            return await response.json();
        } catch (error) {
            return [];
        }
    }

    async function fetchMonthlySchedules() {
        monthlySchedules = await fetchJson(API.monthly + "?year=" + currentYear + "&month=" + currentMonth);
        if (!Array.isArray(monthlySchedules)) {
            monthlySchedules = [];
        }
    }

    async function fetchDailySchedules(date) {
        const schedules = await fetchJson(API.daily + "?date=" + date);
        return Array.isArray(schedules) ? schedules : [];
    }

    function updateSummary(todaySchedules) {
        if (todayCount) {
            todayCount.textContent = todaySchedules.length + "";
        }
        if (monthCount) {
            monthCount.textContent = monthlySchedules.length + "";
        }
        updateSelectedSummaryDate();
    }

    function buildScheduleCountByDate() {
        return monthlySchedules.reduce(function (map, item) {
            if (!item || !item.date) {
                return map;
            }
            map[item.date] = (map[item.date] || 0) + 1;
            return map;
        }, {});
    }

    function renderCalendar() {
        const firstDay = new Date(currentYear, currentMonth - 1, 1);
        const lastDay = new Date(currentYear, currentMonth, 0);
        const startWeekday = firstDay.getDay();
        const totalDays = lastDay.getDate();
        const scheduleCountByDate = buildScheduleCountByDate();

        if (calendarTitle) {
            calendarTitle.textContent = currentYear + "." + currentMonth;
        }
        calendarGrid.innerHTML = "";

        for (let blank = 0; blank < startWeekday; blank += 1) {
            const empty = document.createElement("div");
            empty.className = "calendar-cell empty";
            calendarGrid.appendChild(empty);
        }

        for (let day = 1; day <= totalDays; day += 1) {
            const cellDate = formatDateParts(currentYear, currentMonth, day);
            const scheduleCount = scheduleCountByDate[cellDate] || 0;
            const cell = createCalendarCell(cellDate, day, scheduleCount);
            calendarGrid.appendChild(cell);
        }
    }

    function createCalendarCell(cellDate, day, scheduleCount) {
        const cell = document.createElement("button");
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

        return cell;
    }

    async function loadDailySchedules() {
        const displayDate = formatDisplayDate(selectedDate);

        if (selectedDateText) {
            selectedDateText.textContent = displayDate;
        }
        if (selectedListDate) {
            selectedListDate.textContent = displayDate;
        }
        updateSelectedSummaryDate();

        dailySchedules = await fetchDailySchedules(selectedDate);
        renderDailySchedules();
    }

    function renderDailySchedules() {
        if (!dailyScheduleList) {
            return;
        }
        dailyScheduleList.innerHTML = "";

        if (saveOrderButton) {
            saveOrderButton.disabled = dailySchedules.length <= 1;
        }

        if (dailySchedules.length === 0) {
            dailyScheduleList.innerHTML = "<div class='empty-state'>" + MESSAGES.noSchedulesForDate + "</div>";
            return;
        }

        dailySchedules.forEach(function (schedule, index) {
            const item = document.createElement("div");
            item.className = "schedule-item";

            const content = document.createElement("div");
            content.className = "schedule-content";
            content.innerHTML = "<strong>" + escapeHtml(schedule.content) + "</strong><p>" + formatDisplayDate(schedule.date) + "</p>";

            const actions = document.createElement("div");
            actions.className = "schedule-item-actions";

            const upButton = document.createElement("button");
            upButton.type = "button";
            upButton.className = "mini-button";
            upButton.textContent = "Up";
            upButton.disabled = index === 0;
            upButton.addEventListener("click", function () {
                moveSchedule(index, -1);
            });

            const downButton = document.createElement("button");
            downButton.type = "button";
            downButton.className = "mini-button";
            downButton.textContent = "Down";
            downButton.disabled = index === dailySchedules.length - 1;
            downButton.addEventListener("click", function () {
                moveSchedule(index, 1);
            });

            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.className = "mini-button";
            deleteButton.textContent = "Delete";
            deleteButton.addEventListener("click", function () {
                deleteSchedule(schedule.id);
            });

            actions.appendChild(upButton);
            actions.appendChild(downButton);
            actions.appendChild(deleteButton);
            item.appendChild(content);
            item.appendChild(actions);
            dailyScheduleList.appendChild(item);
        });
    }

    function moveSchedule(index, offset) {
        const target = index + offset;
        if (target < 0 || target >= dailySchedules.length) {
            return;
        }

        const moved = dailySchedules[index];
        dailySchedules.splice(index, 1);
        dailySchedules.splice(target, 0, moved);
        setStatus(MESSAGES.orderChanged);
        renderDailySchedules();
    }

    async function saveSchedule() {
        if (!scheduleInput) {
            return;
        }

        const content = scheduleInput.value.trim();
        if (!content) {
            setStatus(MESSAGES.enterSchedule);
            return;
        }

        const response = await fetch(API.monthly, {
            method: "POST",
            headers: createHeaders(),
            body: JSON.stringify({
                date: selectedDate,
                content: content
            })
        });

        if (redirectIfUnauthorized(response)) {
            return;
        }
        if (!response.ok) {
            const message = await response.text();
            setStatus(message || MESSAGES.saveFailed);
            return;
        }

        scheduleInput.value = "";
        setStatus(MESSAGES.scheduleSaved);
        await refreshAll();
    }

    async function saveOrder() {
        if (!dailySchedules.length) {
            setStatus(MESSAGES.noSchedulesToSave);
            return;
        }

        const response = await fetch(API.reorder, {
            method: "POST",
            headers: createHeaders(),
            body: JSON.stringify({
                date: selectedDate,
                scheduleIds: dailySchedules.map(function (item) {
                    return item.id;
                })
            })
        });

        if (redirectIfUnauthorized(response)) {
            return;
        }
        if (!response.ok) {
            const message = await response.text();
            setStatus(message || MESSAGES.orderSaveFailed);
            return;
        }

        setStatus(MESSAGES.orderSaved);
        await refreshAll();
    }

    async function deleteSchedule(scheduleId) {
        const response = await fetch(API.deleteById(scheduleId), {
            method: "POST",
            headers: createHeaders()
        });

        if (redirectIfUnauthorized(response)) {
            return;
        }
        if (!response.ok) {
            setStatus(MESSAGES.deleteFailed);
            return;
        }

        setStatus(MESSAGES.scheduleDeleted);
        await refreshAll();
    }

    async function refreshAll() {
        await fetchMonthlySchedules();
        renderCalendar();
        await loadDailySchedules();

        const todaySchedules = await fetchDailySchedules(todayDate);
        updateSummary(todaySchedules);

        if (monthlySchedules.length === 0 && dailySchedules.length === 0) {
            setStatus(MESSAGES.emptyApiResponse);
        }
    }

    function escapeHtml(value) {
        return value
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }

    async function moveMonth(offset) {
        currentMonth += offset;

        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear -= 1;
        } else if (currentMonth > 12) {
            currentMonth = 1;
            currentYear += 1;
        }

        selectedDate = formatDateParts(currentYear, currentMonth, 1);
        await refreshAll();
    }

    if (prevMonthButton) {
        prevMonthButton.addEventListener("click", function () {
            moveMonth(-1);
        });
    }
    if (nextMonthButton) {
        nextMonthButton.addEventListener("click", function () {
            moveMonth(1);
        });
    }
    if (saveScheduleButton) {
        saveScheduleButton.addEventListener("click", saveSchedule);
    }
    if (saveOrderButton) {
        saveOrderButton.addEventListener("click", saveOrder);
    }

    refreshAll();
})();
