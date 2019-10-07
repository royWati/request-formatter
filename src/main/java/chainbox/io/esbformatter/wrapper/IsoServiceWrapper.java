package chainbox.io.esbformatter.wrapper;

import chainbox.io.esbformatter.dao.iso8583.IsoConfigurationTemplate;
import chainbox.io.esbformatter.dao.universal.EsbService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IsoServiceWrapper {
    private EsbService service;
    private IsoConfigurationTemplate template;
}
