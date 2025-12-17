/**
 * 网上书店管理系统 - 通用JavaScript函数
 */

// ==================== AJAX请求函数 ====================

/**
 * 通用AJAX GET请求
 * @param {string} url - 请求URL
 * @param {function} successCallback - 成功回调函数
 * @param {function} errorCallback - 失败回调函数
 */
function ajaxGet(url, successCallback, errorCallback) {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
    
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    const response = JSON.parse(xhr.responseText);
                    if (successCallback) {
                        successCallback(response);
                    }
                } catch (e) {
                    console.error('JSON解析失败:', e);
                    if (errorCallback) {
                        errorCallback('响应数据格式错误');
                    }
                }
            } else {
                if (errorCallback) {
                    errorCallback('请求失败: ' + xhr.status);
                } else {
                    showError('请求失败，状态码: ' + xhr.status);
                }
            }
        }
    };
    
    xhr.onerror = function() {
        if (errorCallback) {
            errorCallback('网络错误');
        } else {
            showError('网络连接失败，请检查网络设置');
        }
    };
    
    xhr.send();
}

/**
 * 通用AJAX POST请求
 * @param {string} url - 请求URL
 * @param {object} data - 请求数据对象
 * @param {function} successCallback - 成功回调函数
 * @param {function} errorCallback - 失败回调函数
 */
function ajaxPost(url, data, successCallback, errorCallback) {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
    
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    const response = JSON.parse(xhr.responseText);
                    if (successCallback) {
                        successCallback(response);
                    }
                } catch (e) {
                    // 如果响应不是JSON，可能是HTML重定向
                    if (xhr.responseText.trim() === '') {
                        if (successCallback) {
                            successCallback({ success: true });
                        }
                    } else {
                        console.error('JSON解析失败:', e);
                        if (errorCallback) {
                            errorCallback('响应数据格式错误');
                        }
                    }
                }
            } else {
                if (errorCallback) {
                    errorCallback('请求失败: ' + xhr.status);
                } else {
                    showError('请求失败，状态码: ' + xhr.status);
                }
            }
        }
    };
    
    xhr.onerror = function() {
        if (errorCallback) {
            errorCallback('网络错误');
        } else {
            showError('网络连接失败，请检查网络设置');
        }
    };
    
    xhr.send(JSON.stringify(data));
}

/**
 * 表单提交AJAX请求
 * @param {string} formId - 表单ID
 * @param {string} url - 提交URL
 * @param {function} successCallback - 成功回调函数
 * @param {function} errorCallback - 失败回调函数
 */
function ajaxFormSubmit(formId, url, successCallback, errorCallback) {
    const form = document.getElementById(formId);
    if (!form) {
        showError('表单不存在');
        return;
    }
    
    const formData = new FormData(form);
    const data = {};
    for (let [key, value] of formData.entries()) {
        data[key] = value;
    }
    
    ajaxPost(url, data, successCallback, errorCallback);
}

// ==================== 消息提示函数 ====================

/**
 * 显示成功消息
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认3000
 */
function showSuccess(message, duration) {
    showMessage(message, 'success', duration || 3000);
}

/**
 * 显示错误消息
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认5000
 */
function showError(message, duration) {
    showMessage(message, 'danger', duration || 5000);
}

/**
 * 显示警告消息
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认4000
 */
function showWarning(message, duration) {
    showMessage(message, 'warning', duration || 4000);
}

/**
 * 显示信息消息
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认3000
 */
function showInfo(message, duration) {
    showMessage(message, 'info', duration || 3000);
}

/**
 * 显示消息（内部函数）
 * @param {string} message - 消息内容
 * @param {string} type - 消息类型（success/danger/warning/info）
 * @param {number} duration - 显示时长（毫秒）
 */
