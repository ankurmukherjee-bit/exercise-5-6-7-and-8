curl -X POST http://localhost:8080/leave \
  -H "Content-Type: application/json" \
  -d '{
    "studentName": "Priya",
    "regNo": "RA2311003010001",
    "fromDate": "2026-11-10",
    "toDate": "2026-11-11",
    "reason": "Medical appointment"
  }'
