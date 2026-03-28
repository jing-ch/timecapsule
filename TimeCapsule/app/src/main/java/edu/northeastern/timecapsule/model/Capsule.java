package edu.northeastern.timecapsule.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.List;

/**
 * Represents a Time Capsule document in Firestore.
 * Field names match the agreed Firestore document structure.
 *
 * Hey teammates B and C — feel free to use this model when reading/writing capsule data,
 * or ignore it and handle Firestore fields manually if you prefer. Up to you!
 */
public class Capsule {

    @DocumentId
    private String capsuleId;   // auto-populated by Firestore

    private String userId;
    private String title;
    private String content;
    private List<String> mediaUrls;   // nullable
    private List<String> mediaTypes;  // each entry is "image" or "video", nullable
    private String thumbnailUrl; // video only, nullable
    private String locationName; // nullable
    private Timestamp unlockTime;
    private boolean isPublic;
    private boolean isUnlocked;
    private Timestamp createdAt;

    // Required empty constructor for Firestore deserialization
    public Capsule() {}

    public String getCapsuleId() { return capsuleId; }
    public void setCapsuleId(String capsuleId) { this.capsuleId = capsuleId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public List<String> getMediaTypes() { return mediaTypes; }
    public void setMediaTypes(List<String> mediaTypes) { this.mediaTypes = mediaTypes; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Timestamp getUnlockTime() { return unlockTime; }
    public void setUnlockTime(Timestamp unlockTime) { this.unlockTime = unlockTime; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean isUnlocked) { this.isUnlocked = isUnlocked; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
