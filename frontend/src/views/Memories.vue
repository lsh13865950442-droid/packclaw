<template>
  <div class="memories-container">
    <!-- 顶部标题栏 -->
    <div class="memories-header">
      <div class="header-left">
        <h1 class="page-title">记忆管理</h1>
        <p class="page-desc">查看和管理 AI 助手的记忆与知识</p>
      </div>
    </div>

    <!-- 记忆内容区域 -->
    <div class="memories-content">
      <!-- 左侧记忆类型列表 -->
      <div class="memory-types">
        <div 
          v-for="type in memoryTypes" 
          :key="type.key"
          class="memory-type-item"
          :class="{ active: activeMemoryType === type.key }"
          @click="selectMemoryType(type.key)"
        >
          <div class="type-icon">
            <component :is="type.icon" />
          </div>
          <div class="type-info">
            <div class="type-name">{{ type.name }}</div>
            <div class="type-desc">{{ type.description }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧记忆内容显示 -->
      <div class="memory-viewer">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <a-spin size="large" />
          <p>加载中...</p>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="error-state">
          <icon-info-circle :size="48" style="color: #f53f3f" />
          <p>{{ error }}</p>
          <a-button type="primary" @click="loadMemoryContent">重试</a-button>
        </div>

        <!-- 记忆内容 -->
        <div v-else class="memory-content-wrapper">
          <!-- 每日记忆文件选择器 -->
          <div v-if="activeMemoryType === 'daily' && dailyMemoryFiles.length > 0" class="daily-selector">
            <a-select 
              v-model="selectedDailyFile" 
              @change="loadSelectedDailyMemory"
              style="width: 240px"
            >
              <a-option 
                v-for="file in dailyMemoryFiles" 
                :key="file" 
                :value="file"
              >
                {{ file }}
              </a-option>
            </a-select>
          </div>

          <!-- MEMORY.md - 只读 -->
          <div v-if="activeMemoryType === 'memory'" class="readonly-notice">
            <icon-lock :size="14" />
            <span>此文件为只读,由系统自动维护</span>
          </div>

          <!-- 每日记忆 - 只读 -->
          <div v-if="activeMemoryType === 'daily'" class="readonly-notice">
            <icon-lock :size="14" />
            <span>每日记忆为只读,由系统自动维护</span>
          </div>

          <!-- 内容编辑器/查看器 -->
          <div class="content-area">
            <!-- 可编辑模式 -->
            <a-textarea
              v-if="activeMemoryType === 'agents' || activeMemoryType === 'knowledge'"
              v-model="memoryContent"
              :auto-size="{ minRows: 20, maxRows: 50 }"
              class="memory-editor"
              placeholder="在此编辑内容..."
            />
            
            <!-- 只读模式 -->
            <pre v-else><code>{{ memoryContent }}</code></pre>
          </div>

          <!-- 操作按钮 -->
          <div v-if="activeMemoryType === 'agents' || activeMemoryType === 'knowledge'" class="action-buttons">
            <a-button 
              type="primary" 
              :loading="saving"
              @click="saveMemoryContent"
            >
              <template #icon>
                <icon-save />
              </template>
              保存修改
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api/index.js'
import { Message } from '@arco-design/web-vue'
import { 
  IconInfoCircle, IconLock, IconSave,
  IconFile, IconCalendar, IconBook, IconMindMapping
} from '@arco-design/web-vue/es/icon'

const memoryTypes = [
  {
    key: 'memory',
    name: 'MEMORY.md',
    description: '系统 consolidated 记忆',
    icon: IconMindMapping
  },
  {
    key: 'daily',
    name: '每日记忆',
    description: '按日期存储的对话记忆',
    icon: IconCalendar
  },
  {
    key: 'agents',
    name: 'AGENTS.md',
    description: 'Agent 行为准则与能力',
    icon: IconFile
  },
  {
    key: 'knowledge',
    name: 'KNOWLEDGE.md',
    description: '领域知识文档',
    icon: IconBook
  }
]

const activeMemoryType = ref('memory')
const memoryContent = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref('')

// 每日记忆文件列表
const dailyMemoryFiles = ref([])
const selectedDailyFile = ref('')

// 选择记忆类型
const selectMemoryType = async (type) => {
  activeMemoryType.value = type
  await loadMemoryContent()
}

// 加载选择的每日记忆
const loadSelectedDailyMemory = async () => {
  if (!selectedDailyFile.value) return
  
  loading.value = true
  try {
    const res = await api.readDailyMemory(selectedDailyFile.value)
    memoryContent.value = res.data
  } catch (err) {
    error.value = '加载失败: ' + err.message
    Message.error(error.value)
  } finally {
    loading.value = false
  }
}

// 加载记忆内容
const loadMemoryContent = async () => {
  loading.value = true
  error.value = ''
  
  try {
    if (activeMemoryType.value === 'memory') {
      // 加载 MEMORY.md
      const res = await api.readMemoryFile('MEMORY.md')
      memoryContent.value = res.data
    } else if (activeMemoryType.value === 'agents') {
      // 加载 AGENTS.md
      const res = await api.readMemoryFile('AGENTS.md')
      memoryContent.value = res.data
    } else if (activeMemoryType.value === 'knowledge') {
      // 加载 KNOWLEDGE.md
      const res = await api.readMemoryFile('knowledge/KNOWLEDGE.md')
      memoryContent.value = res.data
    } else if (activeMemoryType.value === 'daily') {
      // 加载每日记忆列表
      const res = await api.listDailyMemories()
      dailyMemoryFiles.value = res.data || []
      if (dailyMemoryFiles.value.length > 0) {
        selectedDailyFile.value = dailyMemoryFiles.value[0]
        const contentRes = await api.readDailyMemory(selectedDailyFile.value)
        memoryContent.value = contentRes.data
      } else {
        memoryContent.value = '暂无每日记忆'
      }
    }
  } catch (err) {
    error.value = '加载失败: ' + err.message
    Message.error(error.value)
  } finally {
    loading.value = false
  }
}

// 保存记忆内容
const saveMemoryContent = async () => {
  if (!memoryContent.value.trim()) {
    Message.warning('内容不能为空')
    return
  }

  saving.value = true
  try {
    let filePath = ''
    if (activeMemoryType.value === 'agents') {
      filePath = 'AGENTS.md'
    } else if (activeMemoryType.value === 'knowledge') {
      filePath = 'knowledge/KNOWLEDGE.md'
    }

    await api.saveMemoryFile(filePath, memoryContent.value)
    Message.success('保存成功')
  } catch (err) {
    Message.error('保存失败: ' + err.message)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadMemoryContent()
})
</script>

<style scoped>
.memories-container {
  padding: 24px 32px;
  background: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 顶部标题栏 */
.memories-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-shrink: 0;
}

.header-left {
  flex: 1;
}

.page-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.page-desc {
  margin: 0;
  color: #999;
  font-size: 14px;
}

/* 记忆内容区域 */
.memories-content {
  flex: 1;
  display: flex;
  gap: 24px;
  overflow: hidden;
}

/* 左侧记忆类型列表 */
.memory-types {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}

.memory-type-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
}

