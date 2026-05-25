<template>
  <a-modal
    v-model:visible="visible"
    width="1000px"
    :footer="false"
    :mask-closable="false"
    unmount-on-close
    body-class="settings-modal-body"
    modal-class="settings-modal"
    hide-title
  >
    <div class="settings-container">
      <!-- 左侧导航 -->
      <aside class="settings-sidebar">
        <div class="sidebar-header">
          <icon-left class="back-icon" @click="visible = false" />
          <span class="back-text" @click="visible = false">返回应用</span>
        </div>
        <nav class="sidebar-nav">
          <div class="nav-group">
            <div 
              class="nav-item" 
              :class="{ active: activeMenu === 'model' }"
              @click="activeMenu = 'model'"
            >
              <icon-settings />
              <span>模型配置</span>
            </div>
          </div>
        </nav>
      </aside>

      <!-- 右侧内容区 -->
      <main class="settings-content">
        <!-- 模型配置 -->
        <div v-if="activeMenu === 'model'" class="content-panel">
          <h2 class="panel-title">模型配置</h2>
          <p class="panel-desc">配置大模型提供商和访问参数</p>

          <!-- 模型列表 -->
          <div class="model-list">
            <div 
              v-for="model in modelList" 
              :key="model.id"
              class="model-item"
            >
              <div class="model-info">
                <div class="model-icon">
                  <icon-computer v-if="model.provider === 'DASHSCOPE'" />
                  <icon-message v-else-if="model.provider === 'OPENAI'" />
                  <icon-voice v-else-if="model.provider === 'ANTHROPIC'" />
                  <icon-settings v-else />
                </div>
                <div class="model-details">
                  <div class="model-name">
                    {{ model.modelId }}
                  </div>
                  <div class="model-desc">{{ getProviderName(model.provider) }}</div>
                </div>
              </div>
              <div class="model-actions">
                <a-button type="text" @click="handleEdit(model)">
                  <icon-edit />
                </a-button>
                <a-button type="text" status="danger" @click="handleDelete(model)">
                  <icon-delete />
                </a-button>
              </div>
            </div>

            <!-- 添加按钮 -->
            <div class="add-model-btn" @click="showAddDialog">
              <icon-plus :size="20" />
              <span>添加模型配置</span>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 添加/编辑模型对话框 -->
    <a-modal
      v-model:visible="dialogVisible"
      :title="isEdit ? '编辑模型' : '添加模型'"
      width="500px"
      :mask-closable="false"
      @ok="handleSave"
      @cancel="dialogVisible = false"
      :ok-loading="saving"
      modal-class="dialog-modal"
    >
      <a-form :model="formModel" layout="vertical">
        <a-form-item label="模型厂商">
          <a-select v-model="formModel.provider" placeholder="选择模型厂商" @change="onProviderChange">
            <a-option value="DASHSCOPE">DashScope (阿里云)</a-option>
            <a-option value="OPENAI">OpenAI</a-option>
            <a-option value="ANTHROPIC">Anthropic</a-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="apiUrlLabel">
          <a-input 
            v-model="formModel.apiUrl" 
            :placeholder="apiUrlPlaceholder"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="模型ID">
          <a-input 
            v-model="formModel.modelId" 
            :placeholder="modelIdPlaceholder"
          />
        </a-form-item>

        <a-form-item label="API密钥">
          <a-input-password
            v-model="formModel.apiKey" 
            placeholder="请输入API密钥"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { api } from '../api/index.js'
import { Message, Modal } from '@arco-design/web-vue'
import { 
  IconLeft, IconPlus, IconEdit, IconDelete, 
  IconComputer, IconMessage, IconVoice, IconSettings 
} from '@arco-design/web-vue/es/icon'

const visible = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const activeMenu = ref('model')
const modelList = ref([])

// 表单模型
const formModel = reactive({
  id: null,
  provider: 'DASHSCOPE',
  apiUrl: '',
  modelId: '',
  apiKey: ''
})

