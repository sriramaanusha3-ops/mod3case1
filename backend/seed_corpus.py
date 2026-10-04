import os
from pathlib import Path

DOCS_DIR = Path(__file__).resolve().parent / "documents"
DOCS_DIR.mkdir(parents=True, exist_ok=True)

SAMPLE_DOCS = {
    "Artificial Intelligence.txt": """--- Page 1 ---
Artificial Intelligence (AI) is a multidisciplinary domain of computer science dedicated to creating computational systems capable of performing cognitive tasks historically requiring human intelligence. Key historical milestones include Alan Turing's 1950 proposal of the Turing Test, the 1956 Dartmouth Summer Research Project on Artificial Intelligence where the term 'Artificial Intelligence' was coined by John McCarthy, and the emergence of knowledge-based expert systems during the 1970s and 1980s.
--- Page 2 ---
Modern AI systems are broadly classified into Narrow AI (Weak AI), which is engineered and trained for a specific discrete task like facial recognition, internet search, or chess; and General AI (Strong AI / AGI), which refers to theoretical systems possessing human-level cognitive capabilities across diverse problem spaces.
--- Page 3 ---
Core methodologies within AI include heuristic search algorithms (A* search, minimax, alpha-beta pruning), automated reasoning, constraint satisfaction problems (CSPs), probabilistic graphical models (Bayesian networks), and machine learning pipelines that extrapolate patterns from high-dimensional observational data.
""",
    "Machine Learning Notes.txt": """--- Page 1 ---
Machine Learning is a subset of artificial intelligence where statistical models learn patterns and relationships directly from data without being explicitly programmed with deterministic if-else rules.
--- Page 2 ---
The three foundational paradigms of Machine Learning are:
1. Supervised Learning: The algorithm is trained on a labeled dataset consisting of input features and ground-truth targets. The goal is to learn a mapping function f(x) -> y. Tasks include classification (predicting discrete class labels) and regression (predicting continuous numerical quantities). Algorithms include Linear Regression, Logistic Regression, Support Vector Machines (SVM), Decision Trees, Random Forests, and Gradient Boosting.
--- Page 3 ---
2. Unsupervised Learning: The algorithm discovers latent structures, clusters, or probability distributions in unlabeled data. Tasks include clustering (K-Means, DBSCAN, Hierarchical Clustering) and dimensionality reduction (Principal Component Analysis - PCA, t-SNE, UMAP).
3. Reinforcement Learning: An autonomous agent learns optimal behavioral policies through sequential interactions with an environment, maximizing cumulative scalar reward signals via algorithms such as Q-Learning and Policy Gradients.
--- Page 4 ---
Model optimization relies on gradient descent techniques to minimize empirical loss functions (Mean Squared Error, Cross-Entropy Loss). Overfitting is mitigated through L1/L2 regularization, cross-validation, and dropout techniques.
""",
    "Neural Networks.txt": """--- Page 1 ---
Artificial Neural Networks (ANNs) are biologically inspired computational models structured in hierarchical layers of interconnected processing units called neurons. A basic feedforward network comprises an input layer, one or more hidden layers, and an output layer.
--- Page 2 ---
Each artificial neuron computes a weighted sum of its inputs, adds a learnable bias term, and passes the result through a non-linear activation function (such as Sigmoid, Tanh, ReLU, or GELU). Non-linear activations enable neural networks to approximate arbitrary continuous mathematical functions, formalizing the Universal Approximation Theorem.
--- Page 3 ---
Network parameters (weights and biases) are iteratively optimized using the Backpropagation algorithm, which calculates the partial derivative (gradient) of the loss function with respect to each weight using the mathematical chain rule of calculus.
--- Page 4 ---
Specialized Deep Learning Architectures:
• Convolutional Neural Networks (CNNs): Utilize learnable convolution filters and pooling layers to extract translation-invariant spatial features from grid data like images.
• Recurrent Neural Networks (RNNs) & LSTMs: Process sequential data with internal hidden states to capture temporal dependencies.
• Transformer Architectures: Replace recurrent recurrence with multi-head self-attention mechanisms, enabling massive parallelization and state-of-the-art representation learning for large language models.
""",
    "Cloud Computing.txt": """--- Page 1 ---
Cloud Computing is the delivery of on-demand computing services—including compute power, storage repositories, databases, networking, software, and analytics—over the internet with pay-as-you-go pricing models.
--- Page 2 ---
The three principal cloud service models are:
1. Infrastructure as a Service (IaaS): Provides virtualized computing infrastructure such as virtual machines, elastic block storage, and software-defined networks (e.g., AWS EC2, Google Compute Engine).
2. Platform as a Service (PaaS): Delivers hardware and software development frameworks allowing developers to build, test, and deploy applications without managing underlying infrastructure (e.g., Google App Engine, AWS Elastic Beanstalk).
3. Software as a Service (SaaS): Delivers fully managed, ready-to-use software applications accessed via web browsers (e.g., Google Workspace, Salesforce).
--- Page 3 ---
Cloud Deployment Models include:
• Public Cloud: Infrastructure owned and operated by third-party cloud providers and shared across multiple tenants.
• Private Cloud: Infrastructure provisioned for exclusive use by a single organization.
• Hybrid Cloud: Combines public and private infrastructures with data and application portability.
• Multi-cloud: Leveraging multiple cloud service providers to minimize vendor lock-in and optimize latency.
""",
    "Natural Language Processing.txt": """--- Page 1 ---
Natural Language Processing (NLP) is an interdisciplinary subfield of linguistics, computer science, and artificial intelligence concerned with interactions between computers and human natural language.
--- Page 2 ---
Foundational text processing steps include tokenization (splitting text into subwords/words), normalization (lowercasing, punctuation stripping), stopword filtering, and stem/lemmatization. Text representations evolved from sparse vector space models (Bag of Words, Term Frequency-Inverse Document Frequency / TF-IDF) to dense distributed continuous semantic vector embeddings (Word2Vec, GloVe, FastText).
--- Page 3 ---
Retrieval-Augmented Generation (RAG) is an architectural framework that enhances Large Language Models by dynamically retrieving relevant verified passages from external private knowledge bases. RAG breaks down text into semantic chunks, generates vector embeddings with encoder models, performs cosine similarity searches against vector databases, and injects the retrieved context directly into the LLM inference prompt to eliminate hallucinations and supply source-grounded citations.
"""
}

def seed_documents():
    for name, content in SAMPLE_DOCS.items():
        file_path = DOCS_DIR / name
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content.strip())
        print(f"[SeedCorpus] Wrote {name}")

if __name__ == "__main__":
    seed_documents()
