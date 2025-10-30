<template>
  <div class="club-mgmt">
    <h2>我的社团管理</h2>

    <el-select v-model="currentClubId" placeholder="选择社团" style="width: 320px" @change="handleClubChange">
      <el-option v-for="club in myClubs" :key="club.id" :label="club.name" :value="club.id" />
    </el-select>

    <el-card
      v-if="currentClubId && clubDetail"
      shadow="never"
      class="club-info-card"
    >
      <template #header>
        <div class="club-info-card__header">
          <div>
            <span class="club-info-card__title">社团信息</span>
            <p class="club-info-card__subtitle">在这里快速浏览社团概况，支持随时调整详情。</p>
          </div>
          <el-button type="primary" @click="openClubInfoDialog">编辑信息</el-button>
        </div>
      </template>
      <div class="club-info-card__body">
        <div class="club-info-card__meta">
          <el-avatar
            v-if="clubDetail.logoUrl"
            :src="clubDetail.logoUrl"
            :size="96"
            shape="square"
            fit="cover"
            class="club-info-card__avatar"
          />
          <el-avatar
            v-else
            :size="96"
            shape="square"
            class="club-info-card__avatar club-info-card__avatar--placeholder"
          >
            {{ clubDetail.name.slice(0, 2) }}
          </el-avatar>
          <div>
            <h3>{{ clubDetail.name }}</h3>
            <p class="club-info-card__description">
              {{ clubDetail.description }}
            </p>
          </div>
        </div>
        <el-descriptions
          v-if="clubDetail"
          :column="2"
          border
          size="small"
          class="club-info-card__descriptions"
        >
          <el-descriptions-item label="类别">
            {{ clubDetail.category ?? '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="成立日期">
            {{ clubDetail.foundedDate ? formatDate(clubDetail.foundedDate) : '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系邮箱">
            {{ clubDetail.contactEmail ?? '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ clubDetail.contactPhone ?? '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="宣传视频链接" :span="2">
            <el-link
              v-if="clubDetail.promoVideoUrl"
              type="primary"
              :href="clubDetail.promoVideoUrl"
              target="_blank"
            >
              打开预览
            </el-link>
            <span v-else>未填写</span>
          </el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <el-space wrap v-if="(clubDetail.tags ?? []).length">
              <el-tag v-for="tag in clubDetail.tags ?? []" :key="tag" effect="light">{{ tag }}</el-tag>
            </el-space>
            <span v-else>未设置</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <div v-if="!currentClubId" class="club-mgmt__empty">
      <el-empty description="请选择一个社团进行管理" />
    </div>

    <el-tabs v-else v-model="activeTab" @tab-change="handleTabChange" class="club-mgmt__tabs">
      <el-tab-pane label="成员管理" name="members">
        <el-card shadow="never">
          <el-table :data="members" v-loading="membersLoading" empty-text="暂无数据">
            <el-table-column prop="fullName" label="姓名" min-width="140" />
            <el-table-column prop="email" label="邮箱" min-width="200" />
            <el-table-column label="角色" width="140">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ formatRole(row.membershipRole) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="加入时间" width="180">
              <template #default="{ row }">
                {{ formatDatetime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="入团审核" name="applicants">
        <el-card shadow="never">
          <el-table :data="applicants" v-loading="applicantsLoading" empty-text="暂无待审核">
            <el-table-column prop="fullName" label="姓名" min-width="140" />
            <el-table-column prop="applicationReason" label="原因" min-width="200" />
            <el-table-column label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatDatetime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button type="success" link @click="decideMembership(row.id, true)">通过</el-button>
                <el-button type="danger" link @click="decideMembership(row.id, false)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="活动管理" name="activities">
        <el-button type="primary" @click="openActivityModal()">发布活动</el-button>
        <el-table :data="clubActivities" v-loading="activitiesLoading" class="club-mgmt__table" empty-text="暂无活动">
          <el-table-column prop="title" label="活动主题" min-width="160" />
          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="{ row }">
              {{ formatDatetime(row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="地点" min-width="140" />
          <el-table-column label="报名人数" width="120">
            <template #default="{ row }">
              {{ row.attendeeCount }} 人
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button link type="primary" @click="openActivityModal(row)">编辑</el-button>
              <el-button link type="success" @click="gotoCheckIn(row.id)">签到管理</el-button>
              <el-button link type="danger" @click="deleteActivityById(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="签到管理" name="attendance">
        <div class="attendance-panel">
          <el-form inline label-width="90px">
            <el-form-item label="活动">
              <el-select
                v-model="attendanceSelectedActivityId"
                placeholder="选择活动"
                :loading="attendanceActivitiesLoading"
                style="width: 320px"
                @change="loadAttendanceRegistrations"
              >
                <el-option
                  v-for="activity in clubActivities"
                  :key="activity.id"
                  :label="`${activity.title} (${formatDatetime(activity.startTime)})`"
                  :value="activity.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="attendanceLoading"
                :disabled="!attendanceSelectedActivityId || !attendanceSelection.length"
                @click="submitManualCheckIn"
              >
                手动签到
              </el-button>
              <el-button
                :disabled="!attendanceSelectedActivityId"
                @click="downloadAttendance"
              >
                导出报表
              </el-button>
            </el-form-item>
          </el-form>

          <el-table
            v-loading="attendanceLoading"
            :data="attendanceRegistrations"
            @selection-change="onAttendanceSelectionChange"
            empty-text="请选择活动查看报名信息"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="attendeeName" label="姓名" min-width="140" />
            <el-table-column prop="attendeeEmail" label="邮箱" min-width="200" />
            <el-table-column label="报名状态" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatDatetime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="积分管理" name="points">
        <div class="points-panel">
          <el-card class="points-panel__form" shadow="never">
            <template #header>
              <span>积分变动</span>
            </template>
            <el-form label-width="88px">
              <el-form-item label="成员">
                <el-select
                  v-model="pointForm.memberId"
                  placeholder="选择成员"
                  filterable
                  style="width: 260px"
                >
                <el-option
                  v-for="member in members"
                  :key="member.userId"
                  :label="member.fullName ?? member.memberName ?? member.memberEmail ?? '未命名成员'"
                  :value="member.userId"
                />
                </el-select>
              </el-form-item>
              <el-form-item label="积分变动">
                <el-input-number v-model="pointForm.points" :step="5" />
              </el-form-item>
              <el-form-item label="原因">
                <el-input v-model="pointForm.reason" maxlength="200" show-word-limit />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pointSubmitting" @click="submitPointRecord">记录积分</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <div class="points-panel__content">
            <el-card shadow="never" class="points-panel__leaderboard">
              <template #header>
                <span>积分排行榜</span>
              </template>
              <el-table :data="pointLeaderboard" size="small" empty-text="暂无数据">
                <el-table-column type="index" width="60" label="#" />
                <el-table-column prop="memberName" label="姓名" min-width="140" />
                <el-table-column prop="totalPoints" label="总积分" width="120" />
              </el-table>
            </el-card>

            <el-card shadow="never" class="points-panel__records">
              <template #header>
                <span>积分记录</span>
              </template>
              <el-table :data="pointRecords" v-loading="pointsLoading" empty-text="暂无记录">
                <el-table-column prop="memberName" label="姓名" min-width="140" />
                <el-table-column prop="points" label="变动" width="100">
                  <template #default="{ row }">
                    <span :class="row.points >= 0 ? 'points-positive' : 'points-negative'">
                      {{ row.points >= 0 ? `+${row.points}` : row.points }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="reason" label="原因" min-width="200" />
                <el-table-column label="记录人" width="140">
                  <template #default="{ row }">
                    {{ row.createdByName ?? '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="时间" width="180">
                  <template #default="{ row }">
                    {{ formatDatetime(row.createdAt) }}
                  </template>
                </el-table-column>
              </el-table>
              <div class="points-panel__pagination" v-if="pointPagination.total > pointPagination.size">
                <el-pagination
                  layout="prev, pager, next"
                  :current-page="pointPagination.page"
                  :page-size="pointPagination.size"
                  :total="pointPagination.total"
                  @current-change="handlePointPageChange"
                />
              </div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="任务分配" name="tasks">
        <div class="tasks-panel">
          <el-button type="primary" @click="openTaskDialog()">创建任务</el-button>
          <el-table :data="taskList" v-loading="tasksLoading" empty-text="暂无任务" class="tasks-panel__table">
            <el-table-column prop="title" label="任务" min-width="160" />
            <el-table-column label="截止" width="180">
              <template #default="{ row }">
                {{ row.dueAt ? formatDatetime(row.dueAt) : '未设置' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="负责人" min-width="220">
              <template #default="{ row }">
                <div class="tasks-panel__assignees" v-if="row.assignments?.length">
                  <el-dropdown
                    v-for="assign in row.assignments"
                    :key="assign.assignmentId"
                    trigger="click"
                    @command="(cmd: string) => updateAssignmentStatusQuick(row, assign, cmd as any)"
                  >
                    <span class="tasks-panel__assignment">
                      <el-tag
                        size="small"
                        :type="assign.status === 'COMPLETED' ? 'success' : assign.status === 'IN_PROGRESS' ? 'warning' : 'info'"
                      >
                        {{ assign.userName }} ({{ assign.status === 'NOT_STARTED' ? '未开始' : assign.status === 'IN_PROGRESS' ? '进行中' : '已完成' }})
                      </el-tag>
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="NOT_STARTED">标记为未开始</el-dropdown-item>
                        <el-dropdown-item command="IN_PROGRESS">标记为进行中</el-dropdown-item>
                        <el-dropdown-item command="COMPLETED">标记为已完成</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="openTaskDialog(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tasks-panel__pagination" v-if="taskPagination.total > taskPagination.size">
            <el-pagination
              layout="prev, pager, next"
              :current-page="taskPagination.page"
              :page-size="taskPagination.size"
              :total="taskPagination.total"
              @current-change="handleTaskPageChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="消息公告" name="messages">
        <el-button type="primary" @click="openBroadcastModal">发公告</el-button>
        <el-table :data="clubMessages" v-loading="messagesLoading" empty-text="暂无公告">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="content" label="内容" min-width="240" />
          <el-table-column label="发送时间" width="180">
            <template #default="{ row }">
              {{ formatDatetime(row.createdAt) }}
            </template>
          </el-table-column>
      </el-table>
    </el-tab-pane>
  </el-tabs>

    <el-dialog
      v-model="clubInfoDialogVisible"
      title="编辑社团信息"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="clubInfoForm"
        :rules="clubInfoRules"
        ref="clubInfoFormRef"
        label-width="96px"
      >
        <el-form-item label="社团名称" prop="name">
          <el-input v-model="clubInfoForm.name" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="社团简介" prop="description">
          <el-input
            type="textarea"
            :rows="4"
            v-model="clubInfoForm.description"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="社团 Logo">
          <div class="club-logo-field">
            <el-image
              v-if="clubInfoForm.logoUrl"
              :src="clubInfoForm.logoUrl"
              fit="cover"
              class="club-logo-field__preview"
            />
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :http-request="handleLogoUpload"
            >
              <el-button type="primary" :loading="logoUploading">上传 Logo</el-button>
            </el-upload>
            <el-button v-if="clubInfoForm.logoUrl" text type="danger" @click="clearLogo">移除</el-button>
            <span class="form-item__hint">建议正方形图片，≤ 5MB。</span>
          </div>
        </el-form-item>
        <el-form-item label="类别">
          <el-input v-model="clubInfoForm.category" maxlength="60" placeholder="如 科技 / 公益" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="clubInfoForm.tags"
            multiple
            filterable
            placeholder="选择或搜索标签"
          >
            <el-option v-for="tag in clubTagOptions" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="clubInfoForm.contactEmail" placeholder="example@campus.edu" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="clubInfoForm.contactPhone" placeholder="010-12345678" />
        </el-form-item>
        <el-form-item label="成立日期">
          <el-date-picker
            v-model="clubInfoForm.foundedDate"
            type="date"
            placeholder="选择日期"
          />
        </el-form-item>
        <el-form-item label="宣传视频">
          <el-input v-model="clubInfoForm.promoVideoUrl" placeholder="https://" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clubInfoDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="clubInfoSaving" @click="saveClubInfo">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="activityDialogVisible" :title="activityForm.id ? '编辑活动' : '发布活动'" width="600px">
      <el-form :model="activityForm" :rules="activityRules" ref="activityFormRef" label-width="90px">
        <el-form-item label="活动主题" prop="title">
          <el-input v-model="activityForm.title" />
        </el-form-item>
        <el-form-item label="活动时间" prop="time">
          <el-date-picker
            v-model="activityForm.time"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="activityForm.location" />
        </el-form-item>
        <el-form-item label="活动海报">
          <div class="activity-banner-field">
            <el-upload
              class="activity-banner-field__upload"
              :show-file-list="false"
              :auto-upload="false"
              :http-request="handleBannerUpload"
            >
              <el-button type="primary" :loading="bannerUploading">上传图片</el-button>
            </el-upload>
            <el-button
              v-if="activityForm.bannerUrl"
              class="activity-banner-field__remove"
              text
              type="danger"
              @click="clearBanner"
            >
              移除
            </el-button>
          </div>
          <span class="form-item__hint">建议 4:3 比例，支持 JPG/PNG/WebP，≤5MB</span>
          <el-image
            v-if="activityForm.bannerUrl"
            class="activity-banner-field__preview"
            :src="activityForm.bannerUrl"
            fit="cover"
          />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="activityForm.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="需要审批">
          <el-switch v-model="activityForm.requiresApproval" />
        </el-form-item>
        <el-form-item label="活动介绍" prop="description">
          <el-input type="textarea" :rows="4" v-model="activityForm.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="activityDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="activitySaving" @click="submitActivity">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="broadcastDialogVisible" title="发送公告" width="520px">
      <el-form :model="broadcastForm" :rules="broadcastRules" ref="broadcastFormRef" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="broadcastForm.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" :rows="4" v-model="broadcastForm.content" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="broadcastDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="broadcastSaving" @click="submitBroadcast">发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskDialogVisible" :title="taskEditingId ? '编辑任务' : '创建任务'" width="560px">
      <el-form :model="taskForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="taskForm.title" maxlength="150" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" :rows="4" v-model="taskForm.description" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="taskForm.dueAt" type="datetime" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="taskForm.assigneeIds" multiple filterable placeholder="选择成员">
            <el-option
              v-for="member in members"
              :key="member.userId"
              :label="member.fullName ?? member.memberName ?? member.memberEmail ?? '未命名成员'"
              :value="member.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="taskSaving" @click="submitTask">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import dayjs from 'dayjs';
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import {
  fetchMyClubs,
  fetchClubActivities,
  createActivity,
  updateActivity,
  deleteActivity,
} from '../../api/clubManagement';
import {
  fetchClubApplicants,
  fetchClubMembers,
  approveMembership,
} from '../../api/membershipAdmin';
import { fetchClubMessages, broadcastClubMessage } from '../../api/messageAdmin';
import type {
  ClubSummary,
  ClubDetail,
  ActivitySummary,
  MembershipRecord,
  MembershipAdminResponse,
  AnnouncementRecord,
  ActivityRegistrationResponse,
  PointRecord,
  PointLeaderboardEntry,
  ClubTaskItem,
  ClubTaskAssignment,
} from '../../types/models';
import { uploadImage } from '../../api/storage';
import {
  fetchActivityDetail,
  fetchRegistrations,
  manualCheckIn,
  exportAttendance,
} from '../../api/activity';
import { fetchPointRecords, addPointRecord, fetchPointLeaderboard } from '../../api/points';
import { listTasks, createTask, updateTask, updateAssignmentStatus } from '../../api/tasks';
import { fetchClub, updateClub } from '../../api/club';
import api from '../../api/http';

const route = useRoute();
const getRouteTab = () => {
  const metaTab = route.meta.defaultTab as string | undefined;
  const queryTab = (route.query.tab as string | undefined) ?? null;
  return metaTab ?? queryTab ?? 'members';
};

const activeTab = ref(getRouteTab());
const myClubs = ref<ClubSummary[]>([]);
const currentClubId = ref<number | null>(null);

const clubDetail = ref<ClubDetail | null>(null);
const clubInfoFormRef = ref<FormInstance>();
const clubInfoSaving = ref(false);
const logoUploading = ref(false);
const clubTagOptions = ref<string[]>([]);
const clubInfoDialogVisible = ref(false);

const clubInfoForm = reactive({
  name: '',
  description: '',
  category: '',
  contactEmail: '',
  contactPhone: '',
  foundedDate: null as Date | null,
  promoVideoUrl: '',
  logoUrl: '',
  tags: [] as string[],
});

const router = useRouter();

const membersLoading = ref(false);
const applicantsLoading = ref(false);
const activitiesLoading = ref(false);
const messagesLoading = ref(false);

const members = ref<MembershipAdminResponse[]>([]);
const applicants = ref<MembershipRecord[]>([]);
const clubActivities = ref<ActivitySummary[]>([]);
const clubMessages = ref<AnnouncementRecord[]>([]);

const activityDialogVisible = ref(false);
const broadcastDialogVisible = ref(false);
const activityFormRef = ref<FormInstance>();
const broadcastFormRef = ref<FormInstance>();
const activitySaving = ref(false);
const broadcastSaving = ref(false);
const bannerUploading = ref(false);

const attendanceLoading = ref(false);
const attendanceActivitiesLoading = ref(false);
const attendanceSelectedActivityId = ref<number | null>(null);
const attendanceRegistrations = ref<ActivityRegistrationResponse[]>([]);
const attendanceSelection = ref<number[]>([]);

const pointsLoading = ref(false);
const pointRecords = ref<PointRecord[]>([]);
const pointLeaderboard = ref<PointLeaderboardEntry[]>([]);
const pointPagination = reactive({ page: 1, size: 10, total: 0 });
const pointForm = reactive({ memberId: undefined as number | undefined, points: 0, reason: '' });
const pointSubmitting = ref(false);

const tasksLoading = ref(false);
const taskList = ref<ClubTaskItem[]>([]);
const taskPagination = reactive({ page: 1, size: 10, total: 0 });
const taskDialogVisible = ref(false);
const taskEditingId = ref<number | null>(null);
const taskForm = reactive({
  title: '',
  description: '',
  dueAt: null as Date | null,
  assigneeIds: [] as number[],
});
const taskSaving = ref(false);

const activityForm = reactive({
  id: undefined as number | undefined,
  title: '',
  description: '',
  location: '',
  capacity: undefined as number | undefined,
  requiresApproval: true,
  time: [] as [Date, Date] | [],
  bannerUrl: '' as string,
});

const broadcastForm = reactive({
  title: '',
  content: '',
});

const activityRules: FormRules<typeof activityForm> = {
  title: [{ required: true, message: '请输入活动主题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动介绍', trigger: 'blur' }],
  time: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
};

const broadcastRules: FormRules<typeof broadcastForm> = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
};

const clubInfoRules: FormRules<typeof clubInfoForm> = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入社团简介', trigger: 'blur' }],
  contactEmail: [{ type: 'email', message: '请输入合法的邮箱地址', trigger: 'blur' }],
  tags: [{ type: 'array', max: 10, message: '最多选择10个标签', trigger: 'change' }],
};

const formatRole = (role: string) => {
  switch (role) {
    case 'LEADER':
      return '负责人';
    case 'STAFF':
      return '干事';
    case 'ADVISOR':
      return '指导老师';
    default:
      return '成员';
  }
};

const formatDatetime = (value?: string | null) => {
  if (!value) {
    return '-';
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const formatDate = (value?: string | null) => {
  if (!value) {
    return '未填写';
  }
  return dayjs(value).format('YYYY-MM-DD');
};

const loadMyClubList = async () => {
  myClubs.value = await fetchMyClubs();
  const firstClub = myClubs.value[0];
  if (!currentClubId.value && firstClub) {
    currentClubId.value = firstClub.id;
    await loadClubTagOptions();
    attendanceSelectedActivityId.value = null;
    attendanceRegistrations.value = [];
    pointPagination.page = 1;
    taskPagination.page = 1;
    await loadClubData();
  } else if (currentClubId.value) {
    await loadClubTagOptions();
    await loadClubInfo();
  }
};

const loadMembers = async () => {
  const clubId = currentClubId.value;
  if (!clubId) return;
  membersLoading.value = true;
  try {
    members.value = await fetchClubMembers(clubId);
  } finally {
    membersLoading.value = false;
  }
};

const loadApplicants = async () => {
  const clubId = currentClubId.value;
  if (!clubId) return;
  applicantsLoading.value = true;
  try {
    applicants.value = await fetchClubApplicants(clubId);
  } finally {
    applicantsLoading.value = false;
  }
};

const loadActivities = async () => {
  if (!currentClubId.value) return;
  activitiesLoading.value = true;
  try {
    const clubId = currentClubId.value;
    if (!clubId) return;
    const result = await fetchClubActivities(clubId);
    clubActivities.value = result.content;
  } finally {
    activitiesLoading.value = false;
  }
};

const loadMessages = async () => {
  if (!currentClubId.value) return;
  messagesLoading.value = true;
  try {
    const clubId = currentClubId.value;
    if (!clubId) return;
    clubMessages.value = await fetchClubMessages(clubId);
  } finally {
    messagesLoading.value = false;
  }
};

const ensureAttendanceActivity = () => {
  if (!attendanceSelectedActivityId.value && clubActivities.value.length) {
    const first = clubActivities.value[0];
    if (first) {
      attendanceSelectedActivityId.value = first.id;
    }
  }
};

const loadAttendanceRegistrations = async () => {
  if (!currentClubId.value) return;
  ensureAttendanceActivity();
  const activityId = attendanceSelectedActivityId.value;
  if (!activityId) {
    attendanceRegistrations.value = [];
    return;
  }
  attendanceLoading.value = true;
  try {
    const result = await fetchRegistrations(activityId, { page: 0, size: 200 });
    attendanceRegistrations.value = result.content;
    attendanceSelection.value = [];
  } finally {
    attendanceLoading.value = false;
  }
};

const onAttendanceSelectionChange = (selection: ActivityRegistrationResponse[]) => {
  attendanceSelection.value = selection
    .map((item) => item.attendeeId)
    .filter((id): id is number => typeof id === 'number');
};

const submitManualCheckIn = async () => {
  if (!attendanceSelectedActivityId.value || !attendanceSelection.value.length) return;
  attendanceLoading.value = true;
  try {
    await manualCheckIn(attendanceSelectedActivityId.value, {
      attendeeIds: attendanceSelection.value,
    });
    ElMessage.success('签到完成');
    await loadAttendanceRegistrations();
    await loadActivities(); // refresh counts
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '签到失败，请稍后再试');
  } finally {
    attendanceLoading.value = false;
  }
};

const downloadAttendance = async () => {
  if (!attendanceSelectedActivityId.value) return;
  try {
    const buffer = await exportAttendance(attendanceSelectedActivityId.value);
    const blob = new Blob([buffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `attendance-${attendanceSelectedActivityId.value}.xlsx`;
    link.click();
    URL.revokeObjectURL(url);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '导出失败，请稍后再试');
  }
};

const loadPointData = async () => {
  if (!currentClubId.value) return;
  pointsLoading.value = true;
  try {
    const [records, leaderboard] = await Promise.all([
      fetchPointRecords(currentClubId.value, {
        page: pointPagination.page - 1,
        size: pointPagination.size,
      }),
      fetchPointLeaderboard(currentClubId.value, 20),
    ]);
    pointRecords.value = records.content;
    pointPagination.total = records.totalElements;
    pointLeaderboard.value = leaderboard;
  } finally {
    pointsLoading.value = false;
  }
};

const handlePointPageChange = async (page: number) => {
  pointPagination.page = page;
  await loadPointData();
};

const resetPointForm = () => {
  pointForm.memberId = undefined;
  pointForm.points = 0;
  pointForm.reason = '';
};

const submitPointRecord = async () => {
  if (!currentClubId.value || !pointForm.memberId || !pointForm.reason) {
    ElMessage.warning('请完善积分记录信息');
    return;
  }
  if (!pointForm.points) {
    ElMessage.warning('积分变动值不能为 0');
    return;
  }
  pointSubmitting.value = true;
  try {
    await addPointRecord(currentClubId.value, {
      memberId: pointForm.memberId,
      points: pointForm.points,
      reason: pointForm.reason,
    });
    ElMessage.success('积分记录成功');
    resetPointForm();
    await loadPointData();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '记录失败，请稍后再试');
  } finally {
    pointSubmitting.value = false;
  }
};

const loadTasks = async () => {
  if (!currentClubId.value) return;
  tasksLoading.value = true;
  try {
    const result = await listTasks(currentClubId.value, {
      page: taskPagination.page - 1,
      size: taskPagination.size,
    });
    taskList.value = result.content;
    taskPagination.total = result.totalElements;
  } finally {
    tasksLoading.value = false;
  }
};

const handleTaskPageChange = async (page: number) => {
  taskPagination.page = page;
  await loadTasks();
};

const openTaskDialog = (task?: ClubTaskItem) => {
  if (task) {
    taskEditingId.value = task.id;
    taskForm.title = task.title;
    taskForm.description = task.description ?? '';
    taskForm.dueAt = task.dueAt ? new Date(task.dueAt) : null;
    taskForm.assigneeIds = task.assignments?.map((assignment) => assignment.userId) ?? [];
  } else {
    taskEditingId.value = null;
    taskForm.title = '';
    taskForm.description = '';
    taskForm.dueAt = null;
    taskForm.assigneeIds = [];
  }
  taskDialogVisible.value = true;
};

const submitTask = async () => {
  if (!currentClubId.value) return;
  if (!taskForm.title) {
    ElMessage.warning('请填写任务标题');
    return;
  }
  taskSaving.value = true;
  try {
    const payload: Record<string, unknown> = {
      title: taskForm.title,
      description: taskForm.description,
      dueAt: taskForm.dueAt ? taskForm.dueAt.toISOString() : undefined,
      assigneeIds: taskForm.assigneeIds,
    };
    if (taskEditingId.value) {
      await updateTask(currentClubId.value, taskEditingId.value, payload);
    } else {
      await createTask(currentClubId.value, payload);
      taskPagination.page = 1;
    }
    ElMessage.success('任务保存成功');
    taskDialogVisible.value = false;
    await loadTasks();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '保存失败，请稍后再试');
  } finally {
    taskSaving.value = false;
  }
};

const updateAssignmentStatusQuick = async (
  task: ClubTaskItem,
  assignment: ClubTaskAssignment,
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED',
) => {
  if (!currentClubId.value) return;
  try {
    await updateAssignmentStatus(currentClubId.value, task.id, assignment.assignmentId, {
      status,
      remark: assignment.remark ?? undefined,
    });
    await loadTasks();
    ElMessage.success('任务状态已更新');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '更新失败，请稍后再试');
  }
};

const loadClubTagOptions = async () => {
  if (clubTagOptions.value.length) return;
  try {
    const { data } = await api.get<string[]>('/api/auth/tags');
    clubTagOptions.value = data;
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '标签选项加载失败');
  }
};

const hydrateClubInfoForm = (detail: ClubDetail) => {
  clubInfoForm.name = detail.name;
  clubInfoForm.description = detail.description;
  clubInfoForm.category = detail.category ?? '';
  clubInfoForm.contactEmail = detail.contactEmail ?? '';
  clubInfoForm.contactPhone = detail.contactPhone ?? '';
  clubInfoForm.promoVideoUrl = detail.promoVideoUrl ?? '';
  clubInfoForm.logoUrl = detail.logoUrl ?? '';
  clubInfoForm.tags = [...(detail.tags ?? [])];
  clubInfoForm.foundedDate = detail.foundedDate ? new Date(detail.foundedDate) : null;
};

const loadClubInfo = async () => {
  const clubId = currentClubId.value;
  if (!clubId) return;
  try {
    clubDetail.value = await fetchClub(clubId);
    const detail = clubDetail.value;
    hydrateClubInfoForm(detail);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '加载社团信息失败');
  }
};

const openClubInfoDialog = () => {
  if (clubDetail.value) {
    hydrateClubInfoForm(clubDetail.value);
  }
  clubInfoFormRef.value?.clearValidate();
  clubInfoDialogVisible.value = true;
};

const loadClubData = async () => {
  if (!currentClubId.value) return;
  await loadClubInfo();
  switch (activeTab.value) {
    case 'members':
      await loadMembers();
      break;
    case 'applicants':
      await loadApplicants();
      break;
    case 'activities':
      await loadActivities();
      break;
    case 'attendance':
      await loadActivities();
      ensureAttendanceActivity();
      await loadAttendanceRegistrations();
      break;
    case 'points':
      await loadMembers();
      await loadPointData();
      break;
    case 'tasks':
      await loadMembers();
      await loadTasks();
      break;
    case 'messages':
      await loadMessages();
      break;
  }
};

const handleClubChange = async () => {
  attendanceSelectedActivityId.value = null;
  attendanceRegistrations.value = [];
  attendanceSelection.value = [];
  pointPagination.page = 1;
  taskPagination.page = 1;
  await loadClubData();
};

const handleTabChange = async (tab: string | number) => {
  const tabName = tab as string;
  activeTab.value = tabName;
  if (tabName === 'activities') {
    if (route.name !== 'club-manage-activities') {
      await router.replace({ name: 'club-manage-activities' });
    }
  } else {
    if (route.name !== 'club-manage' || (tabName === 'members' && route.query.tab) || (tabName !== 'members' && route.query.tab !== tabName)) {
      await router.replace({ name: 'club-manage', query: tabName !== 'members' ? { tab: tabName } : {} });
    }
  }
  await loadClubData();
};

const saveClubInfo = () => {
  if (!currentClubId.value) return;
  clubInfoFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    clubInfoSaving.value = true;
    try {
      const payload = {
        name: clubInfoForm.name,
        description: clubInfoForm.description,
        category: clubInfoForm.category || undefined,
        logoUrl: clubInfoForm.logoUrl || undefined,
        promoVideoUrl: clubInfoForm.promoVideoUrl || undefined,
        contactEmail: clubInfoForm.contactEmail || undefined,
        contactPhone: clubInfoForm.contactPhone || undefined,
        foundedDate: clubInfoForm.foundedDate
          ? dayjs(clubInfoForm.foundedDate).format('YYYY-MM-DD')
          : undefined,
        tags: clubInfoForm.tags,
      };
      const updated = await updateClub(currentClubId.value!, payload);
      clubDetail.value = updated;
      hydrateClubInfoForm(updated);
      const index = myClubs.value.findIndex((club) => club.id === updated.id);
      if (index >= 0) {
        const existing = myClubs.value[index];
        if (existing) {
          myClubs.value.splice(index, 1, {
            ...existing,
            name: updated.name,
            description: updated.description,
            category: updated.category ?? '',
            logoUrl: updated.logoUrl ?? null,
            tags: updated.tags ?? [],
          });
        }
      }
      ElMessage.success('社团信息已更新');
      clubInfoDialogVisible.value = false;
      clubInfoFormRef.value?.clearValidate();
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '更新失败，请稍后再试');
    } finally {
      clubInfoSaving.value = false;
    }
  });
};

const handleLogoUpload = async (options: UploadRequestOptions) => {
  const { file, onError, onSuccess } = options;
  try {
    logoUploading.value = true;
    const result = await uploadImage(file as File, 'clubs/logos');
    clubInfoForm.logoUrl = result.url;
    onSuccess?.(result);
    ElMessage.success('Logo 上传成功');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? 'Logo 上传失败，请稍后再试');
    onError?.(error);
  } finally {
    logoUploading.value = false;
  }
};

const clearLogo = () => {
  clubInfoForm.logoUrl = '';
};

const gotoCheckIn = (activityId: number) => {
  if (!currentClubId.value) {
    ElMessage.warning('请先选择社团');
    return;
  }
  router.push({ name: 'club-checkin-manager', query: { clubId: currentClubId.value, activityId } });
};

const decideMembership = async (membershipId: number, approve: boolean) => {
  try {
    await approveMembership(membershipId, approve);
    ElMessage.success('操作成功');
    await Promise.all([loadMembers(), loadApplicants()]);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '操作失败，请稍后再试');
  }
};

const openActivityModal = async (activity?: ActivitySummary) => {
  if (activity) {
    activityForm.id = activity.id;
    activityForm.title = activity.title;
    activityForm.description = activity.description ?? '';
    activityForm.location = activity.location ?? '';
    activityForm.capacity = activity.capacity ?? undefined;
    activityForm.requiresApproval = activity.requiresApproval ?? true;
    activityForm.bannerUrl = '';
    activityForm.time = activity.startTime && activity.endTime
      ? [new Date(activity.startTime), new Date(activity.endTime)]
      : [];

    try {
      const detail = await fetchActivityDetail(activity.id);
      activityForm.description = detail.description ?? '';
      activityForm.location = detail.location ?? '';
      activityForm.capacity = detail.capacity ?? undefined;
      activityForm.requiresApproval = detail.requiresApproval ?? true;
      activityForm.time = detail.startTime && detail.endTime
        ? [new Date(detail.startTime), new Date(detail.endTime)]
        : [];
      activityForm.bannerUrl = detail.bannerUrl ?? '';
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '获取活动详情失败');
    }
  } else {
    activityForm.id = undefined;
    activityForm.title = '';
    activityForm.description = '';
    activityForm.location = '';
    activityForm.capacity = undefined;
    activityForm.requiresApproval = true;
    activityForm.time = [];
    activityForm.bannerUrl = '';
  }
  activityDialogVisible.value = true;
};

const submitActivity = () => {
  if (!currentClubId.value) return;
  activityFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    activitySaving.value = true;
    try {
      const [start, end] =
        activityForm.time.length === 2
          ? (activityForm.time as [Date, Date])
          : ([undefined, undefined] as [Date | undefined, Date | undefined]);
      const payload = {
        title: activityForm.title,
        description: activityForm.description,
        location: activityForm.location,
        capacity: activityForm.capacity,
        requiresApproval: activityForm.requiresApproval,
        startTime: start ? start.toISOString() : undefined,
        endTime: end ? end.toISOString() : undefined,
        bannerUrl: activityForm.bannerUrl || undefined,
      };
      if (activityForm.id) {
        await updateActivity(activityForm.id, payload);
      } else {
        const clubId = currentClubId.value;
        if (!clubId) return;
        await createActivity(clubId, payload);
      }
      ElMessage.success('活动保存成功');
      activityDialogVisible.value = false;
      await loadActivities();
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '保存失败，请稍后再试');
    } finally {
      activitySaving.value = false;
    }
  });
};

const handleBannerUpload = async (options: UploadRequestOptions) => {
  const { file, onError, onSuccess } = options;
  try {
    bannerUploading.value = true;
    const uploadResult = await uploadImage(file as File, 'activities/banners');
    activityForm.bannerUrl = uploadResult.url;
    onSuccess?.(uploadResult);
    ElMessage.success('海报上传成功');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message ?? '上传失败，请稍后再试');
    onError?.(error);
  } finally {
    bannerUploading.value = false;
  }
};

const clearBanner = () => {
  activityForm.bannerUrl = '';
};

const deleteActivityById = async (activityId: number) => {
  try {
    await ElMessageBox.confirm('确认删除该活动吗？', '提示', { type: 'warning' });
    await deleteActivity(activityId);
    ElMessage.success('删除成功');
    await loadActivities();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后再试');
    }
  }
};

