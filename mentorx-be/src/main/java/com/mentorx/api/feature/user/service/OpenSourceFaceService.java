package com.mentorx.api.feature.user.service;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Face detection + coarse face similarity (histogram + CLAHE + cosine on aligned crops).
 * Liveness uses inter-frame motion in the uploaded video (not spoof-proof, but rejects static uploads).
 */
@Service
@Slf4j
public class OpenSourceFaceService {

    private CascadeClassifier faceDetector;
    private boolean initialized = false;

    /** Ngưỡng tương quan tổng hợp (max của nhiều metric); ảnh chip CCCD vs webcam thường cần ~0.52–0.60 */
    @Value("${app.kyc.face-match-min-correlation:0.56}")
    private double faceMatchMinCorrelation;

    @Value("${app.kyc.liveness-min-mean-diff:3.0}")
    private double livenessMinMeanDiff;

    /** Minimum decodable frames; browser WebM often reports wrong CAP_PROP_FRAME_COUNT — do not set above ~4. */
    @Value("${app.kyc.liveness-min-frames:2}")
    private int livenessMinFrames;

    static {
        try {
            nu.pattern.OpenCV.loadLocally();
            log.info("OpenCV native library loaded");
        } catch (Exception e) {
            log.error("Failed to load OpenCV", e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            String cascadePath = "haarcascade_frontalface_default.xml";
            var resource = getClass().getClassLoader().getResourceAsStream(cascadePath);
            if (resource != null) {
                Path tempFile = Files.createTempFile("haarcascade", ".xml");
                Files.copy(resource, tempFile, StandardCopyOption.REPLACE_EXISTING);
                faceDetector = new CascadeClassifier(tempFile.toString());
                tempFile.toFile().deleteOnExit();
            } else {
                faceDetector = new CascadeClassifier();
                faceDetector.load(cascadePath);
            }
            initialized = faceDetector != null && !faceDetector.empty();
            if (initialized) {
                log.info("Face detector (Haar) initialized");
            } else {
                log.warn("Face detector is empty");
            }
        } catch (Exception e) {
            log.error("Failed to initialize face detector", e);
            initialized = false;
        }
    }

    /**
     * Count frontal faces (for diagnostics / admin); uses a temp copy of the upload.
     */
    public int detectFaces(MultipartFile imageFile) {
        if (!initialized) {
            return 0;
        }
        try {
            Path tmp = Files.createTempFile("face_cnt", extensionOf(imageFile));
            imageFile.transferTo(tmp.toFile());
            try {
                return countFacesOnPath(tmp);
            } finally {
                Files.deleteIfExists(tmp);
            }
        } catch (Exception e) {
            log.warn("detectFaces failed: {}", e.getMessage());
            return 0;
        }
    }

    public LivenessResult checkLiveness(MultipartFile videoFile) {
        log.info("Liveness: file={}, {} bytes, type={}",
                videoFile.getOriginalFilename(), videoFile.getSize(), videoFile.getContentType());

        long size = videoFile.getSize();
        if (size < 12_000) {
            return new LivenessResult(false, 0.08, "Video quá nhỏ. Hãy ghi lại vài giây với khuôn mặt rõ trong khung.");
        }
        if (size > 50_000_000) {
            return new LivenessResult(false, 0.1, "Video quá lớn.");
        }
        String ct = videoFile.getContentType();
        if (ct != null && !ct.startsWith("video/") && !ct.equals("application/octet-stream")) {
            return new LivenessResult(false, 0.12, "Định dạng tệp không phải video.");
        }

        Path tmp = null;
        try {
            tmp = Files.createTempFile("liv_", extensionOf(videoFile));
            videoFile.transferTo(tmp.toFile());
            return analyzeVideoMotion(tmp);
        } catch (IOException e) {
            log.error("Liveness temp file failed", e);
            return new LivenessResult(false, 0.0, "Không thể lưu video tạm để phân tích.");
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public FaceMatchResult matchFaces(MultipartFile image1, MultipartFile image2) {
        log.info("Face match: {} vs {}", image1.getOriginalFilename(), image2.getOriginalFilename());

        if (!initialized) {
            return new FaceMatchResult(false, 0.0,
                    "Hệ thống so khớp khuôn mặt chưa sẵn sàng. Liên hệ quản trị hoặc bật profile dev-mock để thử nghiệm.");
        }

        Path p1 = null;
        Path p2 = null;
        try {
            p1 = Files.createTempFile("kyc_a", extensionOf(image1));
            p2 = Files.createTempFile("kyc_b", extensionOf(image2));
            Files.write(p1, image1.getBytes());
            Files.write(p2, image2.getBytes());

            int c1 = countFacesOnPath(p1);
            int c2 = countFacesOnPath(p2);
            if (c1 == 0 && c2 == 0) {
                return new FaceMatchResult(false, 0.0,
                        "Không phát hiện khuôn mặt trên ảnh CCCD mặt trước và trên khung hình lấy từ video. Hãy chụp CCCD rõ, đủ sáng, và quay video mặt hướng thẳng camera.");
            }
            if (c1 == 0) {
                return new FaceMatchResult(false, 0.0,
                        "Không phát hiện khuôn mặt trên ảnh CCCD mặt trước (ảnh chip nhỏ cần chụp gần, rõ nét).");
            }
            if (c2 == 0) {
                return new FaceMatchResult(false, 0.0,
                        "Không phát hiện khuôn mặt trên ảnh lấy từ video. Hãy quay lại: mặt trong khung, đủ sáng, tránh quá xa hoặc quá mờ.");
            }

            Mat face1 = extractLargestFaceGray(p1);
            Mat face2 = extractLargestFaceGray(p2);
            if (face1 == null || face2 == null) {
                if (face1 != null) {
                    face1.release();
                }
                if (face2 != null) {
                    face2.release();
                }
                return new FaceMatchResult(false, 0.0, "Không trích xuất được vùng mặt từ ảnh.");
            }

            double corrHist = grayHistogramCorrelation(face1, face2);
            Mat cl1 = applyClahe(face1);
            Mat cl2 = applyClahe(face2);
            double corrClahe = grayHistogramCorrelation(cl1, cl2);
            cl1.release();
            cl2.release();
            double cosNorm = normalizedCosineSimilarity(face1, face2);
            face1.release();
            face2.release();

            double corr = maxFaceSimilarityScore(corrHist, corrClahe, cosNorm);

            if (Double.isNaN(corr) || corr < 0) {
                corr = 0;
            }

            double similarityPct = corr * 100.0;
            boolean isMatch = corr >= faceMatchMinCorrelation;
            String msg = isMatch
                    ? "So khớp khuôn mặt đạt ngưỡng"
                    : String.format(
                            "Khuôn mặt trên video không khớp đủ với ảnh chip trên CCCD (điểm ~%.0f%%). Thử: chụp CCCD rõ chip, đủ sáng; quay mặt thẳng, gần camera, tránh ngược sáng.",
                            similarityPct);

            log.info("Face match scores hist={} clahe={} cos={} combined={} threshold={} isMatch={}",
                    corrHist, corrClahe, cosNorm, corr, faceMatchMinCorrelation, isMatch);
            return new FaceMatchResult(isMatch, similarityPct, msg);

        } catch (Exception e) {
            log.error("Face match failed", e);
            return new FaceMatchResult(false, 0.0, "Lỗi xử lý so khớp: " + e.getMessage());
        } finally {
            try {
                if (p1 != null) {
                    Files.deleteIfExists(p1);
                }
                if (p2 != null) {
                    Files.deleteIfExists(p2);
                }
            } catch (IOException ignored) {
            }
        }
    }

    private LivenessResult analyzeVideoMotion(Path videoPath) {
        VideoCapture cap = new VideoCapture(videoPath.toString());
        if (!cap.isOpened()) {
            return new LivenessResult(false, 0.14,
                    "Không đọc được file video. Hãy ghi lại bằng camera trên trình duyệt (WebM) hoặc cập nhật trình duyệt.");
        }
        try {
            long frameCount = (long) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
            if (frameCount <= 0 || frameCount > 500_000) {
                frameCount = countFramesByReading(cap);
            } else if (frameCount < 4) {
                // Browser WebM often reports CAP_PROP_FRAME_COUNT as 1; re-count by decoding.
                long byRead = countFramesByReading(cap);
                frameCount = Math.max(frameCount, byRead);
            }
            if (frameCount < Math.max(2, livenessMinFrames)) {
                return new LivenessResult(false, 0.18,
                        "Không đọc được đủ khung hình từ video. Hãy quay lại bằng camera trình duyệt hoặc thử Chrome/Edge.");
            }

            long effectiveFrames = Math.max(frameCount, 3);
            double motion = frameMotionScore(cap, effectiveFrames);
            if (motion < livenessMinMeanDiff && frameCount >= 4) {
                double alt = frameMotionScoreEnds(cap, frameCount);
                motion = Math.max(motion, alt);
            }
            if (motion < livenessMinMeanDiff) {
                return new LivenessResult(false, 0.22,
                        "Không thấy đủ chuyển động trong video. Hãy quay lại, hơi xoay đầu hoặc chớp mắt (tránh đứng yên hoàn toàn).");
            }

            double score = Math.min(0.96, 0.52 + motion / 45.0);
            return new LivenessResult(true, score, "Đã kiểm tra chuyển động cơ bản trong video.");
        } finally {
            cap.release();
        }
    }

    private static long countFramesByReading(VideoCapture cap) {
        cap.set(Videoio.CAP_PROP_POS_FRAMES, 0);
        Mat buf = new Mat();
        long n = 0;
        while (cap.read(buf) && !buf.empty() && n < 2_000) {
            n++;
        }
        buf.release();
        cap.set(Videoio.CAP_PROP_POS_FRAMES, 0);
        return n;
    }

    private static double frameMotionScore(VideoCapture cap, long frameCount) {
        cap.set(Videoio.CAP_PROP_POS_FRAMES, 0);
        Mat f0 = new Mat();
        if (!cap.read(f0) || f0.empty()) {
            f0.release();
            return 0;
        }
        long idx1 = Math.max(1L, frameCount / 3);
        cap.set(Videoio.CAP_PROP_POS_FRAMES, idx1);
        Mat f1 = new Mat();
        if (!cap.read(f1) || f1.empty()) {
            f0.release();
            return 0;
        }
        return meanAbsDiffGray(f0, f1);
    }

    /** Compare first and last decodable frame (helps short WebM where middle ≈ first). */
    private static double frameMotionScoreEnds(VideoCapture cap, long frameCount) {
        cap.set(Videoio.CAP_PROP_POS_FRAMES, 0);
        Mat f0 = new Mat();
        if (!cap.read(f0) || f0.empty()) {
            f0.release();
            return 0;
        }
        long last = Math.max(1L, frameCount - 1);
        cap.set(Videoio.CAP_PROP_POS_FRAMES, last);
        Mat f1 = new Mat();
        if (!cap.read(f1) || f1.empty()) {
            f0.release();
            return 0;
        }
        return meanAbsDiffGray(f0, f1);
    }

    private static double meanAbsDiffGray(Mat f0, Mat f1) {
        Mat g0 = new Mat();
        Mat g1 = new Mat();
        Mat diff = new Mat();
        try {
            Imgproc.cvtColor(f0, g0, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(f1, g1, Imgproc.COLOR_BGR2GRAY);
            Imgproc.resize(g0, g0, new Size(200, 112));
            Imgproc.resize(g1, g1, new Size(200, 112));
            Core.absdiff(g0, g1, diff);
            Scalar m = Core.mean(diff);
            return m.val[0];
        } finally {
            f0.release();
            f1.release();
            g0.release();
            g1.release();
            diff.release();
        }
    }

    /**
     * CCCD chip portrait is often &lt; 40px; Haar with min 40×40 misses. Uses multi-pass + upscale.
     */
    private int countFacesOnPath(Path path) {
        Mat bgr = imreadBgr(path);
        if (bgr == null || bgr.empty()) {
            log.warn("countFacesOnPath: cannot decode image {}", path);
            return 0;
        }
        Mat gray = new Mat();
        try {
            Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY);
            Rect r = findLargestFaceRectFromGray(gray);
            return r == null ? 0 : 1;
        } finally {
            bgr.release();
            gray.release();
        }
    }

    private Mat extractLargestFaceGray(Path path) {
        Mat bgr = imreadBgr(path);
        if (bgr == null || bgr.empty()) {
            return null;
        }
        Mat gray = new Mat();
        try {
            Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY);
            Rect best = findLargestFaceRectFromGray(gray);
            if (best == null) {
                return null;
            }
            Rect use = expandRectClamped(best, 0.14, gray.cols(), gray.rows());
            if (use == null) {
                use = best;
            }
            Mat roiView = new Mat(gray, use);
            Mat roi = roiView.clone();
            Mat resized = new Mat();
            Imgproc.resize(roi, resized, new Size(160, 160));
            roi.release();
            Mat out = new Mat();
            Imgproc.equalizeHist(resized, out);
            resized.release();
            return out;
        } finally {
            bgr.release();
            gray.release();
        }
    }

    private static Mat imreadBgr(Path path) {
        Mat m = Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_COLOR);
        if (!m.empty()) {
            return m;
        }
        m.release();
        Mat raw = Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_UNCHANGED);
        if (raw.empty()) {
            return raw;
        }
        if (raw.channels() == 4) {
            Mat bgr = new Mat();
            Imgproc.cvtColor(raw, bgr, Imgproc.COLOR_BGRA2BGR);
            raw.release();
            return bgr;
        }
        if (raw.channels() == 1) {
            Mat bgr = new Mat();
            Imgproc.cvtColor(raw, bgr, Imgproc.COLOR_GRAY2BGR);
            raw.release();
            return bgr;
        }
        return raw;
    }

