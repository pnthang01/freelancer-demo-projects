package flc.social;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by thangpham on 11/09/2017.
 */
public class ApplicationStarter {

    static final Logger LOGGER = LogManager.getLogger(ApplicationStarter.class);

    public static void main(String[] args) {
        if(args.length < 1) {
            LOGGER.error("Parameters are missing, please check again.");
        }
    }


}
