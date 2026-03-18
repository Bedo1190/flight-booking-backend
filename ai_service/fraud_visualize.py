import numpy as np
import matplotlib.pyplot as plt
import joblib
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

print("Loading model")

# Load your trained model
try:
    model = joblib.load(os.path.join(BASE_DIR, "fraud_model.pkl"))
except FileNotFoundError:
    print("Model not found. Run fraud_model.py first!")
    exit()

# Create a grid of every possible combination of Price ($50 to $3000) and Days (0 to 100)
prices = np.linspace(50, 3000, 100)
days = np.linspace(0, 100, 100)
xx, yy = np.meshgrid(prices, days)

# fix "Time of Day" to 3 AM (high risk)
time_fixed = np.full(xx.shape, 3)

# We must also fix the two new velocity features to simulate a "normal" baseline user.
hours_fixed = np.full(xx.shape, 4320)       # 6 months = 4320 hours
last_amount_fixed = np.full(xx.shape, 300)  # $300 previous ticket

grid_features = np.c_[
    xx.ravel(), 
    time_fixed.ravel(), 
    yy.ravel(),
    hours_fixed.ravel(),
    last_amount_fixed.ravel()
]

# Predict the fraud probability for every single pixel on the graph
probs = model.predict_proba(grid_features)[:, 1]
probs = probs.reshape(xx.shape)

#Plotting the Heatmap
plt.figure(figsize=(12, 8))

# Draw the heatmap (Green = Safe, Red = Fraud)
contour = plt.contourf(xx, yy, probs, 50, cmap='RdYlGn_r', alpha=0.9)
cbar = plt.colorbar(contour)
cbar.set_label('Fraud Probability', rotation=270, labelpad=20, fontsize=12)

# Draw the strict 75% decision boundary
plt.contour(xx, yy, probs, levels=[0.75], colors='black', linewidths=3, linestyles='dashed')

plt.title('Fraud Decision Boundary (Fixed: 3 AM, Normal Velocity)', fontsize=16)
plt.xlabel('Ticket Price ($)', fontsize=14)
plt.ylabel('Days Until Flight', fontsize=14)
plt.text(1800, 90, 'Black Line = Java 403 Block', color='black', weight='bold', fontsize=12, bbox=dict(facecolor='white', alpha=0.7))

output_path = os.path.join(BASE_DIR, "plot_5_fraud_boundary.png")
plt.savefig(output_path)
print(f"Saved visualization to {output_path}")