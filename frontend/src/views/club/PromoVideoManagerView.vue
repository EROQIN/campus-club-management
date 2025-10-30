<template>
  <div class="promo-video-manager">
    <el-card shadow="never" class="promo-video-manager__card">
      <template #header>
        <div class="promo-video-manager__card-header">
          <div>
            <div class="promo-video-manager__title">宣传视频与字幕管理</div>
            <div class="promo-video-manager__subtitle">
              选择负责的社团，上传宣传视频并维护字幕内容。
            </div>
          </div>
          <el-tag v-if="selectedClub" type="success" size="small">{{ selectedClub.name }}</el-tag>
        </div>
      </template>
      <div class="promo-video-manager__controls">
        <el-form label-width="84px">
          <el-form-item label="选择社团">
            <el-select
              v-model="selectedClubId"
              placeholder="请选择社团"
              style="width: 320px"
              :disabled="clubsLoading || !myClubs.length"
            >
              <el-option
                v-for="club in myClubs"
                :key="club.id"
                :label="club.name"
                :value="club.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <el-empty v-if="!clubsLoading && !myClubs.length" description="暂无可管理的社团" />
      </div>
    </el-card>

    <el-skeleton v-if="promoVideoLoading" animated :rows="6" />

    <template v-else-if="selectedClubId">
      <el-card shadow="never" class="promo-video-card">
        <template #header>
          <div class="promo-video-card__header">
            <div>
              <div class="promo-video-card__title">上传宣传视频</div>
              <div class="promo-video-card__subtitle">
                支持 mp4，单个文件时长 ≤ 30 秒，大小 ≤ 50MB。上传后可自动生成字幕。
              </div>
            </div>
            <el-tag
              v-if="promoVideo"
              :type="promoStatusTagType(promoVideo.status)"
              size="small"
            >
              {{ formatPromoStatus(promoVideo.status) }}
            </el-tag>
          </div>
        </template>

        <el-upload
          drag
          class="promo-video-upload"
          accept="video/mp4"
          :show-file-list="false"
          :before-upload="handleVideoBeforeUpload"
          :disabled="videoUploading"
        >
          <el-icon class="promo-video-upload__icon"><Plus /></el-icon>
          <div class="el-upload__text">
            拖拽视频到此处或<em>点击上传</em>社团宣传短视频
          </div>
          <div class="el-upload__tip">仅支持 mp4，视频大小 ≤ 50MB，长度 ≤ 30 秒</div>
        </el-upload>

        <el-alert
          v-if="promoVideoError"
          type="error"
          :closable="false"
          show-icon
          class="promo-video-card__alert"
          :title="promoVideoError"
        />

        <div v-if="promoVideo" class="promo-video-preview">
          <video
            class="promo-video-preview__player"
            controls
            :src="promoVideo.playbackUrl"
            preload="metadata"
          >
            <track
              v-if="subtitleTrackUrl"
              kind="subtitles"
              srclang="zh"
              label="字幕"
              :src="subtitleTrackUrl"
              default
            />
          </video>
          <ul class="promo-video-preview__meta">
            <li>文件名：{{ promoVideo.originalFileName || '未提供' }}</li>
            <li>视频时长：{{ formatVideoDuration(promoVideo.durationSeconds) }}</li>
            <li>文件大小：{{ formatFileSize(promoVideo.fileSizeBytes) }}</li>
            <li v-if="promoVideo.subtitlesUpdatedAt">
              字幕更新时间：{{ formatTimestamp(promoVideo.subtitlesUpdatedAt) }}
            </li>
          </ul>
        </div>
        <el-empty v-else description="尚未上传宣传视频" :image-size="160" />
      </el-card>

      <el-card
        v-if="promoVideo"
        shadow="never"
        class="promo-video-card"
      >
        <template #header>
          <div class="promo-video-card__header promo-video-card__header--actions">
            <div>
              <div class="promo-video-card__title">字幕管理</div>
              <div class="promo-video-card__subtitle">
                生成 AI 字幕后可手动调整时间与内容，保存后可在社团详情页展示。
              </div>
            </div>
            <el-space wrap>
              <el-button
                type="primary"
                :loading="isGeneratingSubtitles"
                :disabled="isGeneratingSubtitles"
                @click="handleGenerateAiSubtitles"
              >
                生成 AI 字幕
              </el-button>
              <el-button
                :disabled="!subtitlesDirty || subtitlesSaving"
                @click="resetSubtitles"
              >
                恢复未保存修改
              </el-button>
              <el-button
                type="success"
                :loading="subtitlesSaving"
                :disabled="!subtitlesDirty || subtitlesSaving || isGeneratingSubtitles"
                @click="saveSubtitlesChanges"
              >
                保存字幕
              </el-button>
            </el-space>
          </div>
        </template>

        <el-alert
          v-if="promoVideo.status === 'FAILED'"
          type="warning"
          :closable="false"
          show-icon
          class="promo-video-card__alert"
          title="上次生成字幕失败，请稍后重试或手动编辑。"
        />

        <div class="promo-video-subtitles__toolbar">
          <el-button
            type="primary"
            plain
            size="small"
            :disabled="isGeneratingSubtitles"
            @click="addSubtitleRow"
          >
            新增字幕行
          </el-button>
          <span class="promo-video-subtitles__hint">建议每条字幕 2-3 秒，尽量避免重叠。</span>
        </div>

        <el-table
          :data="subtitlesWorkingCopy"
          size="small"
          empty-text="暂无字幕，请生成或手动添加。"
          class="promo-video-subtitles__table"
        >
          <el-table-column label="顺序" width="100">
            <template #default="{ $index }">
              <el-space>
                <span>{{ $index + 1 }}</span>
                <div class="promo-video-subtitles__order-controls">
                  <el-button
                    circle
                    size="small"
                    :icon="ArrowUp"
                    :disabled="$index === 0 || isGeneratingSubtitles"
                    @click="moveSubtitleUp($index)"
                  />
                  <el-button
                    circle
                    size="small"
                    :icon="ArrowDown"
                    :disabled="$index === subtitlesWorkingCopy.length - 1 || isGeneratingSubtitles"
                    @click="moveSubtitleDown($index)"
                  />
                </div>
              </el-space>
            </template>
          </el-table-column>
          <el-table-column label="开始 (秒)" width="150">
            <template #default="{ row }">
              <el-input-number
                v-model="row.startSec"
                :precision="2"
                :step="0.5"
                :min="0"
                controls-position="right"
                size="small"
                :disabled="isGeneratingSubtitles"
              />
            </template>
          </el-table-column>
          <el-table-column label="结束 (秒)" width="150">
            <template #default="{ row }">
              <el-input-number
                v-model="row.endSec"
                :precision="2"
                :step="0.5"
                :min="0"
                controls-position="right"
                size="small"
                :disabled="isGeneratingSubtitles"
              />
            </template>
          </el-table-column>
          <el-table-column label="字幕内容">
            <template #default="{ row }">
              <el-input
                v-model="row.text"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 4 }"
                maxlength="500"
                show-word-limit
                placeholder="请输入字幕内容"
                :disabled="isGeneratingSubtitles"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ $index }">
              <el-button
                link
                type="danger"
                size="small"
                :disabled="isGeneratingSubtitles"
                @click="removeSubtitleRow($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import type { UploadProps } from 'element-plus';
