package com.mentorx.api.feature.user.service;

import com.mentorx.api.common.exception.FrameExtractionException;
import com.mentorx.api.feature.user.util.ByteArrayMultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class VideoFrameExtractorService {

    /** JPEG từ khung tối — vẫn đủ cho face Haar nếu > ~400 byte và đúng header JFIF */
    private static final int MIN_JPEG_BYTES = 420;

    static {
        try {
            nu.pattern.OpenCV.loadLocally();
            log.info("OpenCV loaded for VideoFrameExtractorService");
        } catch (Exception e) {
            log.warn("OpenCV load failed in VideoFrameExtractorService: {}", e.getMessage());
        }
    }

    public MultipartFile extractMiddleFrame(MultipartFile livenessVideo) {
        log.info("Extracting portrait frame from video: name={} type={} size={}",
                livenessVideo.getOriginalFilename(),
                livenessVideo.getContentType(),
                livenessVideo.getSize());

        String ext = guessVideoExtension(livenessVideo);
        Path tempVideoPath = null;
        try {
            tempVideoPath = Files.createTempFile("kyc_video_", ext);
            Files.write(tempVideoPath, livenessVideo.getBytes());

            if (isFfmpegAvailable()) {
                byte[] first = runFfmpegNoSeek(tempVideoPath);
                if (isReasonableJpeg(first)) {
                    log.info("FFmpeg first-frame JPEG {} bytes", first.length);
                    return new ByteArrayMultipartFile("portrait", "portrait.jpg", "image/jpeg", first);
                }
                byte[] piped = runFfmpegMjpegPipe(tempVideoPath);
                if (isReasonableJpeg(piped)) {
                    log.info("FFmpeg pipe JPEG {} bytes", piped.length);
                    return new ByteArrayMultipartFile("portrait", "portrait.jpg", "image/jpeg", piped);
                }
                for (String sec : new String[]{"0.05", "0.15", "0.30", "0.50", "0.75", "1.00", "1.40", "2.00"}) {
                    byte[] jpg = runFfmpegOneFrame(tempVideoPath, sec);
                    if (isReasonableJpeg(jpg)) {
                        log.info("FFmpeg JPEG at {}s — {} bytes", sec, jpg.length);
                        return new ByteArrayMultipartFile("portrait", "portrait.jpg", "image/jpeg", jpg);
                    }
                }
                log.warn("FFmpeg produced no usable JPEG; trying OpenCV");
            } else {
                log.warn("FFmpeg not on PATH; using OpenCV only");
            }

            byte[] openCv = extractBestFrameOpenCv(tempVideoPath);
            if (isReasonableJpeg(openCv)) {
                log.info("OpenCV JPEG {} bytes", openCv.length);
                return new ByteArrayMultipartFile("portrait", "portrait.jpg", "image/jpeg", openCv);
            }

            throw new FrameExtractionException(
                    "Không trích được khung hình từ video. Thử: quay 3–5 giây (WebM/Chrome), mặt gần camera; trên Windows hãy cài FFmpeg và thêm vào PATH, hoặc chạy backend trong Docker có sẵn ffmpeg.");
        } catch (IOException e) {
            log.error("Frame extraction IO error", e);
            throw new FrameExtractionException("Error during frame extraction: " + e.getMessage(), e);
        } finally {
            if (tempVideoPath != null) {
                try {
                    Files.deleteIfExists(tempVideoPath);
                } catch (IOException e) {
                    log.warn("Failed to delete temp video: {}", e.getMessage());
                }
            }
        }
    }

    private static String guessVideoExtension(MultipartFile f) {
        String ct = f.getContentType();
        if (ct != null) {
            if (ct.contains("webm")) {
                return ".webm";
            }
            if (ct.contains("mp4") || ct.contains("mpeg4")) {
                return ".mp4";
            }
            if (ct.contains("quicktime")) {
                return ".mov";
            }
        }
        String n = f.getOriginalFilename();
        if (n != null) {
            String lower = n.toLowerCase();
            if (lower.endsWith(".webm")) {
                return ".webm";
            }
            if (lower.endsWith(".mp4") || lower.endsWith(".m4v")) {
                return ".mp4";
            }
            if (lower.endsWith(".mov")) {
                return ".mov";
            }
        }
        byte[] head = peekHead(f);
        if (head != null && head.length >= 12) {
            if (head[0] == 0x1a && head[1] == 0x45 && head[2] == (byte) 0xdf && head[3] == (byte) 0xa3) {
                return ".webm";
            }
            if (head[4] == 'f' && head[5] == 't' && head[6] == 'y' && head[7] == 'p') {
                return ".mp4";
            }
        }
        return ".webm";
    }

    private static byte[] peekHead(MultipartFile f) {
        try (var in = f.getInputStream()) {
            return in.readNBytes(32);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean isReasonableJpeg(byte[] data) {
        if (data == null || data.length < MIN_JPEG_BYTES) {
            return false;
        }
        return data[0] == (byte) 0xFF && data[1] == (byte) 0xD8;
    }

    private byte[] runFfmpegNoSeek(Path videoPath) {
        Path out = null;
        try {
            out = videoPath.getParent().resolve("f0_" + UUID.randomUUID() + ".jpg");
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-hide_banner",
                    "-loglevel", "error",
                    "-y",
                    "-i", videoPath.toString(),
                    "-an",
                    "-sn",
                    "-frames:v", "1",
                    "-q:v", "3",
                    out.toString()
            );
            return runFfmpegToFile(pb, out);
        } catch (IOException e) {
            log.debug("ffmpeg no-seek: {}", e.getMessage());
            return null;
        } finally {
            deleteQuiet(out);
        }
    }

    private byte[] runFfmpegOneFrame(Path videoPath, String offsetSeconds) {
        Path out = null;
        try {
            out = videoPath.getParent().resolve("f_" + UUID.randomUUID() + ".jpg");
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-hide_banner",
                    "-loglevel", "error",
                    "-y",
                    "-ss", offsetSeconds,
                    "-i", videoPath.toString(),
                    "-frames:v", "1",
                    "-q:v", "3",
                    out.toString()
            );
            return runFfmpegToFile(pb, out);
        } catch (IOException e) {
            log.debug("ffmpeg at {}s: {}", offsetSeconds, e.getMessage());
            return null;
        } finally {
            deleteQuiet(out);
        }
    }

    private static byte[] runFfmpegToFile(ProcessBuilder pb, Path out) throws IOException {
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exit;
        try {
            exit = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        byte[] pipeOut = process.getInputStream().readAllBytes();
        if (exit != 0) {
            log.debug("ffmpeg exit {}: {}", exit, new String(pipeOut, java.nio.charset.StandardCharsets.UTF_8).trim());
            return null;
        }
        if (!Files.exists(out) || Files.size(out) < MIN_JPEG_BYTES) {
            return null;
        }
        return Files.readAllBytes(out);
    }

    private byte[] runFfmpegMjpegPipe(Path videoPath) {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-hide_banner",
                "-loglevel", "error",
                "-y",
                "-i", videoPath.toString(),
                "-an",
                "-sn",
                "-frames:v", "1",
                "-f", "image2pipe",
                "-vcodec", "mjpeg",
                "-q:v", "3",
                "pipe:1"
        );
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        try {
            Process process = pb.start();
            byte[] combined;
            try (var in = process.getInputStream()) {
                combined = in.readAllBytes();
            }
            int exit = process.waitFor();
            if (exit != 0) {
                return null;
            }
            int sof = indexOfJpegStart(combined);
            if (sof < 0) {
                return null;
            }
            int end = indexOfJpegEnd(combined, sof);
            if (end < 0) {
                return null;
            }
            byte[] jpg = Arrays.copyOfRange(combined, sof, end + 1);
            return jpg.length >= MIN_JPEG_BYTES ? jpg : null;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    private static int indexOfJpegStart(byte[] data) {
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i] == (byte) 0xFF && data[i + 1] == (byte) 0xD8) {
                return i;
            }
        }
        return -1;
    }

    private static int indexOfJpegEnd(byte[] data, int from) {
        for (int i = from + 2; i < data.length - 1; i++) {
            if (data[i] == (byte) 0xFF && data[i + 1] == (byte) 0xD9) {
                return i + 1;
            }
        }
        return -1;
    }

    private static void deleteQuiet(Path p) {
        if (p == null) {
            return;
        }
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
        }
    }

    /**
     * Đọc tuần tự từ đầu clip (ổn định với WebM khi seek theo số khung sai).
     */
    private byte[] extractBestFrameOpenCv(Path videoPath) {
        VideoCapture cap = tryOpenCapture(videoPath.toString());
        if (!cap.isOpened()) {
            log.warn("OpenCV: cannot open {}", videoPath);
            return null;
        }
        try {
            List<byte[]> candidates = new ArrayList<>();
            Mat frame = new Mat();
            for (int i = 0; i < 90; i++) {
                if (!cap.read(frame) || frame.empty()) {
                    break;
                }
                if (i < 1) {
                    continue;
                }
                MatOfByte mob = new MatOfByte();
                MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 88);
                if (Imgcodecs.imencode(".jpg", frame, mob, params)) {
                    byte[] jpg = mob.toArray();
                    if (isReasonableJpeg(jpg)) {
                        candidates.add(jpg);
                    }
                }
            }
            frame.release();
            if (candidates.isEmpty()) {
                cap.set(Videoio.CAP_PROP_POS_FRAMES, 0);
                Mat f2 = new Mat();
                if (cap.read(f2) && !f2.empty()) {
                    MatOfByte mob = new MatOfByte();
                    if (Imgcodecs.imencode(".jpg", f2, mob, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 88))
                            && isReasonableJpeg(mob.toArray())) {
                        candidates.add(mob.toArray());
                    }
                }
                f2.release();
            }
            return candidates.stream().max(Comparator.comparingInt(a -> a.length)).orElse(null);
        } finally {
            cap.release();
        }
    }

    private static VideoCapture tryOpenCapture(String path) {
        VideoCapture cap = new VideoCapture(path, Videoio.CAP_FFMPEG);
        if (cap.isOpened()) {
            return cap;
        }
        cap.release();
        cap = new VideoCapture(path, Videoio.CAP_ANY);
        return cap;
    }

    private boolean isFfmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exit = process.waitFor();
            return exit == 0;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}