// 各厂商的默认配置
const providerDefaults = {
  DASHSCOPE: {
    apiUrl: '',
    modelId: 'qwen3.6-plus',
    apiUrlLabel: 'API地址',
    apiUrlPlaceholder: '留空使用默认地址',
    modelIdPlaceholder: '例如: qwen3.6-plus'
  },
  OPENAI: {
    apiUrl: 'https://api.openai.com/v1',
    modelId: 'gpt-4o',
    apiUrlLabel: 'API地址',
    apiUrlPlaceholder: 'https://api.openai.com/v1',
    modelIdPlaceholder: '例如: gpt-4o'
  },
  ANTHROPIC: {
    apiUrl: 'https://api.anthropic.com/v1',
    modelId: 'claude-sonnet-4-5-20250929',
    apiUrlLabel: 'API地址',
    apiUrlPlaceholder: 'https://api.anthropic.com/v1',
    modelIdPlaceholder: '例如: claude-sonnet-4'
  }
}

// 计算属性
const apiUrlLabel = computed(() => {
  return providerDefaults[formModel.provider]?.apiUrlLabel || 'API地址'
})

const apiUrlPlaceholder = computed(() => {
  return providerDefaults[formModel.provider]?.apiUrlPlaceholder || '请输入API地址'
})

const modelIdPlaceholder = computed(() => {
  return providerDefaults[formModel.provider]?.modelIdPlaceholder || '请输入模型ID'
})

// 厂商切换
const onProviderChange = (provider) => {
  const defaults = providerDefaults[provider]
  if (defaults && !isEdit.value) {
    if (!formModel.apiUrl) {
      formModel.apiUrl = defaults.apiUrl
    }
    if (!formModel.modelId) {
      formModel.modelId = defaults.modelId
    }
  }
}

// 获取厂商名称
const getProviderName = (provider) => {
  const names = {
    DASHSCOPE: 'DashScope',
    OPENAI: 'OpenAI',
    ANTHROPIC: 'Anthropic'
  }
  return names[provider] || provider
}

