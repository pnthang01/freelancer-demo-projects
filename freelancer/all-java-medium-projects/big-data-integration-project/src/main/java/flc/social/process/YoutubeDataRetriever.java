package flc.social.process;

import flc.social.ApplicationStarter;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by thangpham on 12/09/2017.
 */
public class YoutubeDataRetriever extends AbstractProcess {


    static final Logger LOGGER = LogManager.getLogger(YoutubeDataRetriever.class);

    public YoutubeDataRetriever() throws ConfigurationException {
    }

    public List<String> readDataSource() {
        return null;
    }
}
