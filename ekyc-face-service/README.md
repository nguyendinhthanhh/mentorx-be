# MentorX eKYC Face Service

Self-hosted face verification service using InsightFace/ArcFace embeddings.

## Setup

```powershell
cd mentorx-be\ekyc-face-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app:app --host 127.0.0.1 --port 8091
```

Or run with Docker:

```powershell
cd mentorx-be\ekyc-face-service
docker build -t mentorx-ekyc-face .
docker run --rm -p 8091:8091 -v "$env:USERPROFILE\.insightface:/root/.insightface" mentorx-ekyc-face
```

The first request downloads InsightFace model files. For production, pre-warm `/health`
at deploy time and mount the model cache as persistent storage.

## Backend Config

```properties
KYC_FACE_PROVIDER=arcface
KYC_FACE_SERVICE_URL=http://127.0.0.1:8091
KYC_FACE_MATCH_MIN_CORR=0.42
KYC_FACE_REVIEW_MIN_CORR=0.34
```

If licensing is important for commercial production, review InsightFace model licensing
before launch. The Java backend can still route high-risk cases to VNPT/FPT later.
