(function () {
    var root = document.documentElement;
    if (!root) {
        return;
    }

    function rand(min, max) {
        return Math.random() * (max - min) + min;
    }

    function pct(min, max) {
        return rand(min, max).toFixed(2) + "%";
    }

    function px(min, max) {
        return Math.round(rand(min, max)) + "px";
    }

    function randomPoint() {
        return {
            x: rand(-25, 125),
            y: rand(-30, 145)
        };
    }

    var accentA = randomPoint();
    var accentB = randomPoint();
    var attempts = 0;
    while (attempts < 8) {
        var dx = Math.abs(accentA.x - accentB.x);
        var dy = Math.abs(accentA.y - accentB.y);
        if (dx + dy > 40) {
            break;
        }
        accentB = randomPoint();
        attempts++;
    }

    root.style.setProperty("--accent-a-x", accentA.x.toFixed(2) + "%");
    root.style.setProperty("--accent-a-y", accentA.y.toFixed(2) + "%");
    root.style.setProperty("--accent-b-x", accentB.x.toFixed(2) + "%");
    root.style.setProperty("--accent-b-y", accentB.y.toFixed(2) + "%");
    root.style.setProperty("--accent-a-w", px(760, 1140));
    root.style.setProperty("--accent-a-h", px(360, 620));
    root.style.setProperty("--accent-b-w", px(700, 1080));
    root.style.setProperty("--accent-b-h", px(340, 580));
})();
