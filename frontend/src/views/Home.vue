<template>
  <div class="app-container">
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="logo-section">
        <div class="logo-icon">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <circle cx="16" cy="16" r="14" fill="url(#gradient)"/>
            <path d="M10 16L14 20L22 12" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="gradient" x1="2" y1="2" x2="30" y2="30">
                <stop stop-color="#4CAF50"/>
                <stop offset="1" stop-color="#8BC34A"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <span class="logo-text">PackClaw</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <div 
          class="nav-item" 
          :class="{ active: activeNav === 'new-task' }"
          @click="handleNewTask"
        >
          <component :is="IconPlus" class="nav-icon" />
          <span>新任务</span>
        </div>
        <div class="nav-item" @click="goToSkills">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="nav-icon">
            <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
          </svg>
          <span>技能</span>
        </div>
        <div class="nav-item" @click="goToMemories">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="nav-icon">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            <path d="M8 7h8M8 11h6"/>
          </svg>
          <span>记忆</span>
        </div>
        <div class="nav-item">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          <span>定时任务</span>
        </div>
      </nav>

      <!-- 任务列表 -->
      <div class="task-group-label">任务</div>
      <div class="task-list">
        <div
          v-for="session in sessionList"
          :key="session.chatId"
          class="task-item"
          :class="{ active: currentChatId === session.chatId }"
          @click="selectSession(session)"
        >
          <div class="task-title">{{ truncateTitle(session.title) }}</div>
        </div>
      </div>

      <!-- 版本信息 -->
      <div class="version-section">
        <span class="version-text">v0.0.1</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="settings-icon" title="设置" @click="openSettings">
          <circle cx="12" cy="12" r="3"/>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
        </svg>
      </div>
    </aside>

    <!-- 设置弹窗 -->
    <Settings ref="settingsRef" />

    <!-- 自定义 tooltip -->
    <div class="custom-tooltip" v-show="tooltipVisible" :style="tooltipStyle">
      {{ tooltipText }}
    </div>

    <!-- 中间主内容区 -->
    <main class="main-content">
      <!-- 技能管理视图 -->
      <div v-if="currentView === 'skills'" class="skills-view-container">
        <Skills />
      </div>

      <!-- 记忆管理视图 -->
      <div v-else-if="currentView === 'memories'" class="memories-view-container">
        <Memories />
      </div>

      <!-- 聊天视图 -->
      <div v-else class="chat-view-container">
      <!-- 顶部栏 -->
      <header class="top-bar">
        <div class="header-left">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="menu-icon" v-if="!showSidebar">
            <line x1="3" y1="12" x2="21" y2="12"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </div>
        <div class="header-center">
          <span class="page-title">{{ currentTitle || '' }}</span>
        </div>
        <div class="header-right">
          <a-button text class="theme-btn" @click="toggleTheme" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
            <!-- 月亮图标（暗色模式时显示） -->
            <svg v-if="isDark" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
            <!-- 太阳图标（亮色模式时显示） -->
            <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5"/>
              <line x1="12" y1="1" x2="12" y2="3"/>
              <line x1="12" y1="21" x2="12" y2="23"/>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
              <line x1="1" y1="12" x2="3" y2="12"/>
              <line x1="21" y1="12" x2="23" y2="12"/>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
            </svg>
          </a-button>
        </div>
      </header>

      <!-- 欢迎页面（未进入对话时） -->
      <div v-if="messages.length === 0 && !isChatting" class="welcome-page">
        <div class="welcome-content">
          <!-- Logo 动画 -->
          <div class="welcome-logo">
            <div class="logo-circle">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <circle cx="24" cy="24" r="20" fill="url(#welcomeGradient)"/>
                <path d="M16 24L22 30L32 18" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                <defs>
                  <linearGradient id="welcomeGradient" x1="4" y1="4" x2="44" y2="44">
                    <stop stop-color="#4CAF50"/>
                    <stop offset="1" stop-color="#8BC34A"/>
                  </linearGradient>
                </defs>
              </svg>
            </div>
          </div>
          
          <h1 class="welcome-title">不止聊天，搞定一切</h1>
          <p class="welcome-subtitle">本地运行、自主规划、安全可控的 AI 工作搭子</p>

          <!-- 功能卡片 -->
          <div class="feature-cards">
            <div class="feature-card" @click="sendQuickMessage('帮我整理一下文件')">
              <div class="card-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
              <h3>文件整理</h3>
              <p>智能整理和管理本地文件</p>
            </div>
            <div class="feature-card" @click="sendQuickMessage('帮我写一篇文章')">
              <div class="card-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                  <polyline points="10 9 9 9 8 9"/>
                </svg>
              </div>
              <h3>内容创作</h3>
              <p>创作演示文稿、文档和多媒体内容</p>
            </div>
            <div class="feature-card" @click="sendQuickMessage('帮我分析这份数据')">
              <div class="card-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="20" x2="18" y2="10"/>
                  <line x1="12" y1="20" x2="12" y2="4"/>
                  <line x1="6" y1="20" x2="6" y2="14"/>
                </svg>
              </div>
              <h3>文档处理</h3>
              <p>处理和分析文档数据</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天消息区域 -->
      <div v-else class="chat-area-wrapper">
        <div class="chat-area" ref="messagesContainer">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message-wrapper"
          :class="msg.role"
          :ref="el => { if (el) messageRefs[index] = el }"
        >
          <div class="message-container">
            <div v-if="msg.role === 'user'" class="user-bubble-wrap">
              <div class="user-bubble">
                <!-- 媒体预览（图片缩略图 / 文件徽章） -->
                <div v-if="msg.mediaItems && msg.mediaItems.length > 0" class="user-media-preview">
                  <template v-for="(m, mi) in msg.mediaItems" :key="mi">
                    <!-- 图片：显示缩略图 -->
                    <template v-if="m.mediaType && m.mediaType.startsWith('image')">
                      <img
                        :src="m.previewUrl"
                        class="msg-image-thumb"
                        :alt="m.name || '图片'"
                      />
                    </template>
                    <div v-else class="msg-media-badge">
                      <!-- 视频图标 -->
                      <svg v-if="m.mediaType && m.mediaType.startsWith('video')" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="2" y="5" width="15" height="14" rx="2"/>
                        <polygon points="22 7 17 10 17 14 22 17 22 7"/>
                      </svg>
                      <!-- 音频图标 -->
                      <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 18V5l12-2v13"/>
                        <circle cx="6" cy="18" r="3"/>
                        <circle cx="18" cy="16" r="3"/>
                      </svg>
                      <span>{{ m.name || m.mediaType }}</span>
                    </div>
                  </template>
                </div>
                {{ msg.content }}
              </div>
              <!-- hover 才显示，在气泡下方 -->
              <div class="user-actions">
                <button class="action-icon-btn" @click="copyText(msg.content, index)" :class="{ copied: copiedIndex === index }" title="复制">
                  <svg v-if="copiedIndex !== index" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="9" y="9" width="13" height="13" rx="2"/>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                  </svg>
                  <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                </button>
              </div>
            </div>
            <div v-else class="assistant-bubble">
              <!-- 深度思考区域 -->
              <div
                v-if="msg.thinking || msg.thinkingDone === false"
                class="thinking-block"
              >
                <div class="thinking-header" @click="toggleThinking(index)">
                  <svg class="thinking-icon-svg" :class="{ spinning: !msg.thinkingDone }" width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M7 1.5A5.5 5.5 0 1 1 1.5 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M7 1.5L5 3.5M7 1.5L9 3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span class="thinking-label">深度思考</span>
                  <svg class="thinking-chevron" :class="{ expanded: !msg.thinkingCollapsed }" width="12" height="12" viewBox="0 0 12 12" fill="none">
                    <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <transition name="thinking-expand">
                  <div v-show="!msg.thinkingCollapsed" class="thinking-content">
                    <div class="thinking-text">{{ msg.thinking }}</div>
                  </div>
                </transition>
              </div>

              <!-- 工具调用区域 -->
              <div v-if="msg.toolCalls && msg.toolCalls.length > 0" class="tool-calls-block">
                <div class="tool-calls-header" @click="msg.toolCallsCollapsed = !msg.toolCallsCollapsed" style="cursor:pointer">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="3"/>
                    <path d="M19.07 4.93A10 10 0 1 0 4.93 19.07"/>
                    <path d="M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2"/>
                  </svg>
                  <span>查看 {{ msg.toolCalls.length }} 个步骤</span>
                  <svg class="tool-calls-chevron" :class="{ expanded: !msg.toolCallsCollapsed }" width="12" height="12" viewBox="0 0 12 12" fill="none">
                    <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <transition name="thinking-expand">
                  <div v-show="!msg.toolCallsCollapsed" class="tool-calls-list">
                    <div v-for="tool in msg.toolCalls" :key="tool.id" class="tool-call-item">
                      <!-- 工具调用标题 -->
                      <div class="tool-call-header" @click="toggleToolCall(index, tool.id)">
                        <svg class="tool-status-icon" :class="{ spinning: tool.status === 'running' }" width="14" height="14" viewBox="0 0 24 24" fill="none">
                          <template v-if="tool.status === 'done'">
                            <circle cx="12" cy="12" r="10" stroke="#4CAF50" stroke-width="2"/>
                            <path d="M8 12l3 3 5-5" stroke="#4CAF50" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                          </template>
                          <template v-else>
                            <circle cx="12" cy="12" r="10" stroke="#999" stroke-width="2"/>
                            <path d="M12 2A10 10 0 0 1 22 12" stroke="#4CAF50" stroke-width="2" stroke-linecap="round"/>
                          </template>
                        </svg>
                        <span class="tool-call-name">调用工具 <code>{{ tool.name }}</code></span>
                        <svg class="tool-call-chevron" :class="{ expanded: !tool.collapsed }" width="12" height="12" viewBox="0 0 12 12" fill="none">
                          <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                      </div>
                      <!-- 展开详情 -->
                      <transition name="thinking-expand">
                        <div v-show="!tool.collapsed" class="tool-call-detail">
                          <!-- 输入参数 -->
                          <div v-if="tool.input && Object.keys(tool.input).length" class="tool-detail-section">
                            <div class="tool-detail-label">Input</div>
                            <pre class="tool-detail-content">{{ formatToolInput(tool.input) }}</pre>
                          </div>
                          <!-- 执行结果 -->
                          <div v-if="tool.result" class="tool-detail-section">
                            <div class="tool-detail-label">Response</div>
                            <pre class="tool-detail-content">{{ tool.result }}</pre>
                          </div>
                          <div v-else-if="tool.status === 'running'" class="tool-running-hint">
                            <span>执行中...</span>
                          </div>
                        </div>
                      </transition>
                    </div>
                  </div>
                </transition>
              </div>

              <!-- 正式回答内容 -->
              <div v-if="msg.content" v-html="renderMarkdown(msg.content)"></div>

              <!-- 加载指示器 -->
              <div v-if="msg.loading && !msg.thinking && !msg.content && (!msg.toolCalls || msg.toolCalls.length === 0)" class="typing-indicator">
                <span></span><span></span><span></span>
              </div>

              <!-- 错误消息 -->
              <div v-if="msg.error && msg.contentBlocks" class="error-message-block">
                <div v-for="(block, idx) in msg.contentBlocks.filter(b => b.type === 'text')" :key="idx" class="error-content">
                  <div v-html="renderMarkdown(block.text)"></div>
                </div>
              </div>

              <!-- AI 操作栏，仅在最后一条消息完成且当前不在流式输出时显示 -->
              <div v-if="msg.content && !msg.loading && !isStreaming && isLastAssistantMessage(index)" class="assistant-actions">
                <button class="action-icon-btn" @click="copyText(msg.content, index + 'a')" :class="{ copied: copiedIndex === index + 'a' }" title="复制">
                  <svg v-if="copiedIndex !== index + 'a'" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="9" y="9" width="13" height="13" rx="2"/>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                  </svg>
                  <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        </div>

        <!-- 右侧小导航条 -->
        <div class="chat-minimap">
          <template v-for="(msg, index) in messages" :key="index">
            <div
              v-if="msg.role === 'user'"
              class="minimap-dot"
              :class="{ active: activeMessageIndex === index || activeMessageIndex === index + 1 }"
              @mouseenter="showMinimapTooltip($event, msg)"
              @mouseleave="hideTooltip"
              @click="scrollToMessage(index)"
            ></div>
          </template>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-section">
        <div class="input-container">
          <div class="input-box">
            <!-- 附件预览区（有文件时展示） -->
            <div v-if="selectedFiles.length > 0" class="file-preview-area">
              <div
                v-for="(item, index) in selectedFiles"
                :key="index"
                class="preview-item"
              >
                <img
                  v-if="item.type === 'image'"
                  :src="item.previewUrl"
                  class="preview-image"
                  :alt="item.file.name"
                />
                <div v-else class="preview-file-badge">
                  <!-- 视频图标 -->
                  <svg v-if="item.type === 'video'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="2" y="5" width="15" height="14" rx="2"/>
                    <polygon points="22 7 17 10 17 14 22 17 22 7"/>
                  </svg>
                  <!-- 音频图标 -->
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 18V5l12-2v13"/>
                    <circle cx="6" cy="18" r="3"/>
                    <circle cx="18" cy="16" r="3"/>
                  </svg>
                  <span class="preview-file-name">{{ item.file.name }}</span>
                </div>
                <button class="preview-remove" @click.stop="removeFile(index)" title="移除">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
              </div>
            </div>
            <a-textarea
              v-model="inputMessage"
              :auto-size="{ minRows: 1, maxRows: 6 }"
              placeholder="告诉我目标，我来搞定"
              @keydown.enter.exact.prevent="sendMessage"
              :resize="false"
              class="message-input"
            />
            <div class="input-actions">
              <div class="action-left">
                <a-button text class="action-btn-icon" @click="fileInput?.click()">
                  <component :is="IconPlus" />
                </a-button>
                <input
                  type="file"
                  ref="fileInput"
                  multiple
                  accept="image/*,audio/*,video/*"
                  style="display: none"
                  @change="handleFileSelected"
                />
              </div>
              <div class="action-right">
                <div class="model-selector" v-if="enabledModels.length > 0" @click="toggleModelDropdown">
                  <span class="model-display">{{ currentModel }}</span>
                  <svg class="model-dropdown-icon" width="12" height="12" viewBox="0 0 12 12" fill="none">
                    <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <div class="model-dropdown" v-show="showModelDropdown" @click.stop>
                    <div 
                      v-for="model in enabledModels" 
                      :key="model.id" 
                      class="model-option"
                      :class="{ active: model.id === currentModelId }"
                      @click="selectModel(model)"
                    >
                      {{ model.modelId }}
                    </div>
                  </div>
                </div>
                <a-button 
                  v-if="!isStreaming"
                  type="primary" 
                  class="send-button"
                  :disabled="!inputMessage.trim() || enabledModels.length === 0"
                  @click="sendMessage"
                  :title="enabledModels.length === 0 ? '请先配置模型' : '发送'"
                >
                  <component :is="IconArrowUp" />
                </a-button>
                <a-button 
                  v-else
                  type="danger" 
                  class="stop-button"
                  @click="stopGeneration"
                >
                  <div class="stop-icon"></div>
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useChatStore, _extractResultText } from '../stores/chat'
import { api } from '../api'
import { marked } from 'marked'
import { 
  IconPlus, 
  IconArrowUp,
  IconStop,
  IconSettings
} from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import Settings from './Settings.vue'
import Skills from './Skills.vue'
import Memories from './Memories.vue'

