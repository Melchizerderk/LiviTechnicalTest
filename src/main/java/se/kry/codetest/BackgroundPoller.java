package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BackgroundPoller {

  private DBConnector connector;

  public Future<List<String>> pollServices(Map<String, List<String>> services) {
    //TODO

    Vertx vertx = Vertx.vertx();
    connector = new DBConnector(vertx);
    HttpClientOptions options = new HttpClientOptions().setLogActivity(true);
    HttpClient client = vertx.createHttpClient(options);

    for (Map.Entry<String, List<String>> entry : services.entrySet())
    {
      client.getAbs(entry.getKey(), response -> {
        response.fetch(1000 * 30);
        if (response.statusCode() == 200)
        {
          entry.getValue().set(0, "OK");
        }
        else
        {
          entry.getValue().set(0, "FAIL");
        }
        System.out.println("Received response with status code " + response.statusCode());
      }).end();
      /*HttpClientRequest requestAnswer = client.request(HttpMethod.GET, entry.getKey());*/

      for (Map.Entry<String, List<String>> service : services.entrySet())
      {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(service.getValue().get(0));
        jsonArray.add(service.getKey());
        connector.query("UPDATE service SET status = ? WHERE url = ?", jsonArray).setHandler(done ->{
          if (done.succeeded())
          {
            System.out.println("Service : " + service.getKey() + " has been updated");
          }
          else
          {
            done.cause().printStackTrace();
          }
        });
      }
    }

    System.out.println("HELLO");
    return Future.failedFuture("TODO");
  }
}
