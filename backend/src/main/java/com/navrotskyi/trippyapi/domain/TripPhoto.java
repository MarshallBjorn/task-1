package com.navrotskyi.trippyapi.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "trip_photo")
public class TripPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private TripPost post;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    public TripPhoto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TripPost getPost() { return post; }
    public void setPost(TripPost post) { this.post = post; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}