const chatStore = useChatStore()
// 用 storeToRefs 保持响应性
const { messages, isStreaming, currentChatId, currentSessionId } = storeToRefs(chatStore)
const { upsertToolCall, toggleToolCall, applyReasoningChunk, applyFinalReasoning, applyToolResult, resetStreamState } = chatStore
const inputMessage = ref('')
const sessionList = ref([])
const currentTitle = ref('')
const messagesContainer = ref(null)
const activeNav = ref('new-task')
const isChatting = ref(false)
const showSidebar = ref(true)
const settingsRef = ref(null)

// Tooltip 状态
const tooltipVisible = ref(false)
const tooltipText = ref('')
const tooltipStyle = ref({})

// 小导航条
const messageRefs = ref({})
const activeMessageIndex = ref(0)
let observer = null

const initObserver = () => {
  if (observer) observer.disconnect()
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const idx = parseInt(entry.target.dataset.index)
        if (!isNaN(idx)) activeMessageIndex.value = idx
      }
    })
  }, { root: messagesContainer.value, threshold: 0.3 })
  Object.entries(messageRefs.value).forEach(([idx, el]) => {
    if (el) {
      el.dataset.index = idx
      observer.observe(el)
    }
  })
}

const scrollToMessage = (index) => {
  const el = messageRefs.value[index]
  if (el && messagesContainer.value) {
    messagesContainer.value.scrollTo({ top: el.offsetTop - 16, behavior: 'smooth' })
    activeMessageIndex.value = index
  }
}

