<template>
  <el-card shadow="never" class="messages">
    <template #header>
      <div class="messages__header">
        <h3>消息通知</h3>
        <el-button type="primary" text @click="refresh">刷新</el-button>
      </div>
    </template>
    <el-empty v-if="loading" description="加载中..." />
    <el-empty v-else-if="messages.length === 0" description="暂无消息" />
    <div v-else class="messages__list">
      <el-card
        v-for="message in messages"
        :key="message.id"
        shadow="hover"
        class="messages__item"
        :class="{ 'messages__item--unread': !message.read }"
      >
        <div class="messages__item-header">
          <div>
            <div class="messages__title">{{ message.title }}</div>
            <div class="messages__meta">
              <span>类型：{{ message.type }}</span>
              <span>时间：{{ formatDate(message.createdAt) }}</span>
            </div>
          </div>
          <el-button
            size="small"
            type="primary"
            link
            v-if="!message.read"
            @click="markRead(message.id)"
          >标记已读</el-button>
        </div>
        <p class="messages__content">{{ message.content }}</p>
      </el-card>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';
import { fetchMessages, markMessage } from '../api/message';
import type { MessageRecord } from '../types/models';

const messages = ref<MessageRecord[]>([]);
const loading = ref(false);

const loadMessages = async () => {
  loading.value = true;
  try {
    messages.value = await fetchMessages();
  } finally {
    loading.value = false;
  }
};

const markRead = async (id: number) => {
  try {
    await markMessage(id, true);
    messages.value = messages.value.map((item) =>
      item.id === id ? { ...item, read: true, readAt: new Date().toISOString() } : item,
    );
  } catch (error) {
    ElMessage.error('操作失败，请稍后再试');
  }
};

const refresh = () => loadMessages();

const formatDate = (value: string) => dayjs(value).format('YYYY-MM-DD HH:mm');

onMounted(() => {
  loadMessages();
});
</script>

<style scoped>
.messages {
  border-radius: 12px;
}

.messages__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.messages__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.messages__item {
  border-radius: 12px;
}

.messages__item--unread {
  border: 1px solid #60a5fa;
}

.messages__item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.messages__title {
  font-weight: 600;
}

.messages__meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}

.messages__content {
  color: #334155;
}
</style>
