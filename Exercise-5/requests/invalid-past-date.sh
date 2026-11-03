curl -X POST http://localhost:8080/leave \
  -H "Content-Type: application/json" \
  -d '{
    "studentName": "Meera",
    "regNo": "RA2311003010003",
    "fromDate": "2026-10-01",
    "toDate": "2026-10-02",
    "reason": "Personal work"
  }'
