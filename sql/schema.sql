-- MySQL schema for Campus Club Management platform
-- Tested with MySQL 8/9. Adjust character set or engine as needed for your environment.

CREATE DATABASE IF NOT EXISTS campus_club
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_club;

# -- Drop existing tables (optional safety switch). Uncomment if you need a clean rebuild.
# SET FOREIGN_KEY_CHECKS = 0;
# DROP TABLE IF EXISTS activity_registrations;
# DROP TABLE IF EXISTS activities;
# DROP TABLE IF EXISTS shared_resources;
# DROP TABLE IF EXISTS messages;
# DROP TABLE IF EXISTS club_memberships;
# DROP TABLE IF EXISTS club_tags;
# DROP TABLE IF EXISTS clubs;
# DROP TABLE IF EXISTS user_interest_tags;
# DROP TABLE IF EXISTS user_roles;
# DROP TABLE IF EXISTS users;
# DROP TABLE IF EXISTS interest_tags;
# SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE interest_tags (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(60) NOT NULL UNIQUE,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_number VARCHAR(11) UNIQUE,
  full_name VARCHAR(80) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  avatar_url VARCHAR(255),
  bio VARCHAR(500),
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  last_login_at DATETIME(6),
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role VARCHAR(32) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_interest_tags (
  user_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, tag_id),
  CONSTRAINT fk_user_interest_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_user_interest_tag FOREIGN KEY (tag_id)
    REFERENCES interest_tags (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE clubs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL UNIQUE,
  description VARCHAR(500) NOT NULL,
  logo_url VARCHAR(255),
  promo_video_url VARCHAR(255),
  category VARCHAR(60),
  contact_email VARCHAR(120),
  contact_phone VARCHAR(30),
  founded_date DATE,
  created_by_id BIGINT,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_club_creator FOREIGN KEY (created_by_id)
    REFERENCES users (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE club_tags (
  club_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (club_id, tag_id),
  CONSTRAINT fk_club_tags_club FOREIGN KEY (club_id)
    REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_club_tags_tag FOREIGN KEY (tag_id)
    REFERENCES interest_tags (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE club_memberships (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  membership_role VARCHAR(20) NOT NULL,
  application_reason VARCHAR(300),
  responded_at DATETIME(6),
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_club_member (club_id, member_id),
  CONSTRAINT fk_membership_club FOREIGN KEY (club_id)
    REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_membership_user FOREIGN KEY (member_id)
    REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activities (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description VARCHAR(800) NOT NULL,
  location VARCHAR(120),
  start_time DATETIME(6),
  end_time DATETIME(6),
  capacity INT,
  banner_url VARCHAR(255),
  requires_approval TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_activity_club FOREIGN KEY (club_id)
    REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_activity_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_registrations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  activity_id BIGINT NOT NULL,
  attendee_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  note VARCHAR(300),
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_activity_attendee (activity_id, attendee_id),
  CONSTRAINT fk_registration_activity FOREIGN KEY (activity_id)
    REFERENCES activities (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_registration_user FOREIGN KEY (attendee_id)
    REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_archives (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  activity_id BIGINT NOT NULL UNIQUE,
  created_by_id BIGINT,
  summary VARCHAR(4000) NOT NULL,
  archived_at DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_archive_activity FOREIGN KEY (activity_id)
    REFERENCES activities (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_archive_creator FOREIGN KEY (created_by_id)
    REFERENCES users (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_archive_photos (
  archive_id BIGINT NOT NULL,
  idx INT NOT NULL,
  photo_url VARCHAR(512) NOT NULL,
  PRIMARY KEY (archive_id, idx),
  CONSTRAINT fk_archive_photo_archive FOREIGN KEY (archive_id)
    REFERENCES activity_archives (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  recipient_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  title VARCHAR(150) NOT NULL,
  content VARCHAR(800) NOT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  read_at DATETIME(6),
  reference_type VARCHAR(50),
  reference_id BIGINT,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_message_recipient FOREIGN KEY (recipient_id)
    REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_message_recipient_read (recipient_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shared_resources (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  resource_type VARCHAR(60) NOT NULL,
  description VARCHAR(500),
  available_from DATETIME(6),
  available_until DATETIME(6),
  contact VARCHAR(120),
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_resource_club FOREIGN KEY (club_id)
    REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE resource_applications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  resource_id BIGINT NOT NULL,
  applicant_id BIGINT NOT NULL,
  purpose VARCHAR(400) NOT NULL,
  requested_from DATETIME(6),
  requested_until DATETIME(6),
  status VARCHAR(20) NOT NULL,
  responded_at DATETIME(6),
  reply_message VARCHAR(300),
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_resource_application_resource FOREIGN KEY (resource_id)
    REFERENCES shared_resources (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_resource_application_user FOREIGN KEY (applicant_id)
    REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE KEY uk_resource_applicant_time (resource_id, applicant_id, requested_from, requested_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE club_announcements (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,
  author_id BIGINT,
  title VARCHAR(150) NOT NULL,
  content VARCHAR(800) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_announcement_club FOREIGN KEY (club_id)
    REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_announcement_author FOREIGN KEY (author_id)
    REFERENCES users (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE club_point_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  created_by BIGINT,
  points INT NOT NULL,
  reason VARCHAR(200) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_points_club FOREIGN KEY (club_id) REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_points_member FOREIGN KEY (member_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_points_creator FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE club_tasks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  club_id BIGINT NOT NULL,
  creator_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description VARCHAR(1000),
  due_at DATETIME(6),
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_task_club FOREIGN KEY (club_id) REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_task_creator FOREIGN KEY (creator_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE club_task_assignments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id BIGINT NOT NULL,
  assignee_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  remark VARCHAR(300),
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_task_assignment_task FOREIGN KEY (task_id) REFERENCES club_tasks (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_task_assignment_user FOREIGN KEY (assignee_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE collaboration_proposals (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  initiator_club_id BIGINT NOT NULL,
  initiator_user_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  collaboration_type VARCHAR(100),
  required_resources VARCHAR(300),
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_collab_proposal_club FOREIGN KEY (initiator_club_id) REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_collab_proposal_user FOREIGN KEY (initiator_user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE collaboration_responses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  proposal_id BIGINT NOT NULL,
  responder_club_id BIGINT NOT NULL,
  responder_user_id BIGINT NOT NULL,
  message VARCHAR(600) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_collab_response_proposal FOREIGN KEY (proposal_id) REFERENCES collaboration_proposals (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_collab_response_club FOREIGN KEY (responder_club_id) REFERENCES clubs (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_collab_response_user FOREIGN KEY (responder_user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Helpful indexes for dashboard queries
CREATE INDEX idx_membership_status ON club_memberships (status);
CREATE INDEX idx_activity_club ON activities (club_id);
CREATE INDEX idx_registration_status_created ON activity_registrations (status, created_at);

-- ------------------------------------------------------------------
-- Sample seed data for development and demo environments
-- ------------------------------------------------------------------

INSERT INTO interest_tags (id, name) VALUES
  (1, '科技创新'),
  (2, '志愿服务'),
  (3, '文化传承'),
  (4, '体育竞技'),
  (5, '创业实践'),
  (6, '音乐表达'),
  (7, '视觉设计'),
  (8, '公共演讲'),
  (9, '可持续发展'),
  (10, '跨学科研究');

INSERT INTO users (id, student_number, full_name, email, password, avatar_url, bio, enabled, last_login_at) VALUES
  (1, '20240001', '李晓晨', 'lixc@example.edu', '$2a$10$7dPSkZl8vLwYx1nN9X1FeuP0G7FX9bGWRzZLHgKw1hp.o3xNmPRU2', NULL, '热爱科技与志愿活动的新生。', 1, '2024-09-18 10:12:00'),
  (2, '20230087', '王珂然', 'wangkr@example.edu', '$2a$10$7dPSkZl8vLwYx1nN9X1FeuP0G7FX9bGWRzZLHgKw1hp.o3xNmPRU2', 'https://cdn.example.edu/avatars/wangkr.png', '量子创新社负责人，关注跨学科研讨。', 1, '2024-09-17 21:33:00'),
  (3, '20210112', '赵雨杉', 'zhaoys@example.edu', '$2a$10$7dPSkZl8vLwYx1nN9X1FeuP0G7FX9bGWRzZLHgKw1hp.o3xNmPRU2', NULL, '文创社资深成员，负责活动策划。', 1, '2024-09-15 08:45:00'),
  (4, NULL, '陈峻岭', 'chenjl@union.example.edu', '$2a$10$7dPSkZl8vLwYx1nN9X1FeuP0G7FX9bGWRzZLHgKw1hp.o3xNmPRU2', NULL, '校团委活动统筹。', 1, '2024-09-16 09:10:00'),
  (5, NULL, '刘安琪', 'liuadmin@example.edu', '$2a$10$7dPSkZl8vLwYx1nN9X1FeuP0G7FX9bGWRzZLHgKw1hp.o3xNmPRU2', NULL, '系统管理员，负责账号与权限。', 1, '2024-09-18 07:30:00'),
  (6, '20220045', '周子墨', 'zhouzm@example.edu', '$2a$10$7dPSkZl8vLwYx1nN9X1FeuP0G7FX9bGWRzZLHgKw1hp.o3xNmPRU2', 'https://cdn.example.edu/avatars/zhouzm.jpg', '摄影与可持续发展倡导者。', 1, '2024-09-18 12:26:00'),
  (7, '20220188', '马欣怡', 'maxy@example.edu', '$2a$10$7dPSkZl8vLwYx1nN9X1FeuP0G7FX9bGWRzZLHgKw1hp.o3xNmPRU2', NULL, '音乐社主唱，热衷跨社团合作演出。', 1, '2024-09-14 19:05:00'),
  (8, '20200221', '郭一鸣', 'guoym@example.edu', '$2a$10$7dPSkZl8vLwYx1nN9X1FeuP0G7FX9bGWRzZLHgKw1hp.o3xNmPRU2', NULL, '体育竞技社教练助理。', 1, '2024-09-13 15:50:00');

INSERT INTO user_roles (user_id, role) VALUES
  (1, 'STUDENT'),
  (2, 'CLUB_MANAGER'),
  (2, 'STUDENT'),
  (3, 'STUDENT'),
  (4, 'UNION_STAFF'),
  (5, 'SYSTEM_ADMIN'),
  (6, 'STUDENT'),
  (6, 'CLUB_MANAGER'),
  (7, 'STUDENT'),
  (8, 'STUDENT');

INSERT INTO user_interest_tags (user_id, tag_id) VALUES
  (1, 1),
  (1, 2),
  (1, 9),
  (2, 1),
  (2, 10),
  (3, 3),
  (3, 6),
  (4, 2),
  (4, 9),
  (5, 5),
  (6, 7),
  (6, 9),
  (7, 6),
  (7, 8),
  (8, 4);

INSERT INTO clubs (id, name, description, logo_url, promo_video_url, category, contact_email, contact_phone, founded_date, created_by_id) VALUES
  (1, '量子创新社', '聚焦物理与工程前沿议题，组织实验室参观与跨学科研讨。', 'https://cdn.example.edu/logos/quantum.png', NULL, '科技', 'quantum@clubs.example.edu', '010-88990011', '2018-03-15', 2),
  (2, '青禾志愿联盟', '联合多院系发起的志愿服务社群，涵盖支教、环保与社区陪伴。', 'https://cdn.example.edu/logos/qinghe.png', NULL, '公益', 'volunteer@clubs.example.edu', '010-88992233', '2015-10-01', 4),
  (3, '声光跨界实验室', '音乐与视觉跨界创作团队，探索舞台叙事与多媒体演出。', 'https://cdn.example.edu/logos/soniclight.svg', 'https://video.example.edu/club/soniclight', '艺术', 'soniclight@clubs.example.edu', NULL, '2020-05-20', 6);

INSERT INTO club_tags (club_id, tag_id) VALUES
  (1, 1),
  (1, 10),
  (2, 2),
  (2, 9),
  (3, 6),
  (3, 7),
  (3, 8);

INSERT INTO club_memberships (id, club_id, member_id, status, membership_role, application_reason, responded_at) VALUES
  (1, 1, 2, 'APPROVED', 'LEADER', '创始成员。', '2018-03-15 09:00:00'),
  (2, 1, 1, 'APPROVED', 'MEMBER', '希望参与量子计算分享会。', '2024-09-12 14:30:00'),
  (3, 2, 4, 'APPROVED', 'ADVISOR', '校团委指导老师。', '2016-01-09 09:00:00'),
  (4, 2, 6, 'PENDING', 'VOLUNTEER', '愿意协助环保行动与影像记录。', NULL),
  (5, 3, 6, 'APPROVED', 'LEADER', '跨界演出策划人。', '2023-09-01 18:00:00'),
  (6, 3, 7, 'APPROVED', 'MEMBER', '负责主唱与曲目编排。', '2023-09-05 13:15:00'),
  (7, 3, 3, 'APPROVED', 'CURATOR', '具备舞美与视觉设计经验。', '2023-09-08 11:40:00'),
  (8, 1, 8, 'REJECTED', 'MEMBER', '希望引入篮球社资源。', '2024-09-10 10:12:00');

INSERT INTO activities (id, club_id, title, description, location, start_time, end_time, capacity, banner_url, requires_approval) VALUES
  (1, 1, '量子芯片前沿工作坊', '邀请实验室导师分享量子芯片设计流程与实操体验。', '工程学院 A305', '2024-10-18 14:00:00', '2024-10-18 17:00:00', 45, 'https://cdn.example.edu/banners/quantum-workshop.jpg', 1),
  (2, 2, '秋季社区环保行', '面向全校的社区清洁与环保宣传行动。', '京华社区服务中心', '2024-10-20 08:30:00', '2024-10-20 16:30:00', 120, 'https://cdn.example.edu/banners/eco-volunteer.png', 1),
  (3, 3, '声光共振创作营', '三天密集排练与创作，完成音乐与视觉交互演示。', '艺术中心·黑匣子剧场', '2024-11-08 09:00:00', '2024-11-10 21:00:00', 30, 'https://cdn.example.edu/banners/soniclight-camp.webp', 1);

INSERT INTO activity_registrations (id, activity_id, attendee_id, status, note) VALUES
  (1, 1, 1, 'APPROVED', '已提交实验室安全协议。'),
  (2, 1, 3, 'WAITLIST', '等待名额释放。'),
  (3, 2, 6, 'APPROVED', '负责影像记录。'),
  (4, 2, 7, 'APPROVED', '参与社区音乐交流环节。'),
  (5, 3, 1, 'PENDING', '期望学习舞美灯光配合。'),
  (6, 3, 2, 'APPROVED', '担任工作坊导师助手。');

INSERT INTO shared_resources (id, club_id, name, resource_type, description, available_from, available_until, contact) VALUES
  (1, 1, '量子计算仿真账号', '软件授权', '提供 30 天的量子电路仿真平台试用账号。', '2024-10-01 08:00:00', '2024-10-31 22:00:00', 'quantum-support@clubs.example.edu'),
  (2, 2, '环保宣传展板套件', '宣传物料', '折叠式展板与可循环使用物料 6 套。', '2024-09-20 09:00:00', '2024-12-31 18:00:00', 'volunteer-assets@clubs.example.edu'),
  (3, 3, '舞台灯光控制台', '设备', '便携式 DMX 控制台一套，附带灯光预设。', '2024-09-25 10:00:00', '2024-12-20 20:00:00', 'soniclight-tech@clubs.example.edu');

INSERT INTO resource_applications (id, resource_id, applicant_id, purpose, requested_from, requested_until, status, responded_at, reply_message) VALUES
  (1, 1, 1, '用于课程项目的量子算法演示。', '2024-10-05 09:00:00', '2024-10-25 18:00:00', 'APPROVED', '2024-09-28 15:40:00', '请遵守使用规范并按时归还。'),
  (2, 2, 6, '社区环保短片拍摄需要展示材料。', '2024-10-15 08:00:00', '2024-10-15 20:00:00', 'PENDING', NULL, NULL),
  (3, 3, 7, '与音乐节联合排练使用灯光设备。', '2024-11-02 14:00:00', '2024-11-03 22:00:00', 'APPROVED', '2024-09-30 10:05:00', '设备运输由社团负责。');

INSERT INTO messages (id, recipient_id, type, title, content, is_read, read_at, reference_type, reference_id) VALUES
  (1, 1, 'NOTICE', '资源申请已通过', '量子计算仿真账号申请已通过，使用说明已发送至邮箱。', 1, '2024-09-28 16:00:00', 'RESOURCE_APPLICATION', 1),
  (2, 6, 'REMINDER', '志愿者报名确认', '请在 10 月 12 日前确认是否参加秋季社区环保行。', 0, NULL, 'ACTIVITY', 2),
  (3, 7, 'NOTICE', '舞台灯光借用审批通过', '请与设备管理员联系确认取件时间。', 0, NULL, 'RESOURCE_APPLICATION', 3),
  (4, 3, 'UPDATE', '活动候补排队提示', '量子芯片前沿工作坊已满额，你当前位列候补第 1 位。', 1, '2024-09-14 18:20:00', 'ACTIVITY_REGISTRATION', 2);

INSERT INTO club_announcements (id, club_id, author_id, title, content, created_at) VALUES
  (1, 1, 2, '实验室开放日预约说明', '10 月 11 日开放日将分两个时段进行，报名链接已发布在社群。', '2024-09-19 13:00:00'),
  (2, 3, 6, '跨界演出招募 “光与声 2024”', '欢迎对音乐、灯光、影像创作有兴趣的同学报名联合演出。', '2024-09-17 20:30:00');
