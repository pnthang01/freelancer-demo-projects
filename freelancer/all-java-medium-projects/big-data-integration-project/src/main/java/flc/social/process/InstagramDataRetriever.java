package flc.social.process;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by thangpham on 12/09/2017.
 */
public class InstagramDataRetriever extends AbstractProcess{

    static final Logger LOGGER = LogManager.getLogger(InstagramDataRetriever.class);

    public InstagramDataRetriever() throws ConfigurationException {
    }

    public List<String> readDataSource() {
        return null;
    }
}
