package com.familytree.usermgmt.service;

import com.familytree.usermgmt.config.AppProperties;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class StorageService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final AppProperties appProperties;

  public StorageService(S3Client s3Client, S3Presigner s3Presigner, AppProperties appProperties) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.appProperties = appProperties;
  }

  /**
   * Uploads a photo to private object storage and returns the object key stored in the database.
   * Key format: {familyId}/{memberId}/{uuid}.{ext}
   */
  public String uploadPhoto(
      String familyId, String memberId, InputStream inputStream, String contentType, long contentLength) {
    String ext = extensionFromContentType(contentType);
    String key = familyId + "/" + memberId + "/" + UUID.randomUUID() + "." + ext;

    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(appProperties.getBucket())
            .key(key)
            .contentType(contentType)
            .contentLength(contentLength)
            .build();

    s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
    return key;
  }

  /**
   * Returns a short-lived presigned URL for clients to display a photo, or null if none.
   * {@code storedPhotoRef} may be a raw object key or a legacy full URL from older rows.
   */
  public String resolvePhotoAccessUrl(String storedPhotoRef) {
    String key = resolveStorageKey(storedPhotoRef);
    if (key == null || key.isBlank()) {
      return null;
    }

    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder()
            .bucket(appProperties.getBucket())
            .key(key)
            .build();

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(appProperties.getPresignedUrlExpiryMinutes()))
            .getObjectRequest(getObjectRequest)
            .build();

    PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
    return presigned.url().toString();
  }

  /** Deletes the object referenced by a storage key or legacy URL. */
  public void deletePhoto(String storedPhotoRef) {
    String key = resolveStorageKey(storedPhotoRef);
    if (key == null || key.isBlank()) {
      return;
    }
    DeleteObjectRequest request =
        DeleteObjectRequest.builder()
            .bucket(appProperties.getBucket())
            .key(key)
            .build();
    s3Client.deleteObject(request);
  }

  /**
   * Normalizes {@code family_members.photo_url}: new rows store the object key only;
   * legacy rows may still hold a full MinIO/Supabase public URL.
   */
  public String resolveStorageKey(String storedPhotoRef) {
    if (storedPhotoRef == null || storedPhotoRef.isBlank()) {
      return null;
    }
    String trimmed = storedPhotoRef.trim();
    if (!trimmed.contains("://")) {
      return trimmed;
    }

    String bucket = appProperties.getBucket();
    String pathStyleMarker = "/" + bucket + "/";
    int pathIdx = trimmed.indexOf(pathStyleMarker);
    if (pathIdx >= 0) {
      return trimmed.substring(pathIdx + pathStyleMarker.length());
    }

    String supabasePublicMarker = "/object/public/" + bucket + "/";
    int supabaseIdx = trimmed.indexOf(supabasePublicMarker);
    if (supabaseIdx >= 0) {
      return trimmed.substring(supabaseIdx + supabasePublicMarker.length());
    }

    return trimmed;
  }

  private String extensionFromContentType(String contentType) {
    if (contentType == null) return "bin";
    return switch (contentType.toLowerCase()) {
      case "image/jpeg", "image/jpg" -> "jpg";
      case "image/png" -> "png";
      case "image/gif" -> "gif";
      case "image/webp" -> "webp";
      default -> "bin";
    };
  }
}
