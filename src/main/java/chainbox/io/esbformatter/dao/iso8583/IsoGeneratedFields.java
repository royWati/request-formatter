package chainbox.io.esbformatter.dao.iso8583;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IsoGeneratedFields {
    private String hhmmss_12;
    private String MMDD_13;
    private String MMddHHmmss_37;
}
