import pandas as pd
import torch
import torch.nn.functional as F
from torch_geometric.nn import SAGEConv
from torch_geometric.data import Data
import json
import numpy as np
import os
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

print("🚀 Initializing Graph Neural Network Training Pipeline...")

# --- 1. Load and Prepare Data ---
df = pd.read_csv(os.path.join(BASE_DIR, "interactions.csv"))

# Create unique index mappings for Users and Flights
unique_users = df['passenger_id'].unique()
unique_flights = df['flight_id'].unique()

user_mapping = {str(user_id): i for i, user_id in enumerate(unique_users)}
flight_mapping = {int(flight_id): i for i, flight_id in enumerate(unique_flights)}

num_users = len(user_mapping)
num_flights = len(flight_mapping)
total_nodes = num_users + num_flights

# Save mappings for the FastAPI server to use later
with open(os.path.join(BASE_DIR, "user_mapping.json"), "w") as f:
    json.dump(user_mapping, f)
with open(os.path.join(BASE_DIR, "flight_mapping.json"), "w") as f:
    json.dump(flight_mapping, f)

# Build the Edge Index (Source -> Target)
# In a bipartite graph, user nodes are 0 to (U-1), flight nodes are U to (U+F-1)
src_nodes = [user_mapping[user] for user in df['passenger_id']]
dst_nodes = [flight_mapping[flight] + num_users for flight in df['flight_id']]

# Edges are undirected for GraphSAGE context aggregation
edge_index = torch.tensor([src_nodes + dst_nodes, dst_nodes + src_nodes], dtype=torch.long)

# Create Dummy Node Features (Identity Matrix essentially, typical for Collaborative Filtering)
# We start with random embeddings that the GNN will refine
embedding_dim = 64
x = torch.nn.Embedding(total_nodes, embedding_dim).weight

data = Data(x=x, edge_index=edge_index)

# --- 2. Define the GraphSAGE Model ---
class FlightRecommendationGNN(torch.nn.Module):
    def __init__(self, in_channels, hidden_channels, out_channels):
        super().__init__()
        # GraphSAGE layers aggregate information from neighbors (Flights users interacted with)
        self.conv1 = SAGEConv(in_channels, hidden_channels)
        self.conv2 = SAGEConv(hidden_channels, out_channels)

    def forward(self, x, edge_index):
        x = self.conv1(x, edge_index)
        x = F.relu(x)
        x = F.dropout(x, p=0.2, training=self.training)
        x = self.conv2(x, edge_index)
        return x # Returns the final node embeddings

# --- 3. Training Setup ---
device = torch.device('mps' if torch.backends.mps.is_available() else 'cpu') # Native Mac support or CPU fallback
model = FlightRecommendationGNN(embedding_dim, 32, 16).to(device)
data = data.to(device)
optimizer = torch.optim.Adam(model.parameters(), lr=0.01)

# Simple Link Prediction Loss (Dot product of connected nodes should be high)
def compute_loss(embeddings, edge_index):
    # Get embeddings for source and destination nodes of the edges
    src_emb = embeddings[edge_index[0]]
    dst_emb = embeddings[edge_index[1]]
    
    # Positive scores (actual interactions)
    pos_scores = (src_emb * dst_emb).sum(dim=1)
    
    # Negative sampling (random non-interactions)
    random_dst = torch.randint(num_users, total_nodes, (edge_index.size(1),), device=device)
    neg_emb = embeddings[random_dst]
    neg_scores = (src_emb * neg_emb).sum(dim=1)
    
    # BPR Loss (Bayesian Personalized Ranking) - Standard for Recommender Systems
    loss = -torch.log(torch.sigmoid(pos_scores - neg_scores) + 1e-15).mean()
    return loss

# --- 4. Training Loop ---
print(f"🧠 Training model on {device}...")
model.train()

loss_history = [] # NEW: Array to store loss values

for epoch in range(1, 101):
    optimizer.zero_grad()
    out_embeddings = model(data.x, data.edge_index)
    loss = compute_loss(out_embeddings, data.edge_index)
    loss.backward()
    optimizer.step()
    
    loss_history.append(loss.item()) # NEW: Save the loss every epoch
    
    if epoch % 20 == 0:
        print(f"Epoch {epoch:03d}, Loss: {loss.item():.4f}")

# --- 5. Save the Output ---
print("✅ Training Complete! Saving learned embeddings...")
model.eval()
with torch.no_grad():
    final_embeddings = model(data.x, data.edge_index).cpu()

# NEW: Save the loss history
with open(os.path.join(BASE_DIR, "loss_history.json"), "w") as f:
    json.dump(loss_history, f)

torch.save(final_embeddings, os.path.join(BASE_DIR, "node_embeddings.pt"))
print("💾 Saved: user_mapping.json, flight_mapping.json, loss_history.json, node_embeddings.pt")