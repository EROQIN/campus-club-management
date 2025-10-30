package com.erokin.campusclubmanagement.entity;

import com.erokin.campusclubmanagement.enums.PromoVideoStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "club_promo_videos")
public class ClubPromoVideo extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false, unique = true)
    private Club club;

    @Column(nullable = false, length = 255)
    private String objectKey;

    @Column(nullable = false, length = 500)
    private String playbackUrl;

    @Column(length = 255)
    private String originalFileName;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(nullable = false)
    private Integer durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromoVideoStatus status = PromoVideoStatus.UPLOADED;

    @Column(length = 80)
    private String transcriptionTaskId;

    private Instant subtitlesUpdatedAt;

    @OneToMany(
            mappedBy = "video",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<ClubPromoSubtitle> subtitles = new ArrayList<>();

    public void replaceSubtitles(List<ClubPromoSubtitle> items) {
        subtitles.clear();
        if (items != null) {
            items.forEach(this::addSubtitle);
        }
    }

    public void addSubtitle(ClubPromoSubtitle subtitle) {
        subtitle.setVideo(this);
        subtitles.add(subtitle);
    }
}
