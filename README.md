Пайплайн для запуска в файле Jenkinsfile. 
Пайплайн предполагает, что minikube уже запущен. 
Для работы с приложением нужно пробросить порт командой:
```kubectl port-forward service/front -n bankapp 8088:8088```

<p>Logstash</p>

```kubectl port-forward service/logstash -n bankapp 5044:5044```

<p>Kibana</p>

```kubectl port-forward service/kibana -n bankapp 5061:5061```
<p>Prometheus</p>

```kubectl port-forward service/prometheus -n bankapp 9090:9090```
<p>Grafana</p>

```kubectl port-forward service/grafana -n bankapp 3000:3000```
<p>Zipkin</p>

```kubectl port-forward service/zipkin -n bankapp 9411:9411```

Учетные записи:
- ```user``` / ```1```
- ```user2``` / ```1```