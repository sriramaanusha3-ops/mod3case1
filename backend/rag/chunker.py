import re
from typing import List, Dict, Any

class TextChunker:
    """Partitions document page texts into semantic overlapping chunks."""

    def __init__(self, chunk_size: int = 500, chunk_overlap: int = 50):
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap

    def chunk_document(
        self,
        document_id: str,
        document_name: str,
        pages: List[Dict[str, Any]]
    ) -> List[Dict[str, Any]]:
        chunks = []
        global_chunk_idx = 1

        for page_data in pages:
            page_num = page_data.get("page", 1)
            raw_text = page_data.get("text", "")
            
            # Clean excessive whitespace while preserving paragraphs
            cleaned_text = re.sub(r'\s+', ' ', raw_text).strip()
            if not cleaned_text:
                continue

            words = cleaned_text.split(" ")
            if len(words) <= self.chunk_size:
                chunks.append({
                    "chunk_id": f"chunk_{global_chunk_idx:03d}",
                    "document_id": document_id,
                    "document_name": document_name,
                    "page": page_num,
                    "text": cleaned_text
                })
                global_chunk_idx += 1
            else:
                # Sliding window chunking
                start_idx = 0
                step = max(1, self.chunk_size - self.chunk_overlap)
                while start_idx < len(words):
                    end_idx = min(len(words), start_idx + self.chunk_size)
                    chunk_text = " ".join(words[start_idx:end_idx])
                    
                    chunks.append({
                        "chunk_id": f"chunk_{global_chunk_idx:03d}",
                        "document_id": document_id,
                        "document_name": document_name,
                        "page": page_num,
                        "text": chunk_text
                    })
                    global_chunk_idx += 1

                    if end_idx >= len(words):
                        break
                    start_idx += step

        return chunks
