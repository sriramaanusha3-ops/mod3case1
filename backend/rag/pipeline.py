import json
import uuid
from pathlib import Path
from typing import List, Dict, Any, Optional
from datetime import datetime

from .document_loader import DocumentLoader
from .chunker import TextChunker
from .embeddings import EmbeddingEngine
from .vector_store import VectorStore
from .retriever import RagRetriever
from .generator import RagGenerator
from ..config import DATA_DIR, DOCUMENTS_DIR, EMBEDDING_MODEL_NAME, CHUNK_SIZE, CHUNK_OVERLAP

class RagPipeline:
    """Master RAG Pipeline coordinating ingestion, indexing, retrieval, and generation."""

    def __init__(self):
        self.doc_registry_file = DATA_DIR / "documents_registry.json"
        self.documents_registry: Dict[str, Dict[str, Any]] = {}
        
        self.loader = DocumentLoader()
        self.chunker = TextChunker(chunk_size=CHUNK_SIZE, chunk_overlap=CHUNK_OVERLAP)
        self.embedding_engine = EmbeddingEngine(model_name=EMBEDDING_MODEL_NAME)
        self.vector_store = VectorStore(data_dir=DATA_DIR)
        self.retriever = RagRetriever(self.embedding_engine, self.vector_store)
        self.generator = RagGenerator()

        self._load_registry()

    def _load_registry(self):
        if self.doc_registry_file.exists():
            try:
                with open(self.doc_registry_file, "r", encoding="utf-8") as f:
                    self.documents_registry = json.load(f)
            except Exception as e:
                print(f"[RagPipeline] Registry load error: {e}")
                self.documents_registry = {}

    def _save_registry(self):
        try:
            with open(self.doc_registry_file, "w", encoding="utf-8") as f:
                json.dump(self.documents_registry, f, indent=2)
        except Exception as e:
            print(f"[RagPipeline] Registry save error: {e}")

    def ingest_document(self, file_path: Path, filename: str) -> Dict[str, Any]:
        """Loads, chunks, embeds, and indexes a document."""
        doc_id = f"doc_{uuid.uuid4().hex[:8]}"
        pages = self.loader.load(file_path)
        chunks = self.chunker.chunk_document(
            document_id=doc_id,
            document_name=filename,
            pages=pages
        )

        # Generate embeddings
        chunk_texts = [c["text"] for c in chunks]
        embeddings = self.embedding_engine.encode(chunk_texts)

        # Add to vector store
        self.vector_store.add_chunks(chunks, embeddings)

        # Generate summary preview
        summary = " ".join([c["text"] for c in chunks[:2]])[:240] + "..." if chunks else "No extractable text."

        doc_meta = {
            "document_id": doc_id,
            "filename": filename,
            "file_type": file_path.suffix.replace(".", "").upper() or "PDF",
            "pages_count": len(pages),
            "chunks_count": len(chunks),
            "status": "READY",
            "indexed_at": datetime.now().strftime("%I:%M %p"),
            "summary": summary,
            "chunks": chunks[:10]  # Store preview
        }

        self.documents_registry[doc_id] = doc_meta
        self._save_registry()
        return doc_meta

    def get_all_documents(self) -> List[Dict[str, Any]]:
        return list(self.documents_registry.values())

    def get_document(self, doc_id: str) -> Optional[Dict[str, Any]]:
        return self.documents_registry.get(doc_id)

    def delete_document(self, doc_id: str) -> bool:
        if doc_id in self.documents_registry:
            del self.documents_registry[doc_id]
            self.vector_store.delete_document(doc_id)
            self._save_registry()
            return True
        return False

    def answer_question(
        self,
        question: str,
        document_id: Optional[str] = None,
        top_k: int = 4,
        similarity_threshold: float = 0.50
    ) -> Dict[str, Any]:
        """Executes full RAG retrieval and generation cycle."""
        sources = self.retriever.retrieve(
            query=question,
            document_id=document_id,
            top_k=top_k,
            similarity_threshold=similarity_threshold
        )

        answer = self.generator.generate_answer(question, sources)

        docs_used = len(set(s["document_id"] for s in sources))
        top_score = sources[0]["score"] if sources else 0.0

        return {
            "question": question,
            "answer": answer,
            "sources": sources,
            "retrieval": {
                "chunks_used": len(sources),
                "documents_used": docs_used,
                "top_match": top_score
            }
        }
