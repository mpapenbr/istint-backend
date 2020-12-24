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