watch(() => messages.value.length, async () => {
  await nextTick()
  initObserver()
})

// 当前选择的模型名称，从数据库获取
const currentModel = ref('')
const currentModelId = ref(null)
const enabledModels = ref([])
const showModelDropdown = ref(false)

// 加载会话列表
const loadSessionList = async () => {
  try {
    const res = await api.getSessionList({ pageNum: 1, pageSize: 50 })
    sessionList.value = res.data.rows || []
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 加载启用的模型列表
const loadEnabledModels = async () => {
  try {
    const res = await api.getModelConfigList()
    enabledModels.value = res.data || []
    
    // 如果有模型，从数据库获取当前激活的模型
    if (enabledModels.value.length > 0) {
      try {
        // 获取激活的模型
        const activeRes = await api.getActiveModelConfig()
        const activeModel = activeRes.data
        
        if (activeModel) {
          currentModelId.value = activeModel.id
          currentModel.value = activeModel.modelId
        } else {
          // 如果没有激活的模型，使用第一个
          const firstModel = enabledModels.value[0]
          currentModelId.value = firstModel.id
          currentModel.value = firstModel.modelId
        }
      } catch (error) {
        // 获取失败，使用第一个模型
        const firstModel = enabledModels.value[0]
        currentModelId.value = firstModel.id
        currentModel.value = firstModel.modelId
      }
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

// 模型切换
const selectModel = async (model) => {
  // 先关闭下拉框
  showModelDropdown.value = false
  
  // 如果选择的是当前模型，不做任何操作
  if (model.id === currentModelId.value) {
    return
  }
  
  // 调用接口切换到选中的模型
  try {
    await api.activateModelConfig(model.id)
    currentModel.value = model.modelId
    currentModelId.value = model.id
    Message.success(`已切换到 ${model.modelId}`)
    await loadEnabledModels()
  } catch (error) {
    Message.error('切换模型失败')
  }
}

// 切换下拉框显示
const toggleModelDropdown = () => {
  showModelDropdown.value = !showModelDropdown.value
}

// 打开设置弹窗
const openSettings = () => {
  settingsRef.value?.open()
}

// 当前视图：'chat' | 'skills'
const currentView = ref('chat')

// 跳转到技能页面
const goToSkills = () => {
  currentView.value = 'skills'
}

// 跳转到记忆页面
const goToMemories = () => {
  currentView.value = 'memories'
}

// 创建新聊天
const handleNewTask = async () => {
  currentView.value = 'chat'
  chatStore.clearMessages()
  chatStore.setCurrentSession('')
  currentTitle.value = ''
  inputMessage.value = ''
  isChatting.value = false
  activeNav.value = 'new-task'
}

// 选择会话
const selectSession = async (session) => {
  currentView.value = 'chat'
  try {
    const res = await api.getSessionDetails(session.chatId)
    chatStore.setCurrentSession(session.chatId, session.chatId)
    currentTitle.value = session.title
    isChatting.value = true

    chatStore.clearMessages()
    if (!res.data.messages) return

    for (const msg of res.data.messages) {
      const role = (msg.role || '').toUpperCase()
      const contentBlocks = Array.isArray(msg.content) ? msg.content : []

      if (role === 'USER') {
        const text = extractMessageText(msg)
        // 提取多媒体内容块，还原气泡内展示
        const mediaItems = extractMessageMedia(msg)
        if (text || mediaItems.length > 0) {
          chatStore.addMessage({
            role: 'user',
            content: text,
            mediaItems: mediaItems.length > 0 ? mediaItems : undefined
          })
        }

      } else if (role === 'ASSISTANT') {
        const text = extractMessageText(msg)
        const thinking = extractMessageThinking(msg)
        // 提取 tool_use blocks 重建工具调用列表
        const toolCalls = contentBlocks
          .filter(b => b.type === 'tool_use' && b.id)
          .map(b => ({
            id: b.id,
            name: b.name,
            input: b.input,
            collapsed: true,  // 历史默认折叠
            status: 'done'
          }))
        chatStore.addMessage({
          role: 'assistant',
          content: text || '',
          thinking: thinking || '',
          thinkingDone: true,
          thinkingCollapsed: true,
          toolCalls: toolCalls.length ? toolCalls : undefined,
          toolCallsCollapsed: toolCalls.length ? true : undefined
        })

      } else if (role === 'TOOL') {
        // 将工具结果回填到上一条 ASSISTANT 消息的 toolCalls 中
        const msgs = chatStore.messages
        const lastAssistant = [...msgs].reverse().find(m => m.role === 'assistant')
        if (lastAssistant && lastAssistant.toolCalls) {
          for (const block of contentBlocks) {
            if (block.type === 'tool_result') {
              const tool = lastAssistant.toolCalls.find(t => t.id === (block.id || block.toolUseId))
              if (tool) {
                tool.result = _extractResultText(block)
                tool.status = 'done'
              }
            }
          }
        }
      }
    }
  } catch (error) {
    Message.error('加载会话失败')
  }
}

// 停止生成
const stopGeneration = async () => {
  if (!currentSessionId.value) return
  try {
    await api.stopSession(currentSessionId.value)
    Message.success('已停止生成')
  } catch (e) {
    Message.error('停止失败')
  }
  chatStore.setStreaming(false)
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isStreaming.value) return
  
  // 检查是否配置了模型
  if (enabledModels.value.length === 0) {
    Message.warning('当前没有可用模型，请先配置模型')
    return
  }

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  isChatting.value = true

  // 将已选文件上传到服务器，拿到路径后构建 mediaItems
  let mediaItems = []
  const filesToSend = [...selectedFiles.value]
  if (filesToSend.length > 0) {
    try {
      mediaItems = await Promise.all(
        filesToSend.map(async (item) => {
          const res = await api.uploadMediaFile(item.file)
          return {
            mediaType: item.file.type,
            filePath: res.data,          // 后端保存后返回的服务器绝对路径
            name: item.file.name,
            previewUrl: item.previewUrl  // 仅前端展示用，不发到后端
          }
        })
      )
    } catch (e) {
      Message.error('文件上传失败,请重试')
      return
    }
    // 清空预览区
    selectedFiles.value = []
  }

  // 添加用户消息（包含 mediaItems用于气泡内展示）
  chatStore.addMessage({
    role: 'user',
    content: userMessage,
    mediaItems: mediaItems.length > 0 ? mediaItems : undefined
  })

  // 添加加载占位（第一个 REASONING 到来前会被复用）
  chatStore.addMessage({
    role: 'assistant',
    content: '',
    loading: true
  })

  // 重置流式状态（确保上一次的 messageId 不会干扰本次）
  resetStreamState()
  chatStore.setStreaming(true)

  try {
    // 如果没有会话ID，先创建会话
    if (!currentSessionId.value) {
      const sessionRes = await api.createSession({ message: userMessage })
      chatStore.setCurrentSession(sessionRes.data)
      // 立即设置临时标题并加入左侧列表
      currentTitle.value = '新对话'
      sessionList.value.unshift({ chatId: sessionRes.data, title: '新对话' })
      // 立即异步生成真实标题（不需要等 chat 完成）
      api.generateTitle(sessionRes.data)
        .then(res => {
          currentTitle.value = res.data
          const item = sessionList.value.find(s => s.chatId === sessionRes.data)
          if (item) item.title = res.data
        })
        .catch(e => console.error('生成标题失败:', e))
    }

    // 构建请求体（去除 previewUrl，不发到后端）
    const requestBody = {
      query: userMessage,
      sessionId: currentSessionId.value
    }
    if (mediaItems.length > 0) {
      requestBody.mediaItems = mediaItems.map(({ mediaType, filePath, name }) => ({ mediaType, filePath, name }))
    }

    // 流式请求
    const response = await fetch('/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        chatStore.setStreaming(false)
        scrollToBottom()
        break
      }

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine) continue

        // 处理 SSE data: 前缀
        let data = trimmedLine
        if (trimmedLine.startsWith('data:')) {
          data = trimmedLine.slice(5).trim()
        }
        if (!data || data === '[START]') continue

        if (data === '[DONE]') {
          chatStore.setStreaming(false)
          scrollToBottom()
          continue
        }

        try {
          const parsed = JSON.parse(data)
          const eventType = parsed.type          // REASONING | TOOL_RESULT | ERROR
          const isLast = parsed.isLast === true   // 注意：字段名是 isLast
          const messageId = parsed.messageId
          const msg = parsed.message

          // 处理错误消息 - 必须在 msg 检查之前
          if (eventType === 'error' || parsed.type === 'error') {
            const errorMessage = parsed.content || parsed.message?.content || '未知错误'
            chatStore.setStreaming(false)
            
            // 分析错误并提供友好的提示
            let errorTitle = '对话出错'
            let errorDetail = errorMessage
            let suggestions = []
            
            if (errorMessage.includes('API key') || errorMessage.includes('authentication')) {
              errorTitle = 'API密钥错误'
              errorDetail = '模型API密钥无效或已过期'
              suggestions = ['检查设置中的API密钥是否正确', '确认API密钥是否已过期']
            } else if (errorMessage.includes('model') && (errorMessage.includes('not found') || errorMessage.includes('invalid'))) {
              errorTitle = '模型配置错误'
              errorDetail = '模型ID无效或不存在'
              suggestions = ['检查设置中的模型ID是否正确', '确认该模型是否可用']
            } else if (errorMessage.includes('rate limit') || errorMessage.includes('quota')) {
              errorTitle = '请求频率超限'
              errorDetail = 'API请求频率过高或额度已用完'
              suggestions = ['稍后重试', '检查API账户的额度和限制']
            } else if (errorMessage.includes('network') || errorMessage.includes('connection')) {
              errorTitle = '网络连接错误'
              errorDetail = '无法连接到模型服务'
              suggestions = ['检查网络连接', '确认API地址是否正确', '稍后重试']
            } else if (errorMessage.includes('timeout')) {
              errorTitle = '请求超时'
              errorDetail = '模型服务响应超时'
              suggestions = ['检查网络连接', '稍后重试', '尝试简化问题']
            } else {
              suggestions = ['检查网络连接', '确认模型配置是否正确', '稍后重试']
            }
            
            // 在对话框中添加错误消息
            const errorBlock = {
              type: 'text',
              text: `❌ **${errorTitle}**\n\n${errorDetail}\n\n**建议解决方案：**\n${suggestions.map((s, i) => `${i + 1}. ${s}`).join('\n')}`
            }
            
            // 找到当前助手消息并添加错误块
            let assistantMsg = messages.value.find(m => m.role === 'assistant' && m.messageId === messageId)
            
            // 如果没找到，使用最后一条助手消息
            if (!assistantMsg) {
              assistantMsg = messages.value.filter(m => m.role === 'assistant').pop()
            }
            
            if (assistantMsg) {
              if (!assistantMsg.contentBlocks) {
                assistantMsg.contentBlocks = []
              }
              assistantMsg.contentBlocks.push(errorBlock)
              assistantMsg.error = true
              assistantMsg.loading = false  // 停止加载动画
            } else {
              // 如果完全没有助手消息，创建一个
              messages.value.push({
                role: 'assistant',
                messageId: messageId || 'error-' + Date.now(),
                content: '',
                contentBlocks: [errorBlock],
                error: true,
                loading: false
              })
            }
            
            scrollToBottom()
            continue
          }

          if (!msg) continue

          const contentBlocks = Array.isArray(msg.content) ? msg.content : []

          if (eventType === 'REASONING') {
            if (!isLast) {
              // 增量 chunk：单个 block，真正的增量片段
              const block = contentBlocks[0]
              if (block) applyReasoningChunk(messageId, block)
            } else {
              // 最终事件：用完整数据覆盖，确保状态正确
              applyFinalReasoning(messageId, contentBlocks)
            }
            scrollToBottom()
          } else if (eventType === 'TOOL_RESULT' && isLast) {
            applyToolResult(contentBlocks)
            scrollToBottom()
          }
        } catch (e) {
          // 忽略解析错误
        }
      }
    }

  } catch (error) {
    Message.error('发送消息失败')
    chatStore.setStreaming(false)
  }
}

// 主题切换
const isDark = ref(false)
const toggleTheme = () => {
  isDark.value = !isDark.value
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

// 复制状态
const copiedIndex = ref(null)

// 复制文本（直接复制 markdown 原文）
const copyText = async (text, idx) => {
  try {
    await navigator.clipboard.writeText(text)
    copiedIndex.value = idx
    setTimeout(() => { copiedIndex.value = null }, 2000)
  } catch (e) {
    // 备用方案
    const el = document.createElement('textarea')
    el.value = text
    document.body.appendChild(el)
    el.select()
    document.execCommand('copy')
    document.body.removeChild(el)
    copiedIndex.value = idx
    setTimeout(() => { copiedIndex.value = null }, 2000)
  }
}

// 切换深度思考折叠状态
const toggleThinking = (index) => {
  const msg = messages.value[index]
  if (msg) {
    msg.thinkingCollapsed = !msg.thinkingCollapsed
  }
}

// 快速发送消息
const sendQuickMessage = (message) => {
  inputMessage.value = message
  sendMessage()
}

// 文件上传 ref
const fileInput = ref(null)

// 已选择的文件列表：{ file, previewUrl, type: 'image'|'audio'|'video' }
const selectedFiles = ref([])

// 将文件加入选择列表并生成预览
const handleFileSelected = (event) => {
  const files = Array.from(event.target.files)
  files.forEach(file => {
    const prefix = file.type.split('/')[0]
    const type = ['image', 'audio', 'video'].includes(prefix) ? prefix : 'other'
    if (type === 'image') {
      // 图片用 FileReader 生成 data URI，永久有效，不依赖 blob URL 生命周期
      const reader = new FileReader()
      reader.onload = (e) => {
        selectedFiles.value.push({ file, previewUrl: e.target.result, type })
      }
      reader.readAsDataURL(file)
    } else {
      selectedFiles.value.push({ file, previewUrl: '', type })
    }
  })
  // 重置 input，允许重复选同一文件
  event.target.value = ''
}

// 移除单个附件
const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
}

// 提取消息文本（兼容字符串和数组格式）
const extractMessageText = (msg) => {
  if (msg.textContent) return msg.textContent
  if (typeof msg.content === 'string') return msg.content
  if (Array.isArray(msg.content)) {
    const textItem = msg.content.find(c => c.type === 'text')
    return textItem ? textItem.text : ''
  }
  return ''
}

// 提取深度思考内容
const extractMessageThinking = (msg) => {
  if (Array.isArray(msg.content)) {
    const thinkingItem = msg.content.find(c => c.type === 'thinking')
    return thinkingItem ? thinkingItem.thinking : ''
  }
  return ''
}

// 从历史消息中提取多媒体内容块（图像/音频/视频）
const extractMessageMedia = (msg) => {
  if (!Array.isArray(msg.content)) return []
  return msg.content
    .filter(c => ['image', 'audio', 'video'].includes(c.type))
    .map(c => {
      const source = c.source || {}
      const mediaType = source.mediaType || source.media_type || ''
      // Base64 格式构建 data URI 用于展示
      const previewUrl = (source.type === 'base64' && mediaType.startsWith('image'))
        ? `data:${mediaType};base64,${source.data}`
        : ''
      return {
        mediaType,
        name: '',
        previewUrl
      }
    })
}

// 判断是否为最后一条 assistant 消息
const isLastAssistantMessage = (index) => {
  const msgs = messages.value
  for (let i = msgs.length - 1; i >= 0; i--) {
    if (msgs[i].role === 'assistant') {
      return i === index
    }
  }
  return false
}

// 截断标题到 15 个字
const truncateTitle = (title) => {
  if (!title) return '新对话'
  return title.length > 15 ? title.slice(0, 15) + '...' : title
}

// 显示小导航条 tooltip（CSS 控制宽度截断）
const showMinimapTooltip = (event, msg) => {
  tooltipText.value = msg.content || ''
  tooltipVisible.value = true
  const rect = event.currentTarget.getBoundingClientRect()
  tooltipStyle.value = {
    top: `${rect.top + rect.height / 2}px`,
    left: `${rect.left - 12}px`,
    transform: 'translateY(-50%) translateX(-100%)'
  }
}

// 隐藏 tooltip
const hideTooltip = () => {
  tooltipVisible.value = false
}

// 格式化工具输入参数（不显示外层 {} 包裹，过滤 @type 等 Java 内部字段）
const formatToolInput = (input) => {
  if (!input) return ''
  if (typeof input === 'string') return input
  return Object.entries(input)
    .filter(([k]) => !k.startsWith('@'))
    .map(([k, v]) => `${k}: ${typeof v === 'string' ? v : JSON.stringify(v, null, 2)}`)
    .join('\n')
}

// 渲染 Markdown
const renderMarkdown = (content) => {
  if (!content) return ''
  return marked(content)
}

// 滚动到底部（仅在用户已处于底部附近时才滚动，允许向上查看历史）
const scrollToBottom = async () => {
  await nextTick()
  const container = messagesContainer.value
  if (!container) return
  // 判断是否已接近底部（容差 100px）
  const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 100
  if (isNearBottom) {
    container.scrollTop = container.scrollHeight
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadSessionList()
  loadEnabledModels()
  
  // 监听模型配置更新事件
  const handleModelConfigUpdated = async () => {
    await loadEnabledModels()
  }
  window.addEventListener('modelConfigUpdated', handleModelConfigUpdated)
  
  // 点击外部关闭下拉框
  document.addEventListener('click', (e) => {
    const modelSelector = e.target.closest('.model-selector')
    if (!modelSelector) {
      showModelDropdown.value = false
    }
  })
})
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: #f0f0f0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
  padding: 4px;
  gap: 10px;
}

