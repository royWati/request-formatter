package chainbox.io.esbformatter.dao.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredProcedures {

    private String procedureName;
    private List<StoredProceduresParameters> parameters;

}
