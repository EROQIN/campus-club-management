package com.erokin.campusclubmanagement.config;

import com.erokin.campusclubmanagement.entity.Activity;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.InterestTag;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.repository.ActivityRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.InterestTagRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final InterestTagRepository interestTagRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createTags();
        User admin = createAdminUser();
        User manager = createManagerUser();
        createSampleClubs(manager, admin);
    }

    private void createTags() {
        if (interestTagRepository.count() > 0) {
            return;
        }
        List<String> tags =
                List.of(
                        "科技创新",
                        "人工智能",
                        "志愿公益",
                        "环保",
                        "篮球",
                        "足球",
                        "羽毛球",
                        "音乐",
                        "合唱",
                        "舞蹈",
                        "吉他",
                        "摄影",
                        "视频制作",
                        "编程",
                        "动漫",
                        "桌游",
                        "读书",
                        "创业",
                        "演讲",
                        "书法",
                        "戏剧",
                        "旅行",
                        "模型",
                        "设计");
        tags.forEach(
                name -> {
                    InterestTag tag = new InterestTag();
                    tag.setName(name);
                    interestTagRepository.save(tag);
                });
    }

    private User createAdminUser() {
        return userRepository
                .findByEmailIgnoreCase("admin@campus.test")
                .orElseGet(
                        () -> {
                            User admin = new User();
                            admin.setFullName("系统管理员");
                            admin.setEmail("admin@campus.test");
                            admin.setPassword(passwordEncoder.encode("Admin123"));
                            admin.setRoles(EnumSet.of(Role.SYSTEM_ADMIN, Role.UNION_STAFF));
                            admin.setEnabled(true);
                            return userRepository.save(admin);
                        });
    }

    private User createManagerUser() {
        return userRepository
                .findByEmailIgnoreCase("manager@campus.test")
                .orElseGet(
                        () -> {
                            User manager = new User();
                            manager.setFullName("示例社团负责人");
                            manager.setEmail("manager@campus.test");
                            manager.setPassword(passwordEncoder.encode("Manager123"));
                            manager.setRoles(EnumSet.of(Role.CLUB_MANAGER));
                            manager.setEnabled(true);
                            manager.setInterests(
                                    new HashSet<>(interestTagRepository.findAll().subList(0, 5)));
                            return userRepository.save(manager);
                        });
    }

    private void createSampleClubs(User manager, User admin) {
        if (clubRepository.count() > 0) {
            return;
        }
        InterestTag techTag = interestTagRepository.findByNameIgnoreCase("科技创新").orElse(null);
        InterestTag musicTag = interestTagRepository.findByNameIgnoreCase("音乐").orElse(null);

        Club techClub = new Club();
        techClub.setName("AI 科技社");
        techClub.setDescription("专注于校园人工智能和机器人项目的创新实践社团");
        techClub.setCategory("科技");
        techClub.setContactEmail("ai.club@example.com");
        techClub.setLogoUrl("https://example.com/ai-club-logo.png");
        techClub.setFoundedDate(LocalDate.now().minusYears(2));
        techClub.setCreatedBy(manager);
        if (techTag != null) {
            techClub.setTags(Set.of(techTag));
        }
        clubRepository.save(techClub);

        Club musicClub = new Club();
        musicClub.setName("星光音乐社");
        musicClub.setDescription("为热爱音乐的同学提供表演展示与合作创作的舞台");
        musicClub.setCategory("文艺");
        musicClub.setContactEmail("music.club@example.com");
        musicClub.setLogoUrl("https://example.com/music-club-logo.png");
        musicClub.setFoundedDate(LocalDate.now().minusYears(1));
        musicClub.setCreatedBy(admin);
        if (musicTag != null) {
            musicClub.setTags(Set.of(musicTag));
        }
        clubRepository.save(musicClub);

        Activity workshop = new Activity();
        workshop.setClub(techClub);
        workshop.setTitle("AI 创新训练营");
        workshop.setDescription("带你实践机器学习与校园机器人项目，欢迎零基础加入");
        workshop.setLocation("创新实验室 302");
        workshop.setStartTime(Instant.now().plus(7, ChronoUnit.DAYS));
        workshop.setEndTime(Instant.now().plus(7, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS));
        workshop.setCapacity(30);
        workshop.setRequiresApproval(false);
        activityRepository.save(workshop);

        Activity concert = new Activity();
        concert.setClub(musicClub);
        concert.setTitle("新生欢迎音乐会");
        concert.setDescription("面向全校的室内音乐会，招募舞台志愿者与表演嘉宾");
        concert.setLocation("大礼堂");
        concert.setStartTime(Instant.now().plus(20, ChronoUnit.DAYS));
        concert.setEndTime(Instant.now().plus(20, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS));
        concert.setCapacity(100);
        concert.setRequiresApproval(true);
        activityRepository.save(concert);
    }
}
