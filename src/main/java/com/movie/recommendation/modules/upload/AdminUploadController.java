package com.movie.recommendation.modules.upload;

import com.movie.recommendation.common.dto.ApiResponse;
import com.movie.recommendation.modules.upload.dto.UploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/uploads")
@Tag(name = "Admin Uploads", description = "Admin media upload endpoints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/poster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a movie poster image to Cloudinary")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadPoster(
            @RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadPoster(file);
        return ResponseEntity.ok(ApiResponse.success(new UploadResponse(url), "Poster uploaded successfully"));
    }
}
