package chainbox.io.esbformatter.services;

import chainbox.io.esbformatter.dao.iso8583.IsoConfigurationTemplate;
import chainbox.io.esbformatter.dao.iso8583.IsoGeneratedFields;
import com.google.gson.JsonObject;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;

public interface IsoMessageProcessing {

    ResponseEntity PROCESS_ISO_REQUEST(String serviceName, JsonObject jsonObject);
    ISOMsg PRODUCE_ISO_MSG(IsoConfigurationTemplate isoConfigurationTemplate, JsonObject jsonObject,String method);
    void READ_ISO_MESSAGE(ISOMsg isoMsg);
    void VALIDATE_ISO_MESSAGE(ISOMsg isoMsg);

    IsoGeneratedFields ISO_GENERATED_FIELDS();
    ISOMsg MAKE_ISO_REQUEST(String connection,String method,ISOMsg isoMsg);
    String GET_ISO_STRING(ISOMsg isoMsg) throws ISOException, UnsupportedEncodingException;

    IsoConfigurationTemplate GET_ISO_CONFIG_FILE(String fileName);

    JsonObject CREATE_RESPONSE_BODY(ISOMsg isoMsg,IsoConfigurationTemplate isoConfigurationTemplate);
}
