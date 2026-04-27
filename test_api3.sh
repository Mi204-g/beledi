#!/bin/bash
# Generate a fake token for an email not in the DB
# (I'll just use the test2 token, then delete test2 from the DB, then call the API)
# Or I can just hit the API with a curl command using a JWT with a non-existent email
# Actually, I'll just change the token payload manually!

HEADER="eyJhbGciOiJIUzI1NiJ9"
PAYLOAD=$(echo -n '{"sub":"fake@test.com","iat":1777294930,"exp":1777381330}' | base64 | tr -d '=' | tr '+/' '-_')
SIGNATURE="fake_signature_that_wont_validate_but_we_want_to_see_if_extract_fails"

# Since the signature will be invalid, jwtUtil.extractUsername might throw SignatureException!
# Wait, extractUsername extracts the claims. It validates the signature!
# Let's see JwtUtil.java
