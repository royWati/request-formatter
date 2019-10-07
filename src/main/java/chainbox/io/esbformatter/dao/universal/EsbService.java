package chainbox.io.esbformatter.dao.universal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EsbService {
    private String serviceName;
    private String serverUrl;
    private String templateType;
    private String methodType;
}