/* ===== 左侧边栏 ===== */
.sidebar {
  width: 260px;
  min-width: 260px;
  background: #ffffff;
  border: 1px solid #ebebeb;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  z-index: 10;
  overflow: hidden;
}

/* Logo 区域 */
.logo-section {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.3px;
}

/* 导航菜单 */
.nav-menu {
  padding: 12px 12px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  color: #555;
  font-weight: 500;
  position: relative;
}

.nav-item:hover {
  background: #f5f7fa;
  color: #333;
}

.nav-item.active {
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.25);
}

.nav-icon {
  font-size: 18px;
  display: flex;
  align-items: center;
}

.beta-badge {
  position: absolute;
  right: 10px;
  background: #e3f2fd;
  color: #1976D2;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 600;
}

/* Tab 切换 */
.tab-switcher {
  display: flex;
  margin: 16px 12px 8px;
  background: #f5f5f5;
  border-radius: 10px;
  padding: 4px;
  gap: 4px;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #888;
  transition: all 0.25s ease;
  font-weight: 500;
}

.tab-item:hover {
  color: #555;
}

.tab-item.active {
  background: white;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 任务列表 */
.task-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 12px;
}

.task-group-label {
  font-size: 11px;
  color: #aaa;
  font-weight: 500;
  padding: 4px 14px 6px;
  letter-spacing: 0.5px;
}