import { ElMessage } from 'element-plus';
import { Plus, ArrowUp, ArrowDown } from '@element-plus/icons-vue';
import dayjs from 'dayjs';

import type { ClubSummary, PromoVideo, SubtitleSegment } from '../../types/models';
import { useAuthStore } from '../../store/auth';
import { fetchMyClubs } from '../../api/clubManagement';
import {
  fetchClubPromoVideo,
  generateClubPromoVideoSubtitles,
  saveClubPromoVideoSubtitles,
  uploadClubPromoVideo,
} from '../../api/promoVideo';

type EditableSubtitle = {
  id?: number | null;
  sequence: number;
  startSec: number;
  endSec: number;
  text: string;
  autoGenerated: boolean;
};

const auth = useAuthStore();

const myClubs = ref<ClubSummary[]>([]);
const clubsLoading = ref(false);
const selectedClubId = ref<number | null>(null);
const selectedClub = computed(() =>
  myClubs.value.find((club) => club.id === selectedClubId.value) ?? null,
);

const promoVideo = ref<PromoVideo | null>(null);
const promoVideoLoading = ref(false);
const promoVideoError = ref<string | null>(null);
const videoUploading = ref(false);
const aiGeneratingSubtitles = ref(false);
const subtitlesSaving = ref(false);
const subtitlesWorkingCopy = ref<EditableSubtitle[]>([]);
const originalSubtitles = ref<EditableSubtitle[]>([]);
const subtitleTrackUrl = ref<string | null>(null);

