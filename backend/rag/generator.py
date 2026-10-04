import os
from typing import List, Dict, Any
from ..config import LLM_API_KEY, LLM_MODEL, LLM_PROVIDER, GEMINI_API_KEY

NO_ANSWER_RESPONSE = (
    "I couldn't find enough information in the provided documents to answer this question.\n\n"
    "Try asking about a topic that appears in your uploaded documents."
)

class RagGenerator:
    """Synthesizes factual, grounded answers from retrieved context passages."""

    def __init__(self):
        self.api_key = LLM_API_KEY
        self.model = LLM_MODEL
        self.provider = LLM_PROVIDER
        self.gemini_key = GEMINI_API_KEY

    def generate_answer(
        self,
        question: str,
        retrieved_sources: List[Dict[str, Any]]
    ) -> str:
        """Constructs prompt and generates grounded response via LLM or grounded fallback."""
        if not retrieved_sources:
            return NO_ANSWER_RESPONSE

        # Combine context passages with source tags
        context_blocks = []
        for s in retrieved_sources:
            tag = f"[{s.get('document_name')} - Page {s.get('page', 1)}]"
            context_blocks.append(f"{tag}\n{s.get('text')}")

        full_context = "\n\n".join(context_blocks)

        # 1. Attempt OpenAI LLM if key present
        if self.api_key and self.provider == "openai":
            try:
                import requests
                headers = {
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json"
                }
                messages = [
                    {
                        "role": "system",
                        "content": (
                            "You are mod3case1, a premium academic AI Document Assistant using RAG. "
                            "Answer the user's question ONLY using the provided document context below. "
                            "If the context does not contain enough information, clearly say: "
                            "'I couldn't find enough information in the provided documents to answer this question.' "
                            "Do NOT invent facts, do NOT extrapolate beyond the context, and cite the document names/pages when appropriate."
                        )
                    },
                    {
                        "role": "user",
                        "content": f"Document Context:\n{full_context}\n\nQuestion: {question}"
                    }
                ]
                payload = {
                    "model": self.model,
                    "messages": messages,
                    "temperature": 0.2,
                    "max_tokens": 600
                }
                res = requests.post("https://api.openai.com/v1/chat/completions", headers=headers, json=payload, timeout=20)
                if res.status_code == 200:
                    return res.json()["choices"][0]["message"]["content"].strip()
            except Exception as e:
                print(f"[RagGenerator] OpenAI generation error: {e}")

        # 2. Intelligent Grounded Synthesizer Fallback
        return self._grounded_extractive_synthesis(question, retrieved_sources)

    def _grounded_extractive_synthesis(self, question: str, sources: List[Dict[str, Any]]) -> str:
        """Deterministic, grounded extractive summarization from retrieved passages."""
        passages = [s.get("text", "") for s in sources]
        
        # Take key sentences from top retrieved sources
        selected_sentences = []
        for p in passages:
            sentences = [s.strip() for s in p.split(".") if len(s.strip()) > 15]
            for sent in sentences[:2]:
                if sent not in selected_sentences:
                    selected_sentences.append(sent)

        if not selected_sentences:
            return NO_ANSWER_RESPONSE

        lead_doc = sources[0].get("document_name", "the knowledge base")
        lead_page = sources[0].get("page", 1)

        body = ". ".join(selected_sentences[:4]) + "."
        return f"Based on {lead_doc} (Page {lead_page}):\n\n{body}"
