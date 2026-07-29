package com.navrotskyi.trippyapi.domain;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trip_post")
public class TripPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private TripNode node;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String note;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<TripPhoto> photos = new ArrayList<>();

    public TripPost() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TripNode getNode() { return node; }
    public void setNode(TripNode node) { this.node = node; }
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<TripPhoto> getPhotos() { return photos; }
    public void setPhotos(List<TripPhoto> photos) { this.photos = photos; }
}