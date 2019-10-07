package chainbox.io.esbformatter.dao.xml;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SoapService {

    private String serviceName;
    private String variableLocation;
    private String tag;
    private String template;
    private String fullTagged;
}
