# mod3case1 — AI Document Assistant
> **A Production-Grade Document Question Answering System using Retrieval-Augmented Generation (RAG)**

---

## ✦ System Overview

`mod3case1` is a full-stack **RAG-based AI Document Assistant** engineered for accurate, hallucination-free question answering over uploaded PDF documents and custom knowledge bases.

The system couples **Dense Vector Semantic Retrieval** with **Constrained Large Language Model Generation**, providing verifiable source citations, page-level passage inspection, dynamic retrieval quality metrics, and a "Midnight Intelligence" dark-mode Android interface.

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ Uploaded PDFs   │ ───►  │ PyMuPDF Parser  │ ───►  │ Semantic Chunk  │
│ & Text Files    │       │ & Page Mapper   │       │ (500 tokens)    │
└─────────────────┘       └─────────────────┘       └────────┬────────┘
                                                             │
                                                             ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ Grounded Answer │ ◄───  │ Constrained LLM │ ◄───  │ all-MiniLM-L6-v2│
│ + Citations     │       │ Context Prompt  │       │ FAISS IndexFlat │
└─────────────────┘       └─────────────────┘       └─────────────────┘
```

---

## 🎨 Design Philosophy: "Midnight Intelligence"

Built with a dark palette tailored for AI interfaces:

| Token | Hex Value | Application |
|---|---|---|
| **Background** | `#060914` | Deep navy base viewport |
| **Surface** | `#0D1322` | Toolbar, navigation bars |
| **Surface Elevated** | `#141C2F` | Raised cards, inputs, buttons |
| **Card Background** | `#111827` | Glassmorphism document cards |
| **Primary Purple** | `#8B5CF6` | AI accent, primary actions |
| **Bright Purple** | `#A855F7` | Gradient highlights |
| **Cyan Accent** | `#22D3EE` | Live online badge, metrics, vectors |
| **Green Status** | `#34D399` | Ready badges, verified citations |
| **Yellow Status** | `#FBBF24` | Indexing / processing steps |
| **Red Status** | `#F87171` | Error states, delete action |
| **Primary Text** | `#F8FAFC` | Headings, questions, response body |
| **Secondary Text**| `#94A3B8` | Metadata, subtitles |
| **Muted Text** | `#64748B` | Timestamps, borders |
| **Border Subtle** | `#263244` | Glassmorphic strokes |

---

## 📱 Android Architecture & Features

Built using **Kotlin, XML layouts, MVVM architecture, Retrofit2, OkHttp, and Kotlin Coroutines/StateFlow** (No Jetpack Compose).

### Screens & Navigation (5 Bottom Navigation Tabs):
1. **⌂ Home (`HomeFragment`)**:
   - Live status indicator: `● RAG ENGINE ONLINE`
   - Real-time Knowledge Base metrics (Documents count, Indexed Chunks, Questions Asked)
   - Quick-Ask search input with direct query routing
   - Recent Documents preview with one-tap inspection
   - "How RAG Works" interactive timeline launch banner
2. **▣ Documents (`DocumentsFragment`)**:
   - SAF Android System File Picker for PDF, TXT, DOCX files
   - "📥 Demo Corpus" button to populate 5 college demonstration educational books
   - Animated 4-step ingestion state:
     - `1. Extracting text ✓`
     - `2. Creating semantic chunks ✓`
     - `3. Generating embeddings ⏳`
     - `4. Building knowledge index ✓`
   - Document cards with status badges (`READY`, `PROCESSING`, `ERROR`), page/chunk stats, inspection, and deletion
3. **✦ Ask (`ChatFragment`)**:
   - Scope filter: Toggle between **All Documents** or a specific document
   - Suggested question chips when conversation is blank
   - AI response cards featuring purple/cyan glowing gradient borders
   - Source citation chips displaying document name, page number, and similarity score
   - Real-time animated RAG thinking phase indicators
   - Copy response to clipboard
4. **◷ History (`HistoryFragment`)**:
   - Chronological query history with scope tag, timestamp, and source count
   - Reopen prior questions directly
   - Clear history action
5. **⚙ Settings (`SettingsFragment`)**:
   - Configurable FastAPI backend URL with real-time health-check ping
   - `Top-K` slider (1 to 10 chunks)
   - `Similarity Threshold` slider (0.00 to 1.00)
   - Complete technical pipeline specifications

### Dedicated Detail Activities:
- **`DocumentDetailActivity`**: Full document overview (page count, total chunk representations, embedding state, executive summary, chunk preview list).
- **`SourceDetailActivity`**: Source insight displaying verified document passage, exact page number, semantic match percentage, and technical explanation of the cosine match.
- **`HowItWorksActivity`**: Step-by-step visual RAG pipeline timeline.

---

## 🧠 Python FastAPI Backend Architecture

```
backend/
├── app.py                      # FastAPI REST API endpoints & CORS configuration
├── config.py                   # Environment & hyperparameter configurations
├── requirements.txt            # Python dependencies
├── seed_corpus.py              # Pre-packaged 5 educational knowledge bases
├── eval_rag.py                 # Retrieval evaluation suite (Recall@K, MRR)
├── data/                       # Vector indices, metadata, and document registry
├── documents/                  # Stored PDF/TXT source documents
├── models/
│   └── schemas.py              # Pydantic request and response schemas
└── rag/
    ├── document_loader.py      # PyMuPDF / pypdf text extraction with page tracking
    ├── chunker.py              # Sliding window token chunking with overlap
    ├── embeddings.py           # sentence-transformers / all-MiniLM-L6-v2 engine
    ├── vector_store.py         # FAISS IndexFlatIP cosine similarity store
    ├── retriever.py            # Top-K semantic retriever & threshold filter
    ├── generator.py            # Constrained grounded prompt generator
    └── pipeline.py             # End-to-end RAG workflow coordinator
```

