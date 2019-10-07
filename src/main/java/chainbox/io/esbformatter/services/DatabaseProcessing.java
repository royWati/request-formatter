package chainbox.io.esbformatter.services;

import chainbox.io.esbformatter.dao.db.ConnectionManager;
import chainbox.io.esbformatter.dao.db.StoredProcedures;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseProcessing {
    Connection MYSQL_CONNECT(ConnectionManager connectionManager) throws SQLException;

    ResponseEntity PROCESS_DATABASE_REQUEST(String serviceName, JsonObject jsonObject) throws SQLException;

    ConnectionManager GET_CONNECTION_MANAGER(String aliasName);

    StoredProcedures GET_STORED_PROCEDURE(String name,String fileName);

    JsonArray CALL_STORED_PROCEDURE(ConnectionManager connectionManager ,
                                    StoredProcedures storedProcedures, JsonObject jsonObject) throws SQLException;


}