// 加载模型列表
const loadModelList = async () => {
  try {
    const res = await api.getModelConfigList()
    modelList.value = res.data || []
  } catch (error) {
    Message.error('加载模型列表失败: ' + error.message)
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  formModel.id = null
  formModel.provider = 'DASHSCOPE'
  formModel.apiUrl = ''
  formModel.modelId = 'qwen3.6-plus'
  formModel.apiKey = ''
  dialogVisible.value = true
}

// 编辑模型
const handleEdit = (model) => {
  isEdit.value = true
  formModel.id = model.id
  formModel.provider = model.provider
  formModel.apiUrl = model.apiUrl || ''
  formModel.modelId = model.modelId
  formModel.apiKey = model.apiKey
  dialogVisible.value = true
}

// 保存模型
const handleSave = async () => {
  if (!formModel.provider) {
    Message.warning('请选择模型厂商')
    return
  }
  if (!formModel.modelId) {
    Message.warning('请输入模型ID')
    return
  }
  if (!formModel.apiKey) {
    Message.warning('请输入API密钥')
    return
  }

  saving.value = true
  try {
    // 显示验证提示
    Message.loading({
      content: '正在验证模型配置...',
      duration: 0
    })
    
    if (isEdit.value) {
      await api.updateModelConfig(formModel.id, formModel)
      Message.clear()
      Message.success('验证通过，更新成功')
      dialogVisible.value = false  // 只有成功才关闭弹窗
      await loadModelList()
    } else {
      await api.addModelConfig(formModel)
      Message.clear()
      Message.success('验证通过，添加成功')
      dialogVisible.value = false  // 只有成功才关闭弹窗
      await loadModelList()
    }
    // 触发事件通知父组件刷新模型列表
    window.dispatchEvent(new CustomEvent('modelConfigUpdated'))
  } catch (error) {
    Message.clear()
    Message.error((isEdit.value ? '更新' : '添加') + '失败: ' + error.message)
    // 验证失败不关闭弹窗，让用户继续修改
  } finally {
    saving.value = false
  }
}

// 点击模型卡片 - 不再需要激活功能
const handleActivate = (model) => {
  // 保留点击事件但不再做任何操作
}

// 删除模型
const handleDelete = async (model) => {
  try {
    Modal.confirm({
      title: '删除模型',
      content: `确定要删除 ${getProviderName(model.provider)} - ${model.modelId} 吗？`,
      okText: '确定',
      cancelText: '取消',
      status: 'warning',
      onOk: async () => {
        await api.deleteModelConfig(model.id)
        Message.success('删除成功')
        await loadModelList()
      }
    })
  } catch (error) {
    Message.error('删除失败: ' + error.message)
  }
}

// 监听visible变化
watch(visible, (val) => {
  if (val) {
    loadModelList()
  }
})

// 暴露方法供外部调用
defineExpose({
  open: () => { visible.value = true }
})
</script>

<style>
/* 全局样式 - Modal body无padding */
.settings-modal-body {
  padding: 0 !important;
}

/* 设置相关的Modal圆角 */
.arco-modal-wrapper.settings-modal .arco-modal,
.settings-modal-wrapper .arco-modal,
.arco-modal.settings-modal {
  border-radius: 16px !important;
  overflow: hidden !important;
}

.arco-modal-wrapper.dialog-modal .arco-modal,
.dialog-modal-wrapper .arco-modal,
.arco-modal.dialog-modal {
  border-radius: 16px !important;
  overflow: hidden !important;
}

/* 确保wrapper也有圆角 */
.settings-modal-wrapper,
.dialog-modal-wrapper {
  border-radius: 16px !important;
}

/* 设置容器 - 增加高度 */
.settings-container {
  min-height: 600px !important;
  max-height: 750px !important;
}
</style>

<style scoped>
.settings-container {
  display: flex;
  min-height: 600px;
  max-height: 750px;
}

/* 左侧导航 */
.settings-sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0px 0px 8px;
}

.back-icon {
  font-size: 14px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s;
}

.back-text {
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.back-text:hover {
  color: #333;
}

.sidebar-nav {
  padding: 12px;
  flex: 1;
}

.nav-group {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin: 0;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: all 0.2s;
  border-radius: 8px;
}

.nav-item:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #333;
}

.nav-item.active {
  background: #e8e8e8;
  color: #1a1a1a;
  font-weight: 500;
}

/* 右侧内容区 */
.settings-content {
  flex: 1;
  padding: 32px 40px;
  overflow-y: auto;
  background: #fff;
  min-height: 600px;
}

.content-panel {
  max-width: 800px;
}

.panel-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.panel-desc {
  margin: 0 0 40px;
  color: #999;
  font-size: 14px;
}

/* 模型列表 */
.model-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.model-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  background: #fff;
  transition: all 0.2s;
}

.model-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-color: #d0d0d0;
}

.model-item.active {
  border-color: #00b42a;
  background: linear-gradient(135deg, #f8fdf8 0%, #f0f9f0 100%);
}

.model-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  min-width: 0;
}

.model-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 12px;
  font-size: 24px;
  color: #666;
  flex-shrink: 0;
}

.model-item.active .model-icon {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #00b42a;
}

.model-details {
  flex: 1;
  min-width: 0;
}

.model-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #1a1a1a;
}

.model-desc {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 添加按钮 */
.add-model-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  border: 2px dashed #e0e0e0;
  border-radius: 10px;
  cursor: pointer;
  color: #999;
  font-size: 14px;
  transition: all 0.2s;
}

.add-model-btn:hover {
  border-color: #00b42a;
  color: #00b42a;
  background: #f8fdf8;
}
</style>