    private Rect findLargestFaceRectFromGray(Mat grayFull) {
        Rect r = detectLargestFaceOnGray(grayFull, 1.06, 2, new Size(18, 18));
        if (r != null) {
            return r;
        }
        Mat eq = new Mat();
        try {
            Imgproc.equalizeHist(grayFull, eq);
            r = detectLargestFaceOnGray(eq, 1.06, 2, new Size(18, 18));
            if (r != null) {
                return r;
            }
            r = detectLargestFaceOnGray(eq, 1.1, 1, new Size(15, 15));
            if (r != null) {
                return r;
            }
        } finally {
            eq.release();
        }
        r = detectLargestFaceOnGray(grayFull, 1.1, 1, new Size(15, 15));
        if (r != null) {
            return r;
        }
        if (grayFull.width() < 2200 && grayFull.height() < 2200) {
            Mat big = new Mat();
            try {
                Imgproc.resize(grayFull, big, new Size(grayFull.cols() * 2.0, grayFull.rows() * 2.0), 0, 0, Imgproc.INTER_CUBIC);
                r = detectLargestFaceOnGray(big, 1.06, 2, new Size(24, 24));
                if (r != null) {
                    double sx = grayFull.cols() / (double) big.cols();
                    return clampRect(
                            new Rect(
                                    (int) Math.round(r.x * sx),
                                    (int) Math.round(r.y * sx),
                                    (int) Math.round(r.width * sx),
                                    (int) Math.round(r.height * sx)),
                            grayFull.cols(),
                            grayFull.rows());
                }
            } finally {
                big.release();
            }
        }
        return null;
    }

