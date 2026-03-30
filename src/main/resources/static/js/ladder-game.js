(function () {
    const participantsInput = document.getElementById("participantsInput");
    const resultsInput = document.getElementById("resultsInput");
    const generateGameButton = document.getElementById("generateGameButton");
    const revealAllButton = document.getElementById("revealAllButton");
    const playerTabs = document.getElementById("playerTabs");
    const resultBoard = document.getElementById("resultBoard");
    const gameStatus = document.getElementById("gameStatus");
    const canvas = document.getElementById("ladderCanvas");

    if (!participantsInput || !resultsInput || !canvas) {
        return;
    }

    const ctx = canvas.getContext("2d");

    let state = null;

    function parseLines(value) {
        return value
            .split(/\r?\n/)
            .map((line) => line.trim())
            .filter((line) => line.length > 0);
    }

    function buildConnections(playerCount, steps) {
        const rows = [];

        for (let step = 0; step < steps; step += 1) {
            const cols = [];
            let previous = -2;

            for (let col = 0; col < playerCount - 1; col += 1) {
                const canConnect = col - previous > 1 && Math.random() > 0.45;
                if (canConnect) {
                    cols.push(col);
                    previous = col;
                }
            }

            rows.push(cols);
        }

        return rows;
    }

    function traceResultIndex(startIndex, connections, playerCount) {
        let current = startIndex;

        for (let row = 0; row < connections.length; row += 1) {
            const cols = connections[row];

            if (cols.includes(current)) {
                current += 1;
            } else if (cols.includes(current - 1)) {
                current -= 1;
            }
        }

        return Math.max(0, Math.min(playerCount - 1, current));
    }

    function createGame() {
        const participants = parseLines(participantsInput.value);
        const rewards = parseLines(resultsInput.value);

        if (participants.length < 2 || participants.length > 6) {
            gameStatus.textContent = "참가자는 2명 이상 6명 이하로 입력해주세요.";
            return;
        }

        if (participants.length !== rewards.length) {
            gameStatus.textContent = "참가자 수와 결과 수를 같게 입력해주세요.";
            return;
        }

        const connections = buildConnections(participants.length, 10);
        const mappings = participants.map((name, index) => {
            const rewardIndex = traceResultIndex(index, connections, participants.length);
            return {
                name,
                reward: rewards[rewardIndex],
                rewardIndex
            };
        });

        state = {
            participants,
            rewards,
            connections,
            mappings,
            selectedIndex: null
        };

        gameStatus.textContent = "사다리가 생성되었습니다. 참가자를 눌러 결과를 확인해보세요.";
        render();
    }

    function drawBoard() {
        if (!state) {
            return;
        }

        const width = canvas.width;
        const height = canvas.height;
        const top = 70;
        const bottom = height - 90;
        const usableHeight = bottom - top;
        const steps = state.connections.length;
        const spacingX = width / (state.participants.length + 1);
        const spacingY = usableHeight / steps;

        ctx.clearRect(0, 0, width, height);
        ctx.lineCap = "round";
        ctx.lineJoin = "round";

        ctx.fillStyle = "#ffffff";
        ctx.fillRect(0, 0, width, height);

        state.participants.forEach((participant, index) => {
            const x = spacingX * (index + 1);
            const isSelected = state.selectedIndex === index;

            ctx.strokeStyle = isSelected ? "#111827" : "#cbd5e1";
            ctx.lineWidth = isSelected ? 5 : 3;
            ctx.beginPath();
            ctx.moveTo(x, top);
            ctx.lineTo(x, bottom);
            ctx.stroke();

            ctx.fillStyle = "#111827";
            ctx.font = "600 14px Pretendard, sans-serif";
            ctx.textAlign = "center";
            ctx.fillText(participant, x, 36);
        });

        state.rewards.forEach((reward, index) => {
            const x = spacingX * (index + 1);
            ctx.fillStyle = "#6b7280";
            ctx.font = "600 13px Pretendard, sans-serif";
            ctx.textAlign = "center";
            ctx.fillText(reward, x, height - 32);
        });

        state.connections.forEach((cols, rowIndex) => {
            const y = top + spacingY * (rowIndex + 0.5);

            cols.forEach((col) => {
                const leftX = spacingX * (col + 1);
                const rightX = spacingX * (col + 2);
                const isHighlighted = state.selectedIndex !== null &&
                    (traceSegment(state.selectedIndex, rowIndex, col));

                ctx.strokeStyle = isHighlighted ? "#111827" : "#94a3b8";
                ctx.lineWidth = isHighlighted ? 5 : 3;
                ctx.beginPath();
                ctx.moveTo(leftX, y);
                ctx.lineTo(rightX, y);
                ctx.stroke();
            });
        });
    }

    function traceSegment(selectedIndex, targetRow, targetCol) {
        let current = selectedIndex;

        for (let row = 0; row <= targetRow; row += 1) {
            const cols = state.connections[row];

            if (row === targetRow) {
                return cols.includes(current) && current === targetCol
                    || cols.includes(current - 1) && current - 1 === targetCol;
            }

            if (cols.includes(current)) {
                current += 1;
            } else if (cols.includes(current - 1)) {
                current -= 1;
            }
        }

        return false;
    }

    function renderTabs() {
        playerTabs.innerHTML = "";

        state.participants.forEach((participant, index) => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "player-tab" + (state.selectedIndex === index ? " active" : "");
            button.textContent = participant;
            button.addEventListener("click", () => {
                state.selectedIndex = index;
                render();
            });
            playerTabs.appendChild(button);
        });
    }

    function renderResults(showAll) {
        resultBoard.innerHTML = "";

        state.mappings.forEach((mapping, index) => {
            const item = document.createElement("div");
            const shouldShow = showAll || state.selectedIndex === index;
            item.className = "result-item" + (state.selectedIndex === index ? " active" : "");
            item.innerHTML = shouldShow
                ? "<span>" + mapping.name + "</span><strong>" + mapping.reward + "</strong>"
                : "<span>" + mapping.name + "</span><strong>결과 확인</strong>";
            resultBoard.appendChild(item);
        });
    }

    function render(showAll) {
        if (!state) {
            return;
        }

        renderTabs();
        drawBoard();
        renderResults(Boolean(showAll));
    }

    generateGameButton.addEventListener("click", createGame);
    revealAllButton.addEventListener("click", function () {
        if (!state) {
            createGame();
            return;
        }
        render(true);
        gameStatus.textContent = "전체 결과를 확인하고 있습니다.";
    });

    window.addEventListener("resize", drawBoard);
    createGame();
})();
