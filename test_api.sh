#!/bin/bash
# Register
curl -s -X POST -H "Content-Type: application/json" -d '{"nom":"wedf", "email":"wedf@test.com", "password":"password"}' http://localhost:8080/api/auth/register > /dev/null

# Login
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"wedf@test.com", "password":"password"}' http://localhost:8080/api/auth/login | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Token: $TOKEN"

# Get signalements
curl -v -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/signalements/mes