    private Rect detectLargestFaceOnGray(Mat gray, double scaleFactor, int minNeighbors, Size minSize) {
        MatOfRect faces = new MatOfRect();
        try {
            faceDetector.detectMultiScale(gray, faces, scaleFactor, minNeighbors, 0, minSize, new Size());
            Rect[] arr = faces.toArray();
            if (arr.length == 0) {
                return null;
            }
            return Arrays.stream(arr).max(Comparator.comparingInt(r -> r.width * r.height)).orElse(arr[0]);
        } finally {
            faces.release();
        }
    }

    /** Mở rộng bbox mặt một chút (cằm/má) giúp ổn định so khớp chip CCCD vs khung video. */
    private static Rect expandRectClamped(Rect r, double padFrac, int maxW, int maxH) {
        int dx = (int) Math.round(r.width * padFrac);
        int dy = (int) Math.round(r.height * padFrac);
        return clampRect(new Rect(r.x - dx, r.y - dy, r.width + 2 * dx, r.height + 2 * dy), maxW, maxH);
    }

    private static Mat applyClahe(Mat grayU8) {
        CLAHE clahe = Imgproc.createCLAHE(3.5, new Size(8, 8));
        Mat dst = new Mat();
        clahe.apply(grayU8, dst);
        return dst;
    }