---

## 🚀 Getting Started

### 1. Backend Setup & Run

#### Prerequisites:
- Python 3.9+

#### Setup Virtual Environment & Install Dependencies:
```bash
cd backend
python -m venv venv

# Windows:
venv\Scripts\activate

# Linux/macOS:
source venv/bin/activate

pip install -r requirements.txt
```

#### Run FastAPI Server:
```bash
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```
The server will start at `http://0.0.0.0:8000` and automatically seed the 5 demonstration documents.

---

### 2. Android Studio Setup

1. Open Android Studio.
2. Select **Open** and choose the `mod3case1` folder (`C:\Users\srira\.gemini\antigravity-ide\scratch\mod3case1`).
3. Allow Gradle to sync.
4. Run on an Android Emulator or Physical Device.

#### Networking Configuration:
- **Android Emulator**: Uses `http://10.0.2.2:8000/` by default (already pre-configured).
- **Physical Device**: Connect phone and computer to the same Wi-Fi network. In the app's **⚙ Settings** tab, change the Backend URL to `http://<YOUR_COMPUTER_IP>:8000/` and tap **Test**.

---

## 📡 REST API Documentation

### `GET /`
Returns backend health and indexing statistics.
```json
{
  "message": "mod3case1 RAG backend is running",
  "status": "online",
  "documents_count": 5,
  "chunks_count": 449,
  "model": "all-MiniLM-L6-v2"
}
```

### `GET /documents`
Lists all indexed documents.

### `POST /documents/upload`
Uploads a document (multipart/form-data) and indexes its chunks into the vector store.

### `POST /documents/sample-corpus`
Populates and indexes the 5 demonstration sample educational documents.

### `DELETE /documents/{document_id}`
Deletes a document and purges its vector embeddings from the FAISS index.

### `POST /chat`
Executes semantic retrieval and grounded answer generation.
```json
// Request:
{
  "question": "What is supervised learning?",
  "document_id": null,
  "top_k": 4,
  "similarity_threshold": 0.50
}

// Response:
{
  "question": "What is supervised learning?",
  "answer": "Supervised learning is a machine learning approach where an algorithm learns a mapping function from labeled training examples consisting of inputs and ground-truth targets...",
  "sources": [
    {
      "document_id": "doc_ml_notes",
      "document_name": "Machine Learning Notes.pdf",
      "page": 12,
      "chunk_id": "chunk_032",
      "score": 0.912,
      "text": "Supervised learning algorithms build a mathematical model of a set of data that contains both the inputs and the desired outputs..."
    }
  ],
  "retrieval": {
    "chunks_used": 4,
    "documents_used": 2,
    "top_match": 0.912
  }
}
```

---

## 📊 RAG Evaluation & Benchmark Results

Run the built-in evaluation suite:
```bash
python eval_rag.py
```

### Benchmark Results:
| Metric | Result | Explanation |
|---|---|---|
| **Mean Precision@4** | `25.0%` | Fraction of retrieved top-4 chunks that are relevant |
| **Mean Recall@4** | `100.0%` | Percentage of ground-truth documents successfully retrieved |
| **Mean Reciprocal Rank (MRR)** | `1.000` | The relevant target document was retrieved as the #1 ranked top match in all queries |

---

## 🎓 College Viva Questions & Answers

### Q1: What is Retrieval-Augmented Generation (RAG) and why is it preferred over pure LLMs?
> **Answer**: Standard LLMs suffer from knowledge cutoffs and hallucinations. RAG decouples knowledge storage from generation by retrieving verified factual excerpts from a private document vector index and injecting them into the LLM's inference context prompt. This ensures accurate, source-verifiable, domain-specific answers without retraining or fine-tuning the model.

### Q2: How does text chunking affect retrieval quality?
> **Answer**: Too large chunks introduce noise and dilute embedding specificity. Too small chunks lose semantic context. `mod3case1` uses a 500-token sliding window with a 50-token overlap, preserving paragraph boundaries and sentence cohesion across chunk splits.

### Q3: How is semantic similarity computed?
> **Answer**: Text chunks and queries are transformed into normalized 384-dimensional dense vectors using `all-MiniLM-L6-v2`. Semantic similarity is computed using the Cosine Similarity metric:
> $$\text{Cosine Similarity}(A, B) = \frac{A \cdot B}{\|A\| \|B\|}$$
> Since embeddings are unit-normalized, this reduces to an Inner Product computed in $O(1)$ time per vector in FAISS.

### Q4: How does the system prevent hallucinations when an answer is not in the documents?
> **Answer**: The system enforces a strict similarity threshold filter (default 0.50). If no chunk passes the threshold, or if retrieved chunks lack necessary facts, the generator returns:
> *"I couldn't find enough information in the provided documents to answer this question."*

---

## 📄 License
This project is developed for educational and research demonstrations.