const subtitlesDirty = computed(
  () => serializeSubtitles(originalSubtitles.value) !== serializeSubtitles(subtitlesWorkingCopy.value),
);
const isGeneratingSubtitles = computed(
  () => (promoVideo.value?.status === 'TRANSCRIBING') || aiGeneratingSubtitles.value,
);

const loadMyClubs = async () => {
  clubsLoading.value = true;
  try {
    myClubs.value = await fetchMyClubs();
    if (myClubs.value.length === 0) {
      selectedClubId.value = null;
      resetPromoVideoState();
      return;
    }
    if (!selectedClubId.value || !myClubs.value.some((club) => club.id === selectedClubId.value)) {
      selectedClubId.value = myClubs.value[0]?.id ?? null;
    }
  } finally {
    clubsLoading.value = false;
  }
};

const resetPromoVideoState = () => {
  revokeSubtitleTrack();
  promoVideo.value = null;
  promoVideoError.value = null;
  subtitlesWorkingCopy.value = [];
  originalSubtitles.value = [];
};

const setPromoVideo = (data: PromoVideo | null) => {
  revokeSubtitleTrack();
  promoVideo.value = data;
  if (!data) {
    subtitlesWorkingCopy.value = [];
    originalSubtitles.value = [];
    return;
  }
  const sortedSegments = [...(data.subtitles ?? [])]
    .sort((a, b) => a.sequence - b.sequence)
    .map(toEditableSubtitle);
  sortedSegments.forEach((segment, index) => {
    segment.sequence = index + 1;
  });
  subtitlesWorkingCopy.value = sortedSegments.map(cloneEditableSubtitle);
  originalSubtitles.value = sortedSegments.map(cloneEditableSubtitle);
  promoVideoError.value = null;
  rebuildSubtitleTrack();
};

const loadPromoVideo = async (clubId: number | null) => {
  resetPromoVideoState();
  if (!clubId) return;
  promoVideoLoading.value = true;
  try {
    const response = await fetchClubPromoVideo(clubId);
    setPromoVideo(response);
  } catch (error: any) {
    if (error?.response?.status === 404) {
      setPromoVideo(null);
    } else {
      promoVideoError.value = error?.response?.data?.message ?? '宣传视频加载失败，请稍后再试';
    }
  } finally {
    promoVideoLoading.value = false;
  }
};

