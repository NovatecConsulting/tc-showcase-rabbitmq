package rabbitclients;

import java.io.IOException;

public interface ShovelConfig {

    String getSrcProtocol();

    String getDestProtocol();

    String getSrcAddress();

    String getDestAddress();

    void setupShovel() throws IOException;
}