    /**
     * Cosine giữa hai patch đã làm mịn + chuẩn hoá sáng (bổ sung cho histogram khi ảnh chip vs webcam lệch tone).
     */
    private static double normalizedCosineSimilarity(Mat grayA, Mat grayB) {
        Mat a = new Mat();
        Mat b = new Mat();
        Mat sa = new Mat();
        Mat sb = new Mat();
        Mat fa = new Mat();
        Mat fb = new Mat();
        try {
            Imgproc.GaussianBlur(grayA, a, new Size(5, 5), 0);
            Imgproc.GaussianBlur(grayB, b, new Size(5, 5), 0);
            Imgproc.resize(a, sa, new Size(72, 72));
            Imgproc.resize(b, sb, new Size(72, 72));
            sa.convertTo(fa, CvType.CV_32F);
            sb.convertTo(fb, CvType.CV_32F);
            Scalar ma = Core.mean(fa);
            Scalar mb = Core.mean(fb);
            Core.subtract(fa, ma, fa);
            Core.subtract(fb, mb, fb);
            double na = Core.norm(fa);
            double nb = Core.norm(fb);
            if (na < 1e-6 || nb < 1e-6) {
                return 0;
            }
            Core.divide(fa, new Scalar(na), fa);
            Core.divide(fb, new Scalar(nb), fb);
            return fa.dot(fb);
        } finally {
            a.release();
            b.release();
            sa.release();
            sb.release();
            fa.release();
            fb.release();
        }
    }

