package chainbox.io.esbformatter.services;


import chainbox.io.esbformatter.dao.db.ConnectionManager;
import chainbox.io.esbformatter.dao.db.StoredProcedures;
import chainbox.io.esbformatter.dao.iso8583.IsoConfigurationTemplate;
import chainbox.io.esbformatter.dao.json.JsonConfigurationTemplate;
import chainbox.io.esbformatter.dao.universal.EsbService;
import chainbox.io.esbformatter.dao.xml.SoapRequestBody;
import chainbox.io.esbformatter.dao.xml.WSDLConfigurationTemplate;
import chainbox.io.esbformatter.wrapper.IsoServiceWrapper;
import chainbox.io.esbformatter.wrapper.ServiceWrapper;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface TemplateConfiguration {

    ResponseEntity createTemplate(ServiceWrapper serviceWrapper);
    ResponseEntity createTemplate(IsoServiceWrapper serviceWrapper);
    ResponseEntity createTemplate(String xmlWrapper,String templateName);
    ResponseEntity createTemplate(ConnectionManager connectionManager);
    ResponseEntity createTemplate(WSDLConfigurationTemplate wsdlConfigurationTemplate);
    ResponseEntity createTemplate(String wsdl,String wsdlService,String variableLocation,String tag,String xmlBody,
                                  String fullTagged);

    ResponseEntity createTemplate(String wsdl,String tag,String xmlBody,
                                  boolean fullTagged);

    ResponseEntity createTemplate(StoredProcedures storedProcedures,String databaseAlias) throws IOException;
    void createService(EsbService esbService);
    List<EsbService> esbServices();
    List<ConnectionManager> databaseConnectionManagers();
    List<StoredProcedures> getStoredProcedures(String fileName);
    WSDLConfigurationTemplate getWSDLTemplate (String fileName);
    void createDataTemplate(JsonConfigurationTemplate jsonConfigurationTemplate, String fileName);
    void createDataTemplate(IsoConfigurationTemplate isoConfigurationTemplate, String fileName);
    void createDataTemplate(WSDLConfigurationTemplate wsdlConfigurationTemplate, String fileName);

    Object testobject();

    SoapRequestBody CREATE_SOAP_SERVICE_TEMPLATE(String xmlBody,String tagName,boolean fullyTagged);

}
