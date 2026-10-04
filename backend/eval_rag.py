"""
RAG Benchmark and Evaluation Script
Evaluates Information Retrieval Quality:
- Precision@K
- Recall@K
- Mean Reciprocal Rank (MRR)
"""

import sys
from pathlib import Path
from typing import List, Dict, Any

# Ensure backend package resolution
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from backend.rag.pipeline import RagPipeline
from backend.seed_corpus import seed_documents, SAMPLE_DOCS
from backend.config import DOCUMENTS_DIR

# Ground-truth evaluation dataset: (Query -> Relevant Document Names)
EVAL_BENCHMARK = [
    {
        "query": "What is supervised learning and how does it map inputs to outputs?",
        "relevant_docs": ["Machine Learning Notes.txt"]
    },
    {
        "query": "Who coined the term Artificial Intelligence at the Dartmouth workshop?",
        "relevant_docs": ["Artificial Intelligence.txt"]
    },
    {
        "query": "How do Convolutional Neural Networks and Transformers work?",
        "relevant_docs": ["Neural Networks.txt"]
    },
    {
        "query": "Explain Infrastructure as a Service (IaaS) and Platform as a Service (PaaS).",
        "relevant_docs": ["Cloud Computing.txt"]
    },
    {
        "query": "How does Retrieval-Augmented Generation eliminate LLM hallucinations?",
        "relevant_docs": ["Natural Language Processing.txt"]
    },
    {
        "query": "What is reinforcement learning and Q-learning policy optimization?",
        "relevant_docs": ["Machine Learning Notes.txt"]
    },
    {
        "query": "Explain the difference between Narrow AI and General AI (AGI).",
        "relevant_docs": ["Artificial Intelligence.txt"]
    }
]

def run_evaluation(top_k: int = 4):
    print("=" * 60)
    print(f"  mod3case1 RAG PIPELINE EVALUATION (Top-K = {top_k})")
    print("=" * 60)

    pipeline = RagPipeline()
    seed_documents()

    # Index any unindexed documents
    for filename in SAMPLE_DOCS.keys():
        file_path = DOCUMENTS_DIR / filename
        existing = [d for d in pipeline.get_all_documents() if d.get("filename") == filename]
        if not existing and file_path.exists():
            pipeline.ingest_document(file_path, filename)

    precisions = []
    recalls = []
    reciprocal_ranks = []

    print(f"\nEvaluating {len(EVAL_BENCHMARK)} test queries against knowledge base...\n")

    for idx, item in enumerate(EVAL_BENCHMARK, 1):
        query = item["query"]
        expected_docs = set(item["relevant_docs"])

        retrieved = pipeline.retriever.retrieve(query=query, top_k=top_k, similarity_threshold=0.0)
        retrieved_doc_names = [r["document_name"] for r in retrieved]

        # Precision@K: Fraction of retrieved documents that are relevant
        relevant_retrieved = [doc for doc in retrieved_doc_names if doc in expected_docs]
        precision_at_k = len(relevant_retrieved) / len(retrieved_doc_names) if retrieved_doc_names else 0.0
        precisions.append(precision_at_k)

        # Recall@K: Fraction of relevant documents that were retrieved
        recall_at_k = len(set(relevant_retrieved)) / len(expected_docs) if expected_docs else 0.0
        recalls.append(recall_at_k)

        # Reciprocal Rank (RR): 1 / rank of first relevant document
        rr = 0.0
        for rank, doc_name in enumerate(retrieved_doc_names, 1):
            if doc_name in expected_docs:
                rr = 1.0 / rank
                break
        reciprocal_ranks.append(rr)

        top_retrieved = retrieved_doc_names[0] if retrieved_doc_names else "None"
        print(f"[{idx}/{len(EVAL_BENCHMARK)}] Query: \"{query[:45]}...\"")
        print(f"   -> Expected: {list(expected_docs)}")
        print(f"   -> Top Hit : {top_retrieved} (P@{top_k}: {precision_at_k:.2f}, R@{top_k}: {recall_at_k:.2f}, RR: {rr:.2f})")

    avg_precision = sum(precisions) / len(precisions) if precisions else 0.0
    avg_recall = sum(recalls) / len(recalls) if recalls else 0.0
    mrr = sum(reciprocal_ranks) / len(reciprocal_ranks) if reciprocal_ranks else 0.0

    print("\n" + "=" * 60)
    print("  EVALUATION METRICS SUMMARY")
    print("=" * 60)
    print(f"  • Mean Precision@{top_k} : {avg_precision * 100:.1f}%")
    print(f"  • Mean Recall@{top_k}    : {avg_recall * 100:.1f}%")
    print(f"  • Mean Reciprocal Rank (MRR) : {mrr:.3f}")
    print("=" * 60 + "\n")

if __name__ == "__main__":
    run_evaluation(top_k=4)
