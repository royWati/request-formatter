package chainbox.io.esbformatter.dao.xml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WSDLConfigurationTemplate {

    private String wsdlName;
    private String wsdlUrl;
    private List<SoapRequestBody> services;
}