.task-item {
  padding: 7px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 2px;
}

.task-item:hover {
  background: #f0f0f0;
}

.task-item.active {
  background: #e8e8e8;
}

.task-title {
  font-size: 13px;
  color: #333;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 自定义 tooltip */
.custom-tooltip {
  position: fixed;
  background: #1a1a1a;
  color: #fff;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 13px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  pointer-events: none;
  z-index: 9999;
}

/* 版本信息 */
.version-section {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
}

.version-text {
  font-size: 11px;
  color: #bbb;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.settings-icon {
  color: #bbb;
  cursor: pointer;
  font-size: 16px;
  transition: color 0.2s;
}

.settings-icon:hover {
  color: #666;
}

/* ===== 主内容区域 ===== */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #ebebeb;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 技能视图容器 */
.skills-view-container {
  flex: 1;
  overflow: hidden;
  background: #ffffff;
}

/* 记忆视图容器 */
.memories-view-container {
  flex: 1;
  overflow: hidden;
  background: #ffffff;
}

/* 聊天视图容器 */
.chat-view-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #ffffff;
}

/* 顶部栏 */
.top-bar {
  height: 48px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-radius: 16px 16px 0 0;
}

.header-left {
  width: 200px;
}

.menu-icon {
  font-size: 20px;
  color: #666;
  cursor: pointer;
}

