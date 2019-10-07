package chainbox.io.esbformatter.dao.iso8583;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IsoFixedFields {
    private int fieldId;
    private String value;
}
