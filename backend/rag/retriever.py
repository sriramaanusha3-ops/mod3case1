from typing import List, Dict, Any, Optional
from .embeddings import EmbeddingEngine
from .vector_store import VectorStore

class RagRetriever:
    """Retrieves the most semantically relevant text chunks for a user query."""

    def __init__(self, embedding_engine: EmbeddingEngine, vector_store: VectorStore):
        self.embedding_engine = embedding_engine
        self.vector_store = vector_store

    def retrieve(
        self,
        query: str,
        document_id: Optional[str] = None,
        top_k: int = 4,
        similarity_threshold: float = 0.50
    ) -> List[Dict[str, Any]]:
        """
        Generates query embedding and performs vector similarity search.
        Returns formatted sources list:
        [
            {
                "document_id": "doc_001",
                "document_name": "Machine Learning Notes.pdf",
                "page": 12,
                "chunk_id": "chunk_032",
                "score": 0.912,
                "text": "..."
            }
        ]
        """
        query_vec = self.embedding_engine.encode_query(query)
        matches = self.vector_store.search(
            query_vector=query_vec,
            top_k=top_k,
            document_id=document_id,
            similarity_threshold=similarity_threshold
        )

        sources = []
        for chunk_meta, score in matches:
            sources.append({
                "document_id": chunk_meta.get("document_id", "doc_unknown"),
                "document_name": chunk_meta.get("document_name", "Document"),
                "page": chunk_meta.get("page", 1),
                "chunk_id": chunk_meta.get("chunk_id", "chunk_001"),
                "score": round(score, 3),
                "text": chunk_meta.get("text", "")
            })

        return sources
