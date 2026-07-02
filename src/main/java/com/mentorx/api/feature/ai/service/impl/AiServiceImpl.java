package com.mentorx.api.feature.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.ai.config.OpenRouterConfig;
import com.mentorx.api.feature.ai.dto.request.ExplainTaskRequest;
import com.mentorx.api.feature.ai.dto.response.ExplainTaskResponse;
import com.mentorx.api.feature.ai.enums.AiTaskType;
import com.mentorx.api.feature.ai.service.AiService;
import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final RestTemplate restTemplate;
    private final OpenRouterConfig openRouterConfig;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ExplainTaskResponse explainTask(ExplainTaskRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        String context = buildContext(request.taskType(), request.taskId(), currentUserId);

        String contextTruncated = context.length() > 1500 ? context.substring(0, 1500) + "..." : context;
        String systemPrompt = "Bạn là AI MentorX. Trả lời cực ngắn, tối đa 3-4 câu, bằng tiếng Việt.\n"
            + "=== CÔNG VIỆC ===\n" + contextTruncated + "\n"
            + "=== HỎI ===\n" + request.question();

        String explanation = callOpenRouter(systemPrompt);
        return new ExplainTaskResponse(explanation);
    }

    private String buildContext(AiTaskType taskType, UUID taskId, UUID currentUserId) {
        return switch (taskType) {
            case JOB -> buildJobContext(taskId);
            case PROPOSAL -> buildProposalContext(taskId, currentUserId);
            case CONTRACT -> buildContractContext(taskId, currentUserId);
        };
    }

    private String buildJobContext(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.AI_TASK_NOT_FOUND, "Job not found"));

        StringBuilder ctx = new StringBuilder();
        ctx.append("Loại công việc: ").append(job.getJobType()).append("\n");
        ctx.append("Tiêu đề: ").append(nullToEmpty(job.getTitle())).append("\n");
        ctx.append("Mô tả: ").append(nullToEmpty(job.getDescription())).append("\n");

        if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) {
            ctx.append("Kỹ năng yêu cầu: ").append(String.join(", ", job.getRequiredSkills())).append("\n");
        }
        if (job.getExperienceLevel() != null) {
            ctx.append("Cấp độ kinh nghiệm yêu cầu: ").append(job.getExperienceLevel()).append("\n");
        }
        if (job.getCurrentLevel() != null) {
            ctx.append("Trình độ hiện tại của client: ").append(job.getCurrentLevel()).append("\n");
        }
        if (job.getLearningGoals() != null) {
            ctx.append("Mục tiêu học tập: ").append(job.getLearningGoals()).append("\n");
        }
        if (job.getSuccessCriteria() != null) {
            ctx.append("Tiêu chí thành công: ").append(job.getSuccessCriteria()).append("\n");
        }
        if (job.getAvailabilityExpectation() != null) {
            ctx.append("Yêu cầu thời gian: ").append(job.getAvailabilityExpectation()).append("\n");
        }
        if (job.getCommunicationPreference() != null) {
            ctx.append("Hình thức giao tiếp: ").append(job.getCommunicationPreference()).append("\n");
        }
        if (job.getBudgetMinMxc() != null && job.getBudgetMaxMxc() != null) {
            ctx.append("Ngân sách: ").append(job.getBudgetMinMxc()).append(" - ").append(job.getBudgetMaxMxc()).append(" MXC\n");
        } else if (job.getHourlyRateMxc() != null) {
            ctx.append("Giá theo giờ: ").append(job.getHourlyRateMxc()).append(" MXC/giờ\n");
        }

        return ctx.toString();
    }

    private String buildProposalContext(UUID proposalId, UUID currentUserId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.AI_TASK_NOT_FOUND, "Proposal not found"));

        if (!proposal.getMentor().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.AI_UNAUTHORIZED_ACCESS);
        }

        Job job = proposal.getJob();

        StringBuilder ctx = new StringBuilder();
        ctx.append("=== THÔNG TIN JOB GỐC ===\n");
        ctx.append("Tiêu đề job: ").append(nullToEmpty(job.getTitle())).append("\n");
        ctx.append("Mô tả job: ").append(nullToEmpty(job.getDescription())).append("\n");
        if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) {
            ctx.append("Kỹ năng yêu cầu: ").append(String.join(", ", job.getRequiredSkills())).append("\n");
        }
        ctx.append("\n=== THÔNG TIN PROPOSAL ===\n");
        ctx.append("Cover letter: ").append(nullToEmpty(proposal.getCoverLetter())).append("\n");
        ctx.append("Số tiền đề xuất: ").append(proposal.getProposedAmount()).append(" MXC\n");
        if (proposal.getScopeDescription() != null) {
            ctx.append("Phạm vi công việc: ").append(proposal.getScopeDescription()).append("\n");
        }
        if (proposal.getRelevantExperience() != null) {
            ctx.append("Kinh nghiệm liên quan: ").append(proposal.getRelevantExperience()).append("\n");
        }
        if (proposal.getEstimatedDurationDays() != null) {
            ctx.append("Thời gian dự kiến: ").append(proposal.getEstimatedDurationDays()).append(" ngày\n");
        }
        if (proposal.getQuestions() != null) {
            ctx.append("Câu hỏi/Thắc mắc: ").append(proposal.getQuestions()).append("\n");
        }
        if (proposal.getTerms() != null) {
            ctx.append("Điều khoản: ").append(proposal.getTerms()).append("\n");
        }

        return ctx.toString();
    }

    private String buildContractContext(UUID contractId, UUID currentUserId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.AI_TASK_NOT_FOUND, "Contract not found"));

        boolean isMentor = contract.getMentor().getId().equals(currentUserId);
        boolean isClient = contract.getClient().getId().equals(currentUserId);
        if (!isMentor && !isClient) {
            throw new AppException(ErrorCode.AI_UNAUTHORIZED_ACCESS);
        }

        StringBuilder ctx = new StringBuilder();
        ctx.append("Tiêu đề hợp đồng: ").append(nullToEmpty(contract.getTitle())).append("\n");
        ctx.append("Mô tả hợp đồng: ").append(nullToEmpty(contract.getDescription())).append("\n");
        ctx.append("Trạng thái: ").append(contract.getStatus()).append("\n");
        ctx.append("Tổng số tiền: ").append(contract.getTotalAmount()).append(" MXC\n");
        if (contract.getScopeDescription() != null) {
            ctx.append("Phạm vi công việc: ").append(contract.getScopeDescription()).append("\n");
        }
        if (contract.getTermsAndConditions() != null) {
            ctx.append("Điều khoản & điều kiện: ").append(contract.getTermsAndConditions()).append("\n");
        }
        if (contract.getPaymentTerms() != null) {
            ctx.append("Điều khoản thanh toán: ").append(contract.getPaymentTerms()).append("\n");
        }
        if (contract.getDeliverables() != null) {
            ctx.append("Kết quả bàn giao: ").append(contract.getDeliverables()).append("\n");
        }
        if (contract.getStartDate() != null) {
            ctx.append("Ngày bắt đầu: ").append(contract.getStartDate()).append("\n");
        }
        if (contract.getEndDate() != null) {
            ctx.append("Ngày kết thúc: ").append(contract.getEndDate()).append("\n");
        }
        if (contract.getMilestones() != null && !contract.getMilestones().isEmpty()) {
            ctx.append("Số lượng milestone: ").append(contract.getMilestones().size()).append("\n");
            contract.getMilestones().forEach(m -> {
                ctx.append("  - ").append(nullToEmpty(m.getTitle()))
                   .append(" (").append(m.getAmount()).append(" MXC, trạng thái: ").append(m.getStatus()).append(")\n");
            });
        }

        return ctx.toString();
    }

    private static final List<String> FALLBACK_MODELS = List.of(
        "qwen/qwen-2.5-72b-instruct"
    );

    @SuppressWarnings("unchecked")
    private String callOpenRouter(String prompt) {
        String url = openRouterConfig.url();
        String apiKey = openRouterConfig.apiKey();

        List<String> modelsToTry = new java.util.ArrayList<>(FALLBACK_MODELS);
        String configModel = openRouterConfig.model();
        if (configModel != null && !configModel.isBlank() && !modelsToTry.contains(configModel)) {
            modelsToTry.add(0, configModel);
        }

        AppException lastError = null;

        for (String model : modelsToTry) {
            log.info("Calling OpenRouter: model={}, url={}, apiKey={}", model, url, maskKey(apiKey));

            try {
                Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.1,
                    "max_tokens", 128,
                    "top_p", 0.5
                );

                String jsonRequest = objectMapper.writeValueAsString(requestBody);
                log.debug("OpenRouter request body: {}", jsonRequest);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);
                headers.add("HTTP-Referer", "https://mentorx.app");
                headers.add("X-Title", "MentorX");

                HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                if (log.isDebugEnabled()) {
                    log.debug("OpenRouter response status: {}", response.getStatusCode());
                }

                Map<String, Object> body = response.getBody();
                if (body == null || !body.containsKey("choices")) {
                    log.error("OpenRouter returned unexpected response: {}", body);
                    continue;
                }

                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices.isEmpty()) {
                    log.error("OpenRouter empty choices");
                    continue;
                }

                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                log.info("OpenRouter success with model {}: response length={} chars", model, content != null ? content.length() : 0);
                return content;

            } catch (HttpClientErrorException e) {
                log.warn("OpenRouter model {} failed: status={}, body={}", model, e.getStatusCode(), e.getResponseBodyAsString());
                int status = e.getStatusCode().value();
                if (status == 401) {
                    throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE, "API key OpenRouter không hợp lệ.");
                }
                if (status == 403) {
                    throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE, "Key OpenRouter đã hết hạn mức.");
                }
                if (status == 429 || status == 503) {
                    lastError = new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE, "Hết quota OpenRouter API. Chi tiết: " + e.getResponseBodyAsString());
                    continue;
                }
                throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE, "OpenRouter lỗi (" + status + "). Chi tiết: " + e.getResponseBodyAsString());
            } catch (Exception e) {
                log.warn("OpenRouter model {} unexpected error: {}", model, e.getMessage());
                lastError = new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE, e.getMessage());
                continue;
            }
        }

        if (lastError != null) throw lastError;
        throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE, "Không thể kết nối OpenRouter. Vui lòng thử lại sau.");
    }

    private static String maskKey(String key) {
        if (key == null || key.length() < 8) return "<invalid>";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
