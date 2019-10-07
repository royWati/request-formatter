package chainbox.io.esbformatter.services;

import chainbox.io.esbformatter.dao.xml.AttributesStore;
import chainbox.io.esbformatter.dao.xml.SoapRequestBody;
import chainbox.io.esbformatter.dao.xml.WSDLConfigurationTemplate;
import chainbox.io.esbformatter.wrapper.CustomResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class SoapMessageProcessingImpl implements SoapMessageProcessing {

    private TemplateConfiguration templateConfiguration ;

    @Autowired @Lazy
    public void setTemplateConfiguration(TemplateConfiguration templateConfiguration) {
        this.templateConfiguration = templateConfiguration;
    }

    @Override
    public SoapRequestBody GET_SOAP_REQUEST_BODY(WSDLConfigurationTemplate template, String service) {

        SoapRequestBody requestBody = new SoapRequestBody();
        List<SoapRequestBody> list = template.getServices();

        for (SoapRequestBody soapRequestBody : list){

            if (service.equals(soapRequestBody.getSoapName())){
                requestBody = soapRequestBody;
            }
        }
        return requestBody;
    }

    @Override
    public ResponseEntity MAKE_REQUEST(WSDLConfigurationTemplate template, String service,
                                            JsonObject jsonObject) throws IOException {


        System.out.println(service+"...this got here");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");

        SoapRequestBody soapRequestBody = GET_SOAP_REQUEST_BODY(template,service);

        if (soapRequestBody != null){

            String soapBody = BUILD_SOAP_REQUEST_BODY(soapRequestBody,jsonObject);

            String response = MAKE_SOAP_REQUEST(template,soapBody,soapRequestBody.getSoapName());

            String bodyType = soapRequestBody.getSoapName()+"_Result";
            JsonObject responseObject = CREATE_RESPONSE(response,bodyType);

            System.out.println(responseObject);

            responseObject.addProperty("esbStatus",200);
            responseObject.addProperty("esbMessage","request was successful");

            System.out.println(responseObject);

            return new ResponseEntity<String>(responseObject.toString(),headers,HttpStatus.OK);

        }else{
            CustomResponse response = new CustomResponse();
            response.setEsbMessage(service+" not found");
            response.setEsbStatus(400);
            return new ResponseEntity<>(response,headers, HttpStatus.OK);
        }

    }

    @Override
    public String BUILD_SOAP_REQUEST_BODY(SoapRequestBody soapRequestBody,JsonObject jsonObject) {
        System.out.println("BUILD_SOAP_REQUEST_BODY"+"...this got here");

        StringBuilder soapBodyBuilder = new StringBuilder();

        soapBodyBuilder.append("<soapenv:Envelope");

        // add soap attributes to the envelope

        for (AttributesStore store : soapRequestBody.getAttributesStores()){

            String key = store.getAttribute();
            String value = store.getValue();

            StringBuilder builder = new StringBuilder();
            builder.append(key).append('=').append('"').append(value).append('"');
            soapBodyBuilder.append(" ").append(builder.toString());
        }

        // end add soap envelope request

        soapBodyBuilder.append(">").append("<soapenv:Header/>");

        soapBodyBuilder.append("<soapenv:Body>");

        //create request body

        if (soapRequestBody.isFullyTagged()){
            String request = soapRequestBody.getSoapName();
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("<").append(soapRequestBody.getTagName())
                    .append(":").append(request).append(">");

            soapBodyBuilder.append(bodyBuilder.toString());
        }else{
            String request = soapRequestBody.getSoapName();
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("<").append(request).append(">");
            soapBodyBuilder.append(bodyBuilder.toString());
        }


        // map entry values to the respective fields
        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();

        for (Map.Entry<String, JsonElement> entry : entries){

            if (soapRequestBody.isFullyTagged()){
                StringBuilder builder = new StringBuilder();

                builder.append("<").append(soapRequestBody.getTagName())
                        .append(":").append(entry.getKey()).append(">").
                        append(entry.getValue().getAsString()).
                        append("</").append(soapRequestBody.getTagName())
                        .append(":").append(entry.getKey()).append(">");

                soapBodyBuilder.append(builder.toString());
            }else{
                StringBuilder builder = new StringBuilder();
                builder.append("<").append(entry.getKey()).append(">").append(entry.getValue().getAsString())
                        .append("</").append(entry.getKey()).append(">");

                soapBodyBuilder.append(builder.toString());
            }



        }

        // end map entry values to the respective fields

        if (soapRequestBody.isFullyTagged()){
            String request = soapRequestBody.getSoapName();
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("</").append(soapRequestBody.getTagName())
                    .append(":").append(request).append(">");

            soapBodyBuilder.append(bodyBuilder.toString());
        }else{
            String request = soapRequestBody.getSoapName();
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("</").append(request).append(">");
            soapBodyBuilder.append(bodyBuilder.toString());
        }



        soapBodyBuilder.append("</soapenv:Body>");
        soapBodyBuilder.append("</soapenv:Envelope>");


        return soapBodyBuilder.toString();



    }

    @Override
    public JsonObject CREATE_RESPONSE(String response, String bodyType) {
        System.out.println("CREATE_RESPONSE"+"  got here");


        JSONObject jsonObject = XML.toJSONObject(response);

        JSONObject responseObject = jsonObject.getJSONObject("Soap:Envelope").getJSONObject("Soap:Body")
                .getJSONObject(bodyType);

        String r = String.valueOf(responseObject);
        JsonObject j = new JsonParser().parse(r).getAsJsonObject();



        System.out.println("json body..."+j.toString());
        return j;
    }

    @Override
    public ResponseEntity PROCESS_SOAP_REQUEST(String serviceName, JsonObject jsonObject) {

        System.out.println(serviceName);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");



        String[] serviceBreak = serviceName.split("@");

        String wsdl = serviceBreak[0]+"-wsdl.json";
        String service = serviceBreak[1];

        WSDLConfigurationTemplate wsdlConfigurationTemplate = templateConfiguration.getWSDLTemplate(wsdl);

        if (wsdlConfigurationTemplate != null){
            try {
                return MAKE_REQUEST(wsdlConfigurationTemplate,service,jsonObject);
            } catch (IOException e) {
                e.printStackTrace();

                CustomResponse customResponse = new CustomResponse();
                customResponse.setEsbStatus(503);
                customResponse.setEsbMessage("service not available at the moment");

                return new ResponseEntity<>(customResponse,headers, HttpStatus.OK);
            }
        }else{
            CustomResponse customResponse = new CustomResponse();
            customResponse.setEsbStatus(404);
            customResponse.setEsbMessage("wsdl location not found");

            return new ResponseEntity<>(customResponse,headers, HttpStatus.OK);


        }

    }

    @Override
    public String MAKE_SOAP_REQUEST(WSDLConfigurationTemplate template, String request,String requestType) throws IOException {
        StringBuilder response = new StringBuilder();

        System.out.println("MAKE_SOAP_REQUEST"+"...this got here");

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                String domain = "HQ";
                String username = "wgicheru";
                String password = "1qaz2wsx3EDC.";

                return new PasswordAuthentication(domain+"\\"+username,password.toCharArray());
            }
        });

        String urlStr =template.getWsdlUrl();
        URL urlRequest = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setAllowUserInteraction(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml");
        conn.setRequestProperty("SOAPAction", "urn:microsoft-dynamics-schemas/codeunit/EclectTest:"+requestType);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(request.getBytes());
        }


        InputStream stream = conn.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String str = "";
        while ((str = in.readLine()) != null) {
            response.append(str);
        }
        in.close();
        return response.toString();

    }
}
