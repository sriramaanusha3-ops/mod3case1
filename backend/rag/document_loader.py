import os
from pathlib import Path
from typing import List, Dict, Any

class DocumentLoader:
    """Extracts text and page metadata from PDF, TXT, DOCX, and MD files."""

    @staticmethod
    def load(file_path: Path) -> List[Dict[str, Any]]:
        """
        Loads document content and returns a list of pages with page numbers:
        [{"page": 1, "text": "..."}, ...]
        """
        suffix = file_path.suffix.lower()
        if suffix == ".pdf":
            return DocumentLoader._load_pdf(file_path)
        elif suffix in [".txt", ".md", ".log"]:
            return DocumentLoader._load_txt(file_path)
        else:
            return DocumentLoader._load_generic(file_path)

    @staticmethod
    def _load_pdf(file_path: Path) -> List[Dict[str, Any]]:
        pages = []
        # Attempt 1: PyMuPDF (fitz)
        try:
            import fitz
            doc = fitz.open(file_path)
            for page_num in range(len(doc)):
                page = doc[page_num]
                text = page.get_text()
                if text.strip():
                    pages.append({
                        "page": page_num + 1,
                        "text": text.strip()
                    })
            doc.close()
            if pages:
                return pages
        except ImportError:
            pass
        except Exception as e:
            print(f"[DocumentLoader] PyMuPDF extraction note: {e}")

        # Attempt 2: pypdf fallback
        try:
            from pypdf import PdfReader
            reader = PdfReader(str(file_path))
            for page_idx, page in enumerate(reader.pages):
                text = page.extract_text() or ""
                if text.strip():
                    pages.append({
                        "page": page_idx + 1,
                        "text": text.strip()
                    })
            if pages:
                return pages
        except Exception as e:
            print(f"[DocumentLoader] pypdf extraction note: {e}")

        # If no extractable text found
        if not pages:
            pages.append({
                "page": 1,
                "text": "This PDF document does not contain directly extractable text layers. OCR is required for scanned image PDFs."
            })
        return pages

    @staticmethod
    def _load_txt(file_path: Path) -> List[Dict[str, Any]]:
        try:
            with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                content = f.read()
        except Exception:
            with open(file_path, "r", encoding="latin-1", errors="ignore") as f:
                content = f.read()

        # Split into logical virtual pages every ~2500 characters
        chunk_page_size = 2500
        if len(content) <= chunk_page_size:
            return [{"page": 1, "text": content.strip()}]

        pages = []
        current_idx = 0
        page_num = 1
        while current_idx < len(content):
            page_text = content[current_idx:current_idx + chunk_page_size]
            pages.append({"page": page_num, "text": page_text.strip()})
            current_idx += chunk_page_size
            page_num += 1
        return pages

    @staticmethod
    def _load_generic(file_path: Path) -> List[Dict[str, Any]]:
        return DocumentLoader._load_txt(file_path)