    /** Đưa cosine [-1,1] về thang gần correlation để lấy max với histogram. */
    private static double maxFaceSimilarityScore(double histCorr, double claheHistCorr, double cosineRaw) {
        double cos01 = (cosineRaw + 1.0) / 2.0;
        double h1 = histCorr;
        double h2 = claheHistCorr;
        if (Double.isNaN(h1) || h1 < 0) {
            h1 = 0;
        }
        if (Double.isNaN(h2) || h2 < 0) {
            h2 = 0;
        }
        if (Double.isNaN(cos01)) {
            cos01 = 0;
        }
        cos01 = Math.max(0, Math.min(1, cos01));
        return Math.max(h1, Math.max(h2, cos01));
    }

    private static Rect clampRect(Rect r, int w, int h) {
        int x = Math.max(0, Math.min(r.x, Math.max(0, w - 1)));
        int y = Math.max(0, Math.min(r.y, Math.max(0, h - 1)));
        int rw = Math.min(r.width, w - x);
        int rh = Math.min(r.height, h - y);
        if (rw < 12 || rh < 12) {
            return null;
        }
        return new Rect(x, y, rw, rh);
    }

    private static double grayHistogramCorrelation(Mat grayA, Mat grayB) {
        Mat h1 = new Mat();
        Mat h2 = new Mat();
        try {
            Imgproc.calcHist(
                    Collections.singletonList(grayA),
                    new MatOfInt(0),
                    new Mat(),
                    h1,
                    new MatOfInt(256),
                    new MatOfFloat(0f, 256f),
                    false
            );
            Imgproc.calcHist(
                    Collections.singletonList(grayB),
                    new MatOfInt(0),
                    new Mat(),
                    h2,
                    new MatOfInt(256),
                    new MatOfFloat(0f, 256f),
                    false
            );
            Core.normalize(h1, h1, 1, 0, Core.NORM_L1);
            Core.normalize(h2, h2, 1, 0, Core.NORM_L1);
            double c = Imgproc.compareHist(h1, h2, Imgproc.HISTCMP_CORREL);
            return c;
        } finally {
            h1.release();
            h2.release();
        }
    }

    private static String extensionOf(MultipartFile file) {
        String n = file.getOriginalFilename();
        if (n != null && n.contains(".")) {
            return n.substring(n.lastIndexOf('.'));
        }
        return ".bin";
    }

    public boolean isInitialized() {
        return initialized;
    }

    public record LivenessResult(boolean isLive, double score, String message) {}

    public record FaceMatchResult(boolean isMatch, double similarity, String message) {}
}
