package com.mentorx.api.feature.user.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Open-source OCR service using Tesseract
 * Completely FREE - no API key required!
 */
@Service
@Slf4j
public class OpenSourceOcrService {

    private final Tesseract tesseract;

    // Regex patterns for Vietnamese ID card
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("\\b\\d{12}\\b");
    private static final Pattern DOB_PATTERN = Pattern.compile("\\b(\\d{2}[/-]\\d{2}[/-]\\d{4})\\b");
    private static final Pattern NAME_PATTERN = Pattern.compile("(?:Họ và tên|Ho va ten|Full name)[:\\s]*([A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ\\s]+)", Pattern.CASE_INSENSITIVE);

    public OpenSourceOcrService() {
        this.tesseract = new Tesseract();
        
        // Configure Tesseract
        // Download Vietnamese language data from: https://github.com/tesseract-ocr/tessdata
        // Place in: src/main/resources/tessdata/vie.traineddata
        try {
            tesseract.setDatapath("src/main/resources/tessdata");
            tesseract.setLanguage("vie+eng"); // Vietnamese + English
            tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
            tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
        } catch (Exception e) {
            log.warn("Tesseract configuration warning: {}. Will use defaults.", e.getMessage());
            // Fallback to system tessdata if available
            tesseract.setLanguage("eng");
        }
    }

    /**
     * Extract text from ID card image
     */
    public Map<String, String> extractIdCardInfo(MultipartFile imageFile) {
        log.info("🔍 Extracting ID card info using Tesseract OCR (FREE)");
        log.info("Image: {}, Size: {} bytes", imageFile.getOriginalFilename(), imageFile.getSize());

        Map<String, String> result = new HashMap<>();

        try {
            // Read image
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            if (image == null) {
                throw new IOException("Failed to read image");
            }

            // Perform OCR
            String text = tesseract.doOCR(image);
            log.debug("OCR Raw Text:\n{}", text);

            // Extract information using regex
            result.put("rawText", text);
            result.put("name", extractName(text));
            result.put("idNumber", extractIdNumber(text));
            result.put("dob", extractDateOfBirth(text));
            result.put("gender", extractGender(text));
            result.put("address", extractAddress(text));

            log.info("✅ OCR completed - Name: {}, ID: {}, DOB: {}", 
                    result.get("name"), result.get("idNumber"), result.get("dob"));

            return result;

        } catch (TesseractException e) {
            log.error("Tesseract OCR failed", e);
            throw new RuntimeException("OCR processing failed: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to read image", e);
            throw new RuntimeException("Failed to read image: " + e.getMessage(), e);
        }
    }

    private String extractName(String text) {
        Matcher matcher = NAME_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // Fallback: look for capitalized words after common keywords
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].toLowerCase();
            if (line.contains("họ và tên") || line.contains("ho va ten") || line.contains("full name")) {
                if (i + 1 < lines.length) {
                    return lines[i + 1].trim();
                }
            }
        }
        
        return "Unknown";
    }

    private String extractIdNumber(String text) {
        Matcher matcher = ID_NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Unknown";
    }

    private String extractDateOfBirth(String text) {
        Matcher matcher = DOB_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Look for date patterns near "Ngày sinh" or "Date of birth"
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].toLowerCase();
            if (line.contains("ngày sinh") || line.contains("ngay sinh") || line.contains("date of birth")) {
                // Check current line and next line
                String combined = lines[i] + (i + 1 < lines.length ? " " + lines[i + 1] : "");
                Matcher m = DOB_PATTERN.matcher(combined);
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        
        return "Unknown";
    }

    private String extractGender(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("nam") && !lowerText.contains("nữ")) {
            return "Nam";
        } else if (lowerText.contains("nữ") || lowerText.contains("nu")) {
            return "Nữ";
        } else if (lowerText.contains("male")) {
            return "Nam";
        } else if (lowerText.contains("female")) {
            return "Nữ";
        }
        return "Unknown";
    }

    private String extractAddress(String text) {
        // Look for address after common keywords
        String[] lines = text.split("\\n");
        StringBuilder address = new StringBuilder();
        boolean foundAddressKeyword = false;
        
        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("nơi thường trú") || 
                lowerLine.contains("noi thuong tru") || 
                lowerLine.contains("address") ||
                lowerLine.contains("quê quán")) {
                foundAddressKeyword = true;
                continue;
            }
            
            if (foundAddressKeyword && !line.trim().isEmpty()) {
                address.append(line.trim()).append(" ");
                if (address.length() > 50) { // Reasonable address length
                    break;
                }
            }
        }
        
        return address.length() > 0 ? address.toString().trim() : "Unknown";
    }

    /**
     * Check if Tesseract is properly configured
     */
    public boolean isConfigured() {
        try {
            tesseract.doOCR(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
            return true;
        } catch (Exception e) {
            log.warn("Tesseract not properly configured: {}", e.getMessage());
            return false;
        }
    }
}
