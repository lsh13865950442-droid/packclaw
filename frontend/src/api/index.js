import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 600000 // 10分钟超时
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    return Promise.reject(error)
  }
)

// API 接口
export const api = {
  // 创建会话
  createSession(data) {
    return request.post('/chat/session', data)
  },

  // 流式对话
  chat(data) {
    return request.post('/chat', data, {
      responseType: 'text',
      headers: {
        'Accept': 'text/event-stream'
      }
    })
  },

  // 停止会话
  stopSession(sessionId) {
    return request.post('/chat/stop', null, {
      params: { sessionId }
    })
  },

  // 生成会话标题
  generateTitle(sessionId) {
    return request.post('/chat/generate-title', null, {
      params: { sessionId }
    })
  },

  // 获取建议
  getNextSuggestions(sessionId) {
    return request.post('/chat/next-suggestions', null, {
      params: { sessionId }
    })
  },

  // 获取会话列表
  getSessionList(params) {
    return request.post('/history/list', null, { params })
  },

  // 获取会话详情
  getSessionDetails(chatId) {
    return request.post('/history/details', null, {
      params: { chatId }
    })
  },

  // 删除会话
  deleteSession(chatId) {
    return request.post('/history/delete', null, {
      params: { chatId }
    })
  },

  // 重命名会话
  renameSession(chatId, title) {
    return request.post('/history/rename', null, {
      params: { chatId, title }
    })
  },

  // 上传媒体文件（图像/音频/视频），返回服务器文件路径
  uploadMediaFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 获取模型配置列表
  getModelConfigList() {
    return request.get('/model-config/list')
  },

  // 获取激活的模型配置
  getActiveModelConfig() {
    return request.get('/model-config/active')
  },

  // 添加模型配置
  addModelConfig(data) {
    return request.post('/model-config', data)
  },

  // 更新模型配置
  updateModelConfig(id, data) {
    return request.put(`/model-config/${id}`, data)
  },

  // 删除模型配置
  deleteModelConfig(id) {
    return request.delete(`/model-config/${id}`)
  },

  // 激活模型配置
  activateModelConfig(id) {
    return request.post(`/model-config/${id}/activate`)
  }
}

export default request
