## Reactive Event Sourcing pattern in Java

Based on series of articles about implementing Event Sourcing pattern provided by SoftwareMill Academy.

### Run

1. Start postgres container:
```shell
docker-compose -p cinema -f development/docker-compose-jdbc.yml up
```

2. Run the application with JDK 21:
```shell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="--enable-preview"
```

### Test

1. Create show:
```shell
curl -X POST --location --location "http://localhost:8080/shows" \
    -H "Content-Type: application/json" \
    -d "{
          \"id\": \"16441a2e-7f04-432c-be9f-aa4e7377e4ce\",
          \"title\": \"Chicago\",
          \"maxSeats\": 100
        }"
```

2. Get show:
```shell
curl -X GET --location http://localhost:8080/shows/16441a2e-7f04-432c-be9f-aa4e7377e4ce
```

3. Reserve seat:
```shell
curl -X PATCH --location http://localhost:8080/shows/16441a2e-7f04-432c-be9f-aa4e7377e4ce/seats/1 \
  -H "Content-Type: application/json" \
  -d "{
        \"action\": \"RESERVE\"
      }"
```

4. Cancel seat reservation:
```shell
curl -X PATCH --location http://localhost:8080/shows/16441a2e-7f04-432c-be9f-aa4e7377e4ce/seats/1 \
  -H "Content-Type: application/json" \
  -d "{
        \"action\": \"CANCEL_RESERVATION\"
      }"
```

### Debug

* Adminer: http://localhost:8081/?pgsql=postgres_container&db=postgres&username=admin