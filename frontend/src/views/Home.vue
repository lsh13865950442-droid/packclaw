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
          <el-icon class="nav-icon"><Plus /></el-icon>
          <span>新任务</span>
        </div>
        <div class="nav-item">
          <el-icon class="nav-icon"><Tools /></el-icon>
          <span>技能</span>
        </div>
        <div class="nav-item">
          <el-icon class="nav-icon"><Clock /></el-icon>
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
          <div class="task-title">{{ session.title || '新对话' }}</div>
        </div>
      </div>

      <!-- 用户信息 -->
      <div class="user-section">
        <div class="user-avatar">
          <el-avatar :size="36" class="avatar-img">U</el-avatar>
        </div>
        <div class="user-details">
          <div class="user-name">shihong liu</div>
          <div class="user-plan">Pro Plan</div>
        </div>
        <el-icon class="settings-icon"><Setting /></el-icon>
      </div>
    </aside>

    <!-- 中间主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="top-bar">
        <div class="header-left">
          <el-icon class="menu-icon" v-if="!showSidebar"><Expand /></el-icon>
        </div>
        <div class="header-center">
          <span class="page-title">{{ currentTitle || '' }}</span>
        </div>
        <div class="header-right">
          <el-button text class="theme-btn" @click="toggleTheme" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
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
          </el-button>
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
                <el-icon :size="24"><FolderOpened /></el-icon>
              </div>
              <h3>文件整理</h3>
              <p>智能整理和管理本地文件</p>
            </div>
            <div class="feature-card" @click="sendQuickMessage('帮我写一篇文章')">
              <div class="card-icon">
                <el-icon :size="24"><Document /></el-icon>
              </div>
              <h3>内容创作</h3>
              <p>创作演示文稿、文档和多媒体内容</p>
            </div>
            <div class="feature-card" @click="sendQuickMessage('帮我分析这份数据')">
              <div class="card-icon">
                <el-icon :size="24"><DataAnalysis /></el-icon>
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
              <div class="user-bubble">{{ msg.content }}</div>
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

              <!-- 正式回答内容 -->
              <div v-if="msg.content" v-html="renderMarkdown(msg.content)"></div>

              <!-- 加载指示器 -->
              <div v-if="msg.loading && !msg.thinking && !msg.content" class="typing-indicator">
                <span></span><span></span><span></span>
              </div>

              <!-- AI 操作栏，一直显示 -->
              <div v-if="msg.content && !msg.loading" class="assistant-actions">
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
              @click="scrollToMessage(index)"
            ></div>
          </template>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-section">
        <div class="input-container">
          <div class="input-box">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 6 }"
              placeholder="描述任务，/ 快捷调用，@ 添加上下文，标准模式经济高效"
              @keydown.enter.exact.prevent="sendMessage"
              resize="none"
              class="message-input"
            />
            <div class="input-actions">
              <div class="action-left">
                <el-button text class="action-btn" @click="openFolderSelect">
                  <el-icon><Folder /></el-icon>
                  <span>选择工作目录</span>
                </el-button>
                <input 
                  type="file" 
                  ref="folderInput" 
                  webkitdirectory 
                  directory 
                  style="display: none" 
                  @change="handleFolderSelected"
                />
                <el-button text class="action-btn-icon" @click="triggerFolderSelect">
                  <el-icon><Plus /></el-icon>
                </el-button>
              </div>
              <div class="action-right">
                <span class="model-name">{{ currentModel }}</span>
                <el-button 
                  type="primary" 
                  class="send-button"
                  :disabled="!inputMessage.trim() || isStreaming"
                  @click="sendMessage"
                  :loading="isStreaming"
                >
                  <el-icon><Top /></el-icon>
                </el-button>
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
import { useChatStore } from '../stores/chat'
import { api } from '../api'
import { marked } from 'marked'
import { 
  Plus, Tools, Clock,
  QuestionFilled, Sunny, FolderOpened, Document, DataAnalysis,
  Folder, Top, Setting, Expand
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const chatStore = useChatStore()
// 用 storeToRefs 保持响应性
const { messages, isStreaming, currentChatId, currentSessionId } = storeToRefs(chatStore)
const inputMessage = ref('')
const sessionList = ref([])
const currentTitle = ref('')
const messagesContainer = ref(null)
const folderInput = ref(null)
const activeNav = ref('new-task')
const isChatting = ref(false)
const showSidebar = ref(true)

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

// 当前选择的模型名称，后续从设置中读取
const currentModel = ref('qwen3-max')

// 加载会话列表
const loadSessionList = async () => {
  try {
    const res = await api.getSessionList({ pageNum: 1, pageSize: 50 })
    sessionList.value = res.data.rows || []
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 创建新聊天
const handleNewTask = async () => {
  chatStore.clearMessages()
  chatStore.setCurrentSession('')
  currentTitle.value = ''
  inputMessage.value = ''
  isChatting.value = false
  activeNav.value = 'new-task'
}

// 选择会话
const selectSession = async (session) => {
  try {
    const res = await api.getSessionDetails(session.chatId)
    chatStore.setCurrentSession(session.chatId, session.chatId)
    currentTitle.value = session.title
    isChatting.value = true
    
    // 加载历史消息
    chatStore.clearMessages()
    if (res.data.messages) {
      res.data.messages.forEach(msg => {
        if (msg.role === 'USER') {
          // 用户消息
          const text = msg.textContent || (msg.content && msg.content[0] && msg.content[0].text) || ''
          chatStore.addMessage({ role: 'user', content: text })
        } else {
          // AI 消息：从 content 数组里分别提取 thinking 和 text
          const contentArr = msg.content || []
          const thinkingItem = contentArr.find(c => c.type === 'thinking')
          const textItem = contentArr.find(c => c.type === 'text')
          const thinking = thinkingItem ? thinkingItem.thinking : ''
          const text = msg.textContent || (textItem ? textItem.text : '') || ''
          chatStore.addMessage({
            role: 'assistant',
            content: text,
            thinking: thinking || '',
            thinkingDone: !!thinking,
            thinkingCollapsed: !!thinking  // 历史消息默认折叠
          })
        }
      })
    }
  } catch (error) {
    ElMessage.error('加载会话失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isStreaming.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  isChatting.value = true

  // 添加用户消息
  chatStore.addMessage({
    role: 'user',
    content: userMessage
  })

  // 添加助手消息（加载中）
  chatStore.addMessage({
    role: 'assistant',
    content: '',
    loading: true
  })

  chatStore.setStreaming(true)

  try {
    // 如果没有会话ID，先创建会话
    if (!currentSessionId.value) {
      const sessionRes = await api.createSession({ message: userMessage })
      chatStore.setCurrentSession(sessionRes.data)
    }

    // 流式请求
    const response = await fetch('/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        query: userMessage,
        sessionId: currentSessionId.value
      })
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let fullContent = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()

          if (data === '[START]') continue

          if (data === '[DONE]') {
            chatStore.updateLastMessage(fullContent)
            chatStore.setStreaming(false)
            scrollToBottom()
            // 生成标题（不需要等待）
            if (!currentTitle.value) {
              api.generateTitle(currentSessionId.value)
                .then(res => {
                  currentTitle.value = res.data
                  loadSessionList()
                })
                .catch(e => console.error('生成标题失败:', e))
            }
            continue
          }

          try {
            const parsed = JSON.parse(data)
            const item = parsed.content && parsed.content[0]
            if (!item) continue

            if (item.type === 'thinking' && item.thinking) {
              // 深度思考内容增量
              chatStore.updateLastMessageThinking(item.thinking)
              scrollToBottom()
            } else if (item.type === 'text' && item.text) {
              // 切换到正式回答时，先标记思考完毕
              if (!fullContent) {
                chatStore.finishLastMessageThinking()
              }
              fullContent += item.text
              chatStore.updateLastMessage(fullContent)
              scrollToBottom()
            } else if (parsed.textContent) {
              if (!fullContent) {
                chatStore.finishLastMessageThinking()
              }
              fullContent += parsed.textContent
              chatStore.updateLastMessage(fullContent)
              scrollToBottom()
            }
          } catch (e) {
            // 忽略解析错误
          }
        }
      }
    }

  } catch (error) {
    ElMessage.error('发送消息失败')
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

// 触发文件夹选择
const triggerFolderSelect = () => {
  if (folderInput.value) {
    folderInput.value.click()
  }
}

// 打开文件夹选择（选择工作目录按钮）
const openFolderSelect = () => {
  if (folderInput.value) {
    folderInput.value.click()
  }
}

// 处理文件夹选择
const handleFolderSelected = (event) => {
  const files = event.target.files
  if (files.length > 0) {
    // 获取第一个文件的目录路径
    const filePath = files[0].webkitRelativePath || files[0].name
    const folderPath = filePath.split('/')[0]
    ElMessage.success(`已选择工作目录: ${folderPath}`)
    // TODO: 将工作目录信息发送到后端或存储在状态中
  }
}

// 渲染 Markdown
const renderMarkdown = (content) => {
  if (!content) return ''
  return marked(content)
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
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

/* 用户信息 */
.user-section {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
}

.avatar-img {
  background: linear-gradient(135deg, #FF6B6B, #FF8E53) !important;
  color: white;
  font-weight: 600;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.user-plan {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.settings-icon {
  color: #999;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;
}

.settings-icon:hover {
  color: #333;
  transform: rotate(30deg);
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

.assistant-bubble :deep(pre) {
  background: #1e1e1e;
  padding: 16px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 16px 0;
}

.assistant-bubble :deep(pre code) {
  background: transparent;
  padding: 0;
  color: #e6e6e6;
  font-size: 13px;
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
}

.input-box:focus-within {
  background: white;
  border-color: #ddd;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.input-box :deep(.el-textarea__inner) {
  background: transparent;
  border: none;
  box-shadow: none;
  font-size: 15px;
  color: #333;
  padding: 4px 8px;
  line-height: 1.6;
}

.input-box :deep(.el-textarea__inner::placeholder) {
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
  width: 32px;
  height: 32px;
  border-radius: 50%;
  padding: 0;
  color: #888;
}

.action-btn-icon:hover {
  background: #f0f0f0;
  color: #555;
}

.action-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mode-select {
  width: 90px;
}

.model-name {
  font-size: 13px;
  color: #888;
  font-weight: 500;
  padding: 4px 12px;
  background: #f5f5f5;
  border-radius: 20px;
  cursor: default;
}

.mode-select :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none;
}

.send-button {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  padding: 0;
  background: #1a1a1a;
  border: none;
}

.send-button:hover {
  background: #333;
  transform: scale(1.05);
}

.send-button:disabled {
  background: #ddd;
  transform: none;
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

</style>
