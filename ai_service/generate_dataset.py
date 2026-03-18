import pandas as pd
import numpy as np
import uuid
import random
import os
from datetime import datetime, timedelta

NUM_USERS = 500
NUM_FLIGHTS = 50  
NUM_INTERACTIONS = 5000

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 1. Generate Mock Users (Passenger UUIDs)
users = [str(uuid.uuid4()) for _ in range(NUM_USERS)]

# 2. Generate Mock Flights (IDs 1 through 50)
flights = list(range(1, NUM_FLIGHTS + 1))

# 3. Define Interactions
interactions = []
interaction_types = ["SEARCH", "PURCHASE"]
# Realistic distribution: 85% of traffic is searching, 15% is actual purchasing
weights = [0.85, 0.15] 

base_time = datetime.now()

# 4. Generate the Graph Edges
for _ in range(NUM_INTERACTIONS):
    user = random.choice(users)
    
    # Introduce a statistical bias: popular flights (lower IDs) get interacted with more often.
    # We use a beta distribution to simulate "trending" flights instead of pure random chance.
    flight = int(np.random.beta(a=2, b=5) * NUM_FLIGHTS) + 1 
    if flight > NUM_FLIGHTS:
        flight = NUM_FLIGHTS
        
    action = random.choices(interaction_types, weights=weights)[0]
    
    # Stagger the timestamps over the last 30 days
    time_offset = timedelta(days=random.randint(0, 30), minutes=random.randint(0, 1440))
    timestamp = base_time - time_offset
    
    interactions.append({
        "passenger_id": user,
        "flight_id": flight,
        "interaction_type": action,
        "timestamp": timestamp.isoformat()
    })

# Generate Flight Metadata
regions = ["Europe", "Asia", "North America", "South America", "Middle East"]
flight_metadata = []

for f in flights:
    flight_metadata.append({
        "flight_id": f,
        "latitude": round(random.uniform(20.0, 60.0), 4),  # Northern hemisphere focus
        "longitude": round(random.uniform(-10.0, 50.0), 4),
        "region": random.choice(regions),
        "popularityScore": random.randint(0, 100)
    })

meta_df = pd.DataFrame(flight_metadata)
meta_path = os.path.join(BASE_DIR, "flights_metadata.csv")
meta_df.to_csv(meta_path, index=False)
print(f"Generated hybrid flight metadata: {meta_path}")

# 5. Export to CSV
df = pd.DataFrame(interactions)

#Sort chronologically so it looks like a real database dump
df = df.sort_values(by="timestamp", ascending=True)

# Save
output_path = os.path.join(BASE_DIR, "interactions.csv")
df.to_csv(output_path, index=False)
print(f"Successfully generated {output_path} with {len(df)} edges")