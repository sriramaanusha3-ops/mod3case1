import shutil
from pathlib import Path
from typing import List, Optional
from fastapi import FastAPI, UploadFile, File, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware

from .config import DOCUMENTS_DIR, DATA_DIR, EMBEDDING_MODEL_NAME, DEFAULT_TOP_K, DEFAULT_SIMILARITY_THRESHOLD
from .models.schemas import (
    DocumentSchema,
    UploadResponse,
    ChatRequest,
    ChatResponse,
    StatusResponse
)
from .rag.pipeline import RagPipeline
from .seed_corpus import seed_documents, SAMPLE_DOCS

app = FastAPI(
    title="mod3case1 RAG Engine",
    description="Document Question Answering Backend using Retrieval-Augmented Generation",
    version="1.0.0"
)

# Enable CORS for Android clients and web dashboards
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

pipeline = RagPipeline()

@app.on_event("startup")
async def startup_event():
    seed_documents()
    # Auto-index if registry is empty
    if not pipeline.get_all_documents():
        for filename in SAMPLE_DOCS.keys():
            file_path = DOCUMENTS_DIR / filename
            if file_path.exists():
                pipeline.ingest_document(file_path, filename)
        print(f"[AppStartup] Indexed sample educational documents.")

@app.get("/", response_model=StatusResponse)
async def root():
    docs = pipeline.get_all_documents()
    total_chunks = sum(d.get("chunks_count", 0) for d in docs)
    return StatusResponse(
        message="mod3case1 RAG backend is running",
        status="online",
        documents_count=len(docs),
        chunks_count=total_chunks,
        model=EMBEDDING_MODEL_NAME
    )

@app.get("/documents", response_model=List[DocumentSchema])
async def list_documents():
    return pipeline.get_all_documents()

@app.get("/documents/{document_id}", response_model=DocumentSchema)
async def get_document(document_id: str):
    doc = pipeline.get_document(document_id)
    if not doc:
        raise HTTPException(status_code=404, detail="Document not found")
    return doc

@app.post("/documents/upload", response_model=UploadResponse)
async def upload_document(file: UploadFile = File(...)):
    if not file.filename:
        raise HTTPException(status_code=400, detail="Missing filename")

    file_path = DOCUMENTS_DIR / file.filename
    try:
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to save uploaded file: {str(e)}")

    try:
        doc_meta = pipeline.ingest_document(file_path, file.filename)
        return UploadResponse(
            document_id=doc_meta["document_id"],
            filename=doc_meta["filename"],
            status=doc_meta["status"],
            pages_count=doc_meta["pages_count"],
            chunks_count=doc_meta["chunks_count"],
            summary=doc_meta.get("summary")
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to process and index document: {str(e)}")

@app.post("/documents/sample-corpus", response_model=List[DocumentSchema])
async def load_sample_corpus():
    seed_documents()
    for filename in SAMPLE_DOCS.keys():
        file_path = DOCUMENTS_DIR / filename
        if file_path.exists():
            # Check if not already indexed
            existing = [d for d in pipeline.get_all_documents() if d.get("filename") == filename]
            if not existing:
                pipeline.ingest_document(file_path, filename)
    return pipeline.get_all_documents()

@app.delete("/documents/{document_id}")
async def delete_document(document_id: str):
    success = pipeline.delete_document(document_id)
    if not success:
        raise HTTPException(status_code=404, detail="Document not found")
    return {"message": f"Document {document_id} deleted successfully", "success": True}

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    if not request.question.strip():
        raise HTTPException(status_code=400, detail="Question cannot be empty")

    res = pipeline.answer_question(
        question=request.question,
        document_id=request.document_id,
        top_k=request.top_k,
        similarity_threshold=request.similarity_threshold
    )
    return res

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
