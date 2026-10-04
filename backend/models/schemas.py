from typing import List, Optional
from pydantic import BaseModel, Field

class ChunkSchema(BaseModel):
    chunk_id: str
    document_id: str
    document_name: str
    page: Optional[int] = 1
    text: str

class DocumentSchema(BaseModel):
    document_id: str
    filename: str
    file_type: str = "PDF"
    pages_count: int = 1
    chunks_count: int = 0
    status: str = "READY"
    indexed_at: str = "Just now"
    summary: Optional[str] = None
    chunks: List[ChunkSchema] = []

class UploadResponse(BaseModel):
    document_id: str
    filename: str
    status: str
    pages_count: int = 1
    chunks_count: int = 0
    summary: Optional[str] = None

class SourceSchema(BaseModel):
    document_id: str
    document_name: str
    page: Optional[int] = 1
    chunk_id: str
    score: float
    text: str

class RetrievalInfo(BaseModel):
    chunks_used: int
    documents_used: int
    top_match: float

class ChatRequest(BaseModel):
    question: str
    document_id: Optional[str] = None
    top_k: int = Field(default=4, ge=1, le=15)
    similarity_threshold: float = Field(default=0.50, ge=0.0, le=1.0)

class ChatResponse(BaseModel):
    question: str
    answer: str
    sources: List[SourceSchema] = []
    retrieval: Optional[RetrievalInfo] = None

class StatusResponse(BaseModel):
    message: str
    status: str = "online"
    documents_count: int = 0
    chunks_count: int = 0
    model: str = "all-MiniLM-L6-v2"
