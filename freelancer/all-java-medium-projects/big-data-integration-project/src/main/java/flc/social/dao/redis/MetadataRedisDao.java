package flc.social.dao.redis;

import com.ants.common.config.RedisInfoConfiguration;
import com.ants.common.model.RedisCommand;
import com.ants.common.model.RedisInfo;
import com.ants.common.util.DateTimeUtil;
import com.google.api.client.util.DateTime;
import flc.social.config.ConfigurationUtil;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;

/**
 * Created by Segnal on 12/09/2017.
 */
public class MetadataRedisDao {

    private static Logger LOGGER = LogManager.getLogger(MetadataRedisDao.class);

    private static MetadataRedisDao _instance;
    private RedisInfo metadataRedis = null;

    public MetadataRedisDao() throws ConfigurationException, IOException {
        metadataRedis = RedisInfoConfiguration.load().getSingleRedisInfo(ConfigurationUtil.RedisInfo.METADATA_INFO_REDIS);
    }

    /**
     * Load this Redis data access class. If null, initialize new one
     * @return
     * @throws ConfigurationException
     * @throws IOException
     */
    public synchronized static MetadataRedisDao load() throws ConfigurationException, IOException {
        if (null == _instance) _instance = new MetadataRedisDao();
        return _instance;
    }

    /**
     * Get the jobName whether is stopped
     * @param jobName
     * @return
     */
    public boolean checkStopJob(final String jobName) {
        return (new RedisCommand<Boolean>(metadataRedis.getShardedJedisPool()) {
            @Override
            protected Boolean build() throws JedisException {
                return Boolean.valueOf(jedis.hget(jobName, "stop_job"));
            }
        }).execute();
    }

    /**
     * Set jobName to false, used for starting process
     * @param jobName
     */
    public void setStopJobToFalse(final String jobName) {
        (new RedisCommand<Boolean>(metadataRedis.getShardedJedisPool()) {
            @Override
            protected Boolean build() throws JedisException {
                jedis.hset(jobName, "stop_job", "false");
                return true;
            }
        }).execute();
    }

    /**
     * Set last query token for #YoutubeDataRetriever
     * @param regionCode
     * @param token
     */
    public void setPopularToken(final String regionCode, final String token) {
        (new RedisCommand<Integer>(metadataRedis.getShardedJedisPool()) {
            @Override
            public Integer build() throws JedisException {
                String key = "youtube:" + regionCode + "_" +
                        DateTimeUtil.formatDate(System.currentTimeMillis(), DateTimeUtil.DDMMYYYY_DASH);
                jedis.set(key, token);
                return 1;
            }
        }).execute();
    }

    /**
     * Get last query token for #YoutubeDataRetriever
     * @param regionCode
     * @return
     */
    public String getPopularToken(final String regionCode) {
        return (new RedisCommand<String>(metadataRedis.getShardedJedisPool()) {
            @Override
            public String build() throws JedisException {
                String key = "youtube:" + regionCode + "_" +
                        DateTimeUtil.formatDate(System.currentTimeMillis(), DateTimeUtil.DDMMYYYY_DASH);
                return jedis.get(key);
            }
        }).execute();
    }

    /**
     * Get last query id for #FacebookDataRetriever
     * @param pageId
     * @return
     */
    public String getNextPageUrl(final String pageId) {
        return (new RedisCommand<String>(metadataRedis.getShardedJedisPool()) {
            @Override
            public String build() throws JedisException {
                String key = "facebook:" + pageId + "_" +
                        DateTimeUtil.formatDate(System.currentTimeMillis(), DateTimeUtil.DDMMYYYY_DASH);
                return jedis.get(key);
            }
        }).execute();
    }

    /**
     *  Set last query id for #FacebookDataRetriever
     * @param pageId
     * @param nextPageUrl
     */
    public void setNextPageUrl(final String pageId, final String nextPageUrl) {
        (new RedisCommand<Integer>(metadataRedis.getShardedJedisPool()) {
            @Override
            public Integer build() throws JedisException {
                String key = "youtube:" + pageId + "_" +
                        DateTimeUtil.formatDate(System.currentTimeMillis(), DateTimeUtil.DDMMYYYY_DASH);
                jedis.set(key, nextPageUrl);
                return 1;
            }
        }).execute();
    }

    /**
     * Get last query id for #TwitterDataRetriever
     * @param pageId
     * @return
     */
    public String getMaxId(final long pageId) {
        return (new RedisCommand<String>(metadataRedis.getShardedJedisPool()) {
            @Override
            public String build() throws JedisException {
                String key = "twitter:" + pageId;
                return jedis.get(key);
            }
        }).execute();
    }

    /**
     * Set last query id for #TwitterDataRetriever
     * @param pageId
     * @param maxId
     */
    public void setMaxId(final String pageId, final String maxId) {
        (new RedisCommand<Integer>(metadataRedis.getShardedJedisPool()) {
            @Override
            public Integer build() throws JedisException {
                String key = "twitter:" + pageId;
                jedis.set(key, maxId);
                return 1;
            }
        }).execute();
    }


}