const openBroadcastModal = () => {
  broadcastForm.title = '';
  broadcastForm.content = '';
  broadcastDialogVisible.value = true;
};

const submitBroadcast = () => {
  if (!currentClubId.value) return;
  broadcastFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    broadcastSaving.value = true;
    try {
      const clubId = currentClubId.value;
      if (!clubId) return;
      await broadcastClubMessage(clubId, {
        title: broadcastForm.title,
        content: broadcastForm.content,
      });
      ElMessage.success('公告已发送');
      broadcastDialogVisible.value = false;
      await loadMessages();
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message ?? '发送失败，请稍后再试');
    } finally {
      broadcastSaving.value = false;
    }
  });
};

onMounted(async () => {
  await loadClubTagOptions();
  await loadMyClubList();
});

watch(
  () => route.fullPath,
  async () => {
    const targetTab = getRouteTab();
    if (activeTab.value !== targetTab) {
      activeTab.value = targetTab;
      if (currentClubId.value) {
        await loadClubData();
      }
    }
  },
);
</script>

<style scoped>
.club-mgmt {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.club-mgmt__tabs {
  margin-top: 16px;
}

.club-mgmt__table {
  margin-top: 12px;
}

.club-mgmt__empty {
  padding: 40px 0;
}

.club-info-card {
  margin-top: 16px;
  border-radius: 12px;
  border: 1px solid var(--ccm-border);
  background: var(--ccm-surface-gradient);
}

.club-info-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  gap: 16px;
}

