[![Java CI with Gradle](https://github.com/felipegutierrez/bidding-system-java/actions/workflows/gradle.yml/badge.svg)](https://github.com/felipegutierrez/bidding-system-java/actions/workflows/gradle.yml)
[![Run Test Suites](https://github.com/felipegutierrez/bidding-system-java/actions/workflows/codecov-test-suites.yml/badge.svg)](https://github.com/felipegutierrez/bidding-system-java/actions/workflows/codecov-test-suites.yml)
[![Docker biddingsystem-java project](https://github.com/felipegutierrez/bidding-system-java/actions/workflows/docker-hub-publish.yml/badge.svg)](https://github.com/felipegutierrez/bidding-system-java/actions/workflows/docker-hub-publish.yml)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ed3ee8d5f83443d6a487f4bab72f2f4a)](https://app.codacy.com/gh/felipegutierrez/bidding-system-java?utm_source=github.com&utm_medium=referral&utm_content=felipegutierrez/bidding-system-java&utm_campaign=Badge_Grade_Settings)
[![codecov](https://codecov.io/gh/felipegutierrez/bidding-system-java/branch/main/graph/badge.svg?token=wsGWEdNtGR)](https://codecov.io/gh/felipegutierrez/bidding-system-java)
![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/felipeogutierrez/biddingsystem-java)


Bidding system
==============

Yieldlab is a technology service provider, connecting suppliers (those who
have space to show ads, e.g. on their websites) to bidders (those who actually
want to show ads). The core process is to listen for requests, gather metadata
and bids, and afterwards to determine who is winning. This challenge is about
setting up a simplified version of this core process as its own application.

Contents of this document:

- [1 The task](#1-the-task)
  - [1.1 Incoming Requests](#11-incoming-requests)
  - [1.2 Bid Requests](#12-bidRequest-requests)
  - [1.3 Bid Response](#13-bidRequest-response)
  - [1.4 Auction Response](#14-auction-response)
  - [1.5 Configuration](#15-configuration)
- [2 How to test your application](#2-how-to-test-your-application)
  - [2.1 Prerequisites](#21-prerequisites)
  - [2.2 Start the Docker containers](#22-start-the-docker-containers)
  - [2.3 Start the application](#23-start-the-application)
  - [2.4 Run the test](#24-run-the-test)
- [3 Running](#3-running)


## 1 The task

Build a bidding system behaving as following:

For every incoming request as described in [1], send out bidRequest requests as
described in [2] to a configurable number of bidders [5]. Responses from these
bidders as described in [3] must be processed. The highest bidder wins, and
payload is sent out as described in [4].

Incoming and outgoing communication is to be done over HTTP. Message formats
are described below.

Please write code that you would want to maintain in production as well, or
document all exceptions to this rule and give reasons as to why you made those
exceptions.

Please stay with commonly known frameworks for easier reviewing and explaining
afterwards.

[1]: #1-incoming-requests
[2]: #2-bidRequest-requests
[3]: #3-bidRequest-response
[4]: #4-auction-response

### 1.1 Incoming Requests

The application must listen to incoming HTTP requests on port 8080.

An incoming request is of the following format:

    http://localhost:8080/[id]?[key=value,...]

The URL will contain an ID to identify the ad for the auction, and a number of
query-parameters.

### 1.2 Bid Requests

The application must forward incoming bidRequest requests by sending a corresponding
HTTP POST request to each of the configured bidders with the body in the
following JSON format:

```json
{
	“id”: $id,
	“attributes” : {
		“$key”: “$value”,
		…
	}
}
```

The property `attributes` must contain all incoming query-parameters.
Multi-value parameters need not be supported.

### 1.3 Bid Response

The bidders' response will contain details of the bidRequest(offered price), with `id` and `bidRequest`
values in a numeric format:

```json
{
	"id" : $id,
	"bidRequest": bidRequest,
	"content": "the string to deliver as a response"
}
```

### 1.4 Auction Response

The response for the auction must be the `content` property of the winning bidRequest,
with some tags that can be mentioned in the content replaced with respective values.

For now, only `$price$` must be supported, denoting the final price of the bidRequest.

Example:


Following bidRequest responses:
```json
{
	"id" : 123,
	"bidRequest": 750,
	"content": "a:$price"
}
```
and

```json
{
	"id" : 123,
	"bidRequest": 500,
	"content": "b:$price"
}
```
will produce auction response as string:
a:750

### 1.5 Configuration

The application should have means to accept accept a number of configuration
parameters. For the scope of this task, only one parameter is to be supported:

| Parameter | Meaning                                                  |
|-----------|----------------------------------------------------------|
| `bidders` | a comma-separated list of URLs denoting bidder endpoints |


## 2 How to test your application

In order to test your application for correctness, a simple test suite is
provided.

### 2.1 Prerequisites

First, a set of bidders is required that will respoond to bidding requests
sent out by your application. For this test suite, we will be using a
pre-built [Docker][what-is-docker] image that will be started several times
with sligthly different configuration values.

Moreover, we provide a shell script that executes the tests and verifies the
test results. That shell script requires the `curl` and `diff` binaries to be
in your `PATH`.

So, here is a list of the requirements:

- Docker ([official installation docs][install-docker])
- A shell (or you'll need to carry out the tests manually)
- `diff` (e.g. from [GNU Diffutils][diffutils] package)
- `curl` ([official download link][curl-dl])

[what-is-docker]: https://www.docker.com/what-docker
[install-docker]: https://docs.docker.com/engine/installation/
[diffutils]: https://www.gnu.org/software/diffutils/
[curl-dl]: https://curl.haxx.se/download.html

### 2.2 Start the Docker containers

To start the test environment, either use the script `test-setup.sh` or run the
following commands one after the other from your shell:

```sh
docker run --rm -e server.port=8081 -e biddingTrigger=a -e initial=150 -p 8081:8081 yieldlab/recruiting-test-bidder &
docker run --rm -e server.port=8082 -e biddingTrigger=b -e initial=250 -p 8082:8082 yieldlab/recruiting-test-bidder &
docker run --rm -e server.port=8083 -e biddingTrigger=c -e initial=500 -p 8083:8083 yieldlab/recruiting-test-bidder &
```

This will set up three bidders on localhost, opening ports 8081, 8082 and 8083.

### 2.3 Start the application

You can use the following configuration parameters to connect to these bidders
from your application:

| Parameter | Value                                                                 |
|-----------|-----------------------------------------------------------------------|
| `bidders` | `http://localhost:8081, http://localhost:8082, http://localhost:8083` |

### 2.4 Run the test

To run the test, execute the shell script `run-test.sh`. The script expects
your application to listen on `localhost:8080`. It will issue a number of bidRequest
requests to your application and verify the responses to these requests. If
your application doesn't respond correctly, it will print out a diff between
the expected and the actual results.

## 3 Running

 - Build: `./gradlew build`
 - Test: `./gradlew test`
 - Test report: `./gradlew jacocoTestReport`
 - Run with 3 bidders: 
```
java -jar ./build/libs/biddingsystem-0.0.1.jar \
-Dspring-boot.run.arguments=--bidders="http://localhosost:8081, http://localhost:8082, http://localhost:8083"
```
 - Commands to test:
```
./run-test.sh
curl -s "http://localhost:8080/1?a=5"; echo
curl -s "http://localhost:8080/2?c=5&b=2"; echo
```
 - Metrics:
   - Actuator metrics: [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)
   - Prometheus metrics: [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)
   - Start docker image: `ADMIN_USER=admin ADMIN_PASSWORD=admin ADMIN_PASSWORD_HASH=JDJhJDE0JE91S1FrN0Z0VEsyWmhrQVpON1VzdHVLSDkyWHdsN0xNbEZYdnNIZm1pb2d1blg4Y09mL0ZP docker-compose up`
     - Prometheus
       - Targets: [http://localhost:9090/targets](http://localhost:9090/targets)
       - Dashboard: [http://localhost:9090/graph](http://localhost:9090/graph)
         - Examples:
           - average inbound request duration: `rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])`
           - sum of http requests: `sum(prometheus_http_requests_total)`
           - sum of http requests grouped by code: `sum(prometheus_http_requests_total) by (code)`
           - Node exporter aggregation CPU modes: `sum(node_cpu_seconds_total) by (mode)`
           - Top-K CPU modes: `topk(3, sum(node_cpu_seconds_total) by (mode))`
           - Bottom-K CPU modes: `bottomk(3, sum(node_cpu_seconds_total) by (mode))`
           - Max and min: `max(node_cpu_seconds_total)` - `min(node_cpu_seconds_total)`
           - Rate/frequency of http requests per 1 minute: `rate(prometheus_http_requests_total[1m])`
           - Rate/frequency with handler: `rate(prometheus_http_requests_total{handler=~"/api.*"}[1m])`
           - irate to calculate based on the last 2 points measured: `irate(prometheus_http_requests_total[1m])`
           - how many times a given metric changed over some time: `changes(process_start_time_seconds[1h])`
           - calculates the per-second derivative of the time series in a range vector v, using simple linear regression: `deriv(process_resident_memory_bytes[1h])`
           - predict how much memory will be available for the next 2 hours based on the last hour: `predict_linear(node_memory_MemFree_bytes[1h], 2*60*60)/1024/1024`
           - [Cpu usage proportion](https://www.robustperception.io/using-group_left-to-calculate-label-proportions): `sum without (cpu)(rate(node_cpu_seconds_total[1m])) / ignoring(mode) group_left sum without (mode, cpu)(rate(node_cpu_seconds_total[1m]))`
          - Check if node memory is less than 20% or not: `100 * (node_memory_Active_bytes / node_memory_MemTotal_bytes)` or `(node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100` 
           - Graph the disk read rate: `rate(node_disk_reads_merged_total[30m])`
           - Based on the past 2 hours of data, find out how much disk will fill in next 6 hours: `predict_linear(node_disk_reads_merged_total[1h], 6*60*60)` 
           - Find out the CPU usage percentage: `system_cpu_usage`
           - PromQL functions definitions: [https://prometheus.io/docs/prometheus/latest/querying/functions/](https://prometheus.io/docs/prometheus/latest/querying/functions/)
         - Bidder requests:
           - successful and failure calls: `bidder_calls_success_total` and `bidder_calls_fail_total`
           - successful and failure calls together: `bidder_calls_success_total or bidder_calls_fail_total`
           - average: `avg_over_time(bidder_calls_success_total[1m])` and `avg_over_time(bidder_calls_fail_total[1m])`
           - Auctions in progress: `bidder_auction_inprogress`
           - Auction latency: `bidder_auction_latency_seconds_sum` and `bidder_auction_latency_seconds_count` and `bidder_auction_latency_seconds_max`
       - Rules: [http://127.0.0.1:9090/rules](http://127.0.0.1:9090/rules)
       - Alert manager: [http://127.0.0.1:9093/](http://127.0.0.1:9093/)
     - Blackbox exporter: [http://127.0.0.1:9115/](http://127.0.0.1:9115/)
       - Default debug ipv4 probe (property: `http_ipv4`): [http://127.0.0.1:9115/probe?target=prometheus.io&module=http_ipv4&debug=true](http://127.0.0.1:9115/probe?target=prometheus.io&module=http_ipv4&debug=true)
       - Pattern matching debug ipv4 probe (property: `http_find_prom`): [http://127.0.0.1:9115/probe?target=prometheus.io&module=http_find_prom&debug=true](http://127.0.0.1:9115/probe?target=prometheus.io&module=http_find_prom&debug=true)
       - ICMP probe: [http://127.0.0.1:9115/probe?target=8.8.8.8&module=icmp](http://127.0.0.1:9115/probe?target=8.8.8.8&module=icmp)
       - DNS probe: [http://127.0.0.1:9115/probe?target=8.8.8.8&module=dns_example](http://127.0.0.1:9115/probe?target=8.8.8.8&module=dns_example)
       - Scraping prometheus.io using Blackbox:
         - probe_success: [http://127.0.0.1:9090/graph?g0.expr=probe_success&g0.tab=1&g0.stacked=0&g0.range_input=1h](http://127.0.0.1:9090/graph?g0.expr=probe_success&g0.tab=1&g0.stacked=0&g0.range_input=1h)
         - probe_http_duration_seconds: [http://127.0.0.1:9090/graph?g0.expr=probe_http_duration_seconds&g0.tab=1&g0.stacked=0&g0.range_input=1h](http://127.0.0.1:9090/graph?g0.expr=probe_http_duration_seconds&g0.tab=1&g0.stacked=0&g0.range_input=1h)
     - Pushgateway: [http://127.0.0.1:9091/](http://127.0.0.1:9091/)
       - Push metrics: `echo "some_metric 3.14" | curl --data-binary @- http://admin:admin@localhost:9091/metrics/job/some_job`
     - Prometheus HTTP Rest endpoints using [httpie](https://httpie.io/):
       - services status: `http GET http://admin:admin@127.0.0.1:9090/api/v1/query?query=up`
       - prometheus http requests for the last 1 minute: `http GET http://admin:admin@127.0.0.1:9090/api/v1/query?query=prometheus_http_requests_total[1m]`
       - targets: `http GET http://admin:admin@127.0.0.1:9090/api/v1/targets?state=active`
       - rules: `http GET http://admin:admin@127.0.0.1:9090/api/v1/rules`
         - records: `http GET http://admin:admin@127.0.0.1:9090/api/v1/rules?types=record`
         - alerts: `http GET http://admin:admin@127.0.0.1:9090/api/v1/rules?types=alert`
       - alerts: `http GET http://admin:admin@127.0.0.1:9090/api/v1/alerts`
       - status
         - runtime: `http GET http://admin:admin@127.0.0.1:9090/api/v1/status/runtimeinfo`
         - build: `http GET http://admin:admin@127.0.0.1:9090/api/v1/status/buildinfo`
     - Grafana (admin/admin): [http://localhost:3000/](http://localhost:3000/)
       - docker container dashboard: [http://127.0.0.1:3000/d/s889cJ3Gz/docker-containers?orgId=1&refresh=10s](http://127.0.0.1:3000/d/s889cJ3Gz/docker-containers?orgId=1&refresh=10s)
       - docker host dashboard: [http://127.0.0.1:3000/d/C8U9c1qGz/docker-host?orgId=1&refresh=10s](http://127.0.0.1:3000/d/C8U9c1qGz/docker-host?orgId=1&refresh=10s)
       - services dashboard: [http://127.0.0.1:3000/d/VwU9cJ3Mz/monitor-services?orgId=1&refresh=10s](http://127.0.0.1:3000/d/VwU9cJ3Mz/monitor-services?orgId=1&refresh=10s)
       - nginx dashboard: [http://127.0.0.1:3000/d/hwUrcJ3Gk/nginx?orgId=1&refresh=10s](http://127.0.0.1:3000/d/hwUrcJ3Gk/nginx?orgId=1&refresh=10s)
       - prometheus stats dashboard: [http://127.0.0.1:3000/d/yt7cH93Mz/prometheus-stats?orgId=1](http://127.0.0.1:3000/d/yt7cH93Mz/prometheus-stats?orgId=1)
       - prometheus stats 2.0 dashboard: [http://127.0.0.1:3000/d/UI45H9qMz/prometheus-2-0-stats?orgId=1&refresh=1m](http://127.0.0.1:3000/d/UI45H9qMz/prometheus-2-0-stats?orgId=1&refresh=1m)
       - grafana dashboard: [http://127.0.0.1:3000/d/A6VcH93Gk/grafana-metrics?orgId=1](http://127.0.0.1:3000/d/A6VcH93Gk/grafana-metrics?orgId=1)
  
  
![grafana prometheus dashboard](pictures/grafana-prometheus.png?raw=true "Grafana Prometheus dashboard")

- Kubernetes
 - create the bidding system namespace with all pods: `./k8s/create-cluster.sh`
 - delete everything on the namespace: `kubectl delete all --all --namespace=bidding-system`
 - Prometheus: [http://localhost:9090](http://localhost:9090)

- Create docker image:
```
$ ./gradlew jar docker
$ docker images
REPOSITORY                                        TAG                          IMAGE ID       CREATED          SIZE
biddingsystem-java                                0.0.1                        8cfc0bd8626e   10 seconds ago   168MB
```
- Running the docker image: `./gradlew jar docker dockerRun` or `docker run -i -t biddingsystem-java:0.1.0`
- Stop docker image: `docker stop biddingsystem-java`
- Logs:
```
docker logs biddingsystem-java
docker inspect biddingsystem-java
```

