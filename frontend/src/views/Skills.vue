<template>
  <div class="skills-container">
    <!-- 顶部标题栏 -->
    <div class="skills-header">
      <div class="header-left">
        <h1 class="page-title">技能管理</h1>
        <p class="page-desc">安装和管理 Agent 技能，增强 AI 助手能力</p>
      </div>
      <div class="header-right">
        <a-button 
          class="install-btn"
          @click="showInstallDialog"
        >
          <template #icon>
            <icon-plus />
          </template>
          安装技能
        </a-button>
      </div>
    </div>

    <!-- 技能列表 -->
    <div class="skills-list">
      <!-- 空状态 -->
      <a-empty v-if="skillList.length === 0" description="暂无技能，点击右上角安装技能">
        <template #icon>
          <icon-tool />
        </template>
      </a-empty>

      <!-- 技能卡片列表 -->
      <div v-else class="skill-items">
        <div 
          v-for="skill in skillList" 
          :key="skill.id"
          class="skill-item"
          :class="{ 'skill-item-hovered': hoveredSkillId === skill.id }"
          @click="handleViewSkill(skill)"
          @mouseover="hoveredSkillId = skill.id"
          @mouseout="handleMouseOut($event, skill.id)"
        >
          <div class="skill-info">
            <div class="skill-icon">
              <icon-tool />
            </div>
            <div class="skill-details">
              <div class="skill-name">
                {{ skill.skillName }}
              </div>
              <div class="skill-desc">{{ skill.description || '暂无描述' }}</div>
            </div>
          </div>
          <div class="skill-actions">
            <a-switch 
              v-model="skill.isEnabled" 
              @click.stop
              @change="handleToggleEnabled(skill)"
              :disabled="togglingId === skill.id"
            />
            <button 
              class="delete-btn"
              :style="{ opacity: hoveredSkillId === skill.id ? 1 : 0 }"
              @click.stop="handleDelete(skill)"
            >
              <icon-delete />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 安装技能对话框 -->
    <a-modal
      v-model:visible="dialogVisible"
      title="安装技能"
      width="600px"
      :mask-closable="false"
      @ok="handleSave"
      @cancel="handleCancel"
      :ok-loading="saving"
    >
      <div class="upload-area">
        <div 
          class="upload-box"
          :class="{ 'has-file': selectedFile }"
          @dragover.prevent="handleDragOver"
          @dragleave="handleDragLeave"
          @drop.prevent="handleDrop"
          @click="triggerFileSelect"
        >
          <div v-if="!selectedFile" class="upload-placeholder">
            <icon-file :size="48" style="color: #ccc" />
            <p class="upload-text">拖放 .zip 或 SKILL.md 文件，或点击选择</p>
          </div>
          <div v-else class="upload-file-info">
            <icon-file :size="24" style="color: #165dff" />
            <div class="file-details">
              <div class="file-name">{{ selectedFile.name }}</div>
              <div class="file-size">{{ formatFileSize(selectedFile.size) }}</div>
            </div>
            <icon-close 
              class="remove-file-btn" 
              @click.stop="removeSelectedFile"
            />
          </div>
        </div>
        
        <div class="upload-hint">
          <icon-info-circle :size="14" />
          <span>支持 .zip 压缩包（需包含 SKILL.md）或直接上传 SKILL.md 文件</span>
        </div>

        <input
          ref="fileInputRef"
          type="file"
          accept=".zip,.md"
          style="display: none"
          @change="handleFileSelected"
        />
      </div>
    </a-modal>

    <!-- 查看技能内容对话框 -->
    <a-modal
      v-model:visible="viewDialogVisible"
      :title="currentSkill?.skillName || '技能内容'"
      width="800px"
      :footer="false"
      body-class="skill-content-modal"
    >
      <div class="skill-content-viewer">
        <pre><code>{{ skillContent }}</code></pre>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api/index.js'
import { Message, Modal } from '@arco-design/web-vue'
import { 
  IconPlus, IconTool, IconDelete, 
  IconInfoCircle, IconFile, IconClose
} from '@arco-design/web-vue/es/icon'

const skillList = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const saving = ref(false)
const togglingId = ref(null)
const fileInputRef = ref(null)
const selectedFile = ref(null)
const currentSkill = ref(null)
const skillContent = ref('')
const hoveredSkillId = ref(null)

// 表单模型
const formModel = reactive({
  id: null,
  skillName: '',
  description: '',
  skillPath: ''
})

