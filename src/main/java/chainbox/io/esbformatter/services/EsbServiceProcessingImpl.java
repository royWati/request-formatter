package chainbox.io.esbformatter.services;

import chainbox.io.esbformatter.dao.db.ConnectionManager;
import chainbox.io.esbformatter.dao.iso8583.IsoConfigurationTemplate;
import chainbox.io.esbformatter.dao.json.JsonConfigurationTemplate;
import chainbox.io.esbformatter.dao.universal.*;
import chainbox.io.esbformatter.wrapper.CustomResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.catalina.util.CustomObjectInputStream;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class EsbServiceProcessingImpl implements EsbServiceProcessing {

    @Value("${templates-location}")
    public String templateLocation;

    @Autowired
    private TemplateConfiguration templateConfiguration;

    @Autowired
    private IsoMessageProcessing isoMessageProcessing;

    @Autowired
    private DatabaseProcessing databaseProcessing;

    @Autowired
    private SoapMessageProcessing soapMessageProcessing;


    @Override
    public JsonObject CREATE_REQUEST_BODY(JsonConfigurationTemplate template, JsonObject jsonObject) {
        JsonObject json = new JsonObject();
        List<MappingFields> clientRequestList = template.getClientRequest();
        List<FixedFields> fixedFieldsList = template.getFixedFields();

        // map the fields from the client request object to the current request object
        for (MappingFields mappingFields : clientRequestList){
            json.addProperty(mappingFields.getDestinationField(),
                    jsonObject.get(mappingFields.getOriginatorField()).getAsString());
        }

        // map the fixed values of the request object to the current request object
        for (FixedFields fixedFields : fixedFieldsList){
            json.addProperty(fixedFields.getField(),fixedFields.getValue());
        }

        return json;
    }

    @Override
    public JsonObject CREATE_RESPONSE_BODY(JsonConfigurationTemplate template, JsonObject jsonObject) {
        JsonObject json = new JsonObject();
        List<MappingFields> clientRequestList = template.getClientResponse();

        // from the response we get, we map the intended fields to the correct fields
        for (MappingFields mappingFields : clientRequestList){
            json.addProperty(mappingFields.getDestinationField(),
                    jsonObject.get(mappingFields.getOriginatorField()).getAsString());
        }

        return json;
    }

    @Override
    public JsonConfigurationTemplate GET_JSON_CONFIG_FILE(String fileName) {

        JsonConfigurationTemplate jsonConfigurationTemplate = new JsonConfigurationTemplate();
        try {
            InputStream inputStream =new FileInputStream(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = bufferedReader.readLine();

            StringBuilder stringBuilder = new StringBuilder();

            while (line !=null){
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }

            String contents = stringBuilder.toString();

            jsonConfigurationTemplate = new Gson().fromJson(contents,JsonConfigurationTemplate.class);

            System.out.println(new ObjectMapper().writeValueAsString(jsonConfigurationTemplate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonConfigurationTemplate;
    }

    @Override
    public boolean servicePresent(String serviceName) {

        List<EsbService> list = templateConfiguration.esbServices();

        boolean found = false;

        for(EsbService service : list){
            if (service.getServiceName().equals(serviceName)) found = true;
        }
        return found;
    }

    @Override
    public EsbService GET_ESBSERVICE(String serviceName) {
        EsbService esbService=new EsbService();
        List<EsbService> list = templateConfiguration.esbServices();
        for (EsbService service : list){
            if (service.getServiceName().equals(serviceName)) esbService =service;
        }
        return esbService;
    }

    @Override
    public ResponseEntity CONVERT_AND_ROUTE_SERVICE_JSON(String serviceName,JsonObject jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");

        if (servicePresent(serviceName)){

            EsbService esbService = GET_ESBSERVICE(serviceName);

            //get the request data configuration file
            
            String template = templateLocation+"/"+serviceName+"-data.json";
            JsonConfigurationTemplate jsonConfigurationTemplate = GET_JSON_CONFIG_FILE(template);
            
            // validate the json objects present

            StringBuilder missingFields  = new StringBuilder();
            StringJoiner sj = new StringJoiner(",", "[", "]");
            boolean notMissing = true;
            missingFields.append("request requires the following fields ");
            
            List<MappingFields> mappingFieldsList = jsonConfigurationTemplate.getClientRequest();
            
            for (MappingFields mappingFields : mappingFieldsList){
                String originator = mappingFields.getOriginatorField();
                if (!jsonObject.has(originator)) {
                    notMissing = false;
                    sj.add(originator);
                }
            }
            
            if (notMissing){
                // create json request the external API
                JsonObject jsonObject1 = CREATE_REQUEST_BODY(jsonConfigurationTemplate,jsonObject);


                ResponseEntity<String> responseFromExternal = ROUTE_EXTERNAL(
                        jsonObject1,jsonConfigurationTemplate.getHeaders(),esbService
                );


                CustomResponse externalResponse = CONSTRUCT_EXTERNAL_RESPONSE(responseFromExternal,
                        jsonConfigurationTemplate.getResponseMessages());

                if(externalResponse.getEsbStatus()== 200){

                    JsonObject responseBody = new Gson().fromJson(externalResponse.getEsbMessage(),
                            JsonObject.class);

                    JsonObject customResponseBody = CREATE_RESPONSE_BODY(
                            jsonConfigurationTemplate,responseBody
                    );

                    customResponseBody.addProperty("esbStatus",externalResponse.getEsbStatus());
                    customResponseBody.addProperty("esbMessage",
                            "service processed successfully");

                    return new ResponseEntity<>(customResponseBody.toString(),headers,HttpStatus.OK);
                }else{
                    return new ResponseEntity<>(externalResponse,headers,HttpStatus.OK);
                }

            }else{
                CustomResponse customResponse = new CustomResponse();
                customResponse.setEsbStatus(406);
                String message = missingFields.append(sj.toString()).toString();
                customResponse.setEsbMessage(message);

                return new ResponseEntity<>(customResponse, headers, HttpStatus.OK);
            }

        }else{
            CustomResponse customResponse = new CustomResponse();
            customResponse.setEsbStatus(404);
            customResponse.setEsbMessage("requested service not found");

            return new ResponseEntity<>(customResponse,headers,HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity CONVERT_AND_ROUTE_SERVICE(String serviceName, JsonObject jsonObject)  {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");

        System.out.println(serviceName);
        if (servicePresent(serviceName)){
            EsbService esbService = GET_ESBSERVICE(serviceName);

            System.out.println(serviceName);
            System.out.println(esbService.getTemplateType());

            if (esbService.getTemplateType().equals("json")){
                return CONVERT_AND_ROUTE_SERVICE_JSON(serviceName, jsonObject);
            }else if (esbService.getTemplateType().equals("ISO8583")){
                return CONVERT_AND_ROUTE_SERVICE_ISO(serviceName, jsonObject);
            }else if (esbService.getTemplateType().equals("database")){
                try {
                    return CONVERT_AND_ROUTE_SERVICE_DATABASE(serviceName, jsonObject);
                }catch (Exception e){
                    e.printStackTrace();
                    return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
                }

            }else{
                return new ResponseEntity(HttpStatus.OK);
            }
        }else{

            String[] s = serviceName.split("@");

            String wsdlCheck = s[0];

            if (servicePresent(wsdlCheck)){
                EsbService esbService = GET_ESBSERVICE(wsdlCheck);

                if (esbService.getTemplateType().equals("WSDL")){
                    return CONVERT_AND_ROUTE_SERVICE_SOAP(serviceName, jsonObject);
                }else{
                    CustomResponse customResponse = new CustomResponse();
                    customResponse.setEsbStatus(404);
                    customResponse.setEsbMessage("requested service not found");

                    return new ResponseEntity<>(customResponse,headers,HttpStatus.OK);
                }
            }else{
                CustomResponse customResponse = new CustomResponse();
                customResponse.setEsbStatus(404);
                customResponse.setEsbMessage("requested service not found");

                return new ResponseEntity<>(customResponse,headers,HttpStatus.OK);
            }


        }

    }

    @Override
    public ResponseEntity CONVERT_AND_ROUTE_SERVICE_SOAP(String serviceName, JsonObject jsonObject) {
        return soapMessageProcessing.PROCESS_SOAP_REQUEST(serviceName, jsonObject);
    }

    @Override
    public ResponseEntity CONVERT_AND_ROUTE_SERVICE_ISO(String serviceName, JsonObject jsonObject) {

        return isoMessageProcessing.PROCESS_ISO_REQUEST(serviceName, jsonObject);
    }

    @Override
    public ResponseEntity CONVERT_AND_ROUTE_SERVICE_DATABASE(String serviceName, JsonObject jsonObject) throws SQLException {
        System.out.println(jsonObject.toString());

            return databaseProcessing.PROCESS_DATABASE_REQUEST(serviceName, jsonObject);

    }

    @Override
    public ResponseEntity<String> ROUTE_EXTERNAL(JsonObject jsonObject, List<Headers> headers, EsbService esbService) {

        HttpHeaders httpHeaders = CONSTRUCT_HEADERS(headers);

        System.out.println(httpHeaders.toString());
        RestTemplate restTemplate = new RestTemplate();
        String url = esbService.getServerUrl();

        System.out.println(url);
        System.out.println(jsonObject.toString());

        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(),httpHeaders);
        System.out.println(entity.toString());
        ResponseEntity<String> responseEntity = null;

        try {
            if (esbService.getMethodType().equals("POST")){
                responseEntity= restTemplate.postForEntity(url,entity,String.class);
            }else{
                responseEntity= restTemplate.getForEntity(url,String.class,entity);
            }

            return responseEntity;
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getClass().getName());

            if (e.getLocalizedMessage().contains("Connection refused: connect")){
                responseEntity = new ResponseEntity<>("Connection refused: connect",
                        HttpStatus.SERVICE_UNAVAILABLE);
            }
            return responseEntity;
        }



    }

    @Override
    public HttpHeaders CONSTRUCT_HEADERS(List<Headers> headers) {

        HttpHeaders httpHeaders = new HttpHeaders();
        for (Headers h : headers){
            httpHeaders.add(h.getHeader(),h.getValue());
        }
        return httpHeaders;
    }

    @Override
    public CustomResponse CONSTRUCT_EXTERNAL_RESPONSE(ResponseEntity<String> externalResponse,
                                                      List<ResponseMessages> responseMessages) {

        System.out.println("api reconstruction taking place...");
        CustomResponse customResponse = new CustomResponse();
        customResponse.setEsbMessage("service currently unavailable");
        if (externalResponse != null){

            if (externalResponse.getStatusCode().is2xxSuccessful()){
                customResponse.setEsbStatus(200);
                customResponse.setEsbMessage(externalResponse.getBody());
            }else{ customResponse.setEsbStatus(externalResponse.getStatusCodeValue());

                for (ResponseMessages response : responseMessages){
                    if (externalResponse.getStatusCodeValue() == response.getHttpStatus())
                        customResponse.setEsbMessage(response.getMessage());
                }
            }
        }else{
            customResponse.setEsbStatus(404);
            for (ResponseMessages response : responseMessages){
                if (404 == response.getHttpStatus())
                    customResponse.setEsbMessage(response.getMessage());
            }
        }
        return customResponse;
    }

    @Override
    public boolean checkDatabaseAliasPresence(String aliasName) {

        List<ConnectionManager> connectionManagerList = templateConfiguration.databaseConnectionManagers();

        boolean found = false;

        for (ConnectionManager connectionManager : connectionManagerList){

            if (connectionManager.getDatabaseAlias().equals(aliasName)) found = true;
        }
        return found;
    }



}
