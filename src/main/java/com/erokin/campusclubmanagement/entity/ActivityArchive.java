package com.erokin.campusclubmanagement.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "activity_archives")
public class ActivityArchive extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false, length = 4000)
    private String summary;

    @ElementCollection
    @CollectionTable(
            name = "activity_archive_photos",
            joinColumns = @JoinColumn(name = "archive_id"))
    @OrderColumn(name = "idx")
    @Column(name = "photo_url", length = 512)
    private List<String> photoUrls = new ArrayList<>();

    @Column(nullable = false)
    private Instant archivedAt;

}
