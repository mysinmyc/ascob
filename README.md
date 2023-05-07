ASCOB
=====


Wrapper di orchestrazione.

Dispone delle funzionalità minime per gestire l'esecuzione di job su backend eterogenei (es: Jenkins, PageDuty Rundeck,...)

E' studiato per porsi come frontend di portali di automazione custom:
- Repository interno dei run. All'atto della sottomissione è possibile indicare l'utente finale owner del job
- Sistema di locking

## Architettura

Il job è l'entità atomica gestita dal framework. Rappresenta un'operazione da eseguire su un backend; Il backend da utilizzare è identificato mediante labels
Il backend effettua l'operazione descritta dal job con i parameteri forniti.
Il run è l'esecuzione di un job
Per gestire la concorrenza dei job è previsto che i job possano fornire una serie di lock da acquisire. Le lock sono memorizzate in un KV store


## Esecuzione di un job

Il job è descritto dalla classe [JobSpec](ascob-core%2Fsrc%2Fmain%2Fjava%2Fascob%2Fjob%2FJobSpec.java)

Dispone di un builder per semplificarne la definizione.

Lato server il compnente responsabile per la sottomissione del job è il [JobService](ascob-server%2Fsrc%2Fmain%2Fjava%2Fascob%2Fserver%2Fjob%2FJobService.java)

Il jobservice identifica ogni lancio mediante runId ma non espone gli internals del run

```
    JobSpec jobSpec = JobSpec.builder("submitter").withDescription("Job di prova").build();
    Long runId = jobService.submit(jobSpec);
```




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
- [x] swagger api (yaml generato da runtime)
- [ ] logging ed error handling
- [ ] documentazione




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