// 加载技能列表
const loadSkillList = async () => {
  try {
    const res = await api.getSkillConfigList()
    skillList.value = res.data || []
  } catch (error) {
    Message.error('加载技能列表失败: ' + error.message)
  }
}

// 显示安装对话框
const showInstallDialog = () => {
  formModel.id = null
  formModel.skillName = ''
  formModel.description = ''
  formModel.skillPath = ''
  selectedFile.value = null
  dialogVisible.value = true
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
  selectedFile.value = null
}

// 触发文件选择
const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileSelected = (e) => {
  const file = e.target.files?.[0]
  if (file) {
    validateAndSetFile(file)
  }
  // 重置 input
  e.target.value = ''
}

// 拖拽相关
const handleDragOver = () => {
  isDragOver.value = true
}

const handleDragLeave = () => {
  isDragOver.value = false
}

const handleDrop = (e) => {
  isDragOver.value = false
  const file = e.dataTransfer.files?.[0]
  if (file) {
    validateAndSetFile(file)
  }
}

// 验证并设置文件
const validateAndSetFile = (file) => {
  const fileName = file.name.toLowerCase()
  const isZip = fileName.endsWith('.zip')
  const isMd = fileName === 'skill.md' || fileName.endsWith('.skill.md')
  
  if (!isZip && !isMd) {
    Message.error('请选择 .zip 压缩包或 SKILL.md 文件')
    return
  }
  
  // 检查文件大小（最大 50MB）
  if (file.size > 50 * 1024 * 1024) {
    Message.error('文件大小不能超过 50MB')
    return
  }
  
  selectedFile.value = file
  
  // 如果是 SKILL.md，尝试读取内容
  if (isMd) {
    readSkillMdFile(file)
  }
}

// 读取 SKILL.md 文件内容
const readSkillMdFile = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    const content = e.target.result
    // 提取 frontmatter 中的 name 和 description
    const match = content.match(/^---\s*\n([\s\S]*?)\n---/)
    if (match) {
      const frontmatter = match[1]
      const nameMatch = frontmatter.match(/name:\s*(.+)/)
      const descMatch = frontmatter.match(/description:\s*(.+)/)
      
      if (nameMatch) {
        formModel.skillName = nameMatch[1].trim()
      }
      if (descMatch) {
        formModel.description = descMatch[1].trim()
      }
    }
  }
  reader.readAsText(file)
}