.header-center {
  flex: 1;
  text-align: center;
}

.page-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.header-right {
  width: 200px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.feedback-btn {
  color: #666;
  font-size: 13px;
}

.feedback-btn:hover {
  color: #333;
}

.theme-btn {
  color: #666;
}

/* ===== 欢迎页面 ===== */
.welcome-page {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow-y: auto;
  padding: 40px 20px;
}

.welcome-content {
  text-align: center;
  max-width: 680px;
  width: 100%;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-logo {
  margin-bottom: 24px;
}

.logo-circle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.15);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(76, 175, 80, 0.15);
  }
  50% {
    box-shadow: 0 8px 32px rgba(76, 175, 80, 0.25);
  }
}

.welcome-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.welcome-subtitle {
  font-size: 16px;
  color: #888;
  margin-bottom: 40px;
  font-weight: 400;
}

/* 功能卡片 */
.feature-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.feature-card {
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  padding: 28px 24px;
  text-align: left;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
  border-color: #e0e0e0;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  color: #1976D2;
}

.feature-card:nth-child(2) .card-icon {
  background: linear-gradient(135deg, #fce4ec, #f8bbd9);
  color: #c2185b;
}

.feature-card:nth-child(3) .card-icon {
  background: linear-gradient(135deg, #f3e5f5, #e1bee7);
  color: #7b1fa2;
}

.feature-card h3 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.feature-card p {
  font-size: 13px;
  color: #999;
  line-height: 1.5;
}

/* ===== 聊天区域 ===== */
.chat-area-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 8px 24px 16px;
  background: #ffffff;
  scrollbar-width: none;
}

.chat-area::-webkit-scrollbar {
  display: none;
}

/* 右侧小导航条 */
.chat-minimap {
  width: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px 0;
  background: #ffffff;
}

.minimap-dot {
  width: 14px;
  height: 2px;
  border-radius: 2px;
  background: #ddd;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.minimap-dot:hover {
  background: #bbb;
  width: 16px;
}

.minimap-dot.active {
  width: 18px;
  height: 3px;
  background: #4CAF50;
  border-radius: 2px;
}


.message-wrapper {
  margin-bottom: 6px;
  animation: messageSlideIn 0.3s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-container {
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

/* 用户消息靠右 */
.message-wrapper.user .message-container {
  align-items: flex-end;
}

/* AI 消息靠左 */
.message-wrapper.assistant .message-container {
  align-items: flex-start;
}

/* 用户消息包装层 */
.user-bubble-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  max-width: 70%;
}

/* 用户操作栏：hover 才显示 */
.user-actions {
  display: flex;
  justify-content: flex-end;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.15s;
}

.message-wrapper.user:hover .user-actions {
  opacity: 1;
}

.user-bubble {
  background: linear-gradient(135deg, #f5f5f5, #fafafa);
  padding: 14px 20px;
  border-radius: 18px 18px 4px 18px;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  word-break: break-word;
}

.assistant-bubble {
  color: #333;
  line-height: 1.8;
  font-size: 14px;
  max-width: 85%;
  width: 100%;
}

/* AI 消息操作栏，一直显示 */
.assistant-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-top: 4px;
  opacity: 1;
}

/* 通用图标按鈕 */
.action-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: #bbb;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  padding: 0;
}

.action-icon-btn:hover {
  background: #f0f0f0;
  color: #666;
}

.action-icon-btn.copied {
  color: #52c41a;
}

.assistant-bubble :deep(p) {
  margin-bottom: 14px;
}

.assistant-bubble :deep(p:last-child) {
  margin-bottom: 0;
}

.assistant-bubble :deep(code) {
  background: #f5f5f5;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 0.88em;
  font-family: 'SF Mono', 'Fira Code', monospace;
  color: #d73a49;
}

.assistant-bubble :deep(pre):not(.tool-detail-content) {
  background: #1e1e1e;
  padding: 16px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 16px 0;
}

.assistant-bubble :deep(pre):not(.tool-detail-content) code {
  background: transparent;
  padding: 0;
  color: #e6e6e6;
  font-size: 13px;
}

/* 工具调用内容框强制覆盖 pre 的深色样式 */
.assistant-bubble :deep(.tool-detail-content) {
  background: #f7f7f7 !important;
  color: #555 !important;
  padding: 10px 12px !important;
  border-radius: 8px !important;
  border: 1px solid #eeeeee !important;
}

/* 打字指示器 */
.typing-indicator {
  display: inline-flex;
  gap: 6px;
  padding: 16px 20px;
  background: #f9f9f9;
  border-radius: 18px;
  margin-top: 8px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #ccc;
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ===== 输入区域 ===== */
.input-section {
  padding: 0 18px 18px;
  background: white;
  border-radius: 0 0 16px 16px;
}

.input-container {
  max-width: 760px;
  margin: 0 auto;
}

.input-box {
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 24px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
}

.input-box:focus-within {
  background: white;
  border-color: #ddd;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.input-box :deep(.arco-textarea) {
  width: 100%;
}

.input-box :deep(.arco-textarea-wrapper) {
  background: transparent;
  border: none !important;
  box-shadow: none !important;
}

.input-box :deep(.arco-textarea-wrapper:hover) {
  border: none !important;
  box-shadow: none !important;
}

.input-box :deep(.arco-textarea-wrapper.arco-textarea-focus) {
  border: none !important;
  box-shadow: none !important;
}

.input-box :deep(.arco-textarea-wrapper textarea) {
  background: transparent;
  border: none !important;
  box-shadow: none !important;
  font-size: 15px;
  color: #333;
  padding: 4px 8px;
  line-height: 1.6;
  resize: none;
  cursor: text;
  pointer-events: auto;
  min-height: 24px !important;
  height: auto !important;
  outline: none !important;
}

.input-box :deep(.arco-textarea-wrapper textarea:focus) {
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
}

.input-box :deep(.arco-textarea-wrapper textarea::placeholder) {
  color: #bbb;
  font-size: 14px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 0 4px;
}

.action-left {
  display: flex;
  gap: 4px;
}

.action-btn {
  font-size: 13px;
  color: #888;
  padding: 6px 10px;
}

.action-btn:hover {
  color: #555;
  background: #f0f0f0;
}

.action-btn-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  padding: 0;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.action-btn-icon :deep(.arco-icon) {
  font-size: 18px;
  color: #888;
}

.action-btn-icon:hover {
  background: #f0f0f0 !important;
  color: #555;
}

.action-btn-icon:hover :deep(.arco-icon) {
  color: #555;
}

.action-btn-icon :deep(.arco-btn-text) {
  padding: 0;
  background: transparent !important;
  border: none !important;
}

.action-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-selector {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.model-selector:hover {
  background: #f5f5f5;
}

.model-display {
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.model-dropdown-icon {
  color: #999;
  transition: transform 0.2s;
}

.model-dropdown {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 8px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 6px;
  min-width: 160px;
  z-index: 1000;
}

.model-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.15s;
}

.model-option:hover {
  background: #f5f5f5;
}

.model-option.active {
  background: #f0f7f0;
  color: #4CAF50;
  font-weight: 500;
}

.mode-select {
  width: 90px;
}

.send-button {
  width: 32px;
  height: 32px;
  min-width: 32px;
  border-radius: 50%;
  padding: 0;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%) !important;
  border: none !important;
  color: white !important;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-button :deep(.arco-btn) {
  padding: 0 !important;
  background: transparent !important;
  border: none !important;
  color: white !important;
}

.send-button :deep(.arco-icon) {
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white !important;
}

.send-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #43A047 0%, #5DAF5E 100%) !important;
  transform: scale(1.08);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}

.send-button:disabled {
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%) !important;
  opacity: 0.5;
  transform: none;
  box-shadow: none;
  cursor: not-allowed;
}

.send-button :deep(.arco-btn) {
  padding: 0;
  background: transparent;
  border: none;
  color: white;
}

.send-button :deep(.arco-icon) {
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stop-button {
  padding: 0;
  min-width: 32px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5222d 0%, #fa541c 100%) !important;
  border: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(245, 34, 45, 0.3);
}

.stop-icon {
  width: 12px;
  height: 12px;
  background-color: white;
  border-radius: 2px;
}

.stop-button :deep(.arco-btn) {
  padding: 0 !important;
  background: transparent !important;
  border: none !important;
  color: white !important;
}

.stop-button :deep(.arco-icon) {
  color: white !important;
}

.stop-button:hover {
  background: linear-gradient(135deg, #cf1322 0%, #d4380d 100%) !important;
  transform: scale(1.08);
  box-shadow: 0 4px 12px rgba(245, 34, 45, 0.4);
}

.stop-button :deep(.arco-btn) {
  padding: 0;
  background: transparent;
  border: none;
  color: white;
}

.stop-button:hover {
  background: #88d8b0;
}

/* ===== 工具调用样式 ===== */
.tool-calls-block {
  margin-bottom: 12px;
}

.tool-calls-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #888;
  font-size: 13px;
  padding: 2px 0;
}

.tool-calls-header svg {
  flex-shrink: 0;
  color: #aaa;
}

.tool-calls-chevron {
  color: #bbb;
  transform: rotate(-90deg);
  transition: transform 0.22s ease;
  flex-shrink: 0;
}

.tool-calls-chevron.expanded {
  transform: rotate(0deg);
}

.tool-calls-list {
  margin-top: 6px;
  padding-left: 14px;
  border-left: 2px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tool-call-item {
  border-radius: 8px;
  overflow: hidden;
  background: transparent;
}

.tool-call-header {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 8px;
  cursor: pointer;
  font-size: 13px;
  color: #555;
  transition: background 0.15s;
  user-select: none;
  border-radius: 8px;
}

.tool-call-header:hover {
  background: #f5f5f5;
}

.tool-status-icon {
  flex-shrink: 0;
}

.tool-status-icon.spinning {
  animation: thinkingSpin 1.2s linear infinite;
}

.tool-call-name {
  font-size: 13px;
  color: #555;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-call-name code {
  background: transparent;
  color: #444;
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.9em;
  padding: 0;
  font-weight: 500;
}

.tool-call-chevron {
  color: #ccc;
  transform: rotate(-90deg);
  transition: transform 0.22s ease;
  flex-shrink: 0;
}

.tool-call-chevron.expanded {
  transform: rotate(0deg);
}

.tool-call-detail {
  padding: 4px 8px 8px 30px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tool-detail-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tool-detail-label {
  font-size: 11px;
  font-weight: 600;
  color: #aaa;
  text-transform: uppercase;
  letter-spacing: 0.6px;
}

.tool-detail-content {
  font-size: 12.5px;
  color: #555 !important;
  font-family: 'SF Mono', 'Fira Code', monospace;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
  background: #f7f7f7 !important;
  border: 1px solid #eeeeee;
  border-radius: 8px;
  padding: 10px 12px;
  margin: 0;
}

.tool-running-hint {
  font-size: 12px;
  color: #aaa;
  padding: 4px 0;
}

/* ===== 深度思考样式 ===== */
.thinking-block {
  margin-bottom: 12px;
}

.thinking-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  color: #999;
  font-size: 13px;
  padding: 2px 0;
  transition: color 0.2s;
}

.thinking-header:hover {
  color: #666;
}

.thinking-icon-svg {
  color: #aaa;
  flex-shrink: 0;
  transition: color 0.2s;
}

.thinking-header:hover .thinking-icon-svg {
  color: #888;
}

.thinking-icon-svg.spinning {
  animation: thinkingSpin 1.2s linear infinite;
}

@keyframes thinkingSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.thinking-label {
  font-size: 13px;
  color: inherit;
  font-weight: 400;
}

.thinking-chevron {
  color: #bbb;
  transform: rotate(-90deg);
  transition: transform 0.22s ease, color 0.2s;
  flex-shrink: 0;
}

.thinking-chevron.expanded {
  transform: rotate(0deg);
}

/* 内容区域 — 左侧竖线风格 */
.thinking-content {
  margin-top: 8px;
  padding-left: 14px;
  border-left: 2px solid #e0e0e0;
}

.thinking-text {
  font-size: 13px;
  color: #888;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 展开动画 */
.thinking-expand-enter-active,
.thinking-expand-leave-active {
  transition: opacity 0.2s ease, max-height 0.25s ease;
  overflow: hidden;
}

.thinking-expand-enter-from,
.thinking-expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.thinking-expand-enter-to,
.thinking-expand-leave-from {
  opacity: 1;
  max-height: 9999px;
}

/* ===== 媒体附件预览区（输入框上方） ===== */
.file-preview-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 4px 10px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 8px;
}

.preview-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
}

.preview-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #e8e8e8;
  display: block;
}

.preview-file-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 12px;
  color: #666;
  max-width: 160px;
}

