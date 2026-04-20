/**
 * SonarQube AI Fix Plugin - Frontend Widget
 * 
 * Vue.js 组件，用于在 SonarQube UI 中显示 AI 修复建议
 */

// AI 修复建议组件
window.SonarAI = window.SonarAI || {};

(function() {
    'use strict';

    // 注册自定义组件
    if (window.registerExtension) {
        window.registerExtension('sonar-ai-fix', function(options) {
            const el = options.el;
            const issueKey = options.issueKey || options.component.key;

            // 加载 AI 修复建议
            loadAiFixSuggestion(el, issueKey);
        });
    }

    /**
     * 加载 AI 修复建议
     */
    async function loadAiFixSuggestion(container, issueKey) {
        try {
            // 显示加载状态
            container.innerHTML = `
                <div class="ai-fix-loading">
                    <span class="spinner"></span>
                    <span>正在生成 AI 修复建议...</span>
                </div>
            `;

            // 调用 API 获取修复建议
            const response = await fetch(`/api/ai-fix/suggestions/${issueKey}`);
            
            if (!response.ok) {
                throw new Error('Failed to load AI suggestion');
            }

            const data = await response.json();
            
            if (data.suggestion) {
                renderAiFixSuggestion(container, data.suggestion);
            } else {
                container.innerHTML = `
                    <div class="ai-fix-empty">
                        <p>暂无 AI 修复建议</p>
                        <button class="button" onclick="requestAiFix('${issueKey}')">
                            生成修复建议
                        </button>
                    </div>
                `;
            }
        } catch (error) {
            console.error('Error loading AI fix:', error);
            container.innerHTML = `
                <div class="ai-fix-error">
                    <p>加载 AI 修复建议失败</p>
                    <button class="button button-red" onclick="loadAiFixSuggestion(this.parentElement, '${issueKey}')">
                        重试
                    </button>
                </div>
            `;
        }
    }

    /**
     * 渲染 AI 修复建议
     */
    function renderAiFixSuggestion(container, suggestion) {
        container.innerHTML = `
            <div class="ai-fix-container">
                <div class="ai-fix-header">
                    <span class="ai-fix-icon">🤖</span>
                    <h3>AI 修复建议</h3>
                    <span class="ai-fix-severity severity-${suggestion.severity || 'INFO'}">
                        ${getSeverityLabel(suggestion.severity)}
                    </span>
                </div>
                
                <div class="ai-fix-explanation">
                    <h4>问题说明</h4>
                    <p>${escapeHtml(suggestion.explanation || '暂无说明')}</p>
                </div>
                
                ${suggestion.steps && suggestion.steps.length > 0 ? `
                    <div class="ai-fix-steps">
                        <h4>修复步骤</h4>
                        <ol>
                            ${suggestion.steps.map(step => `<li>${escapeHtml(step)}</li>`).join('')}
                        </ol>
                    </div>
                ` : ''}
                
                ${suggestion.fixedCode ? `
                    <div class="ai-fix-code">
                        <h4>修复后代码</h4>
                        <div class="code-container">
                            <pre><code class="language-java">${escapeHtml(suggestion.fixedCode)}</code></pre>
                            <button class="copy-button" onclick="copyCode(this)">
                                📋 复制代码
                            </button>
                        </div>
                    </div>
                ` : ''}
                
                <div class="ai-fix-actions">
                    <button class="button button-primary" onclick="applyFix('${suggestion.id || ''}')">
                        ✓ 应用修复
                    </button>
                    <button class="button" onclick="dismissFix(this)">
                        ✗ 忽略
                    </button>
                    <button class="button button-link" onclick="showMoreOptions()">
                        更多选项
                    </button>
                </div>
                
                <div class="ai-fix-footer">
                    <span class="ai-fix-provider">
                        由 ${suggestion.provider || 'AI'} 生成
                    </span>
                    <span class="ai-fix-confidence">
                        置信度: ${suggestion.confidence || 'N/A'}
                    </span>
                </div>
            </div>
        `;

        // 高亮代码
        if (window.Prism) {
            Prism.highlightAllUnder(container);
        }
    }

    /**
     * 请求生成 AI 修复建议
     */
    window.requestAiFix = async function(issueKey) {
        try {
            const response = await fetch(`/api/ai-fix/generate/${issueKey}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to generate AI fix');
            }

            const data = await response.json();
            
            // 刷新显示
            loadAiFixSuggestion(document.querySelector('.ai-fix-container').parentElement, issueKey);
        } catch (error) {
            console.error('Error generating AI fix:', error);
            alert('生成 AI 修复建议失败，请稍后重试');
        }
    };

    /**
     * 复制代码到剪贴板
     */
    window.copyCode = function(button) {
        const code = button.previousElementSibling.querySelector('code').textContent;
        navigator.clipboard.writeText(code).then(() => {
            const originalText = button.textContent;
            button.textContent = '✓ 已复制';
            setTimeout(() => {
                button.textContent = originalText;
            }, 2000);
        }).catch(err => {
            console.error('Failed to copy:', err);
        });
    };

    /**
     * 应用修复
     */
    window.applyFix = async function(suggestionId) {
        // TODO: 实现应用修复逻辑
        alert('修复应用功能开发中...');
    };

    /**
     * 忽略修复
     */
    window.dismissFix = function(button) {
        const container = button.closest('.ai-fix-container');
        if (container) {
            container.remove();
        }
    };

    /**
     * 显示更多选项
     */
    window.showMoreOptions = function() {
        // TODO: 实现更多选项菜单
        alert('更多选项功能开发中...');
    };

    // 辅助函数
    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function getSeverityLabel(severity) {
        const labels = {
            'INFO': '信息',
            'MINOR': '次要',
            'MAJOR': '主要',
            'CRITICAL': '严重',
            'BLOCKER': '阻断'
        };
        return labels[severity] || severity || '信息';
    }

})();
