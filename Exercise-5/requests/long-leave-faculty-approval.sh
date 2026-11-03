curl -X POST http://localhost:8080/leave \
  -H "Content-Type: application/json" \
  -d '{
    "studentName": "Arun",
    "regNo": "RA2311003010002",
    "fromDate": "2026-11-10",
    "toDate": "2026-11-14",
    "reason": "Family function"
  }'
