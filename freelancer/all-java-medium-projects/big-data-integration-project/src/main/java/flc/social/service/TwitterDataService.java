package flc.social.service;

import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Segnal on 9/21/17.
 */
public class TwitterDataService {
    private static final String CONSUMER_KEY = "RUp3bfE0SRBNG4suYEogyfZ3S";
    private static final String CONSUMER_SECRET = "GBHlfYjAunrLNgMcCY5pvx4BqxHLPVDDFayF4VtjPiLmoE6GHM";
    private static final String ACCESS_TOKEN = "909705148212981761-tfEZDyJmp03iiubisGnDHXXtc4SZQjm";
    private static final String ACCESS_TOKEN_SECRET = "uDcrkj2hqpw5arpH2faccpbYB6lvqngZsWawzCDek4baA";

    private static ConfigurationBuilder cb;

    private static TwitterFactory twitterFactory;

        private static ConfigurationBuilder getConfigurationBuilder() {
            if(cb == null) {
                cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true)
                        .setOAuthConsumerKey(CONSUMER_KEY)
                        .setOAuthConsumerSecret(CONSUMER_SECRET)
                        .setOAuthAccessToken(ACCESS_TOKEN)
                        .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
            }
            return cb;
        }

        public static TwitterFactory getTwitterFactory() {
            if(twitterFactory == null) {
                twitterFactory = new TwitterFactory(getConfigurationBuilder().build());
            }
            return twitterFactory;
        }
}
