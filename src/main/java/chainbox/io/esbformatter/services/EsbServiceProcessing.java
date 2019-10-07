package chainbox.io.esbformatter.services;

import chainbox.io.esbformatter.dao.db.ConnectionManager;
import chainbox.io.esbformatter.dao.iso8583.IsoConfigurationTemplate;
import chainbox.io.esbformatter.dao.json.JsonConfigurationTemplate;
import chainbox.io.esbformatter.dao.universal.EsbService;
import chainbox.io.esbformatter.dao.universal.Headers;
import chainbox.io.esbformatter.dao.universal.ResponseMessages;
import chainbox.io.esbformatter.wrapper.CustomResponse;
import com.google.gson.JsonObject;
import org.jpos.iso.ISOMsg;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.List;

public interface EsbServiceProcessing {

    JsonObject CREATE_REQUEST_BODY(JsonConfigurationTemplate template,JsonObject jsonObject);
    JsonObject CREATE_RESPONSE_BODY(JsonConfigurationTemplate template,JsonObject jsonObject);
    JsonConfigurationTemplate GET_JSON_CONFIG_FILE (String fileName);

    boolean servicePresent(String serviceName);
    EsbService GET_ESBSERVICE(String serviceName);


    ResponseEntity CONVERT_AND_ROUTE_SERVICE(String serviceName,JsonObject jsonObject) ;
    ResponseEntity CONVERT_AND_ROUTE_SERVICE_SOAP(String serviceName,JsonObject jsonObject) ;
    ResponseEntity CONVERT_AND_ROUTE_SERVICE_JSON(String serviceName,JsonObject jsonObject);
    ResponseEntity CONVERT_AND_ROUTE_SERVICE_ISO(String serviceName,JsonObject jsonObject);
    ResponseEntity CONVERT_AND_ROUTE_SERVICE_DATABASE(String serviceName,JsonObject jsonObject) throws SQLException;
    ResponseEntity ROUTE_EXTERNAL(JsonObject jsonObject , List<Headers> headers , EsbService esbService);
    HttpHeaders CONSTRUCT_HEADERS (List<Headers> headers);
    CustomResponse CONSTRUCT_EXTERNAL_RESPONSE(ResponseEntity<String> externalResponse,
                                               List<ResponseMessages> responseMessages);


    boolean checkDatabaseAliasPresence(String aliasName);




}
