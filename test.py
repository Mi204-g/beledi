import requests

base_url = "http://localhost:8080/api"

# 1. Register a user
reg_payload = {
    "nom": "Test User",
    "email": "test2@example.com",
    "motDePasse": "password",
    "role": "CITOYEN"
}
requests.post(base_url + "/auth/register", json=reg_payload)

# 2. Login
login_payload = {
    "email": "test2@example.com",
    "motDePasse": "password"
}
res = requests.post(base_url + "/auth/login", json=login_payload)
token = res.json().get("token")

# 3. Create signalement
headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}
sig_payload = {
    "titre": "lkpoiuytfdrgtfyuhijo",
    "description": "'[poluytdresdrtfuyo",
    "categorie": "VOIRIE",
    "latitude": 18.0785,
    "longitude": -15.9654,
    "photoUrl": None
}
res = requests.post(base_url + "/signalements", json=sig_payload, headers=headers)
print("STATUS:", res.status_code)
print("BODY:", res.text)
