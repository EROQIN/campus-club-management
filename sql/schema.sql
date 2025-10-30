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

-- Helpful indexes for dashboard queries
CREATE INDEX idx_membership_status ON club_memberships (status);
CREATE INDEX idx_activity_club ON activities (club_id);
CREATE INDEX idx_registration_status_created ON activity_registrations (status, created_at);
