import pandas as pd
import torch
import torch.nn.functional as F
from torch_geometric.nn import SAGEConv
from torch_geometric.data import Data
from sklearn.preprocessing import StandardScaler, OneHotEncoder
import json
import numpy as np
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 1. Load Data
df = pd.read_csv(os.path.join(BASE_DIR, "interactions.csv"))
meta_df = pd.read_csv(os.path.join(BASE_DIR, "flights_metadata.csv"))

unique_users = df['passenger_id'].unique()

unique_flights = meta_df['flight_id'].unique()

user_mapping = {str(user_id): i for i, user_id in enumerate(unique_users)}
flight_mapping = {int(flight_id): i for i, flight_id in enumerate(unique_flights)}

num_users = len(user_mapping)
num_flights = len(flight_mapping)
total_nodes = num_users + num_flights

with open(os.path.join(BASE_DIR, "user_mapping.json"), "w") as f:
    json.dump(user_mapping, f)
with open(os.path.join(BASE_DIR, "flight_mapping.json"), "w") as f:
    json.dump(flight_mapping, f)

# Process Physical Features
meta_df['mapped_id'] = meta_df['flight_id'].map(flight_mapping)
meta_df = meta_df.dropna(subset=['mapped_id']).sort_values('mapped_id')

# Normalize continuous features (Latitude, Longitude, Popularity)
scaler = StandardScaler()
continuous_feats = scaler.fit_transform(meta_df[['latitude', 'longitude', 'popularityScore']])

# Encode categorical features (Region)
encoder = OneHotEncoder(sparse_output=False)
region_feats = encoder.fit_transform(meta_df[['region']])

# Combine them into a single tensor
physical_features = torch.tensor(np.hstack([continuous_feats, region_feats]), dtype=torch.float32)
num_physical_dims = physical_features.shape[1]
print(f"Extracted {num_physical_dims} physical features per flight.")

# 3. Build the Hybrid Feature Module
embedding_dim = 64

class HybridFeatureMatrix(torch.nn.Module):
    def __init__(self, num_users, num_flights, emb_dim, phys_feats):
        super().__init__()
        # Users get a full 64-dim learned vector
        self.user_emb = torch.nn.Embedding(num_users, emb_dim)
        
        # Flights get (64 - physical_dims) learned vector. 
        # When concatenated with physical data, it equals 64
        self.flight_learned_emb = torch.nn.Embedding(num_flights, emb_dim - phys_feats.shape[1])
        
        self.register_buffer('phys_feats', phys_feats)

    def forward(self):
        user_x = self.user_emb.weight
        flight_x = torch.cat([self.flight_learned_emb.weight, self.phys_feats], dim=1)
        return torch.cat([user_x, flight_x], dim=0)

# 4. Define the GraphSAGE Model
class FlightRecommendationGNN(torch.nn.Module):
    def __init__(self, in_channels, hidden_channels, out_channels):
        super().__init__()
        self.conv1 = SAGEConv(in_channels, hidden_channels)
        self.conv2 = SAGEConv(hidden_channels, out_channels)

    def forward(self, x, edge_index):
        x = self.conv1(x, edge_index)
        x = F.relu(x)
        x = F.dropout(x, p=0.2, training=self.training)
        x = self.conv2(x, edge_index)
        return x 

# Build Edges
src_nodes = [user_mapping[user] for user in df['passenger_id']]
dst_nodes = [flight_mapping[flight] + num_users for flight in df['flight_id']]
edge_index = torch.tensor([src_nodes + dst_nodes, dst_nodes + src_nodes], dtype=torch.long)

device = torch.device('mps' if torch.backends.mps.is_available() else 'cpu')

# Initialize components
feature_matrix = HybridFeatureMatrix(num_users, num_flights, embedding_dim, physical_features).to(device)
model = FlightRecommendationGNN(embedding_dim, 32, 16).to(device)
edge_index = edge_index.to(device)

# Pass BOTH the feature generator and the GNN to the optimizer
optimizer = torch.optim.Adam(list(model.parameters()) + list(feature_matrix.parameters()), lr=0.01)

def compute_loss(embeddings, edge_index):
    src_emb = embeddings[edge_index[0]]
    dst_emb = embeddings[edge_index[1]]
    pos_scores = (src_emb * dst_emb).sum(dim=1)
    
    random_dst = torch.randint(num_users, total_nodes, (edge_index.size(1),), device=device)
    neg_emb = embeddings[random_dst]
    neg_scores = (src_emb * neg_emb).sum(dim=1)
    
    loss = -torch.log(torch.sigmoid(pos_scores - neg_scores) + 1e-15).mean()
    return loss

# 5. Training
print(f"Training on {device}")
model.train()
feature_matrix.train()

loss_history = []

for epoch in range(1, 101):
    optimizer.zero_grad()
    
    # 1. Generate the hybrid features dynamically
    current_x = feature_matrix()
    
    # 2. Pass them through the GNN
    out_embeddings = model(current_x, edge_index)
    
    loss = compute_loss(out_embeddings, edge_index)
    loss.backward()
    optimizer.step()
    
    loss_history.append(loss.item())
    if epoch % 20 == 0:
        print(f"Epoch {epoch:03d}, Loss: {loss.item():.4f}")

print("Training Complete")
model.eval()
feature_matrix.eval()
with torch.no_grad():
    final_embeddings = model(feature_matrix(), edge_index).cpu()

torch.save(final_embeddings, os.path.join(BASE_DIR, "node_embeddings.pt"))