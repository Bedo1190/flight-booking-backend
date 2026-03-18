from fastapi import FastAPI, HTTPException
import torch
import json
from pydantic import BaseModel
import joblib
import pandas as pd
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

app = FastAPI(title="SphereTech AI Recommendation Engine")

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
    print("Recommendation Model loaded")
    
except FileNotFoundError:
    print("Model artifacts missing")
    embeddings = None

try:
    fraud_model = joblib.load(os.path.join(BASE_DIR, "fraud_model.pkl"))
    print("Fraud Model loaded")
except FileNotFoundError:
    print("Fraud model missing")
    fraud_model = None

# Define the JSON structure
class FraudRequest(BaseModel):
    ticket_price: float
    time_of_day: int
    days_until_flight: int
    hours_since_last_txn: int
    last_txn_amount: float

@app.post("/fraud-check")
def check_fraud(request: FraudRequest):
    if fraud_model is None:
        raise HTTPException(status_code=500, detail="Fraud model is offline.")

    # Create a DataFrame with the exact feature names used during training
    features_df = pd.DataFrame([{
        'ticket_price': request.ticket_price,
        'time_of_day': request.time_of_day,
        'days_until_flight': request.days_until_flight,
        'hours_since_last_txn': request.hours_since_last_txn,
        'last_txn_amount': request.last_txn_amount
    }])
    
    # Passing the DataFrame removes the UserWarning
    fraud_probability = fraud_model.predict_proba(features_df)[0][1]
    is_fraud = bool(fraud_probability > 0.75)
    
    return {
        "fraud_probability": round(fraud_probability, 4),
        "is_fraud": is_fraud
    }

@app.get("/recommend/{passenger_id}")
def get_recommendations(passenger_id: str):
    if embeddings is None:
        raise HTTPException(status_code=500, detail="AI Model is offline.")

    # If this is a brand new user with no history, we return an empty list.
    # The Java backend will see this and automatically fallback to returning 
    # flights going to the highest `popularityScore` airports!
    if passenger_id not in user_mapping:
        return []

    # 1. Get the user's mathematical vector
    user_idx = user_mapping[passenger_id]
    user_vector = embeddings[user_idx]
    
    # 2. Extract all flight vectors 
    flight_vectors = embeddings[num_users : num_users + len(flight_mapping)]
    
    # 3. Calculate similarity scores (Dot Product)
    scores = torch.matmul(flight_vectors, user_vector)
    
    # 4. Get the indices of the top 5 highest scores
    top_k = min(5, len(flight_mapping))
    _, top_indices = torch.topk(scores, top_k)
    
    # 5. Map the tensor indices back
    recommended_flight_ids = [idx_to_flight[idx.item()] for idx in top_indices]
    
    return recommended_flight_ids