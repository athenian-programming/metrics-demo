package org.athenian;

import com.codahale.metrics.health.HealthCheck;
import spark.Service;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleServer {

  private final AtomicLong counter = new AtomicLong(0);

  private final HealthCheckServer healthCheckServer;
  private final Service           http;

  public SimpleServer() {
    this.healthCheckServer = new HealthCheckServer(8090);

    this.http = Service.ignite();
    this.http.port(8080);
  }

  public static void main(String[] args)
      throws Exception {
    SimpleServer server = new SimpleServer();
    server.init();

    Thread.sleep(Integer.MAX_VALUE);
  }

  private void init()
      throws Exception {

    this.healthCheckServer
        .register("HealthCheckServer",
                  this.healthCheckServer.newHealthCheck())
        .register("SimpleServer",
                  new HealthCheck() {
                    @Override
                    protected Result check() {
                      return counter.get() < 10 ? HealthCheck.Result.healthy("SimpleServer is okay")
                                                : HealthCheck.Result.unhealthy("SimpleServer is sick");
                    }
                  });

    this.healthCheckServer.start();

    this.http.get("/reset",
                  (req, res) -> {
                    res.header("cache-control", "must-revalidate,no-cache,no-store");
                    res.status(200);
                    res.type("text/plain");

                    counter.set(0);
                    return "Counter reset";
                  });

    this.http.get("/*",
                  (req, res) -> {
                    res.header("cache-control", "must-revalidate,no-cache,no-store");
                    res.status(200);
                    res.type("text/plain");

                    return "The counter value is " + counter.incrementAndGet();
                  });
  }
}
