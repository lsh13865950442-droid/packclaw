import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const currentSessionId = ref('')
  const currentChatId = ref('')
  const messages = ref([])
  const isStreaming = ref(false)

  function setCurrentSession(sessionId, chatId = '') {
    currentSessionId.value = sessionId
    currentChatId.value = chatId
  }

  function addMessage(message) {
    messages.value.push(message)
  }

  function updateLastMessage(content) {
    if (messages.value.length > 0) {
      const lastMsg = messages.value[messages.value.length - 1]
      lastMsg.content = content
      lastMsg.loading = false
    }
  }

  function updateLastMessageThinking(thinkingChunk) {
    if (messages.value.length > 0) {
      const lastMsg = messages.value[messages.value.length - 1]
      lastMsg.thinking = (lastMsg.thinking || '') + thinkingChunk
      lastMsg.thinkingDone = false
      // 思考中默认展开
      if (lastMsg.thinkingCollapsed === undefined) {
        lastMsg.thinkingCollapsed = false
      }
    }
  }

  function finishLastMessageThinking() {
    if (messages.value.length > 0) {
      const lastMsg = messages.value[messages.value.length - 1]
      lastMsg.thinkingDone = true
      // 思考完毕后自动折叠
      lastMsg.thinkingCollapsed = true
    }
  }

  function clearMessages() {
    messages.value.splice(0)
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
    updateLastMessage,
    updateLastMessageThinking,
    finishLastMessageThinking,
    clearMessages,
    setStreaming
  }
})
