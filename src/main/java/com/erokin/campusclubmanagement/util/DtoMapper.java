package com.erokin.campusclubmanagement.util;

import com.erokin.campusclubmanagement.dto.activity.ActivityArchiveResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityArchiveSummaryResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityCheckInResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivitySummaryResponse;
import com.erokin.campusclubmanagement.dto.admin.UserAdminResponse;
import com.erokin.campusclubmanagement.dto.announcement.AnnouncementResponse;
import com.erokin.campusclubmanagement.dto.club.ClubResponse;
import com.erokin.campusclubmanagement.dto.club.ClubSummaryResponse;
import com.erokin.campusclubmanagement.dto.membership.MembershipAdminResponse;
import com.erokin.campusclubmanagement.dto.membership.MembershipResponse;
import com.erokin.campusclubmanagement.dto.message.MessageResponse;
import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationResponse;
import com.erokin.campusclubmanagement.dto.resource.SharedResourceResponse;
import com.erokin.campusclubmanagement.dto.user.UserProfileResponse;
import com.erokin.campusclubmanagement.dto.user.UserSummary;
import com.erokin.campusclubmanagement.entity.Activity;
import com.erokin.campusclubmanagement.entity.ActivityArchive;
import com.erokin.campusclubmanagement.entity.ActivityRegistration;
import com.erokin.campusclubmanagement.entity.ActivityCheckIn;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubMembership;
import com.erokin.campusclubmanagement.entity.Message;
import com.erokin.campusclubmanagement.entity.ClubAnnouncement;
import com.erokin.campusclubmanagement.entity.ResourceApplication;
import com.erokin.campusclubmanagement.entity.SharedResource;
import com.erokin.campusclubmanagement.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    public UserSummary toUserSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRoles().stream().map(Enum::name).toList());
    }

    public UserProfileResponse toUserProfile(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setStudentNumber(user.getStudentNumber());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setBio(user.getBio());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setEnabled(user.isEnabled());
        response.setCreatedAt(user.getCreatedAt());
        response.setRoles(user.getRoles().stream().map(Enum::name).toList());
        response.setInterests(
                user.getInterests().stream().map(interestTag -> interestTag.getName()).toList());
        return response;
    }

    public ClubSummaryResponse toClubSummary(Club club, int memberCount, int activityCount) {
        ClubSummaryResponse summary = new ClubSummaryResponse();
        summary.setId(club.getId());
        summary.setName(club.getName());
        summary.setDescription(club.getDescription());
        summary.setLogoUrl(club.getLogoUrl());
        summary.setCategory(club.getCategory());
        summary.setMemberCount(memberCount);
        summary.setActivityCount(activityCount);
        summary.setTags(club.getTags().stream().map(t -> t.getName()).toList());
        summary.setRecommendationScore(0);
        summary.setMatchedTags(List.of());
        return summary;
    }

    public ClubResponse toClubResponse(
            Club club, int memberCount, int pendingApplicants, int activityCountLast30Days) {
        ClubResponse response = new ClubResponse();
        response.setId(club.getId());
        response.setName(club.getName());
        response.setDescription(club.getDescription());
        response.setLogoUrl(club.getLogoUrl());
        response.setPromoVideoUrl(club.getPromoVideoUrl());
        response.setCategory(club.getCategory());
        response.setContactEmail(club.getContactEmail());
        response.setContactPhone(club.getContactPhone());
        response.setFoundedDate(club.getFoundedDate());
        response.setCreatedAt(club.getCreatedAt());
        response.setTags(club.getTags().stream().map(tag -> tag.getName()).toList());
        response.setMemberCount(memberCount);
        response.setPendingApplicants(pendingApplicants);
        response.setActivityCountLast30Days(activityCountLast30Days);
        if (club.getCreatedBy() != null) {
            response.setManagerName(club.getCreatedBy().getFullName());
        }
        return response;
    }

    public void attachRecentActivities(
            ClubResponse clubResponse, List<ActivityRegistration> registrations) {
        List<ActivitySummaryResponse> summaries =
                registrations.stream()
                        .map(ActivityRegistration::getActivity)
                        .distinct()
                        .map(activity -> toActivitySummary(activity, 0))
                        .toList();
        clubResponse.setRecentActivities(summaries);
    }

    public ActivitySummaryResponse toActivitySummary(Activity activity, int attendeeCount) {
        ActivitySummaryResponse summary = new ActivitySummaryResponse();
        summary.setId(activity.getId());
        summary.setTitle(activity.getTitle());
        summary.setClubName(activity.getClub().getName());
        summary.setDescription(activity.getDescription());
        summary.setStartTime(activity.getStartTime());
        summary.setEndTime(activity.getEndTime());
        summary.setLocation(activity.getLocation());
        summary.setAttendeeCount(attendeeCount);
        summary.setCapacity(activity.getCapacity());
        summary.setRequiresApproval(activity.isRequiresApproval());
        return summary;
    }

    public ActivityResponse toActivityResponse(Activity activity, int attendeeCount) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setClubId(activity.getClub().getId());
        response.setClubName(activity.getClub().getName());
        response.setTitle(activity.getTitle());
        response.setDescription(activity.getDescription());
        response.setLocation(activity.getLocation());
        response.setStartTime(activity.getStartTime());
        response.setEndTime(activity.getEndTime());
        response.setCapacity(activity.getCapacity());
        response.setBannerUrl(activity.getBannerUrl());
        response.setRequiresApproval(activity.isRequiresApproval());
        response.setAttendeeCount(attendeeCount);
        return response;
    }

    public MembershipResponse toMembershipResponse(ClubMembership membership) {
        MembershipResponse response = new MembershipResponse();
        response.setId(membership.getId());
        response.setClubId(membership.getClub().getId());
        response.setClubName(membership.getClub().getName());
        response.setMemberId(membership.getMember().getId());
        response.setMemberName(membership.getMember().getFullName());
        response.setMemberEmail(membership.getMember().getEmail());
        response.setStatus(membership.getStatus());
        response.setMembershipRole(membership.getMembershipRole());
        response.setApplicationReason(membership.getApplicationReason());
        response.setCreatedAt(membership.getCreatedAt());
        response.setRespondedAt(membership.getRespondedAt());
        return response;
    }

    public MembershipAdminResponse toMembershipAdminResponse(ClubMembership membership) {
        MembershipAdminResponse response = new MembershipAdminResponse();
        response.setId(membership.getId());
        response.setUserId(membership.getMember().getId());
        response.setFullName(membership.getMember().getFullName());
        response.setEmail(membership.getMember().getEmail());
        response.setStatus(membership.getStatus());
        response.setMembershipRole(membership.getMembershipRole());
        response.setApplicationReason(membership.getApplicationReason());
        response.setCreatedAt(membership.getCreatedAt());
        response.setRespondedAt(membership.getRespondedAt());
        return response;
    }

    public ActivityRegistrationResponse toActivityRegistrationResponse(
            ActivityRegistration registration) {
        ActivityRegistrationResponse response = new ActivityRegistrationResponse();
        response.setId(registration.getId());
        response.setActivityId(registration.getActivity().getId());
        response.setActivityTitle(registration.getActivity().getTitle());
        response.setStatus(registration.getStatus());
        response.setNote(registration.getNote());
        response.setCreatedAt(registration.getCreatedAt());
        response.setAttendeeId(registration.getAttendee().getId());
        response.setAttendeeName(registration.getAttendee().getFullName());
        response.setAttendeeEmail(registration.getAttendee().getEmail());
        return response;
    }

    public ActivityCheckInResponse toActivityCheckInResponse(ActivityCheckIn checkIn) {
        ActivityCheckInResponse response = new ActivityCheckInResponse();
        response.setId(checkIn.getId());
        response.setActivityId(checkIn.getActivity().getId());
        response.setAttendeeId(checkIn.getAttendee().getId());
        response.setAttendeeName(checkIn.getAttendee().getFullName());
        response.setCheckedInAt(checkIn.getCheckedInAt());
        response.setMethod(checkIn.getMethod().name());
        return response;
    }

    public ActivityArchiveResponse toActivityArchiveResponse(
            ActivityArchive archive, String shareUrl) {
        return ActivityArchiveResponse.builder()
                .id(archive.getId())
                .activityId(archive.getActivity().getId())
                .activityTitle(archive.getActivity().getTitle())
                .summary(archive.getSummary())
                .photoUrls(List.copyOf(archive.getPhotoUrls()))
                .archivedAt(archive.getArchivedAt())
                .createdByName(
                        archive.getCreatedBy() != null ? archive.getCreatedBy().getFullName() : null)
                .shareUrl(shareUrl)
                .build();
    }

    public ActivityArchiveSummaryResponse toActivityArchiveSummary(ActivityArchive archive) {
        return ActivityArchiveSummaryResponse.builder()
                .id(archive.getId())
                .activityId(archive.getActivity().getId())
                .activityTitle(archive.getActivity().getTitle())
                .archivedAt(archive.getArchivedAt())
                .photoUrls(List.copyOf(archive.getPhotoUrls()))
                .createdByName(
                        archive.getCreatedBy() != null ? archive.getCreatedBy().getFullName() : null)
                .build();
    }

    public MessageResponse toMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setType(message.getType());
        response.setTitle(message.getTitle());
        response.setContent(message.getContent());
        response.setRead(message.isRead());
        response.setCreatedAt(message.getCreatedAt());
        response.setReadAt(message.getReadAt());
        response.setReferenceType(message.getReferenceType());
        response.setReferenceId(message.getReferenceId());
        return response;
    }

    public SharedResourceResponse toSharedResourceResponse(SharedResource resource) {
        SharedResourceResponse response = new SharedResourceResponse();
        response.setId(resource.getId());
        response.setClubId(resource.getClub().getId());
        response.setClubName(resource.getClub().getName());
        response.setName(resource.getName());
        response.setResourceType(resource.getResourceType());
        response.setDescription(resource.getDescription());
        response.setAvailableFrom(resource.getAvailableFrom());
        response.setAvailableUntil(resource.getAvailableUntil());
        response.setContact(resource.getContact());
        return response;
    }

    public ResourceApplicationResponse toResourceApplicationResponse(ResourceApplication application) {
        ResourceApplicationResponse response = new ResourceApplicationResponse();
        response.setId(application.getId());
        response.setResourceId(application.getResource().getId());
        response.setResourceName(application.getResource().getName());
        response.setApplicantId(application.getApplicant().getId());
        response.setApplicantName(application.getApplicant().getFullName());
        response.setPurpose(application.getPurpose());
        response.setRequestedFrom(application.getRequestedFrom());
        response.setRequestedUntil(application.getRequestedUntil());
        response.setStatus(application.getStatus());
        response.setReplyMessage(application.getReplyMessage());
        response.setCreatedAt(application.getCreatedAt());
        response.setRespondedAt(application.getRespondedAt());
        return response;
    }

    public List<String> toTagNames(Club club) {
        return club.getTags().stream().map(tag -> tag.getName()).collect(Collectors.toList());
    }

    public UserAdminResponse toUserAdminResponse(User user) {
        UserAdminResponse response = new UserAdminResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setStudentNumber(user.getStudentNumber());
        response.setEnabled(user.isEnabled());
        response.setRoles(user.getRoles().stream().map(Enum::name).toList());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        return response;
    }

    public AnnouncementResponse toAnnouncementResponse(ClubAnnouncement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setCreatedAt(announcement.getCreatedAt());
        if (announcement.getAuthor() != null) {
            response.setAuthorId(announcement.getAuthor().getId());
            response.setAuthorName(announcement.getAuthor().getFullName());
        }
        return response;
    }
}
