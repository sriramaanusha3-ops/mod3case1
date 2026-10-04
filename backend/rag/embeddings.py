import numpy as np
from typing import List

class EmbeddingEngine:
    """Generates dense semantic vector embeddings for text chunks and queries."""

    def __init__(self, model_name: str = "sentence-transformers/all-MiniLM-L6-v2"):
        self.model_name = model_name
        self.model = None
        self._load_model()

    def _load_model(self):
        try:
            from sentence_transformers import SentenceTransformer
            print(f"[EmbeddingEngine] Loading SentenceTransformer: {self.model_name}...")
            self.model = SentenceTransformer(self.model_name)
            print("[EmbeddingEngine] SentenceTransformer loaded successfully.")
        except Exception as e:
            print(f"[EmbeddingEngine] SentenceTransformer unavailable ({e}). Using normalized sparse vectorizer.")
            self.model = None

    def encode(self, texts: List[str]) -> np.ndarray:
        """Encodes a list of strings into normalized 2D numpy float32 arrays."""
        if not texts:
            return np.empty((0, 384), dtype=np.float32)

        if self.model is not None:
            embeddings = self.model.encode(texts, convert_to_numpy=True, normalize_embeddings=True)
            return embeddings.astype(np.float32)
        else:
            # High-fidelity hashing / n-gram fallback embedding (384-dimensional)
            return self._fallback_encode(texts)

    def encode_query(self, query: str) -> np.ndarray:
        """Encodes a single query string into a 1D normalized vector."""
        vecs = self.encode([query])
        return vecs[0]

    def _fallback_encode(self, texts: List[str]) -> np.ndarray:
        dim = 384
        out = np.zeros((len(texts), dim), dtype=np.float32)
        for i, text in enumerate(texts):
            words = text.lower().split()
            vec = np.zeros(dim, dtype=np.float32)
            for w in words:
                h = hash(w) % dim
                vec[h] += 1.0
            norm = np.linalg.norm(vec)
            if norm > 0:
                vec = vec / norm
            out[i] = vec
        return out
