# ACES ARKA-ARKB Channel Service

ACES ARKA-ARKB transfer channel service

## Local Setup

### Set up Local Database

```
docker run -d -p 5432:5432 \
--name aces_arka_arkb_channel_service_db \
-e POSTGRES_PASSWORD=password \
-e POSTGRES_USER=postgres \
-e POSTGRES_DB=arka_arkb_channel_service_db \
postgres:9.6.1
```

### Run Ark listener

This app depends on a local instance of [Aces Listener Ark](https://github.com/ark-aces/aces-listener-ark) running
to get transactions from. Follow the instructions in the readme to get one running locally for this app to connect
to.

### Configuration

Copy [src/main/resources/application.yml](src/main/resources/application.yml) into an external file on your system
(for example: `/etc/arka-arkb-service/application.yml`) and replace configuration properties to match your
local setup. For example, you would need to change the `serviceArkbAccount` address and passphrase to an actual
ARK account.

Each channel generates and uses a unique deposit address so the balances should be collected periodically
into another account. This app includes a sweep job that is configured under the `arkaSweep` section of the config.
If enabled, the sweep will run on the configured interval (default is once per day) to collect the 
deposit balances into the target address.


### Run Channel Service

```
mvn clean spring-boot:run --spring.config.location=file:/etc/arka-arkb-service/application.yml
```

### Run Channel Service (production)


To run the application in a live environment, you can build a jar package using `mvn package` and then
run the jar app generated under `/target` build directory with you custom configuration:

```
java -jar {jar-name}.jar --spring.config.location=file:/etc/arka-arkb-service/application.yml
```


## Using Service

Get service info:

```
curl http://localhost:9191/
```
```
{
  "name" : "Aces ARKA-ARKB Channel Service",
  "description" : "ACES ARKA to ARKB Channel service for value transfers.",
  "version" : "1.0.0",
  "websiteUrl" : "https://arkaces.com",
  "instructions" : "After this contract is executed, any ARKA sent to depositArkaAddress will be exchanged for ARKB and  sent directly to the given recipientArkbAddress less service fees.\n",
  "flatFee" : "0",
  "percentFee" : "1.00%",
  "capacities": [{
    "value": "100000000.00",
    "unit": "ARKB",
    "displayValue": "100000000 ARKB"
  }],
  "inputSchema" : {
    "type" : "object",
    "properties" : {
      "recipientArkbAddress" : {
        "type" : "string"
      }
    },
    "required" : [ "recipientArkbAddress" ]
  },
  "outputSchema" : {
    "type" : "object",
    "properties" : {
      "depositArkaAddress" : {
        "type" : "string"
      },
      "recipientArkbAddress" : {
        "type" : "string"
      },
      "transfers" : {
        "type" : "array",
        "properties" : {
          "arkaAmount" : {
            "type" : "string"
          },
          "arkbPerArka" : {
            "type" : "string"
          },
          "arkaFlatFee" : {
            "type" : "string"
          },
          "arkaPercentFee" : {
            "type" : "string"
          },
          "arkaTotalFee" : {
            "type" : "string"
          },
          "arkbSendAmount" : {
            "type" : "string"
          },
          "arkbTransactionId" : {
            "type" : "string"
          },
          "createdAt" : {
            "type" : "string"
          }
        }
      }
    }
  }
}
```

Create a new Service Contract:

```
curl -X POST http://localhost:9191/contracts \
-H 'Content-type: application/json' \
-d '{
  "arguments": {
    "recipientArkbAddress": "DDiTHZ4RETZhGxcyAi1VruCXZKxBFqXMeh"
  }
}' 
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
    "recipientArkbAddress": "DDiTHZ4RETZhGxcyAi1VruCXZKxBFqXMeh",
    "depositArkaAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx",
    "transfers": []
}
```

Get Contract information after sending ARKA funds to `depositArkaAddress`:

```
curl -X GET http://localhost:9191/contracts/abe05cd7-40c2-4fb0-a4a7-8d2f76e74978
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
    "recipientArkbAddress": "DDiTHZ4RETZhGxcyAi1VruCXZKxBFqXMeh",
    "depositArkaAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx",
    "transfers" : [ {
      "id" : "uDui0F8PIjldKyGm0rdd",
      "status" : "completed",
      "createdAt" : "2018-01-21T20:24:52.057Z",
      "arkaTransactionId" : "78b6c99c40451d7e46f2eb41cdb831d087fecd759b01e00fd69e34959b5bee25",
      "arkaAmount" : "109.100000",
      "arkbPerArka" : "1000",
      "arkaFlatFee" : "0.00000000",
      "arkaPercentFee" : "1.00000000",
      "arkaTotalFee" : "0.00001000",
      "arkbSendAmount" : "1090999"
    } ]
  }
}
```
