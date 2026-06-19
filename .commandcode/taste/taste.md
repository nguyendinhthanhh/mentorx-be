# Architecture
- Keep Moderation and Complaint as completely separate modules with independent workflows, objectives, and handling procedures — no shared entities, no overlapping logic. Confidence: 0.70
- When refactoring the complaint module, only edit files within the complaint package — do not touch or modify the moderation package. Confidence: 0.70
- Remove moderator/admin concerns (ReportController, ReportService, AdminActivityLog) from the complaint module to avoid Spring bean naming collisions and URL mapping conflicts with the moderation module. Confidence: 0.70
