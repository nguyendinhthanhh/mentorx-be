import os
from functools import lru_cache
from typing import Any

import cv2
import numpy as np
from fastapi import FastAPI, File, HTTPException, UploadFile
from insightface.app import FaceAnalysis
from pydantic import BaseModel


MODEL_NAME = os.getenv("EKYC_FACE_MODEL", "buffalo_l")
DET_SIZE = int(os.getenv("EKYC_FACE_DET_SIZE", "640"))
MATCH_THRESHOLD = float(os.getenv("EKYC_FACE_MATCH_THRESHOLD", "0.42"))
REVIEW_THRESHOLD = float(os.getenv("EKYC_FACE_REVIEW_THRESHOLD", "0.34"))
PROVIDERS = [
    p.strip()
    for p in os.getenv("EKYC_FACE_PROVIDERS", "CPUExecutionProvider").split(",")
    if p.strip()
]


class FaceVerifyResponse(BaseModel):
    is_match: bool
    needs_review: bool
    similarity: float
    threshold: float
    review_threshold: float
    face_count_1: int
    face_count_2: int
    message: str


app = FastAPI(title="MentorX eKYC Face Service", version="1.0.0")


@lru_cache(maxsize=1)
def get_face_app() -> FaceAnalysis:
    face_app = FaceAnalysis(name=MODEL_NAME, providers=PROVIDERS)
    face_app.prepare(ctx_id=0, det_size=(DET_SIZE, DET_SIZE))
    return face_app


@app.get("/health")
def health() -> dict[str, Any]:
    face_app = get_face_app()
    return {
        "status": "UP",
        "model": MODEL_NAME,
        "providers": PROVIDERS,
        "modules": sorted(face_app.models.keys()),
    }


@app.post("/face/verify", response_model=FaceVerifyResponse)
async def verify_faces(
    image1: UploadFile = File(...),
    image2: UploadFile = File(...),
) -> FaceVerifyResponse:
    img1 = _decode_image(await image1.read(), "image1")
    img2 = _decode_image(await image2.read(), "image2")

    faces1 = get_face_app().get(img1)
    faces2 = get_face_app().get(img2)

    if not faces1 and not faces2:
        return _review(0.0, 0, 0, "Không phát hiện khuôn mặt trong cả hai ảnh.")
    if not faces1:
        return _review(0.0, 0, len(faces2), "Không phát hiện khuôn mặt trong ảnh CCCD.")
    if not faces2:
        return _review(0.0, len(faces1), 0, "Không phát hiện khuôn mặt trong ảnh selfie/video.")

    face1 = _largest_face(faces1)
    face2 = _largest_face(faces2)
    sim = _cosine_similarity(face1.normed_embedding, face2.normed_embedding)

    if sim >= MATCH_THRESHOLD:
        return FaceVerifyResponse(
            is_match=True,
            needs_review=False,
            similarity=sim * 100.0,
            threshold=MATCH_THRESHOLD * 100.0,
            review_threshold=REVIEW_THRESHOLD * 100.0,
            face_count_1=len(faces1),
            face_count_2=len(faces2),
            message="ArcFace xác nhận hai khuôn mặt khớp.",
        )

    if sim >= REVIEW_THRESHOLD:
        return _review(
            sim,
            len(faces1),
            len(faces2),
            "Điểm khuôn mặt nằm vùng cần admin kiểm tra thủ công.",
        )

    return FaceVerifyResponse(
        is_match=False,
        needs_review=False,
        similarity=sim * 100.0,
        threshold=MATCH_THRESHOLD * 100.0,
        review_threshold=REVIEW_THRESHOLD * 100.0,
        face_count_1=len(faces1),
        face_count_2=len(faces2),
        message="Khuôn mặt không khớp đủ ngưỡng ArcFace.",
    )


def _decode_image(data: bytes, label: str) -> np.ndarray:
    if not data:
        raise HTTPException(status_code=400, detail=f"{label} rỗng.")
    arr = np.frombuffer(data, dtype=np.uint8)
    image = cv2.imdecode(arr, cv2.IMREAD_COLOR)
    if image is None:
        raise HTTPException(status_code=400, detail=f"Không đọc được {label}.")
    return image


def _largest_face(faces: list[Any]) -> Any:
    return max(faces, key=lambda f: (f.bbox[2] - f.bbox[0]) * (f.bbox[3] - f.bbox[1]))


def _cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    denom = np.linalg.norm(a) * np.linalg.norm(b)
    if denom <= 1e-9:
        return 0.0
    return float(np.dot(a, b) / denom)


def _review(sim: float, count1: int, count2: int, message: str) -> FaceVerifyResponse:
    return FaceVerifyResponse(
        is_match=False,
        needs_review=True,
        similarity=sim * 100.0,
        threshold=MATCH_THRESHOLD * 100.0,
        review_threshold=REVIEW_THRESHOLD * 100.0,
        face_count_1=count1,
        face_count_2=count2,
        message=message,
    )
