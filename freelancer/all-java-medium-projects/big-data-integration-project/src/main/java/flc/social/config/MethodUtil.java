package flc.social.config;

import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * Created by thangpham on 16/11/2017.
 */
public class MethodUtil {

    private static ObjectMapper om = new ObjectMapper();
    private static Gson gson = new Gson();

    public static long getObjectSize(Object o) {
        return gson.toJson(o).getBytes().length;
    }

}
