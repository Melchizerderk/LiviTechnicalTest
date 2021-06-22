package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DBConnector connector = new DBConnector(vertx);
    connector.query("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) PRIMARY KEY NOT NULL, status VARCHAR(128), date VARCHAR(128), name VARCHAR(128))").setHandler(done -> {
      if(done.succeeded())
      {
        System.out.println("completed db migrations");
        connector.query("CREATE UNIQUE INDEX index_service_name \n"+ "ON service (name)").setHandler(secondDone -> {
          if(secondDone.succeeded()) {
            System.out.println("completed unique index creation");
          }
          else
          {
            secondDone.cause().printStackTrace();
          }});
      } else {
        done.cause().printStackTrace();
      }
      vertx.close(shutdown -> {
        System.exit(0);
      });
    });
  }
}
