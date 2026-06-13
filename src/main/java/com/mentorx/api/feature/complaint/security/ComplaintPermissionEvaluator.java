package com.mentorx.api.feature.complaint.security;

import com.mentorx.api.common.security.CustomUserDetails;
import com.mentorx.api.feature.complaint.entity.Complaint;
import com.mentorx.api.feature.complaint.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("complaintEvaluator")
@RequiredArgsConstructor
public class ComplaintPermissionEvaluator {

    private final ComplaintRepository complaintRepository;

    public boolean isParty(UUID complaintId, CustomUserDetails principal) {
        Complaint c = complaintRepository.findById(complaintId).orElse(null);
        return c != null
            && (c.getComplainantId().equals(principal.getUserId())
                || c.getRespondentId().equals(principal.getUserId()));
    }

    public boolean isComplainant(UUID complaintId, CustomUserDetails principal) {
        Complaint c = complaintRepository.findById(complaintId).orElse(null);
        return c != null && c.getComplainantId().equals(principal.getUserId());
    }

    public boolean isRespondent(UUID complaintId, CustomUserDetails principal) {
        Complaint c = complaintRepository.findById(complaintId).orElse(null);
        return c != null && c.getRespondentId().equals(principal.getUserId());
    }
}
