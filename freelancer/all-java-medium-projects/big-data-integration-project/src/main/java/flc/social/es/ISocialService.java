package flc.social.es;

import flc.social.models.SocialModel;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by dientt on 9/14/17.
 */
public interface ISocialService {
    public void putData(List<SocialModel> socialModels) throws UnknownHostException;
}
