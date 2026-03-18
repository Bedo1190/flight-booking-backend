from fastapi import FastAPI, HTTPException
import torch
import json
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

app = FastAPI(title="SphereTech AI Recommendation Engine")

print("Loading PyTorch Graph Embeddings...")
try:
    with open(os.path.join(BASE_DIR, "user_mapping.json"), "r") as f:
        user_mapping = json.load(f)
    with open(os.path.join(BASE_DIR, "flight_mapping.json"), "r") as f:
        flight_mapping = json.load(f)
        
    # Reverse the dictionary so we can map tensor indices back to real Flight IDs
    idx_to_flight = {v: int(k) for k, v in flight_mapping.items()}
    
    # Load the trained model weights
    embeddings = torch.load(os.path.join(BASE_DIR, "node_embeddings.pt"), weights_only=True)
    num_users = len(user_mapping)
    print("✅ Model loaded successfully!")
    
except FileNotFoundError:
    print("⚠️ Warning: Model artifacts missing. Did you run model.py?")
    embeddings = None

@app.get("/recommend/{passenger_id}")
def get_recommendations(passenger_id: str):
    if embeddings is None:
        raise HTTPException(status_code=500, detail="AI Model is offline.")

    # --- COLD START HANDLING ---
    # If this is a brand new user with no history, we return an empty list.
    # The Java backend will see this and automatically fallback to returning 
    # flights going to the highest `popularityScore` airports!
    if passenger_id not in user_mapping:
        return []

    # 1. Get the user's mathematical vector
    user_idx = user_mapping[passenger_id]
    user_vector = embeddings[user_idx]
    
    # 2. Extract all flight vectors (they sit right after the users in the matrix)
    flight_vectors = embeddings[num_users : num_users + len(flight_mapping)]
    
    # 3. Calculate similarity scores (Dot Product)
    scores = torch.matmul(flight_vectors, user_vector)
    
    # 4. Get the indices of the top 5 highest scores
    top_k = min(5, len(flight_mapping))
    _, top_indices = torch.topk(scores, top_k)
    
    # 5. Map the tensor indices back to your H2 Database Flight IDs
    recommended_flight_ids = [idx_to_flight[idx.item()] for idx in top_indices]
    
    return recommended_flight_ids