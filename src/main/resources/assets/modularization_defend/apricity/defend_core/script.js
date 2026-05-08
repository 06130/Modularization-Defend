// DefendCore Menu Script

document.addEventListener('load', function () {
    initCloseButton();
    initData();
    console.log('DefendCore menu loaded');
});

function initCloseButton() {
    var btn = document.getElementById('close-btn');
    if (btn) {
        btn.addEventListener('mousedown', function (e) {
            closeMenu();
        });
    }
}

function closeMenu() {
    console.log('Closing DefendCore menu...');
    // ApricityUI closeScreen if available, otherwise Escape handles it
    if (typeof ApricityUI !== 'undefined' && ApricityUI.closeScreen) {
        ApricityUI.closeScreen();
    }
}

function initData() {
    // 占位数据 — 后续通过网络同步更新
    updateEnergy(0, 0);
    updateStatus({ harm: 0, efficiency: 0, shield: false, core: 'None' });
    updateUpgrades({ speed: 0, energy: 0, efficiency: 0, capacity: 0, security: 0 });
}

// === 公开 API：供 Java 端调用以更新 UI ===
// 用法：window.defendCoreMenu.updateEnergy(current, max)

window.defendCoreMenu = {
    updateEnergy: updateEnergy,
    updateStatus: updateStatus,
    updateUpgrades: updateUpgrades
};

function updateEnergy(current, max) {
    var fill = document.getElementById('energy-bar-fill');
    var text = document.getElementById('energy-text');
    if (fill) {
        var pct = max > 0 ? Math.min(100, (current / max) * 100) : 0;
        fill.setAttribute('style', 'width: ' + pct + '%;');
    }
    if (text) {
        var curStr = formatEnergy(current);
        var maxStr = formatEnergy(max);
        text.innerText = curStr + ' / ' + maxStr + ' FE';
    }
}

function updateStatus(data) {
    setText('val-harm', String(data.harm));
    setText('val-efficiency', String(data.efficiency));
    setText('val-shield', data.shield ? 'Active' : 'Inactive');
    setText('val-core', String(data.core));
}

function updateUpgrades(levels) {
    setUpgrade('speed', levels.speed);
    setUpgrade('energy', levels.energy);
    setUpgrade('efficiency', levels.efficiency);
    setUpgrade('capacity', levels.capacity);
    setUpgrade('security', levels.security);
}

// === 辅助函数 ===

function setUpgrade(name, level) {
    var max = 4;
    var pct = Math.min(100, (level / max) * 100);
    var bar = document.getElementById('upg-' + name);
    var val = document.getElementById('upg-' + name + '-val');
    if (bar) bar.setAttribute('style', 'width: ' + pct + '%;');
    if (val) val.innerText = String(level) + '/' + String(max);
}

function setText(id, text) {
    var el = document.getElementById(id);
    if (el) el.innerText = text;
}

function formatEnergy(value) {
    if (value >= 1000000) return (value / 1000000).toFixed(1) + 'M';
    if (value >= 1000) return (value / 1000).toFixed(1) + 'K';
    return String(value);
}