.preview-file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 110px;
}

.preview-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #555;
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  opacity: 0;
  transition: opacity 0.15s;
  z-index: 2;
}

.preview-item:hover .preview-remove {
  opacity: 1;
}

/* ===== 消息气泡内媒体展示 ===== */
.user-media-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.msg-image-thumb {
  display: block;
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid rgba(0,0,0,0.06);
}

/* 历史消息图片占位符（无 previewUrl 时显示） */
.msg-image-placeholder {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1px solid rgba(0,0,0,0.08);
  background: rgba(0,0,0,0.03);
  color: #aaa;
  font-size: 11px;
}

.msg-media-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(0,0,0,0.05);
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 12px;
  color: #555;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: -260px;
    height: 100vh;
    transition: left 0.3s ease;
  }
  
  .sidebar.show {
    left: 0;
  }
  
  .feature-cards {
    grid-template-columns: 1fr;
  }
}

/* 错误消息样式 */
.error-message-block {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border: 1px solid #ffcdd2;
  border-radius: 12px;
  padding: 16px;
  margin: 8px 0;
}

.error-content {
  color: #d32f2f;
  line-height: 1.6;
}

.error-content :deep(h1),
.error-content :deep(h2),
.error-content :deep(h3) {
  color: #c62828;
  margin: 12px 0 8px 0;
}

.error-content :deep(p) {
  margin: 8px 0;
}

.error-content :deep(ul),
.error-content :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.error-content :deep(li) {
  margin: 4px 0;
}

</style>
