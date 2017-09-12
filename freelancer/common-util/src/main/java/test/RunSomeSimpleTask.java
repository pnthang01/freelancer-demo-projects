package test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thangpham on 08/09/2017.
 */
public class RunSomeSimpleTask {

    public static void main(String[] args) throws IOException {
        String dim = "\t";
        File sectionFile = new File("/home/thangpham/Downloads/Aud_Metadata/Aud_Sections.txt");
        File websiteFile = new File("/home/thangpham/Downloads/Aud_Metadata/Aud_Websites.txt");
        List<String> sectionList = FileUtils.readLines(sectionFile);
        List<String> websiteList = FileUtils.readLines(websiteFile);
        Map<String, SectionModel> sectionMap = new HashMap();
        int i = 0;
        for(String sec : sectionList) {
            if(i++ == 0) continue;
            String[] tmp = sec.split("\t");
            String key = tmp[0] + ":" + tmp[1];
            SectionModel model = new SectionModel();
            model.siteId = tmp[0];
            model.sectionId = tmp[1];
            model.sectionName = tmp[2];
            sectionMap.put(key, model);
        }
        Map<String, WebsiteModel> siteMap = new HashMap();
        i = 0;
        for(String site: websiteList) {
            if(i++ == 0) continue;
            String[] tmp = site.split("\t");
            WebsiteModel model = new WebsiteModel();
            model.siteId = tmp[0];
            model.siteName = tmp[1];
            siteMap.put(tmp[0], model);
        }
        //
        File fullageFile = new File("/home/thangpham/Downloads/Aud_Metadata/fullage.tsv");
        File unknownFile = new File("/home/thangpham/Downloads/Aud_Metadata/unknown.tsv");
        List<String> fullageList = FileUtils.readLines(fullageFile);
        List<String> unknownList = FileUtils.readLines(unknownFile);
        i = 0;
        StringBuilder sb = new StringBuilder("site_id\tsite_name\tcate\tcate_name\ttotal_pageviews\ttotal_users\trange1_pageviews\trange1_users\trange1_rate\trange2_pageviews\trange2_users\trange2_rate\trange3_pageviews\trange3_users\trange3_rate\trange4_pageviews\trange4_users\trange4_rate\tunknown_pageviews\tunknown_users\tunknown_rate\n");
        for(String faline : fullageList) {
            if(i++ == 0) continue;
            String[] tmp = faline.replaceAll("\"","").split("\t");
            String siteName = siteMap.get(tmp[0]).siteName;
            SectionModel sectionModel = sectionMap.get(tmp[0] + ":" + tmp[1]);
            String sectionName = null == sectionModel ? "" : sectionModel.sectionName;
            sb.append(tmp[0]).append(dim).append(siteName).append(dim).append(tmp[1]).append(dim).append(sectionName);
            for(int j = 2 ; j < tmp.length; j++) {
                sb.append(dim).append(tmp[j]);
            }
            sb.append("\n");
        }
        FileUtils.write(new File("/home/thangpham/Downloads/Aud_Metadata/fullage_new.txt"), sb.toString());
        sb = new StringBuilder("site_id\tsection\ttotal_pageviews\ttotal_users\tunknown_pageviews\tunknown_users\tuser_rate\n");
        i = 0;
        for(String ualine : unknownList) {
            if(i++ == 0) continue;
            String[] tmp = ualine.replaceAll("\"","").split("\t");
            String siteName = siteMap.get(tmp[0]).siteName;
            SectionModel sectionModel = sectionMap.get(tmp[0] + ":" + tmp[1]);
            String sectionName = null == sectionModel ? "" : sectionModel.sectionName;
            sb.append(tmp[0]).append(dim).append(siteName).append(dim).append(tmp[1]).append(dim).append(sectionName);
            for(int j = 2 ; j < tmp.length; j++) {
                sb.append(dim).append(tmp[j]);
            }
            sb.append("\n");
        }
        FileUtils.write(new File("/home/thangpham/Downloads/Aud_Metadata/unknownage_new.txt"), sb.toString());
    }

    static class WebsiteModel {
        String siteId;
        String siteName;
    }

    static class SectionModel {
        String siteId;
        String sectionId;
        String sectionName;
    }
}
