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
public class SoapRequestBody {

    private String soapName;
    private String tagName;
    private boolean fullyTagged;
    private List<AttributesStore> attributesStores;
    private List<String> jsonKeys;
}