// 移除已选择的文件
const removeSelectedFile = () => {
  selectedFile.value = null
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 保存技能
const handleSave = async () => {
  if (!selectedFile.value) {
    Message.warning('请选择技能文件')
    return
  }

  saving.value = true
  try {
    await uploadSkillFile(selectedFile.value)
  } catch (error) {
    Message.error('操作失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 上传技能文件
const uploadSkillFile = async (file) => {
  const fileName = file.name.toLowerCase()
  const isZip = fileName.endsWith('.zip')
  
  try {
    // 1. 先上传文件到服务器
    const formData = new FormData()
    formData.append('file', file)
    
    Message.loading({
      content: isZip ? '正在解压并安装技能...' : '正在安装技能...',
      duration: 0
    })
    
    const uploadRes = await api.uploadSkillFile(formData)
    const extractedPath = uploadRes.data
    
    // 2. 读取解压后的 SKILL.md 内容
    const skillMdRes = await api.readSkillFromFile(extractedPath)
    const skillMdContent = skillMdRes.data
    
    // 解析 frontmatter
    const match = skillMdContent.match(/^---\s*\n([\s\S]*?)\n---/)
    if (match) {
      const frontmatter = match[1]
      const nameMatch = frontmatter.match(/name:\s*(.+)/)
      const descMatch = frontmatter.match(/description:\s*(.+)/)
      
      formModel.skillName = nameMatch ? nameMatch[1].trim() : file.name.replace(/\.zip$/i, '')
      formModel.description = descMatch ? descMatch[1].trim() : ''
    } else {
      formModel.skillName = file.name.replace(/\.zip$/i, '')
      formModel.description = ''
    }
    
    formModel.skillPath = extractedPath
    
    // 3. 保存到数据库
    await api.addSkillConfig(formModel)
    Message.clear()
    Message.success('安装成功')
    dialogVisible.value = false
    await loadSkillList()
  } catch (error) {
    Message.clear()
    throw new Error(error.message || '安装失败')
  }
}

// 查看技能内容
const handleViewSkill = async (skill) => {
  currentSkill.value = skill
  try {
    // 从服务器读取 SKILL.md 内容
    const res = await api.readSkillFromFile(skill.skillPath)
    skillContent.value = res.data
    viewDialogVisible.value = true
  } catch (error) {
    Message.error('读取技能内容失败: ' + error.message)
  }
}

// 鼠标离开技能卡片（mouseout 会冒泡，需判断是否真正离开）
const handleMouseOut = (event, skillId) => {
  const relatedTarget = event.relatedTarget
  const currentTarget = event.currentTarget
  // 只有当鼠标从当前卡片移到一个不在卡片内的元素时，才清除 hover 状态
  if (!currentTarget.contains(relatedTarget)) {
    hoveredSkillId.value = null
  }
}

// 切换启用状态
const handleToggleEnabled = async (skill) => {
  togglingId.value = skill.id
  try {
    await api.toggleSkillEnabled(skill.id, skill.isEnabled)
    Message.success(skill.isEnabled ? '已启用' : '已禁用')
  } catch (error) {
    // 失败时恢复状态
    skill.isEnabled = !skill.isEnabled
    Message.error('操作失败: ' + error.message)
  } finally {
    togglingId.value = null
  }
}

// 删除技能
const handleDelete = async (skill) => {
  Modal.confirm({
    title: '删除技能',
    content: `确定要删除技能 "${skill.skillName}" 吗？`,
    okText: '确定',
    cancelText: '取消',
    status: 'warning',
    onOk: async () => {
      try {
        await api.deleteSkillConfig(skill.id)
        Message.success('删除成功')
        await loadSkillList()
      } catch (error) {
        Message.error('删除失败: ' + error.message)
      }
    }
  })
}

onMounted(() => {
  loadSkillList()
})
</script>

<style scoped>
.skills-container {
  padding: 24px 32px;
  background: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 技能内容查看弹窗样式 */
:global(.skill-content-modal) {
  max-height: 80vh;
}

:global(.arco-modal-wrapper .skill-content-modal .arco-modal-content) {
  padding: 0;
}

/* 顶部标题栏 */
.skills-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
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

.header-right {
  display: flex;
  gap: 12px;
}

/* 安装技能按钮样式 */
.install-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 500;
  border-radius: 8px !important;
  padding: 8px 20px !important;
  height: 36px !important;
  transition: all 0.3s !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3) !important;
}

.install-btn:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a4190 100%) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4) !important;
  transform: translateY(-1px);
}

.install-btn:active {
  transform: translateY(0);
}

/* 技能列表 */
.skills-list {
  background: #fff;
  flex: 1;
  overflow-y: auto;
}

.skill-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skill-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  background: #fff;
  transition: all 0.2s;
  cursor: pointer;
  margin-bottom: 8px;
  position: relative;
}

.skill-item:last-child {
  margin-bottom: 0;
}

.skill-item-hovered {
  background: #f5f7fa;
  border-color: #c9cdd4;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.skill-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.skill-icon {
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

.skill-details {
  flex: 1;
  min-width: 0;
}

.skill-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #1a1a1a;
}

.skill-desc {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  max-width: 600px;
}

.skill-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}

/* 美化开关样式 */
:deep(.arco-switch) {
  background: #e5e6eb;
}

:deep(.arco-switch-checked) {
  background: #93d8a8 !important;
}

:deep(.arco-switch:hover) {
  background: #d8d9dc;
}

:deep(.arco-switch-checked:hover) {
  background: #83d49a !important;
}

.delete-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: #86909c;
  opacity: 0;
  transition: opacity 0.2s, color 0.15s, background 0.15s;
  padding: 0;
  font-size: 16px;
}

.delete-btn:hover {
  color: #f53f3f;
  background: #f5f5f5;
}

/* 技能内容查看器 */
.skill-content-viewer {
  max-height: 600px;
  overflow-y: auto;
}

.skill-content-viewer pre {
  margin: 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.skill-content-viewer code {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  color: #333;
}

/* 上传区域 */
.upload-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-box {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.upload-box:hover {
  border-color: #165dff;
  background: #f0f7ff;
}

.upload-box.has-file {
  border-color: #165dff;
  background: #f0f7ff;
  padding: 20px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upload-text {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.upload-file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-details {
  flex: 1;
  text-align: left;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.file-size {
  font-size: 12px;
  color: #999;
}

.remove-file-btn {
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
  font-size: 16px;
}

.remove-file-btn:hover {
  color: #f53f3f;
}

.upload-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 12px;
  padding: 0 4px;
}
</style>
