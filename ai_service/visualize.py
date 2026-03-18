import torch
import json
import os
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.manifold import TSNE
from sklearn.cluster import KMeans

print("📊 Generating AI Performance Visualizations...")

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# --- LOAD DATA ---
try:
    with open(os.path.join(BASE_DIR, "user_mapping.json"), "r") as f:
        num_users = len(json.load(f))
    with open(os.path.join(BASE_DIR, "flight_mapping.json"), "r") as f:
        num_flights = len(json.load(f))
    with open(os.path.join(BASE_DIR, "loss_history.json"), "r") as f:
        loss_history = json.load(f)
        
    embeddings = torch.load(os.path.join(BASE_DIR, "node_embeddings.pt"), weights_only=True).numpy()
    df_interactions = pd.read_csv(os.path.join(BASE_DIR, "interactions.csv"))
except FileNotFoundError as e:
    print(f"❌ Error: Missing file {e}. Please run model.py first.")
    exit()

# Apply a clean, modern aesthetic for the charts
sns.set_theme(style="whitegrid", palette="muted")

# ==========================================
# GRAPH 1: BPR Training Loss Curve
# ==========================================
plt.figure(figsize=(10, 6))
plt.plot(range(1, len(loss_history) + 1), loss_history, color='#2ecc71', linewidth=2.5)
plt.title("Model Convergence: BPR Loss over Epochs", fontsize=16, fontweight='bold', pad=15)
plt.xlabel("Training Epoch", fontsize=12)
plt.ylabel("BPR Loss", fontsize=12)
plt.fill_between(range(1, len(loss_history) + 1), loss_history, color='#2ecc71', alpha=0.1)
plt.tight_layout()
plt.savefig(os.path.join(BASE_DIR, "plot_1_loss_curve.png"), dpi=300)
print("✅ Saved Graph 1: plot_1_loss_curve.png")

# ==========================================
# GRAPH 2: Interaction Distribution (The Long Tail)
# ==========================================
plt.figure(figsize=(12, 6))
flight_counts = df_interactions['flight_id'].value_counts().sort_index()
sns.barplot(x=flight_counts.index, y=flight_counts.values, color="#3498db")
plt.title("Data Skew: Frequency of Flight Interactions", fontsize=16, fontweight='bold', pad=15)
plt.xlabel("Flight ID", fontsize=12)
plt.ylabel("Number of Interactions (Searches/Purchases)", fontsize=12)
plt.xticks(rotation=90, fontsize=8) 
plt.tight_layout()
plt.savefig(os.path.join(BASE_DIR, "plot_2_data_distribution.png"), dpi=300)
print("✅ Saved Graph 2: plot_2_data_distribution.png")

# ==========================================
# GRAPH 3: GNN Latent Space (t-SNE)
# ==========================================
print("⏳ Computing t-SNE projection (this may take a few seconds)...")
tsne = TSNE(n_components=2, perplexity=30, random_state=42, max_iter=1000)
embeddings_2d = tsne.fit_transform(embeddings)

user_coords = embeddings_2d[:num_users]
flight_coords = embeddings_2d[num_users:]

plt.figure(figsize=(12, 8))
# Users
plt.scatter(user_coords[:, 0], user_coords[:, 1], 
            c='#95a5a6', label='Passengers', alpha=0.5, edgecolors='w', s=40)
# Flights
plt.scatter(flight_coords[:, 0], flight_coords[:, 1], 
            c='#e74c3c', label='Flights', marker='^', s=150, edgecolors='black')

plt.title("AI Mind Map: t-SNE Projection of Passengers and Flights", fontsize=16, fontweight='bold', pad=15)
plt.xlabel("Latent Dimension 1", fontsize=12)
plt.ylabel("Latent Dimension 2", fontsize=12)
plt.legend(loc="best", fontsize=12, frameon=True, shadow=True)
plt.tight_layout()
plt.savefig(os.path.join(BASE_DIR, "plot_3_latent_space.png"), dpi=300)
print("✅ Saved Graph 3: plot_3_latent_space.png")

# ==========================================
# GRAPH 4: K-Means Clustering (Traveler Personas)
# ==========================================
print("⏳ Running K-Means Clustering on Passenger Embeddings...")
num_clusters = 5  # Grouping users into 5 distinct personas
kmeans = KMeans(n_clusters=num_clusters, random_state=42, n_init=10)

# We train K-Means on the TRUE 64-dimensional embeddings for mathematical accuracy
user_original_embeddings = embeddings[:num_users]
user_labels = kmeans.fit_predict(user_original_embeddings)

plt.figure(figsize=(12, 8))
palette = sns.color_palette("husl", num_clusters) # Distinct colors for each persona

# Plot each cluster using the 2D coordinates we calculated earlier
for cluster_id in range(num_clusters):
    idx = (user_labels == cluster_id)
    plt.scatter(user_coords[idx, 0], user_coords[idx, 1],
                color=palette[cluster_id], label=f'Traveler Persona {cluster_id + 1}', 
                alpha=0.8, edgecolors='w', s=60)

plt.title("Unsupervised Learning: User Clustering via K-Means", fontsize=16, fontweight='bold', pad=15)
plt.xlabel("Latent Dimension 1", fontsize=12)
plt.ylabel("Latent Dimension 2", fontsize=12)
plt.legend(title="Detected Clusters", loc="best", fontsize=12, title_fontsize=13, shadow=True)
plt.tight_layout()
plt.savefig(os.path.join(BASE_DIR, "plot_4_user_clusters.png"), dpi=300)
print("✅ Saved Graph 4: plot_4_user_clusters.png")

print("🎉 All visual artifacts successfully generated!")