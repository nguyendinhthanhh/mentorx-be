package com.mentorx.api.feature.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_social_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSocialLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;
}
