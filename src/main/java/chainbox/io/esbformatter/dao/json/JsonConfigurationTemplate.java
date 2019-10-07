package chainbox.io.esbformatter.dao.json;


import chainbox.io.esbformatter.dao.universal.FixedFields;
import chainbox.io.esbformatter.dao.universal.Headers;
import chainbox.io.esbformatter.dao.universal.MappingFields;
import chainbox.io.esbformatter.dao.universal.ResponseMessages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JsonConfigurationTemplate {

    private List<MappingFields> clientRequest;
    private List<MappingFields> clientResponse;
    private List<FixedFields> fixedFields;
    private List<Headers> headers;
    private List<ResponseMessages> responseMessages;
}
