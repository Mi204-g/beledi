#!/bin/bash
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"test2@test.com", "motDePasse":"password"}' http://localhost:8080/api/auth/login | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# Create signalement
curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"titre":"Test", "description":"Test description", "categorie":"VOIRIE", "latitude": 18.0, "longitude": -15.0}' http://localhost:8080/api/signalements

# Get signalements
curl -v -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/signalements/mes
