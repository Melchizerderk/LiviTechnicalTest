package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.rmi.registry.Registry;
import java.sql.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private HashMap<String, List<String>> services = new HashMap<>();
  //TODO use this
  private DBConnector connector;
  private BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    getServicesInDb();
    //services.put("https://www.kry.se", "UNKNOWN");
    vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(services));
    setRoutes(router);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void getServicesInDb()
  {
    connector.query("SELECT * FROM service").setHandler( done -> {
      if (done.succeeded())
      {
        services.clear();
        ResultSet rs = done.result();
        for (JsonObject rows : rs.getRows())
        {
          List<String> paramList = new ArrayList<>();
          paramList.add(rows.getString("status"));
          paramList.add(rows.getString("name"));
          services.put(rows.getString("url"), paramList);
        }
      }
    });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());
    router.get("/service").handler(req -> {
      List<JsonObject> jsonServices = services
          .entrySet()
          .stream()
          .map(service ->
              new JsonObject()
                  .put("url", service.getKey())
                  .put("status", service.getValue().get(0))
                  .put("name", service.getValue().get(1)))
          .collect(Collectors.toList());
      req.response()
          .putHeader("content-type", "application/json")
          .end(new JsonArray(jsonServices).encode());
    });
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      addServiceToDb(jsonBody);
      req.response()
              .putHeader("content-type", "text/plain")
              .end("OK");
    });
    router.post("/delete").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      deleteServiceFromDb(jsonBody.getString("url"));
      services.remove(jsonBody.getString("url"));
      req.response()
              .putHeader("content-type", "text/plain")
              .end("OK");
    });
  }

  private void deleteServiceFromDb(String serviceUrl)
  {
    JsonArray jsonArray = new JsonArray();
    jsonArray.add(serviceUrl);
    connector.query("DELETE FROM service WHERE url = ?", jsonArray).setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("Successful query delete");
        services.remove(serviceUrl);
      } else {
        System.out.println("Failed delete because : ");
        done.cause().printStackTrace();
      }
    });
  }

  private void addServiceToDb(JsonObject jsonBody)
  {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
      JsonArray jsonArray = new JsonArray();
      jsonArray.add(jsonBody.getString("url"));
      jsonArray.add("UNKNOWN");
      jsonArray.add(now.toString());
      jsonArray.add(jsonBody.getString("name"));
      connector.query("INSERT OR REPLACE INTO service(url, status, date, name) VALUES(?,?,?,?)", jsonArray).setHandler(done -> {
        if (done.succeeded()) {
          System.out.println("Successful query insert");
          getServicesInDb();
        } else {
          System.out.println("Failed insert into cause : ");
          done.cause().printStackTrace();
        }
      });
      System.out.println("HALLO");
  }
}



