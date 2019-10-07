package chainbox.io.esbformatter.services;

import chainbox.io.esbformatter.dao.db.ConnectionManager;
import chainbox.io.esbformatter.dao.db.StoredProcedures;
import chainbox.io.esbformatter.dao.iso8583.IsoConfigurationTemplate;
import chainbox.io.esbformatter.dao.json.JsonConfigurationTemplate;
import chainbox.io.esbformatter.dao.universal.EsbService;
import chainbox.io.esbformatter.dao.xml.AttributesStore;
import chainbox.io.esbformatter.dao.xml.SoapRequestBody;
import chainbox.io.esbformatter.dao.xml.SoapService;
import chainbox.io.esbformatter.dao.xml.WSDLConfigurationTemplate;
import chainbox.io.esbformatter.wrapper.CustomResponse;
import chainbox.io.esbformatter.wrapper.IsoServiceWrapper;
import chainbox.io.esbformatter.wrapper.ServiceWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;

import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TemplateConfigurationImpl implements TemplateConfiguration {
    @Value("${templates-location}")
    public String templateLocation;
    @Autowired
    private EsbServiceProcessing esbServiceProcessing;

    @Override
    public ResponseEntity createTemplate(ServiceWrapper serviceWrapper) {


        boolean found =esbServiceProcessing.servicePresent(serviceWrapper.getService().getServiceName());
        System.out.println(found);
        if (found){
            String serviceName = serviceWrapper.getService().getServiceName();
            String dataTemplateName = serviceName+"-data.json";
            createDataTemplate(serviceWrapper.getTemplate(),dataTemplateName);
            CustomResponse customResponse = new CustomResponse();
            customResponse.setEsbMessage("service updated successfully");
            customResponse.setEsbStatus(200);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type","application/json");
            return new ResponseEntity<>(customResponse, headers, HttpStatus.OK);
        }else{
            createService(serviceWrapper.getService());
            String serviceName = serviceWrapper.getService().getServiceName();
            String dataTemplateName = serviceName+"-data.json";
            System.out.println(dataTemplateName);
            createDataTemplate(serviceWrapper.getTemplate(),dataTemplateName);
            return new ResponseEntity(HttpStatus.OK);
        }


    }

    @Override
    public ResponseEntity createTemplate(IsoServiceWrapper serviceWrapper) {
        boolean found =esbServiceProcessing.servicePresent(serviceWrapper.getService().getServiceName());
        System.out.println(found);
        if (found){
            String serviceName = serviceWrapper.getService().getServiceName();
            String dataTemplateName = serviceName+"-data.json";
            createDataTemplate(serviceWrapper.getTemplate(),dataTemplateName);
            CustomResponse customResponse = new CustomResponse();
            customResponse.setEsbMessage("service updated successfully");
            customResponse.setEsbStatus(200);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type","application/json");
            return new ResponseEntity<>(customResponse, headers, HttpStatus.OK);
        }else{
            createService(serviceWrapper.getService());
            String serviceName = serviceWrapper.getService().getServiceName();
            String dataTemplateName = serviceName+"-data.json";
            System.out.println(dataTemplateName);
            createDataTemplate(serviceWrapper.getTemplate(),dataTemplateName);
            return new ResponseEntity(HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity createTemplate(String xmlWrapper,String templateName) {

        JSONObject jsonObject = XML.toJSONObject(xmlWrapper);
        String strJsonObject = String.valueOf(jsonObject);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");


        return new ResponseEntity<>(strJsonObject,headers,HttpStatus.OK);
    }

    @Override
    public ResponseEntity createTemplate(ConnectionManager connectionManager) {

        List<ConnectionManager> list = databaseConnectionManagers();
        list.add(connectionManager);

        // TODO Test the connection to make sure that it is okay before saving it
        try {
            String connection = new ObjectMapper().writeValueAsString(list);
            FileWriter fileWriter = new FileWriter(templateLocation+"/db-connections.json");
            fileWriter.write(connection);
            fileWriter.flush();
            System.out.println("db-connections.json file updated");

            String strAlias = "[]";
            String templateName= connectionManager.getDatabaseAlias()+".json";

            FileWriter aliasTemplate = new FileWriter(templateLocation+"/"+templateName);
            aliasTemplate.write(strAlias);
            aliasTemplate.flush();
            System.out.println(templateLocation+"/"+templateName+" file created");
        } catch (Exception e) {
            e.printStackTrace();
        }


        return new ResponseEntity(HttpStatus.OK);
    }

    @Override
    public ResponseEntity createTemplate(WSDLConfigurationTemplate wsdlConfigurationTemplate) {

        try {
            String str_wsdl = new ObjectMapper().writeValueAsString(wsdlConfigurationTemplate);

            EsbService esbService = new EsbService();
            esbService.setTemplateType("WSDL");
            esbService.setServiceName(wsdlConfigurationTemplate.getWsdlName());
            esbService.setMethodType("SOAP");
            esbService.setServerUrl(wsdlConfigurationTemplate.getWsdlUrl());

            createService(esbService);

            String fileName = wsdlConfigurationTemplate.getWsdlName()+"-wsdl.json";

            createDataTemplate(wsdlConfigurationTemplate,fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ResponseEntity createTemplate(String wsdl,String wsdlService,
                                         String variableLocation,
                                         String tag, String xmlBody, String fullTagged) {


//        StringBuilder builder = new StringBuilder();
//        builder.append(wsdl).append("-wsdl.json");
//
//        String fileName = builder.toString();
//
//        //Convert body to JsonObject
//        JSONObject jsonObject = XML.toJSONObject(xmlBody);
//        String strJsonObject = String.valueOf(jsonObject);
//
//
//        System.out.println("org.json--->"+strJsonObject);
//        JsonObject jsonObject1 = new JsonParser().parse(strJsonObject).getAsJsonObject();
//
//        System.out.println("org.gson--->"+jsonObject1.toString());
//
//        JsonObject jsonObject2 = jsonObject1.get("soapenv:Envelope").
//                getAsJsonObject().get("soapenv:Body").getAsJsonObject().get("ecl:GetAccountBalance")
//                .getAsJsonObject();
//
//        JsonObject jsonObject3 = new JsonObject();
//        JsonObject soapEnv = new JsonObject();
//        JsonObject soapBody = new JsonObject();
//        JsonObject body = new JsonObject();
//        body.addProperty("ecl:account","254708691402");
//
//        soapBody.add("soapenv:Body",body);
//        soapEnv.add("soapenv:Envelope",soapBody);
//
//
//        System.out.println("tryyy....>"+jsonObject2.get("ecl:account").getAsString());
//
//
//        //xml convertion
//
//        String xml = XML.toString(jsonObject);
//
//        System.out.println(xml);
//
//        SoapRequestBody soapService = new SoapRequestBody();
//        soapService.setServiceName(wsdlService);
//        soapService.setTag(tag);
//        soapService.setTemplate(jsonObject1.toString());
//        soapService.setVariableLocation(variableLocation);
//        soapService.setFullTagged(fullTagged);
//
//        WSDLConfigurationTemplate template = getWSDLTemplate(fileName);
//
//        List<SoapRequestBody> list = template.getServices();
//        list.add(soapService);
//
//        template.setServices(list);
//
//        createDataTemplate(template,fileName);
//

        return new ResponseEntity(HttpStatus.OK);
    }

    @Override
    public ResponseEntity createTemplate(String wsdl, String tag,
                                         String xmlBody, boolean fullTagged) {

        // check if the wsdl exists
        StringBuilder builder = new StringBuilder();
        builder.append(wsdl).append("-wsdl.json");

        String fileName = builder.toString();

        WSDLConfigurationTemplate template = getWSDLTemplate(fileName);

        if (template != null){

            SoapRequestBody requestBody = CREATE_SOAP_SERVICE_TEMPLATE(xmlBody,tag,fullTagged);

            List<SoapRequestBody> lists = template.getServices();

            lists.add(requestBody);

            template.setServices(lists);

            createDataTemplate(template,fileName);

            return new ResponseEntity(HttpStatus.OK);
        }else{
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity createTemplate(StoredProcedures storedProcedures,String databaseAlias) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");

        // check if the alias name exists
        if (esbServiceProcessing.checkDatabaseAliasPresence(databaseAlias)){

            String fileName = databaseAlias+".json";
            List<StoredProcedures> storedProceduresList =getStoredProcedures(fileName);

            storedProceduresList.add(storedProcedures);

            String str = new ObjectMapper().writeValueAsString(storedProceduresList);

            FileWriter fileWriter = new FileWriter(templateLocation+"/"+fileName);
            fileWriter.write(str);
            fileWriter.flush();

            // create a new service

            EsbService service = new EsbService();
            String serviceName = databaseAlias+"@"+storedProcedures.getProcedureName();
            String templateType = "database";
            String method = "storedProcedure";

            service.setMethodType(method);
            service.setServiceName(serviceName);
            service.setTemplateType(templateType);

            createService(service);

            return new ResponseEntity(HttpStatus.OK);
        }else{
            CustomResponse customResponse = new CustomResponse();
            customResponse.setEsbStatus(404);
            customResponse.setEsbMessage("database alias not found");

            String custom = new ObjectMapper().writeValueAsString(customResponse);

            return new ResponseEntity<>(custom,headers,HttpStatus.OK);

        }


    }

    @Override
    public void createService(EsbService esbService) {
        File file = new File(templateLocation);

        if (!file.exists()){
            if (file.mkdir()){
                System.out.println("folder created");
                String servicesFile ="[]";
                String databaseConnectionsFIle = "[]";

                try {
                    FileWriter fileWriter = new FileWriter(templateLocation+"/services.json");
                    fileWriter.write(servicesFile);
                    fileWriter.flush();
                    System.out.println("services.json file created");

                    FileWriter db_file_writer = new FileWriter(templateLocation+"/db-connections.json");
                    db_file_writer.write(databaseConnectionsFIle);
                    db_file_writer.flush();
                    System.out.println("db-connections.json");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        List<EsbService> list = esbServices();
        list.add(esbService);
        try {
            String getListAsString = new ObjectMapper().writeValueAsString(list);
            FileWriter fileWriter = new FileWriter(templateLocation+"/services.json");
            System.out.println(getListAsString);
            fileWriter.write(getListAsString);
            fileWriter.flush();
            System.out.println("services file created");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<EsbService> esbServices() {
        InputStream is = null;
        try {
            is = new FileInputStream(templateLocation+"/services.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<EsbService> data;
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8));

        Gson gson = new Gson();
        data = gson.fromJson(jsonElement, new TypeToken<List<EsbService>>() {
        }.getType());


        return data;
    }

    @Override
    public List<ConnectionManager> databaseConnectionManagers() {
        InputStream is = null;
        try {
            is = new FileInputStream(templateLocation+"/db-connections.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<ConnectionManager> data;
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8));

        Gson gson = new Gson();
        data = gson.fromJson(jsonElement, new TypeToken<List<ConnectionManager>>() {
        }.getType());


        return data;
    }

    @Override
    public List<StoredProcedures> getStoredProcedures(String fileName) {
        InputStream is = null;
        try {
            is = new FileInputStream(templateLocation+"/"+fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<StoredProcedures> data;
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8));

        Gson gson = new Gson();
        data = gson.fromJson(jsonElement, new TypeToken<List<StoredProcedures>>() {
        }.getType());


        return data;
    }

    @Override
    public WSDLConfigurationTemplate getWSDLTemplate(String fileName) {
        InputStream is = null;
        try {
            is = new FileInputStream(templateLocation+"/"+fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        WSDLConfigurationTemplate data;
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8));

        Gson gson = new Gson();
        data = gson.fromJson(jsonElement, new TypeToken<WSDLConfigurationTemplate>() {
        }.getType());

        return data;
    }

    @Override
    public void createDataTemplate(JsonConfigurationTemplate jsonConfigurationTemplate, String fileName) {
        String storage = templateLocation+"/"+fileName;
        try {
            String dataTemplate = new ObjectMapper().writeValueAsString(jsonConfigurationTemplate);
            System.out.println(dataTemplate);
            FileWriter fileWriter = new FileWriter(storage);
            fileWriter.write(dataTemplate);
            fileWriter.flush();
            System.out.println("data template created");
        } catch (Exception e) {
            e.printStackTrace();
        }

        esbServiceProcessing.GET_JSON_CONFIG_FILE(storage);
    }

    @Override
    public void createDataTemplate(IsoConfigurationTemplate isoConfigurationTemplate, String fileName) {
        String storage = templateLocation+"/"+fileName;
        try {
            String dataTemplate = new ObjectMapper().writeValueAsString(isoConfigurationTemplate);
            System.out.println(dataTemplate);
            FileWriter fileWriter = new FileWriter(storage);
            fileWriter.write(dataTemplate);
            fileWriter.flush();
            System.out.println("data template created");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void createDataTemplate(WSDLConfigurationTemplate wsdlConfigurationTemplate, String fileName) {
        String storage = templateLocation+"/"+fileName;
        try {
            String dataTemplate = new ObjectMapper().writeValueAsString(wsdlConfigurationTemplate);
            System.out.println(dataTemplate);
            FileWriter fileWriter = new FileWriter(storage);
            fileWriter.write(dataTemplate);
            fileWriter.flush();
            System.out.println("data template created");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object testobject() {

//        WSDLConfigurationTemplate template = getWSDLTemplate("microsoft-navition-wsdl.json");
//
//        List<SoapRequestBody> soapServices = template.getServices();
//
//        JsonObject jsonObjects = new JsonObject();
//
//        for (SoapService soapService : soapServices){
//            System.out.println(soapService.getTemplate());
//            JsonObject j  = new JsonParser().parse(soapService.getTemplate()).getAsJsonObject();
//
//
//            InputStream is = new StringBufferInputStream(soapService.getTemplate());
//            JsonObject data;
//            JsonParser jsonParser = new JsonParser();
//            JsonElement jsonElement = jsonParser.parse(new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8));
//
//            Gson gson = new Gson();
//            data = gson.fromJson(jsonElement, new TypeToken<JsonObject>() {
//            }.getType());
//
//            System.out.println("final data ..."+data.toString());
//
//
//            jsonObjects = j;
//        }
//
//        return jsonObjects;
        return "400";
    }

    @Override
    public SoapRequestBody CREATE_SOAP_SERVICE_TEMPLATE(String xmlBody,String tagName,boolean fullyTagged) {
        JSONObject jsonObject= XML.toJSONObject(xmlBody);
        String strJsonObject = String.valueOf(jsonObject);

        // convert string to com.gson.JsonObject

        JsonObject json = new JsonParser().parse(strJsonObject).getAsJsonObject();

        JsonObject jsonEnvelope = json.get("soapenv:Envelope").getAsJsonObject();

        Set<Map.Entry<String, JsonElement>> entries = jsonEnvelope.entrySet();

        SoapRequestBody requestBody = new SoapRequestBody();

        List<AttributesStore> storeList = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : entries){

            AttributesStore store = new AttributesStore();

            String xmln = entry.getKey();

            if (xmln.contains("xmlns")){
                store.setAttribute(entry.getKey());
                store.setValue(entry.getValue().getAsString());
                storeList.add(store);
            }


        }

        requestBody.setAttributesStores(storeList);

        JsonObject jsonBody = jsonEnvelope.get("soapenv:Body").getAsJsonObject();

        Set<Map.Entry<String, JsonElement>> bodyEntries = jsonBody.entrySet();

        String requestName = null;

        JsonObject bodyObject = new JsonObject();

        List<String> restRequest = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : bodyEntries){
            String soapRequest = entry.getKey();

            bodyObject = entry.getValue().getAsJsonObject();
            String replace ="";
            if (fullyTagged){
                String tag = tagName+":";
                replace = soapRequest.replace(tag, "");
            }else {
                replace = soapRequest;
            }
            requestName = replace;

            Set<Map.Entry<String, JsonElement>> restBody = bodyObject.entrySet();

            for (Map.Entry<String, JsonElement> rest : restBody){
                String request = rest.getKey();

                String r = "";
                if (fullyTagged){
                    String tag = tagName+":";
                    r = request.replace(tag, "");
                }else {
                    r = request;
                }
                restRequest.add(r);
            }

        }
        requestBody.setSoapName(requestName);
        requestBody.setFullyTagged(fullyTagged);
        requestBody.setTagName(tagName);
        requestBody.setJsonKeys(restRequest);


        return requestBody;

    }


}
