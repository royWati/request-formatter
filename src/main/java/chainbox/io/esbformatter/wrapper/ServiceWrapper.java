package chainbox.io.esbformatter.wrapper;

import chainbox.io.esbformatter.dao.json.JsonConfigurationTemplate;
import chainbox.io.esbformatter.dao.universal.EsbService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceWrapper {
    private EsbService service;
    private JsonConfigurationTemplate template;
}
