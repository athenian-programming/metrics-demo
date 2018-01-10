package org.athenian;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.PingServlet;
import com.codahale.metrics.servlets.ThreadDumpServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.concurrent.atomic.AtomicBoolean;

public class HealthCheckServer {

  private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
  private final AtomicBoolean       started             = new AtomicBoolean(false);

  private final Server server;

  public HealthCheckServer(int port) {
    this.server = new Server(port);

    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    this.server.setHandler(context);

    context.addServlet(new ServletHolder(new PingServlet()), "/ping");
    context.addServlet(new ServletHolder(new VersionServlet()), "/version");
    context.addServlet(new ServletHolder(new HealthCheckServlet(this.healthCheckRegistry)), "/healthcheck");
    context.addServlet(new ServletHolder(new ThreadDumpServlet()), "/thread-dump");
  }

  public HealthCheckServer register(String name, HealthCheck healthCheck) {
    this.healthCheckRegistry.register(name, healthCheck);
    return this;
  }

  public HealthCheck newHealthCheck() {
    return
        new HealthCheck() {
          @Override
          protected Result check()
              throws Exception {
            return started.get() ? HealthCheck.Result.healthy("HealthCheckServer started")
                                 : HealthCheck.Result.unhealthy("HealthCheckServer not started");
          }
        };
  }

  public void start()
      throws Exception {
    this.started.set(true);
    this.server.start();
  }

}
