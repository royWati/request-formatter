package chainbox.io.esbformatter.dao.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionManager {
    private String host;
    private String username;
    private String password;
    private String databaseName;
    private String databaseAlias;
    private String databaseType;
}
