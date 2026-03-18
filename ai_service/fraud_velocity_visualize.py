import numpy as np
import matplotlib.pyplot as plt
import joblib
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

print("Loading model")

try:
    model = joblib.load(os.path.join(BASE_DIR, "fraud_model.pkl"))
except FileNotFoundError:
    print("Model not found.")
    exit()

# X-axis: 1 to 48 hours since last transaction
# Y-axis: $50 to $3000 spent on the last transaction
hours = np.linspace(1, 48, 100) 
amounts = np.linspace(50, 3000, 100)
xx, yy = np.meshgrid(hours, amounts)

# Simulating a normal user 
price_fixed = np.full(xx.shape, 800)
time_fixed = np.full(xx.shape, 14)
days_fixed = np.full(xx.shape, 14)

grid_features = np.c_[
    price_fixed.ravel(), 
    time_fixed.ravel(), 
    days_fixed.ravel(),
    xx.ravel(),    # Hours since last txn
    yy.ravel()     # Last txn amount
]

# Predict probabilities
probs = model.predict_proba(grid_features)[:, 1]
probs = probs.reshape(xx.shape)

#Plotting the Heatmap 
plt.figure(figsize=(12, 8))

# Draw the heatmap
contour = plt.contourf(xx, yy, probs, 50, cmap='RdYlGn_r', alpha=0.9)
cbar = plt.colorbar(contour)
cbar.set_label('Fraud Probability', rotation=270, labelpad=20, fontsize=12)

# Draw the strict 75% decision boundary
plt.contour(xx, yy, probs, levels=[0.75], colors='black', linewidths=3, linestyles='dashed')

plt.title('Velocity Decision Boundary (Fixed: $800 Flight at 2 PM)', fontsize=16)
plt.xlabel('Hours Since Last Transaction', fontsize=14)
plt.ylabel('Amount of Last Transaction ($)', fontsize=14)
plt.text(25, 2800, 'Black Line = Java 403 Block', color='black', weight='bold', fontsize=12, bbox=dict(facecolor='white', alpha=0.7))

output_path = os.path.join(BASE_DIR, "plot_6_velocity_boundary.png")
plt.savefig(output_path)
print(f"Saved velocity visualization to {output_path}")