.memory-type-item:hover {
  background: #f5f7fa;
  border-color: #c9cdd4;
}

.memory-type-item.active {
  background: #f0f7f0;
  border-color: #4CAF50;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.15);
}

.type-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  font-size: 20px;
  color: #333;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.memory-type-item.active .type-icon {
  background: #4CAF50;
  color: #fff;
}

.type-info {
  flex: 1;
  min-width: 0;
}

.type-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #1a1a1a;
}

.type-desc {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右侧记忆查看器 */
.memory-viewer {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 24px;
  background: #fafafa;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 60px 20px;
  color: #999;
}

.readonly-notice {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fff7e6;
  border: 1px solid #ffe7ba;
  border-radius: 6px;
  color: #fa8c16;
  font-size: 13px;
  margin-bottom: 16px;
}

.daily-selector {
  margin-bottom: 16px;
}

.content-area {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e8e8e8;
  transition: border-color 0.2s ease;
}

.content-area:focus-within {
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.memory-editor {
  width: 100%;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
}

/* 彻底移除 Arco Design textarea 的所有边框 */
.memory-editor.arco-textarea-wrapper,
.memory-editor :deep(.arco-textarea-wrapper) {
  background: transparent !important;
  border: none !important;
  border-color: transparent !important;
  box-shadow: none !important;
  outline: none !important;
  padding: 0 !important;
  margin: 0 !important;
}

.memory-editor.arco-textarea-focus,
.memory-editor.arco-textarea-wrapper:hover,
.memory-editor :deep(.arco-textarea-wrapper:hover),
.memory-editor :deep(.arco-textarea-wrapper.arco-textarea-focus) {
  border: none !important;
  border-color: transparent !important;
  box-shadow: none !important;
  outline: none !important;
}

.memory-editor :deep(textarea),
.memory-editor :deep(.arco-textarea-inner),
.memory-editor :deep(.arco-textarea-inner-wrapper) {
  background: transparent !important;
  border: none !important;
  border-color: transparent !important;
  box-shadow: none !important;
  outline: none !important;
  padding: 0 !important;
  margin: 0 !important;
}

/* 针对 textarea 元素本身的强制覆盖 */
.memory-editor :deep(textarea:focus),
.memory-editor :deep(textarea:active) {
  border: none !important;
  border-color: transparent !important;
  box-shadow: none !important;
  outline: none !important;
}

.content-area pre {
  margin: 0;
  padding: 0;
  background: transparent;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.content-area code {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  color: #333;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.action-buttons .arco-btn {
  min-width: 120px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  border: none;
  color: white;
  padding: 8px 20px;
}

.action-buttons .arco-btn:hover {
  background: linear-gradient(135deg, #43A047 0%, #5DAF5E 100%);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
  transform: translateY(-1px);
}
</style>
