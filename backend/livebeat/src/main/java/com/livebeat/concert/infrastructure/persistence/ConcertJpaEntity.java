package com.livebeat.concert.infrastructure.persistence;

import com.livebeat.concert.domain.model.Concert;
import com.livebeat.concert.domain.model.ConcertCategory;
import com.livebeat.concert.domain.model.ConcertStatus;
import com.livebeat.shared.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * [concert] Concert JPA 實體
 *
 * 負責：對應 concert.concerts 資料表；繼承 AuditedEntity（created_at/updated_at/created_by/updated_by）；
 *       提供 toDomain / fromDomain 轉換方法
 */
@Entity
@Table(name = "concerts", schema = "concert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcertJpaEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcertCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcertStatus status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "organizer_id", nullable = false, columnDefinition = "uuid")
    private UUID organizerId;

    Concert toDomain() {
        return Concert.builder()
                .id(id).title(title).artist(artist).description(description)
                .venue(venue).city(city).country(country)
                .category(category).status(status).imageUrl(imageUrl)
                .organizerId(organizerId)
                .createdBy(getCreatedBy()).updatedBy(getUpdatedBy())
                .createdAt(getCreatedAt()).updatedAt(getUpdatedAt())
                .build();
    }

    static ConcertJpaEntity fromDomain(Concert concert) {
        return ConcertJpaEntity.builder()
                .id(concert.getId()).title(concert.getTitle()).artist(concert.getArtist())
                .description(concert.getDescription()).venue(concert.getVenue())
                .city(concert.getCity()).country(concert.getCountry())
                .category(concert.getCategory()).status(concert.getStatus())
                .imageUrl(concert.getImageUrl()).organizerId(concert.getOrganizerId())
                .build();
    }
}
