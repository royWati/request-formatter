package chainbox.io.esbformatter.controller;


import chainbox.io.esbformatter.dao.db.ConnectionManager;
import chainbox.io.esbformatter.dao.db.StoredProcedures;
import chainbox.io.esbformatter.dao.xml.WSDLConfigurationTemplate;
import chainbox.io.esbformatter.services.TemplateConfiguration;
import chainbox.io.esbformatter.wrapper.IsoServiceWrapper;
import chainbox.io.esbformatter.wrapper.ServiceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/esb/generator")
public class TemplateGeneratorController {

    @Autowired
    private TemplateConfiguration templateConfiguration;

    @PostMapping("/service/create/json-template")
    public ResponseEntity createJsonTempalate(@RequestBody ServiceWrapper serviceWrapper){
       return templateConfiguration.createTemplate(serviceWrapper);
    }

    @PostMapping("/service/create/ISO8583-template")
    public ResponseEntity createISO8583Tempalate(@RequestBody IsoServiceWrapper serviceWrapper){
        return templateConfiguration.createTemplate(serviceWrapper);
    }
    @PostMapping("/service/create/soap-template/{templateName}")
    public ResponseEntity createSoapTemplate(@RequestBody String xmlFile, @PathVariable String templateName){
        return templateConfiguration.createTemplate(xmlFile,templateName);
    }

    @PostMapping("/service/create/wsdl-template")
    public ResponseEntity createWSDLTemplate(@RequestBody WSDLConfigurationTemplate configurationTemplate){
        return templateConfiguration.createTemplate(configurationTemplate);
    }

    @PostMapping("/service/create/db-connection-template")
    public ResponseEntity createDatabaseConnectionTemplate(@RequestBody ConnectionManager connectionManager){
        return templateConfiguration.createTemplate(connectionManager);
    }
    @PostMapping("/service/create/stored-procedure/{aliasName}")
    public ResponseEntity createStoreProcedure(@RequestBody StoredProcedures storedProcedures ,
                                               @PathVariable String aliasName) throws IOException {

        return templateConfiguration.createTemplate(storedProcedures, aliasName);
    }

    @PostMapping("/service/create/wsdl-service/{wsdl}")
    public ResponseEntity createWSDLservice(@PathVariable String wsdl,
                                            @RequestParam String tag,
                                            @RequestParam boolean fullTagged,
                                            @RequestBody String xmlBody){

        return templateConfiguration.createTemplate(wsdl, tag, xmlBody, fullTagged);
    }

    @PostMapping("/test-service")
    public Object createTestJsonObject(){
        return templateConfiguration.testobject();
    }
}
