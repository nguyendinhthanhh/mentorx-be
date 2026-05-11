package com.mentorx.api.feature.user.service;

import com.mentorx.api.common.exception.FrameExtractionException;
import lombok.extern.slf4j.Slf4j;
import com.mentorx.api.feature.user.util.ByteArrayMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
public class VideoFrameExtractorService {

    public MultipartFile extractMiddleFrame(MultipartFile livenessVideo) {
        log.info("Extracting middle frame from video: {}", livenessVideo.getOriginalFilename());
        Path tempVideoPath = null;
        Path tempFramePath = null;

        try {
            // 1. Save video to temp file
            tempVideoPath = Files.createTempFile("kyc_video_", "_" + livenessVideo.getOriginalFilename());
            livenessVideo.transferTo(tempVideoPath.toFile());

            // 2. Determine duration and extract middle frame using FFmpeg
            // Simplified: extract frame at 1.5 seconds mark for a 3-5s video
            String frameFileName = "frame_" + UUID.randomUUID() + ".jpg";
            tempFramePath = tempVideoPath.getParent().resolve(frameFileName);

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-ss", "00:00:01.500",
                    "-i", tempVideoPath.toString(),
                    "-frames:v", "1",
                    "-q:v", "2",
                    tempFramePath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                byte[] errorOutput = process.getInputStream().readAllBytes();
                log.error("FFmpeg failed with exit code {}: {}", exitCode, new String(errorOutput));
                throw new FrameExtractionException("FFmpeg failed to extract frame");
            }

            // 3. Read output jpg
            byte[] frameBytes = Files.readAllBytes(tempFramePath);
            return new ByteArrayMultipartFile(
                    "portrait",
                    "portrait.jpg",
                    "image/jpeg",
                    frameBytes
            );


        } catch (IOException | InterruptedException e) {
            log.error("Error during frame extraction", e);
            throw new FrameExtractionException("Error during frame extraction: " + e.getMessage(), e);
        } finally {
            // Clean up temp files
            try {
                if (tempVideoPath != null) Files.deleteIfExists(tempVideoPath);
                if (tempFramePath != null) Files.deleteIfExists(tempFramePath);
            } catch (IOException e) {
                log.warn("Failed to delete temp files: {}", e.getMessage());
            }
        }
    }
}
