ASCOB
=====


Wrapper di orchestrazione.

Dispone delle funzionalità minime per gestire l'esecuzione di job su backend eterogenei (es: Jenkins, PageDuty Rundeck,...)

E' studiato per porsi come frontend di portali di automazione custom:
- Repository interno dei run. All'atto della sottomissione è possibile indicare l'utente finale owner del job
- Sistema di locking


## Da implementare

- [x] stop job
- [x] gestione output
- [x] hook per consentire ai backend di aggiornare lo stato dei job
- [x] sottomissione file
- [x] hook per consentire ai backend di acquisire file passati in input al job
- [x] polling asincrono stato job sui backend
- [x] security
- [x] ricerca job
- [x] retry job (by resubmission)
- [ ] logging ed error handling
- [ ] documentazione
- [ ] swagger api



## Compilazione ed esecuzione su container

E' possibile creare ed eseguire un container docker con il server 


docker build -t ascob .

docker run -d --rm -p8081:8081 ascob 


## Esempi chiamate

- Sottomissione job dummy

```
curl  -H "X-Api-Token: rootToken"  -X POST http://localhost:8081/api/runs -d '{ "jobSpec": { "description": "dummy", "labels":{"_execution_backend_id":"dummyBackend"}} }' -H "content-type: application/json"
```

Esempio Risposta:

```
{
  "runId": 1
}

```

- Job Info

```
curl  -H "X-Api-Token: rootToken" http://localhost:8081/api/runs/1
```

Esempio risposta

```
{
  "id": 1,
  "status": "SUBMITTED",
  "description": "dummy",
  "submitter": null,
  "definedTime": "2023-04-16T18:41:19.099475",
  "submissionTime": "2023-04-16T18:41:19.142079",
  "endTime": null
}
```