const handleVideoBeforeUpload: UploadProps['beforeUpload'] = async (rawFile) => {
  if (!selectedClubId.value) {
    ElMessage.warning('请先选择社团');
    return false;
  }
  if (videoUploading.value) {
    ElMessage.warning('视频上传进行中，请稍候');
    return false;
  }
  if (rawFile.type !== 'video/mp4') {
    ElMessage.error('仅支持上传 mp4 格式视频');
    return false;
  }
  if (rawFile.size > 50 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过 50MB');
    return false;
  }
  try {
    videoUploading.value = true;
    const duration = await readVideoDuration(rawFile as File);
    const roundedDuration = Math.ceil(duration);
    if (roundedDuration > 30) {
      ElMessage.error('视频时长不能超过 30 秒');
      return false;
    }
    const result = await uploadClubPromoVideo(selectedClubId.value, rawFile as File, roundedDuration);
    setPromoVideo(result);
    ElMessage.success('宣传视频上传成功');
  } catch (error: any) {
    const message = error?.response?.data?.message ?? error?.message ?? '视频上传失败，请稍后再试';
    ElMessage.error(message);
  } finally {
    videoUploading.value = false;
  }
  return false;
};

const handleGenerateAiSubtitles = async () => {
  if (!selectedClubId.value || !promoVideo.value) {
    ElMessage.warning('请先上传宣传视频');
    return;
  }
  aiGeneratingSubtitles.value = true;
  promoVideo.value = { ...promoVideo.value, status: 'TRANSCRIBING' };
  try {
    const response = await generateClubPromoVideoSubtitles(selectedClubId.value);
    setPromoVideo(response);
    ElMessage.success('AI 字幕生成成功');
  } catch (error: any) {
    const message = error?.response?.data?.message ?? '生成字幕失败，请稍后再试';
    promoVideoError.value = message;
    ElMessage.error(message);
    if (promoVideo.value) {
      promoVideo.value = { ...promoVideo.value, status: 'FAILED' };
    }
  } finally {
    aiGeneratingSubtitles.value = false;
  }
};

const saveSubtitlesChanges = async () => {
  if (!selectedClubId.value || !promoVideo.value) {
    ElMessage.warning('请先上传宣传视频');
    return;
  }
  try {
    const segments: SubtitleSegment[] = subtitlesWorkingCopy.value.map((item, index) => {
      const text = item.text.trim();
      if (!text) {
        throw new Error(`第 ${index + 1} 行的字幕内容不能为空`);
      }
      const startMs = millisecondsFromSeconds(item.startSec);
      const endMs = millisecondsFromSeconds(item.endSec);
      if (endMs <= startMs) {
        throw new Error(`第 ${index + 1} 行的结束时间必须大于开始时间`);
      }
      return {
        sequence: index + 1,
        startMs,
        endMs,
        text,
        autoGenerated: item.autoGenerated,
      };
    });
    subtitlesSaving.value = true;
    const response = await saveClubPromoVideoSubtitles(selectedClubId.value, segments);
    setPromoVideo(response);
    ElMessage.success('字幕已保存');
  } catch (error: any) {
    if (error instanceof Error && error.message.startsWith('第')) {
      ElMessage.error(error.message);
    } else {
      const message = error?.response?.data?.message ?? '字幕保存失败，请稍后再试';
      ElMessage.error(message);
    }
  } finally {
    subtitlesSaving.value = false;
  }
};

const resetSubtitles = () => {
  subtitlesWorkingCopy.value = originalSubtitles.value.map(cloneEditableSubtitle);
  resequenceSubtitles();
};

const addSubtitleRow = () => {
  const last = subtitlesWorkingCopy.value[subtitlesWorkingCopy.value.length - 1];
  const start = last ? roundSeconds(last.endSec + 0.5) : 0;
  const end = roundSeconds(start + 2);
  subtitlesWorkingCopy.value = [
    ...subtitlesWorkingCopy.value,
    {
      id: null,
      sequence: subtitlesWorkingCopy.value.length + 1,
      startSec: start,
      endSec: end,
      text: '',
      autoGenerated: false,
    },
  ];
  resequenceSubtitles();
};

