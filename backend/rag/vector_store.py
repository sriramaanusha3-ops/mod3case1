import pickle
from pathlib import Path
from typing import List, Dict, Any, Tuple
import numpy as np

class VectorStore:
    """FAISS-backed semantic vector store with metadata index and persistent storage."""

    def __init__(self, data_dir: Path, dimension: int = 384):
        self.data_dir = data_dir
        self.dimension = dimension
        self.index_file = self.data_dir / "faiss_index.bin"
        self.metadata_file = self.data_dir / "chunks_metadata.pkl"
        
        self.chunks_metadata: List[Dict[str, Any]] = []
        self.vectors_matrix: List[np.ndarray] = []
        self.faiss_index = None

        self._init_faiss()
        self.load()

    def _init_faiss(self):
        try:
            import faiss
            self.faiss_index = faiss.IndexFlatIP(self.dimension)
        except ImportError:
            self.faiss_index = None

    def add_chunks(self, chunks: List[Dict[str, Any]], embeddings: np.ndarray):
        """Adds chunks and their normalized embedding vectors to the index."""
        if len(chunks) == 0:
            return

        for chunk, emb in zip(chunks, embeddings):
            self.chunks_metadata.append(chunk)
            self.vectors_matrix.append(emb)

        if self.faiss_index is not None:
            import faiss
            self.faiss_index.add(embeddings)

        self.save()

    def search(
        self,
        query_vector: np.ndarray,
        top_k: int = 4,
        document_id: str = None,
        similarity_threshold: float = 0.50
    ) -> List[Tuple[Dict[str, Any], float]]:
        """
        Retrieves top_k relevant chunks sorted by cosine similarity score.
        Returns: List of (chunk_metadata, similarity_score)
        """
        if not self.chunks_metadata or len(self.vectors_matrix) == 0:
            return []

        query_vec = np.array(query_vector, dtype=np.float32).reshape(1, -1)
        matrix = np.array(self.vectors_matrix, dtype=np.float32)

        # Compute cosine similarities
        scores = np.dot(matrix, query_vec.T).flatten()

        # Build candidate list
        results = []
        for idx, score in enumerate(scores):
            meta = self.chunks_metadata[idx]
            if document_id and meta.get("document_id") != document_id:
                continue
            
            float_score = float(score)
            if float_score >= similarity_threshold:
                results.append((meta, float_score))

        # Sort descending by score
        results.sort(key=lambda x: x[1], reverse=True)
        return results[:top_k]

    def delete_document(self, document_id: str):
        """Removes all chunks associated with a specific document_id."""
        new_meta = []
        new_vecs = []

        for meta, vec in zip(self.chunks_metadata, self.vectors_matrix):
            if meta.get("document_id") != document_id:
                new_meta.append(meta)
                new_vecs.append(vec)

        self.chunks_metadata = new_meta
        self.vectors_matrix = new_vecs

        self._init_faiss()
        if self.faiss_index is not None and len(new_vecs) > 0:
            import faiss
            matrix = np.array(new_vecs, dtype=np.float32)
            self.faiss_index.add(matrix)

        self.save()

    def save(self):
        """Persists metadata and vectors to disk."""
        try:
            with open(self.metadata_file, "wb") as f:
                pickle.dump({"metadata": self.chunks_metadata, "vectors": self.vectors_matrix}, f)
        except Exception as e:
            print(f"[VectorStore] Save error: {e}")

    def load(self):
        """Loads saved index from disk if present."""
        if self.metadata_file.exists():
            try:
                with open(self.metadata_file, "rb") as f:
                    data = pickle.load(f)
                    self.chunks_metadata = data.get("metadata", [])
                    self.vectors_matrix = data.get("vectors", [])
                
                if self.faiss_index is not None and len(self.vectors_matrix) > 0:
                    import faiss
                    matrix = np.array(self.vectors_matrix, dtype=np.float32)
                    self.faiss_index.add(matrix)
                print(f"[VectorStore] Loaded {len(self.chunks_metadata)} chunks from cache.")
            except Exception as e:
                print(f"[VectorStore] Load error: {e}")
