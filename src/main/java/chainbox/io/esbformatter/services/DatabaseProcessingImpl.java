package chainbox.io.esbformatter.services;

import chainbox.io.esbformatter.dao.db.ConnectionManager;
import chainbox.io.esbformatter.dao.db.StoredProcedures;
import chainbox.io.esbformatter.dao.db.StoredProceduresParameters;
import chainbox.io.esbformatter.wrapper.CustomResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jpos.iso.IF_CHAR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;

import java.sql.*;
import java.util.List;
import java.util.StringJoiner;

@Service
public class DatabaseProcessingImpl implements DatabaseProcessing {
    @Value("${templates-location}")
    public String templateLocation;

    private TemplateConfiguration templateConfiguration;

    @Autowired
    @Lazy
    public void setTemplateConfiguration(TemplateConfiguration templateConfiguration) {
        this.templateConfiguration = templateConfiguration;
    }

    @Override
    public Connection MYSQL_CONNECT(ConnectionManager connectionManager) throws SQLException {
        String url = "jdbc:mysql://"+connectionManager.getHost()+":3306/";
        String connectionUrl = url + connectionManager.getDatabaseName()+"?useUnicode=true" +
                "&useJDBCCompliantTimezoneShift=true" +
                "&useLegacyDatetimeCode=false" +
                "&serverTimezone=UTC" +
                "&useSSL=false";

     //   System.out.println(connectionUrl);
        Connection connection = DriverManager.getConnection(connectionUrl,connectionManager.getUsername(),
                connectionManager.getPassword()
        );
        
        return connection;
    }

    @Override
    public ResponseEntity PROCESS_DATABASE_REQUEST(String serviceName, JsonObject jsonObject) throws SQLException {
        System.out.println("service...."+serviceName);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");

        String[] properties = serviceName.split("@");

        String databaseConn = properties[0];
        String storedProcedureName = properties[1];

        // retrieve connection data
        ConnectionManager connectionManager = GET_CONNECTION_MANAGER(databaseConn);

        String template =databaseConn+".json";

        StoredProcedures storedProcedure = GET_STORED_PROCEDURE(storedProcedureName,template);

        StringBuilder missingFields  = new StringBuilder();
        StringJoiner sj = new StringJoiner(",", "[", "]");
        boolean notMissing = true;
        missingFields.append("request requires the following fields ");

        List<StoredProceduresParameters> parametersList = storedProcedure.getParameters();

        for (StoredProceduresParameters parameters : parametersList){
            String paramter = parameters.getName();
            if (!jsonObject.has(paramter)) {
                notMissing = false;
                sj.add(paramter);
            }
        }

        if (notMissing){

           JsonArray jsonArray = CALL_STORED_PROCEDURE(connectionManager,storedProcedure,jsonObject);
           JsonObject responseObject = new JsonObject();
           responseObject.addProperty("esbStatus",200);
           responseObject.addProperty("message","database execution was successful");
           responseObject.add("data",jsonArray );

            return new ResponseEntity<>(responseObject.toString(),headers,HttpStatus.OK);
        }else{
            CustomResponse customResponse = new CustomResponse();
            customResponse.setEsbStatus(406);
            String message = missingFields.append(sj.toString()).toString();
            customResponse.setEsbMessage(message);

            return new ResponseEntity<>(customResponse, headers, HttpStatus.OK);
        }


    }

    @Override
    public ConnectionManager GET_CONNECTION_MANAGER(String aliasName) {
        ConnectionManager connectionManager = new ConnectionManager();

        List<ConnectionManager> list = templateConfiguration.databaseConnectionManagers();

        for (ConnectionManager connectionManager1 : list){

            if (connectionManager1.getDatabaseAlias().equals(aliasName)){
                connectionManager = connectionManager1;
            }
        }
        return connectionManager;
    }

    @Override
    public StoredProcedures GET_STORED_PROCEDURE(String name, String fileName) {
        StoredProcedures storedProcedures = new StoredProcedures();

        List<StoredProcedures> list = templateConfiguration.getStoredProcedures(fileName);

        for (StoredProcedures storedProcedures1 : list){
            if (storedProcedures1.getProcedureName().equals(name)){
                storedProcedures = storedProcedures1;
            }
        }
        return storedProcedures;
    }

    @Override
    public JsonArray CALL_STORED_PROCEDURE(ConnectionManager connectionManager,
                                      StoredProcedures storedProcedures,JsonObject jsonObject) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();

           StringJoiner parameterJoiner = new StringJoiner(",","(",")");

           for (StoredProceduresParameters parameters : storedProcedures.getParameters()){
               parameterJoiner.add("?");
           }

           stringBuilder.append("{CALL ").append(storedProcedures.getProcedureName())
                   .append(parameterJoiner.toString()).append("}");

        System.out.println(stringBuilder.toString());
        String query = stringBuilder.toString();

        Connection connection = MYSQL_CONNECT(connectionManager);

        CallableStatement callableStatement = connection.prepareCall(query);

        for (StoredProceduresParameters storedProceduresParameters : storedProcedures.getParameters()){
            String dataType = storedProceduresParameters.getDataType();
            String value = jsonObject.get(storedProceduresParameters.getName()).getAsString();

            if (dataType.equals("String") || dataType.equals("varchar")){
                callableStatement.setString(storedProceduresParameters.getName(),value);
            }else{
                callableStatement.setInt(storedProceduresParameters.getName(),Integer.valueOf(value));
            }
        }

        JsonArray jsonArray = new JsonArray();
        ResultSet resultSet = callableStatement.executeQuery();

        if (!resultSet.isClosed()){


            try {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int column = metaData.getColumnCount();

                System.out.println("columns.."+column);

                while (resultSet.next()){

                    JsonObject dataObject = new JsonObject();
                    // create a json object with the respective column as the key
                    for (int i =0 ; i< column ; i++){
                        int columnIndex = i+1;
                        String columnName = metaData.getColumnName(columnIndex);
                        String value = resultSet.getString(columnIndex);

                        if (value.matches("[0-9]")){
                            dataObject.addProperty(columnName,Integer.parseInt(value));
                        }else{
                            dataObject.addProperty(columnName,value);
                        }


                    }

                    jsonArray.add(dataObject);

                }


            }catch (NullPointerException e){
                System.out.println("empty result set");
            }

        }else{
            System.out.println("result set is closed");
        }


        connection.close();

        // add meta data inorder to map the results that are required bu the response
      //  resultSet.getString(0);

        return jsonArray;
    }
}
