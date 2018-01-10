package org.athenian.metrics;

import com.codahale.metrics.health.HealthCheck;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleServer {
  private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);

  private static final Counter requests    = Counter.build()
                                                    .name("requests_total")
                                                    .help("Total requests.")
                                                    .register();
  private static final Gauge   counterVals = Gauge.build()
                                                  .name("counter_vals")
                                                  .help("Counter Values.")
                                                  .register();

  private final AtomicLong     counter       = new AtomicLong(0);
  private final CountDownLatch shutdownLatch = new CountDownLatch(1);

  private final HealthCheckServer healthCheckServer;
  private final Service           http;

  public SimpleServer() {
    this.healthCheckServer = new HealthCheckServer(8090);

    this.http = Service.ignite();
    this.http.port(8080);
  }

  public static void main(String[] args)
      throws Exception {
    new SimpleServer().init()
                      .waitToShutdown()
                      .shutdown();
  }

  private SimpleServer init()
      throws Exception {

    logger.info("Initializing server");

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

    logger.info("Starting HealthCheck server");
    this.healthCheckServer.start();

    this.http.before(
        (request, response) -> {
          requests.inc();
        });

    this.http.get("/reset",
                  (req, res) -> {
                    res.header("cache-control", "must-revalidate,no-cache,no-store");
                    res.status(200);
                    res.type("text/plain");

                    counter.set(0);
                    counterVals.set(0);
                    return "Counter reset";
                  });

    this.http.get("/shutdown",
                  (req, res) -> {
                    res.header("cache-control", "must-revalidate,no-cache,no-store");
                    res.status(200);
                    res.type("text/plain");

                    String passwd = req.queryParams("password");
                    if (passwd != null && passwd.equals("topsecret")) {
                      shutdownLatch.countDown();
                      return "Server shutdown";
                    }
                    else {
                      logger.info("Shutdown request missing a valid password");
                      return "Shutdown requires a password";
                    }
                  });

    this.http.get("/counter-json",
                  (req, res) -> {
                    res.header("cache-control", "must-revalidate,no-cache,no-store");
                    res.status(200);
                    res.type("application/json");

                    return String.format("{ \"counter\": %d }", counter.get());
                  });

    this.http.get("/*",
                  (req, res) -> {
                    res.header("cache-control", "must-revalidate,no-cache,no-store");
                    res.status(200);
                    res.type("text/plain");

                    counterVals.inc();
                    return "The counter value is " + counter.incrementAndGet();
                  });
    return this;
  }

  private SimpleServer waitToShutdown()
      throws InterruptedException {
    logger.info("Waiting for shutdown");
    shutdownLatch.await();
    return this;
  }

  private void shutdown()
      throws Exception {
    logger.info("Shutting down server");
    this.healthCheckServer.stop();
    this.http.stop();
  }
}
