import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
import joblib
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

print("Generating Velocity Fraud Dataset...")

NUM_TRANSACTIONS = 10000

# 1. Base Features
ticket_prices = np.random.exponential(scale=300, size=NUM_TRANSACTIONS) + 50 
ticket_prices[-500:] = np.random.uniform(3000, 50000, size=500) # Extreme outliers
time_of_day = np.random.randint(0, 24, size=NUM_TRANSACTIONS)
days_until_flight = np.random.randint(0, 365, size=NUM_TRANSACTIONS)

# 2. Velocity Features (Derived from Masked Card Numbers)
# Most users buy flights rarely
# Hackers testing cards make transactions within hours of each other.
hours_since_last_txn = np.where(
    np.random.rand(NUM_TRANSACTIONS) < 0.15, 
    np.random.randint(1, 48, size=NUM_TRANSACTIONS),     # 15% are recent (1 to 48 hrs)
    np.random.randint(720, 8760, size=NUM_TRANSACTIONS)  # 85% are old
)
last_txn_amount = np.random.exponential(scale=250, size=NUM_TRANSACTIONS)

df = pd.DataFrame({
    'ticket_price': ticket_prices,
    'time_of_day': time_of_day,
    'days_until_flight': days_until_flight,
    'hours_since_last_txn': hours_since_last_txn,
    'last_txn_amount': last_txn_amount
})

# fraud logic 
def is_fraud(row):
    risk_score = 0
    
    # Standard profile checks
    if row['days_until_flight'] <= 2: risk_score += 3
    if row['time_of_day'] in [1, 2, 3, 4, 5]: risk_score += 2
    
    # Price scaling
    if row['ticket_price'] > 600:
        risk_score += min(5, (row['ticket_price'] - 600) / 300) 
        
    # Velocity check: penalty for rapid high-value transactions
    if row['hours_since_last_txn'] <= 24 and row['last_txn_amount'] > 400:
        risk_score += 6  
        if row['ticket_price'] >= 400:
            risk_score += 3 
    elif row['hours_since_last_txn'] <= 12:
        risk_score += 2
    
    # Bumping the multiplier to 0.15 so a score of 5 reaches the 0.75 threshold
    probability = min(0.98, risk_score * 0.15)
    return np.random.rand() < probability

df['is_fraud'] = df.apply(is_fraud, axis=1).astype(int)
print(f"Generated {len(df)} transactions. Found {df['is_fraud'].sum()} fraudulent records.")

# Training
features = ['ticket_price', 'time_of_day', 'days_until_flight', 'hours_since_last_txn', 'last_txn_amount']
X = df[features]
y = df['is_fraud']

model = RandomForestClassifier(n_estimators=100, max_depth=12, random_state=42)
model.fit(X, y)

model_path = os.path.join(BASE_DIR, "fraud_model.pkl")
joblib.dump(model, model_path)
print(f"Fraud Model saved to {model_path}")