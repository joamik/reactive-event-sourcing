## Reactive Event Sourcing pattern in Java

Based on series of articles about implementing Event Sourcing pattern provided by SoftwareMill Academy.

### Run

1. Run the application with JDK 21:
```shell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="--enable-preview"
```

### Test

1. Get show:
```shell
curl -X GET --location http://localhost:8080/shows/16441a2e-7f04-432c-be9f-aa4e7377e4ce
```
2. Reserve seat:
```shell
curl -X PATCH --location http://localhost:8080/shows/16441a2e-7f04-432c-be9f-aa4e7377e4ce/seats/1 \
  -H "Content-Type: application/json" \
  -d "{
        \"action\": \"RESERVE\"
      }"
```

3. Cancel seat reservation:
```shell
curl -X PATCH --location http://localhost:8080/shows/16441a2e-7f04-432c-be9f-aa4e7377e4ce/seats/1 \
  -H "Content-Type: application/json" \
  -d "{
        \"action\": \"CANCEL_RESERVATION\"
      }"
```