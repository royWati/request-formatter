package chainbox.io.esbformatter.dao.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredProceduresParameters {

    private String name;
    private String dataType;
    private String position;
}
