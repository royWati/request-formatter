package chainbox.io.esbformatter.services;

import chainbox.io.esbformatter.dao.iso8583.*;
import chainbox.io.esbformatter.dao.json.JsonConfigurationTemplate;
import chainbox.io.esbformatter.dao.universal.EsbService;
import chainbox.io.esbformatter.dao.universal.MappingFields;
import chainbox.io.esbformatter.wrapper.CustomResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

@Service
public class IsoMessageProcessingImpl implements IsoMessageProcessing {

    @Value("${templates-location}")
    private String templateLocation;

    private EsbServiceProcessing esbServiceProcessing;
    private GenericPackager packager;
    private ASCIIChannel asciiChannel;

    @Autowired
    @Lazy
    public void setEsbServiceProcessing(EsbServiceProcessing esbServiceProcessing) {
        this.esbServiceProcessing = esbServiceProcessing;
    }

    @Override
    public ResponseEntity PROCESS_ISO_REQUEST(String serviceName, JsonObject jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");

        String template = templateLocation+"/"+serviceName+"-data.json";

        EsbService esbService = esbServiceProcessing.GET_ESBSERVICE(serviceName);

        IsoConfigurationTemplate isoConfigurationTemplate = GET_ISO_CONFIG_FILE(template);

        StringBuilder missingFields  = new StringBuilder();
        StringJoiner sj = new StringJoiner(",", "[", "]");
        boolean notMissing = true;
        missingFields.append("request requires the following fields ");

        List<IsoMappingFields> mappingFieldsList = isoConfigurationTemplate.getRequestFields();

        for (IsoMappingFields mappingFields : mappingFieldsList){
            String originator = mappingFields.getRequestName();
            if (!jsonObject.has(originator)) {
                notMissing = false;
                sj.add(originator);
            }
        }

        if (notMissing){

            ISOMsg isoMsg = PRODUCE_ISO_MSG(isoConfigurationTemplate,jsonObject,esbService.getMethodType());

            String packagerLocation = "config/"+esbService.getMethodType();
            ISOMsg recieveIso = MAKE_ISO_REQUEST(esbService.getServerUrl(),packagerLocation,isoMsg);

            JsonObject responseObject = CREATE_RESPONSE_BODY(recieveIso,isoConfigurationTemplate);

            responseObject.addProperty("esbStatus",200);
            responseObject.addProperty("esbMessage","transaction processed successfully");

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
    public ISOMsg PRODUCE_ISO_MSG(IsoConfigurationTemplate isoConfigurationTemplate, JsonObject jsonObject
    ,String method) {
        ISOMsg isoMsg = new ISOMsg();
        String packagerLocation = "config/"+method;

        IsoGeneratedFields generatedFields = ISO_GENERATED_FIELDS();
        try {

            packager = new GenericPackager(packagerLocation);
            isoMsg.setPackager(packager);
            isoMsg.setMTI(isoConfigurationTemplate.getMtiRequest());

            // inserting iso fixed fields
            for (IsoFixedFields fixedFields : isoConfigurationTemplate.getFixedFields()){
                isoMsg.set(fixedFields.getFieldId(),fixedFields.getValue());
            }

            // map json object data to the respective fields

            for (IsoMappingFields isoMappingFields : isoConfigurationTemplate.getRequestFields()){
                isoMsg.set(isoMappingFields.getField(),
                        jsonObject.get(isoMappingFields.getRequestName()).getAsString());
            }

            // recycle data through the field

            for (DataSharingFields fields : isoConfigurationTemplate.getSharingFields()){
                String value  = isoMsg.getString(fields.getDataField());
                isoMsg.set(fields.getDestinationField(),value);
            }

            //add the generic fields
            // add generated values

            isoMsg.set(12,generatedFields.getHhmmss_12());
            isoMsg.set(13,generatedFields.getMMDD_13());
            isoMsg.set(37,generatedFields.getMMddHHmmss_37());


            READ_ISO_MESSAGE(isoMsg);
        }catch (Exception e){
            e.printStackTrace();
        }

        return isoMsg;
    }

    @Override
    public void READ_ISO_MESSAGE(ISOMsg isoMsg) {
        System.out.println("reading iso...");
        try {
            System.out.println("response..."+isoMsg.getMTI());

        } catch (ISOException e) {
            e.printStackTrace();
        }
        for (int i = 1; i <= isoMsg.getMaxField(); i++) {
            if (isoMsg.hasField(i)) {
                System.out.println("Field - " + i + " : " + isoMsg.getString(i));
            }
        }
    }

    @Override
    public void VALIDATE_ISO_MESSAGE(ISOMsg isoMsg) {

    }

    @Override
    public IsoGeneratedFields ISO_GENERATED_FIELDS() {
        IsoGeneratedFields isoGeneratedFields = new IsoGeneratedFields();

        String hhmmss = new SimpleDateFormat("hhmmss").format(new Date());
        String MMDD = new SimpleDateFormat("MMdd").format(new Date());
        String MMddHHmmss = new SimpleDateFormat("MMddHHmmss").format(new Date());

        isoGeneratedFields.setHhmmss_12(hhmmss);
        isoGeneratedFields.setMMDD_13(MMDD);
        isoGeneratedFields.setMMddHHmmss_37(MMddHHmmss);

        return isoGeneratedFields;
    }

    @Override
    public ISOMsg MAKE_ISO_REQUEST(String connection, String methodLocation,ISOMsg isoMsg) {
        String[] connectionData = connection.split("__");
        String hostName = connectionData[0];
        int port = Integer.parseInt(connectionData[1]);

        ISOMsg recieveIso = new ISOMsg();

        try {
            packager = new GenericPackager(methodLocation);
            asciiChannel = new ASCIIChannel(hostName, port, packager);
            System.out.println("about to open channel on " + hostName + " on port " + port);
            asciiChannel.connect();

            System.out.println("channel connected");
            System.out.println("\nRaw Iso binary request : \n" + GET_ISO_STRING(isoMsg));


            byte[] msg = GET_ISO_STRING(isoMsg).getBytes();
            int length = msg.length;
            System.out.println(length);

            asciiChannel.send(isoMsg); // send iso message at this point
            System.out.println("Result");
            // receive the response
            recieveIso = asciiChannel.receive(); // receive Iso message at this point
            READ_ISO_MESSAGE(recieveIso);
            asciiChannel.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }

        return recieveIso;
    }

    @Override
    public String GET_ISO_STRING(ISOMsg isoMsg) throws ISOException, UnsupportedEncodingException {

        READ_ISO_MESSAGE(isoMsg);
        byte[] dataStream = isoMsg.pack();
        return new String(dataStream, "UTF-8");
    }

    @Override
    public IsoConfigurationTemplate GET_ISO_CONFIG_FILE(String fileName) {
        IsoConfigurationTemplate isoConfigurationTemplate = new IsoConfigurationTemplate();
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

            isoConfigurationTemplate = new Gson().fromJson(contents,IsoConfigurationTemplate.class);

            System.out.println(new ObjectMapper().writeValueAsString(isoConfigurationTemplate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isoConfigurationTemplate;
    }

    @Override
    public JsonObject CREATE_RESPONSE_BODY(ISOMsg isoMsg,IsoConfigurationTemplate isoConfigurationTemplate) {

        JsonObject jsonObject = new JsonObject();

        for (IsoResponseFields isoResponseFields : isoConfigurationTemplate.getResponseFields()){
            String value = isoMsg.getString(isoResponseFields.getField());

            jsonObject.addProperty(isoResponseFields.getResponseName(),value);
        }
        return jsonObject;
    }


}
