// ApricityUI 示例脚本

// 等待 DOM 加载完成
document.addEventListener('load', function() {
    console.log('Example UI loaded!');
    
    // 初始化主题切换
    initThemeToggle();
    
    // 初始化按钮事件
    initButtons();
    
    // 初始化状态显示
    initStatusDisplay();
});

/**
 * 初始化主题切换按钮
 */
function initThemeToggle() {
    const themeBtn = document.getElementById('theme-toggle');
    if (themeBtn) {
        themeBtn.addEventListener('mousedown', function(event) {
            toggleTheme();
        });
    }
}

/**
 * 切换主题
 */
function toggleTheme() {
    const currentTheme = document.getAttribute('data-theme') || 'light';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    
    document.setAttribute('data-theme', newTheme);
    
    // 更新状态显示
    updateStatusDisplay();
    
    console.log('Theme changed to:', newTheme);
}

/**
 * 初始化按钮事件
 */
function initButtons() {
    // 确认按钮
    const confirmBtn = document.getElementById('btn-confirm');
    if (confirmBtn) {
        confirmBtn.addEventListener('mousedown', function(event) {
            onConfirmClick();
        });
    }
    
    // 取消按钮
    const cancelBtn = document.getElementById('btn-cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('mousedown', function(event) {
            onCancelClick();
        });
    }
    
    // 重置按钮
    const resetBtn = document.getElementById('btn-reset');
    if (resetBtn) {
        resetBtn.addEventListener('mousedown', function(event) {
            onResetClick();
        });
    }
}

/**
 * 确认按钮点击事件
 */
function onConfirmClick() {
    const nameInput = document.getElementById('input-name');
    const valueInput = document.getElementById('input-value');
    
    const name = nameInput ? nameInput.getAttribute('value') : '';
    const value = valueInput ? valueInput.getAttribute('value') : '0';
    
    console.log('确认操作 - 名称:', name, '数值:', value);
    
    // 这里可以发送网络包到服务器
    // 例如：ApricityUI.sendPacket('confirm', {name: name, value: value});
    
    alert('已确认：' + name + ' = ' + value);
}

/**
 * 取消按钮点击事件
 */
function onCancelClick() {
    console.log('取消操作');
    
    // 清空输入
    const nameInput = document.getElementById('input-name');
    const valueInput = document.getElementById('input-value');
    
    if (nameInput) nameInput.setAttribute('value', '');
    if (valueInput) valueInput.setAttribute('value', '0');
}

/**
 * 重置按钮点击事件
 */
function onResetClick() {
    console.log('重置操作');
    
    // 恢复默认值
    const nameInput = document.getElementById('input-name');
    const valueInput = document.getElementById('input-value');
    
    if (nameInput) nameInput.setAttribute('placeholder', '请输入名称...');
    if (valueInput) valueInput.setAttribute('placeholder', '0');
    
    // 重置主题
    document.setAttribute('data-theme', 'light');
    updateStatusDisplay();
}

/**
 * 初始化状态显示
 */
function initStatusDisplay() {
    updateStatusDisplay();
    
    // 定时更新 FPS（模拟）
    setInterval(function() {
        const fpsEl = document.getElementById('status-fps');
        if (fpsEl) {
            const fps = Math.floor(55 + Math.random() * 10);
            fpsEl.innerText = String(fps);
        }
    }, 1000);
}

/**
 * 更新状态显示
 */
function updateStatusDisplay() {
    const themeEl = document.getElementById('status-theme');
    const debugEl = document.getElementById('status-debug');
    
    const currentTheme = document.getAttribute('data-theme') || 'light';
    
    if (themeEl) {
        themeEl.innerText = currentTheme;
    }
    
    if (debugEl) {
        // 这里可以从 UIManager 获取调试模式状态
        debugEl.innerText = '关闭';
    }
}

/**
 * 工具函数：显示提示信息
 */
function alert(message) {
    console.log('[Alert]', message);
    // 可以在这里实现自定义的提示框 UI
}
