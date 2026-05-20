import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const currentSessionId = ref('')
  const currentChatId = ref('')
  const messages = ref([])
  const isStreaming = ref(false)

  // 当前正在流式输出的 messageId，用来检测是否需要新建消息
  let _currentStreamMsgId = null

  function setCurrentSession(sessionId, chatId = '') {
    currentSessionId.value = sessionId
    currentChatId.value = chatId
  }

  function addMessage(message) {
    messages.value.push(message)
  }

  // 根据 messageId 获取或创建对应的 assistant 消息
  // 每个 messageId 对应一条独立的 assistant 消息
  function _getOrCreateAssistantMsg(messageId) {
    // 如果 messageId 没变，继续用 last message
    if (_currentStreamMsgId === messageId) {
      return messages.value[messages.value.length - 1]
    }

    // messageId 变化：新一轮 REASONING，需要新建一条 assistant 消息
    _currentStreamMsgId = messageId

    // 如果 last message 是 assistant 且内容为空（刚创建的占位）则复用
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant' && !last.thinking && !last.content && !last.toolCalls && last.loading) {
      return last
    }

    // 否则新建一条（先折叠上一条消息的 toolCalls）
    if (last && last.role === 'assistant' && last.toolCalls && last.toolCalls.length > 0) {
      last.toolCallsCollapsed = true
    }
    const newMsg = { role: 'assistant', content: '', loading: false }
    messages.value.push(newMsg)
    return newMsg
  }

  // 处理 isLast=false 的 REASONING 增量 chunk
  function applyReasoningChunk(messageId, block) {
    const msg = _getOrCreateAssistantMsg(messageId)
    msg.loading = false

    if (block.type === 'thinking') {
      msg.thinking = (msg.thinking || '') + (block.thinking || '')
      msg.thinkingDone = false
      if (msg.thinkingCollapsed === undefined) msg.thinkingCollapsed = false
    } else if (block.type === 'text') {
      msg.content = (msg.content || '') + (block.text || '')
    }
    // tool_use chunk（isLast=false）name=__fragment__，忽略
  }

  // 处理 isLast=true 的最终 REASONING 事件（本轮完整 blocks）
  function applyFinalReasoning(messageId, contentBlocks) {
    const msg = _getOrCreateAssistantMsg(messageId)
    msg.loading = false

    for (const block of contentBlocks) {
      if (block.type === 'thinking') {
        // 用完整值覆盖增量拼接（确保准确）
        msg.thinking = block.thinking || msg.thinking || ''
        msg.thinkingDone = true
        msg.thinkingCollapsed = true
      } else if (block.type === 'text') {
        msg.content = block.text || msg.content || ''
      } else if (block.type === 'tool_use' && block.id && block.name !== '__fragment__') {
        _upsertToolCall(msg, block.id, block.name, block.input)
      }
    }

    // 本轮 REASONING 结束后，重置 _currentStreamMsgId
    // 下一个 messageId 到来时会自动新建消息
    _currentStreamMsgId = null
  }

  // 处理 TOOL_RESULT 事件——找到包含对应 toolId 的 assistant 消息并回填结果
  function applyToolResult(contentBlocks) {
    for (const block of contentBlocks) {
      if (block.type !== 'tool_result') continue
      const toolId = block.id || block.toolUseId
      if (!toolId) continue
      const resultText = _extractResultText(block)

      // 从后往前找含有该 toolId 的 assistant 消息
      for (let i = messages.value.length - 1; i >= 0; i--) {
        const msg = messages.value[i]
        if (msg.role !== 'assistant' || !msg.toolCalls) continue
        const tool = msg.toolCalls.find(t => t.id === toolId)
        if (tool) {
          tool.result = resultText
          tool.status = 'done'
          break
        }
      }
    }
  }

  // 重置流式状态（发送新消息前调用）
  function resetStreamState() {
    _currentStreamMsgId = null
  }

  // 内部：新增或更新工具调用条目
  function _upsertToolCall(msg, toolId, toolName, toolInput) {
    if (!msg.toolCalls) {
      msg.toolCalls = []
      msg.toolCallsCollapsed = false
    }
    const existing = msg.toolCalls.find(t => t.id === toolId)
    if (existing) {
      if (toolName && toolName !== '__fragment__') existing.name = toolName
      if (toolInput && Object.keys(toolInput).length) existing.input = toolInput
    } else {
      msg.toolCalls.push({
        id: toolId,
        name: toolName,
        input: toolInput || {},
        collapsed: false,
        status: 'running'
      })
    }
  }

  // 公开的 upsertToolCall（供历史消息加载使用）
  function upsertToolCall(toolId, toolName, toolInput) {
    if (messages.value.length === 0) return
    _upsertToolCall(messages.value[messages.value.length - 1], toolId, toolName, toolInput)
  }

  // 折叠/展开某个工具调用
  function toggleToolCall(msgIndex, toolId) {
    const msg = messages.value[msgIndex]
    if (!msg || !msg.toolCalls) return
    const tool = msg.toolCalls.find(t => t.id === toolId)
    if (tool) tool.collapsed = !tool.collapsed
  }

  function clearMessages() {
    messages.value.splice(0)
    _currentStreamMsgId = null
  }

  function setStreaming(status) {
    isStreaming.value = status
  }

  return {
    currentSessionId,
    currentChatId,
    messages,
    isStreaming,
    setCurrentSession,
    addMessage,
    applyReasoningChunk,
    applyFinalReasoning,
    applyToolResult,
    resetStreamState,
    upsertToolCall,
    toggleToolCall,
    clearMessages,
    setStreaming
  }
})

// 模块级工具函数
export function _extractResultText(block) {
  if (block.output) {
    if (typeof block.output === 'string') return block.output
    if (Array.isArray(block.output)) return block.output.map(o => o.text || '').join('')
    if (block.output.type === 'text') return block.output.text || ''
    return JSON.stringify(block.output)
  }
  if (block.content) {
    if (typeof block.content === 'string') return block.content
    if (Array.isArray(block.content)) return block.content.map(o => o.text || '').join('')
  }
  return ''
}
