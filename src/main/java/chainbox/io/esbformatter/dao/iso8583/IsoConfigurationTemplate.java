package chainbox.io.esbformatter.dao.iso8583;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IsoConfigurationTemplate {
    private String processingCode;
    private String currencyCode;
    private String mtiRequest;
    private List<IsoMappingFields> requestFields;
    private List<DataSharingFields> sharingFields;
    private List<IsoFixedFields> fixedFields;
    private List<IsoResponseFields> responseFields;
}
