
## 🛠️ The Tech Stack

**Backend:**
* **Java 17 & Spring Boot**
* **Spring Data JPA** & **H2 Database** 
* **RestTemplate / RestClient** for cross-language microservice communication.

**AI Microservice:**
* **Python & FastAPI** (Runs on `localhost:8000`)
* **PyTorch & PyTorch Geometric (GraphSAGE)** for the Hybrid Recommendation Engine.
* **Scikit-Learn (RandomForestClassifier)** for the Aggressive Velocity Fraud Detection model.
* **Pandas & NumPy** for synthetic data generation and feature scaling.
* **Seaborn & Matplotlib** for latent space visualization (t-SNE & K-Means).

## Key Features

### 1. Hybrid Graph Neural Network (Recommendations)
Instead of just asking "what did this user buy?", the system builds a bipartite graph of users and flights. Using Bayesian Personalized Ranking (BPR) loss and GraphSAGE, it pulls neighbor information to cluster similar users. 
* **The Hybrid Twist:** The PyTorch model concatenates learned embeddings with physical coordinates. It understands that a flight to Berlin is similar to a flight to Frankfurt, strictly based on the injected `[latitude, longitude, region_encoded]` tensor.

### 2. Aggressive Fraud Detection (Random Forest)
Hackers testing stolen credit cards usually make rapid, high-value transactions. The AI model checks the "velocity" of purchases. If a user tries to buy a €500 ticket only 1 hour after their last transaction, the Random Forest model slams them with a severe penalty, pushing their risk score over the 0.75 threshold and throwing a `403 Forbidden` from the Java backend.

### 3. "Guest" Search Tracking
To build a dense latent space, the backend logs every single search. If a user isn't logged in, it tracks them as `"guest"`. This ensures the AI always has fresh data on trending global routes, acting as a fallback for new users.

---

## How to Run It

You need to run both servers simultaneously for the system to work.

### Step 1: Start the AI Service (Python)
Navigate to the `ai_service` folder. Make sure you have your virtual environment activated and the required packages installed (`fastapi`, `uvicorn`, `torch`, `pandas`, `scikit-learn`).

```bash
cd ai_service

# 1. Generate the synthetic interactions and physical metadata
python generate_dataset.py

# 2. Train the Fraud Model
python fraud_model.py

# 3. Train the Hybrid GNN (Watch the loss curve drop!)
python model.py

# 4. Start the FastAPI server
uvicorn main:app --reload --port 8000
```

### Step 2: Start the Backend (Spring Boot)
Open the root folder in IntelliJ IDEA or use Maven from the terminal.

```bash
./mvnw spring-boot:run
```
The Spring Boot server will start on `http://localhost:8080`. It automatically connects to the H2 database and drops/creates the schema on startup.

---

## 📡 Core API Endpoints

Here are the main ways to interact with the system (I've included a Postman collection in the repo for easy testing):

* `POST /api/tickets/purchase` - Buy a ticket. Triggers the AI Fraud Check.
* `GET /api/recommendations/{passengerId}` - Gets top 5 personalized flights from the PyTorch GNN.
* `GET /api/flights/search` - Searches flights and quietly logs a "SEARCH" interaction to train the AI.
* `POST /api/routes` & `POST /api/flights` - Admin endpoints to build the network.

# To test further: use https://github.com/Bedo1190/flight-booking-backend/blob/main/flight-booking-backend.postman_collection.json on postman