const removeSubtitleRow = (index: number) => {
  if (index < 0 || index >= subtitlesWorkingCopy.value.length) return;
  const list = [...subtitlesWorkingCopy.value];
  list.splice(index, 1);
  subtitlesWorkingCopy.value = list;
  resequenceSubtitles();
};

const moveSubtitleUp = (index: number) => {
  if (index <= 0) return;
  const list = [...subtitlesWorkingCopy.value];
  const current = list[index]!;
  const previous = list[index - 1]!;
  list[index - 1] = current;
  list[index] = previous;
  subtitlesWorkingCopy.value = list;
  resequenceSubtitles();
};

const moveSubtitleDown = (index: number) => {
  if (index >= subtitlesWorkingCopy.value.length - 1) return;
  const list = [...subtitlesWorkingCopy.value];
  const current = list[index]!;
  const next = list[index + 1]!;
  list[index] = next;
  list[index + 1] = current;
  subtitlesWorkingCopy.value = list;
  resequenceSubtitles();
};

const readVideoDuration = (file: File) =>
  new Promise<number>((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const video = document.createElement('video');
    video.preload = 'metadata';
    video.src = url;
    video.onloadedmetadata = () => {
      const duration = video.duration;
      URL.revokeObjectURL(url);
      resolve(duration);
    };
    video.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error('无法读取视频时长'));
    };
  });

const roundSeconds = (value: number) => Math.round(value * 1000) / 1000;

const millisecondsFromSeconds = (value: number) => Math.max(0, Math.round(value * 1000));

const formatPromoStatus = (status: PromoVideo['status']) => {
  switch (status) {
    case 'READY':
      return '字幕已就绪';
    case 'TRANSCRIBING':
      return '字幕生成中';
    case 'FAILED':
      return '生成失败';
    default:
      return '已上传';
  }
};

const promoStatusTagType = (status: PromoVideo['status']) => {
  switch (status) {
    case 'READY':
      return 'success';
    case 'TRANSCRIBING':
      return 'warning';
    case 'FAILED':
      return 'danger';
    default:
      return 'info';
  }
};

const formatVideoDuration = (seconds?: number | null) => {
  if (!seconds || seconds <= 0) {
    return '0 秒';
  }
  const rounded = Math.round(seconds);
  if (rounded < 60) {
    return `${rounded} 秒`;
  }
  const minutes = Math.floor(rounded / 60);
  const remain = rounded % 60;
  return `${minutes} 分 ${remain} 秒`;
};

const formatFileSize = (bytes?: number | null) => {
  if (!bytes || bytes <= 0) return '0 B';
  const kb = bytes / 1024;
  if (kb < 1024) {
    return `${kb.toFixed(kb >= 100 ? 0 : 1)} KB`;
  }
  const mb = kb / 1024;
  return `${mb.toFixed(mb >= 100 ? 0 : 1)} MB`;
};

