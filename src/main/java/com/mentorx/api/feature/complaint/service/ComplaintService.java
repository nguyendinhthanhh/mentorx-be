package com.mentorx.api.feature.complaint.service;

import com.mentorx.api.feature.complaint.dto.request.ComplaintCreateRequest;
import com.mentorx.api.feature.complaint.dto.request.ComplaintRespondRequest;
import com.mentorx.api.feature.complaint.dto.response.ComplaintResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ComplaintService {
    ComplaintResponse createComplaint(ComplaintCreateRequest request);
    ComplaintResponse getComplaintById(UUID complaintId);
    Page<ComplaintResponse> getComplaintsByUser(UUID userId, Pageable pageable);
    ComplaintResponse respondToComplaint(UUID complaintId, ComplaintRespondRequest request);
    ComplaintResponse withdrawComplaint(UUID complaintId);
}