.club-info-card__title {
  font-size: 16px;
  color: var(--ccm-text-primary);
}

.club-info-card__subtitle {
  font-size: 13px;
  color: var(--ccm-text-secondary);
  margin-top: 4px;
}

.club-info-card__body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.club-info-card__meta {
  display: flex;
  gap: 16px;
  align-items: center;
}

.club-info-card__avatar {
  border-radius: 16px;
  box-shadow: var(--ccm-card-shadow);
}

.club-info-card__avatar--placeholder {
  background: linear-gradient(135deg, var(--ccm-primary-soft), var(--ccm-accent));
  color: var(--ccm-text-primary);
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.club-info-card__description {
  color: var(--ccm-text-secondary);
  margin-top: 4px;
  line-height: 1.6;
}

.club-info-card__descriptions {
  --el-descriptions-table-border: 1px solid var(--ccm-border);
}

.club-logo-field {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.club-logo-field__preview {
  width: 96px;
  height: 96px;
  border-radius: 12px;
  border: 1px solid var(--ccm-border);
  overflow: hidden;
}

.activity-banner-field {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.activity-banner-field__remove {
  color: var(--ccm-danger);
}

.activity-banner-field__preview {
  margin-top: 8px;
  width: 240px;
  height: 180px;
  border-radius: 12px;
  border: 1px solid var(--ccm-border);
  overflow: hidden;
}

.form-item__hint {
  display: block;
  font-size: 12px;
  color: var(--ccm-text-muted);
}

.attendance-panel {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.points-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.points-panel__form {
  border-radius: 12px;
}

.points-panel__content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.points-panel__leaderboard,
.points-panel__records {
  border-radius: 12px;
}

.points-panel__records {
  margin-top: 0;
}

.points-panel__pagination {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}

.points-positive {
  color: var(--ccm-success);
}

.points-negative {
  color: var(--ccm-danger);
}

.tasks-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 12px;
}

.tasks-panel__table {
  margin-top: 8px;
}

.tasks-panel__assignees {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tasks-panel__assignment {
  cursor: pointer;
}

.tasks-panel__pagination {
  display: flex;
  justify-content: center;
}

</style>
