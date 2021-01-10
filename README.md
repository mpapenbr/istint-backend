# istint-backend
Backend for istint

Helper:
time while read line; do curl http://localhost:8080/raceevents/021eb52b-2381-492c-827b-2b6adb880ba5/dummy -X POST -H "Content-Type: application/json" -d "$line"; done < send-data.json 

send-data.json contains ~1000 entries
This way the results are:
```
real    0m18.912s
user    0m5.740s
sys     0m1.849s
```

Using host.docker.internal results in 
```
real    5m40.677s
user    0m10.475s
sys     0m5.895s
```


## Mongo data


- create volume: `docker create volume mongo-backup`
- create a container: `docker run -it --rm -v mongo-backup:/backup  mongo'
- inside the container:  `mongodump --host host.docker.internal -u root -p example --authenticationDatabase admin  -d local -o /backup/`
- get the data out of the volume to local dir: `docker run -u 1000:1000 -it --rm -v mongo-backup:/backup -v $PWD:/local  busybox cp /backup/local.tgz /local/data.tgz`