function showMessage(message, type, duration) {
    // 创建消息容器（如果不存在）
    let messageContainer = document.getElementById('message-container');
    if (!messageContainer) {
        messageContainer = document.createElement('div');
        messageContainer.id = 'message-container';
        messageContainer.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; max-width: 400px;';
        document.body.appendChild(messageContainer);
    }
    
    // 创建消息元素
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-' + type + ' alert-dismissible fade show';
    alertDiv.setAttribute('role', 'alert');
    alertDiv.innerHTML = message + '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>';
    
    messageContainer.appendChild(alertDiv);
    
    // 自动移除
    setTimeout(function() {
        alertDiv.classList.remove('show');
        setTimeout(function() {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, 150);
    }, duration);
}

// ==================== 页面加载通用逻辑 ====================

/**
 * 页面加载完成后执行
 */
document.addEventListener('DOMContentLoaded', function() {
    // 初始化Bootstrap工具提示
    if (typeof bootstrap !== 'undefined') {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function(tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
        
        // 初始化Bootstrap弹出框
        const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        popoverTriggerList.map(function(popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });
    }
    
    // 防止表单重复提交
    const forms = document.querySelectorAll('form');
    forms.forEach(function(form) {
        form.addEventListener('submit', function(e) {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="loading"></span> 提交中...';
                
                // 如果表单验证失败，恢复按钮状态
                setTimeout(function() {
                    if (!form.checkValidity()) {
                        submitBtn.disabled = false;
                        submitBtn.innerHTML = submitBtn.getAttribute('data-original-text') || '提交';
                    }
                }, 100);
            }
        });
    });
    
    // 保存按钮原始文本
    document.querySelectorAll('button[type="submit"]').forEach(function(btn) {
        if (!btn.getAttribute('data-original-text')) {
            btn.setAttribute('data-original-text', btn.innerHTML);
        }
    });
    
    // 数字输入框格式化
    const numberInputs = document.querySelectorAll('input[type="number"]');
    numberInputs.forEach(function(input) {
        input.addEventListener('blur', function() {
            const value = parseFloat(this.value);
            if (!isNaN(value) && this.hasAttribute('data-decimal')) {
                const decimals = parseInt(this.getAttribute('data-decimal')) || 2;
                this.value = value.toFixed(decimals);
            }
        });
    });
    
    // 确认对话框
    document.querySelectorAll('[data-confirm]').forEach(function(element) {
        element.addEventListener('click', function(e) {
            const message = this.getAttribute('data-confirm') || '确定要执行此操作吗？';
            if (!confirm(message)) {
                e.preventDefault();
                return false;
            }
        });
    });
});

// ==================== 工具函数 ====================

/**
 * 格式化金额
 * @param {number} amount - 金额
 * @param {number} decimals - 小数位数，默认2
 * @returns {string} 格式化后的金额字符串
 */
function formatMoney(amount, decimals) {
    if (amount === null || amount === undefined || isNaN(amount)) {
        return '0.00';
    }
    return parseFloat(amount).toFixed(decimals || 2);
}

/**
 * 格式化日期
 * @param {string|Date} date - 日期
 * @param {string} format - 格式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
function formatDate(date, format) {
    if (!date) return '';
    
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');
    
    format = format || 'YYYY-MM-DD HH:mm:ss';
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

/**
 * 显示加载动画
 * @param {string} elementId - 元素ID
 */
function showLoading(elementId) {
    const element = document.getElementById(elementId || 'loading');
    if (element) {
        element.style.display = 'block';
        element.innerHTML = '<div class="text-center"><div class="loading"></div><p>加载中...</p></div>';
    }
}

/**
 * 隐藏加载动画
 * @param {string} elementId - 元素ID
 */
function hideLoading(elementId) {
    const element = document.getElementById(elementId || 'loading');
    if (element) {
        element.style.display = 'none';
    }
}

/**
 * 确认对话框
 * @param {string} message - 提示消息
 * @param {function} callback - 确认后的回调函数
 */
function confirmDialog(message, callback) {
    if (confirm(message)) {
        if (callback) {
            callback();
        }
        return true;
    }
    return false;
}

// ==================== 表单验证 & AJAX 错误处理 ====================

/**
 * 简单表单验证
 * @param {string} formId - 表单ID
 * @param {object} rules - 验证规则 { field: { required: true, message: '...' } }
 * @returns {boolean} 是否通过验证
 */
function validateForm(formId, rules) {
    const form = document.getElementById(formId);
    if (!form || !rules) {
        return true;
    }
    for (let field in rules) {
        if (!Object.prototype.hasOwnProperty.call(rules, field)) continue;
        const config = rules[field];
        const input = form.querySelector('[name="' + field + '"]');
        if (!input) continue;
        const value = (input.value || '').trim();
        if (config.required && !value) {
            showWarning(config.message || (input.getAttribute('data-label') || field) + '不能为空');
            input.focus();
            return false;
        }
    }
    return true;
}

/**
 * 统一AJAX错误处理
 * @param {XMLHttpRequest} xhr - XMLHttpRequest 对象
 * @param {function} errorCallback - 自定义错误回调
 */
function handleAjaxError(xhr, errorCallback) {
    const status = xhr && xhr.status;
    let msg = '请求失败';
    if (status === 0) {
        msg = '网络连接失败，请检查网络设置';
    } else if (status >= 500) {
        msg = '服务器内部错误（' + status + '）';
    } else if (status === 404) {
        msg = '请求地址不存在（404）';
    } else if (status === 401 || status === 403) {
        msg = '没有权限或登录已过期（' + status + '）';
    } else if (status) {
        msg = '请求失败，状态码：' + status;
    }
    if (errorCallback) {
        errorCallback(msg);
    } else {
        showError(msg);
    }
}

