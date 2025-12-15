package com.somesimplify.somesimplify.service;

import com.somesimplify.model.ImageTO;
import com.somesimplify.somesimplify.exception.BadRequestException;
import com.somesimplify.somesimplify.exception.ResourceNotFoundException;
import com.somesimplify.somesimplify.mapper.ImageMapper;
import com.somesimplify.somesimplify.model.ContentFile;
import com.somesimplify.somesimplify.model.User;
import com.somesimplify.somesimplify.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final S3Service s3Service;
    private final UserService userService;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Upload a new image
     */
    @Transactional
    public ImageTO uploadImage(MultipartFile file) throws IOException {
        validateFile(file);

        User currentUser = userService.getCurrentUser();

        // Upload original to S3
        String s3Key = s3Service.uploadFile(file, currentUser.getId());

        // Generate and upload thumbnail
        String thumbnailS3Key = s3Service.uploadThumbnail(file, currentUser.getId());

        // Create database record
        ContentFile contentFile = new ContentFile();
        contentFile.setFileName(file.getOriginalFilename());
        contentFile.setS3Key(s3Key);
        contentFile.setThumbnailS3Key(thumbnailS3Key);
        contentFile.setContentType(file.getContentType());
        contentFile.setFileSize(file.getSize());
        contentFile.setUploadedBy(currentUser);

        contentFile = imageRepository.save(contentFile);

        // Convert to TO with pre-signed URLs
        ImageTO imageTO = imageMapper.toImageTO(contentFile);
        imageTO.setUrl(s3Service.generatePresignedUrl(s3Key));
        imageTO.setThumbnailUrl(s3Service.generatePresignedUrl(thumbnailS3Key));

        log.info("User {} uploaded image {}", currentUser.getId(), contentFile.getId());
        return imageTO;
    }

    /**
     * Get all images for current user
     */
    public List<ImageTO> getUserImages() {
        User currentUser = userService.getCurrentUser();
        List<ContentFile> contentFiles = imageRepository.findAllByUploadedByOrderByCreatedAtDesc(currentUser);

        return contentFiles.stream()
                .map(image -> {
                    ImageTO imageTO = imageMapper.toImageTO(image);
                    imageTO.setUrl(s3Service.generatePresignedUrl(image.getS3Key()));
                    imageTO.setThumbnailUrl(s3Service.generatePresignedUrl(image.getThumbnailS3Key()));
                    return imageTO;
                })
                .toList();
    }

    /**
     * Get a single image by ID
     */
    public ImageTO getImageById(String id) {
        User currentUser = userService.getCurrentUser();
        ContentFile contentFile = imageRepository.findByIdAndUploadedBy(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        ImageTO imageTO = imageMapper.toImageTO(contentFile);
        imageTO.setUrl(s3Service.generatePresignedUrl(contentFile.getS3Key()));
        imageTO.setThumbnailUrl(s3Service.generatePresignedUrl(contentFile.getThumbnailS3Key()));
        return imageTO;
    }

    /**
     * Delete an image
     */
    @Transactional
    public void deleteImage(String id) {
        User currentUser = userService.getCurrentUser();
        ContentFile contentFile = imageRepository.findByIdAndUploadedBy(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        // Delete from S3 (both original and thumbnail)
        s3Service.deleteFile(contentFile.getS3Key());
        s3Service.deleteFile(contentFile.getThumbnailS3Key());

        // Delete from database
        imageRepository.delete(contentFile);

        log.info("User {} deleted image {}", currentUser.getId(), id);
    }

    /**
     * Validate file type and size
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Invalid file type. Only JPG, PNG, GIF, and WebP are allowed");
        }
    }
}
