package com.movie.recommendation.modules.upload;

import com.cloudinary.Cloudinary;
import com.movie.recommendation.config.CloudinaryProperties;
import com.movie.recommendation.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final ObjectProvider<Cloudinary> cloudinaryProvider;
    private final CloudinaryProperties properties;

    public String uploadPoster(MultipartFile file) {
        if (!properties.isConfigured()) {
            throw new BadRequestException("Cloudinary is not configured. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET.");
        }

        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();
        if (cloudinary == null) {
            throw new BadRequestException("Cloudinary client is not available");
        }

        validateFile(file);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", properties.getFolder(),
                            "resource_type", "image"
                    )
            );
            Object url = result.get("secure_url");
            if (url == null) {
                throw new BadRequestException("Cloudinary did not return an image URL");
            }
            return url.toString();
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read image file");
        } catch (Exception ex) {
            throw new BadRequestException("Failed to upload image to Cloudinary: " + ex.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPG, PNG, and WebP images are supported");
        }
    }
}
