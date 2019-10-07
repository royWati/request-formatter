package chainbox.io.esbformatter.controller;

import chainbox.io.esbformatter.services.EsbServiceProcessing;
import chainbox.io.esbformatter.wrapper.CustomResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/esb/process")
public class EsbServices {

    @Autowired
    private EsbServiceProcessing esbServiceProcessing;

    @PostMapping(value="/service/{serviceName}",consumes = {"application/json","application/xml"},
            produces = "text/plain")
    public ResponseEntity processService(@PathVariable String serviceName, @RequestBody String requestObject){

        JsonObject jsonObject;
        try {
            jsonObject = new JsonParser().parse(requestObject).getAsJsonObject();
            return esbServiceProcessing.CONVERT_AND_ROUTE_SERVICE(serviceName,jsonObject);
        }catch (Exception e){
            return new ResponseEntity<>(new CustomResponse(406,"request body not found"),
                    HttpStatus.OK);
        }

    }
}
