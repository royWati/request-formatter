package chainbox.io.esbformatter.services;


import chainbox.io.esbformatter.dao.xml.SoapRequestBody;
import chainbox.io.esbformatter.dao.xml.WSDLConfigurationTemplate;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.MalformedURLException;

public interface SoapMessageProcessing {

    SoapRequestBody GET_SOAP_REQUEST_BODY(WSDLConfigurationTemplate template , String service);
    ResponseEntity MAKE_REQUEST(WSDLConfigurationTemplate template , String service, JsonObject jsonObject) throws IOException;

    String BUILD_SOAP_REQUEST_BODY(SoapRequestBody soapRequestBody,JsonObject jsonObject);

    JsonObject CREATE_RESPONSE(String response, String bodyType);

    ResponseEntity PROCESS_SOAP_REQUEST(String serviceName, JsonObject jsonObject);

    String MAKE_SOAP_REQUEST(WSDLConfigurationTemplate template , String body,String requestType) throws IOException;
}