const formatTimestamp = (value: string | null | undefined) => {
  if (!value) return '—';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const toVttTimestamp = (seconds: number) => {
  const totalMs = Math.max(0, Math.round(seconds * 1000));
  const hours = Math.floor(totalMs / 3600000);
  const minutes = Math.floor((totalMs % 3600000) / 60000);
  const secs = Math.floor((totalMs % 60000) / 1000);
  const millis = totalMs % 1000;
  const hh = String(hours).padStart(2, '0');
  const mm = String(minutes).padStart(2, '0');
  const ss = String(secs).padStart(2, '0');
  const ms = String(millis).padStart(3, '0');
  return `${hh}:${mm}:${ss}.${ms}`;
};

const buildVttContent = (segments: EditableSubtitle[]) => {
  const lines: string[] = ['WEBVTT', ''];
  const sorted = [...segments].sort((a, b) => a.sequence - b.sequence);
  sorted.forEach((segment, index) => {
    lines.push(`${index + 1}`);
    lines.push(`${toVttTimestamp(segment.startSec)} --> ${toVttTimestamp(segment.endSec)}`);
    lines.push(segment.text);
    lines.push('');
  });
  return lines.join('\n');
};

const revokeSubtitleTrack = () => {
  if (subtitleTrackUrl.value) {
    URL.revokeObjectURL(subtitleTrackUrl.value);
    subtitleTrackUrl.value = null;
  }
};

const rebuildSubtitleTrack = () => {
  revokeSubtitleTrack();
  if (!promoVideo.value || !subtitlesWorkingCopy.value.length) {
    return;
  }
  const vtt = buildVttContent(subtitlesWorkingCopy.value);
  subtitleTrackUrl.value = URL.createObjectURL(new Blob([vtt], { type: 'text/vtt' }));
};

function serializeSubtitles(list: EditableSubtitle[]): string {
  return JSON.stringify(
    [...list]
      .sort((a, b) => a.sequence - b.sequence)
      .map((item) => ({
        seq: item.sequence,
        start: millisecondsFromSeconds(item.startSec),
        end: millisecondsFromSeconds(item.endSec),
        text: item.text.trim(),
        auto: !!item.autoGenerated,
      })),
  );
}

const cloneEditableSubtitle = (subtitle: EditableSubtitle): EditableSubtitle => ({
  id: subtitle.id ?? null,
  sequence: subtitle.sequence,
  startSec: roundSeconds(subtitle.startSec),
  endSec: roundSeconds(subtitle.endSec),
  text: subtitle.text,
  autoGenerated: subtitle.autoGenerated,
});

const toEditableSubtitle = (segment: SubtitleSegment): EditableSubtitle => ({
  id: segment.id ?? null,
  sequence: segment.sequence,
  startSec: roundSeconds(segment.startMs / 1000),
  endSec: roundSeconds(segment.endMs / 1000),
  text: segment.text,
  autoGenerated: segment.autoGenerated,
});

const resequenceSubtitles = () => {
  const sorted = [...subtitlesWorkingCopy.value].sort((a, b) => {
    if (a.sequence !== b.sequence) {
      return a.sequence - b.sequence;
    }
    return a.startSec - b.startSec;
  });
  sorted.forEach((item, index) => {
    item.sequence = index + 1;
  });
  subtitlesWorkingCopy.value = sorted;
};

watch(
  subtitlesWorkingCopy,
  () => {
    rebuildSubtitleTrack();
  },
  { deep: true },
);

watch(
  () => selectedClubId.value,
  async (clubId) => {
    await loadPromoVideo(clubId ?? null);
  },
  { immediate: true },
);

onMounted(async () => {
  await auth.bootstrap();
  await loadMyClubs();
});

onBeforeUnmount(() => {
  revokeSubtitleTrack();
});
</script>

<style scoped>
.promo-video-manager {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.promo-video-manager__card {
  border-radius: 12px;
}

.promo-video-manager__card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.promo-video-manager__title {
  font-weight: 600;
}

.promo-video-manager__subtitle {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.promo-video-manager__controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.promo-video-card {
  border-radius: 12px;
}

.promo-video-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.promo-video-card__header--actions {
  flex-wrap: wrap;
}

.promo-video-card__title {
  font-weight: 600;
}

.promo-video-card__subtitle {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.promo-video-upload {
  margin-top: 12px;
}

.promo-video-upload__icon {
  font-size: 36px;
  color: var(--ccm-primary, #2563eb);
}

.promo-video-card__alert {
  margin-top: 12px;
}

.promo-video-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.promo-video-preview__player {
  width: 100%;
  max-height: 360px;
  border-radius: 12px;
  background: #000;
}

.promo-video-preview__meta {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #475569;
}

.promo-video-subtitles__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.promo-video-subtitles__hint {
  font-size: 13px;
  color: #94a3b8;
}

.promo-video-subtitles__order-controls {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.promo-video-subtitles__table :deep(.el-table__cell) {
  vertical-align: top;
}
</style>
