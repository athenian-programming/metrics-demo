# Metrics Demo

## Install

Clone the repo with:
```bash
git clone https://github.com/athenian-robotics/metrics-demo.git
```

## Usage

|  Admin Urls                                       |
|:--------------------------------------------------|
| http://localhost:8090/ping                        |
| http://localhost:8090/thread-dump                 |
| http://localhost:8090/version                     |
| http://localhost:8090/healthcheck                 |
| http://localhost:8090/metrics                     |
|                                                   |
                                              
|  Counter Urls                                     |
|:--------------------------------------------------|
| http://localhost:8080/                            |
| http://localhost:8080/counter-json                |
| http://localhost:8080/reset                       |
| http://localhost:8080/shutdown?password=topsecret |

## Prometheus

Before running Prometheus, update the prometheus.yml file and assign your hostname
to the simple-server targets value (localhost will not work).

Run [Prometheus](https://prometheus.io) in docker with:
```bash
docker run -p 9090:9090 -v ~/git/metrics-demo/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
```

Log into the Prometheus dashboard at: http://localhost:9090

View the Protheus metrics at: http://localhost:9090/metrics


## Grafana

Run [Grafana](https://grafana.com) in docker with:
```bash
docker run -d -p 3000:3000 grafana/grafana
```

Log into Grafana at http://localhost:3000 with username/password
of admin/admin.

To customize the refresh rate, go to Dashboad Settings->Time Picker and update the *auto-refresh* value.


