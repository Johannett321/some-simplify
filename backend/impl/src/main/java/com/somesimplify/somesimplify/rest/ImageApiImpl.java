package com.somesimplify.somesimplify.rest;

import com.somesimplify.api.ImageApi;
import com.somesimplify.model.ImageTO;
import com.somesimplify.somesimplify.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ImageApiImpl implements ImageApi {

    private final ImageService imageService;

    @Override
    public ResponseEntity<List<ImageTO>> getUserImages() {
        List<ImageTO> images = imageService.getUserImages();
        return ResponseEntity.ok(images);
    }

    @Override
    public ResponseEntity<ImageTO> uploadImage(MultipartFile file) {
        try {
            ImageTO image = imageService.uploadImage(file);
            return ResponseEntity.ok(image);
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<ImageTO> getImageById(String id) {
        ImageTO image = imageService.getImageById(id);
        return ResponseEntity.ok(image);
    }

    @Override
    public ResponseEntity<Void> deleteImage(String id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
