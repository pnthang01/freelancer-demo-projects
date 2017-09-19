package flc.social.service;

import com.restfb.*;
import com.restfb.types.Post;

/**
 * Created by thangpham on 14/09/2017.
 */
public class FacebookDataService {

//    private static final String MY_ACCESS_TOKEN = "EAACEdEose0cBAJktliBNB2GJhlawZAcHPcyMuO2ILGFBLuMcpw5ZCMISrqaMsLT0jAmJq15QJZA7F8lZCVrZAJU0iemRmTBxizmsNxyhdgWi2KFwk1e2ZCbh00U7HvZBrY05vMlGssXSgjXYcc4MTROVARxn9LL33ZApCPrIjiJ67YVPIQ8IliApcFtpL2MonW02SwveZCMjAawZDZDEAACEdEose0cBAJktliBNB2GJhlawZAcHPcyMuO2ILGFBLuMcpw5ZCMISrqaMsLT0jAmJq15QJZA7F8lZCVrZAJU0iemRmTBxizmsNxyhdgWi2KFwk1e2ZCbh00U7HvZBrY05vMlGssXSgjXYcc4MTROVARxn9LL33ZApCPrIjiJ67YVPIQ8IliApcFtpL2MonW02SwveZCMjAawZDZD";
    private static final String MY_ACCESS_TOKEN = "EAACEdEose0cBAI8ZC15Yab98PvBO0pZBzep7ehHWOa3qSc56YZBhanENuRhBSkDIix9ZCgkzwGI61Bt4psXByokQsVJRne67LpfbDaLkeHT0UFeuk08ZBcftouc0nao8vjC4tJ8ZAC2NK96laXtzXAZBlOo39tBMNspv16UUZAKeNzedbXt1LZBHB4fZB2mVHbOQmZCXrd8nI3g5wZDZD";

    public static FacebookClient getFacebookClient() {
        return new DefaultFacebookClient(MY_ACCESS_TOKEN, Version.LATEST);
    }

    public static void main(String[] args) {
        FacebookClient facebookClient = getFacebookClient();
        Connection<Post> bbcPage = facebookClient.fetchConnection("bbcnews/posts", Post.class);
        System.out.println(bbcPage.getData().size());
        System.out.println(bbcPage.getData().get(0).getComments().getData().get(0));
        System.out.println(" d" + bbcPage.getNextPageUrl());
        bbcPage = facebookClient.fetchConnectionPage(bbcPage.getNextPageUrl(), Post.class);

        System.out.println(bbcPage.getData().size());
        System.out.println(bbcPage.getData().get(0).getComments().getData().get(0));
//        bbcPage.getNextPageUrl()
//
//        int personalLimit = 50;
//
//        for (List<Comment> commentPage : commentConnection) {
//            for (Comment comment : commentPage) {
//                out.println("Id: " + comment.getId());
//                personalLimit--;
//
//                // break both loops
//                if (personalLimit == 0) {
//                    return;
//                }
//            }
//        }
//
//        // Some Post from the GoT Fanpage with likes and comments total count
//        Post post = facebookClient.fetchObject("74133697733_10152424266332734",
//                Post.class,
//                Parameter.with("fields", "from,to,comments.limit(100).summary(true)"));
//
//        for (Comment cmt : post.getComments().getData()) {
////            System.out.println(cmt.getMessage());
////            System.out.println(cmt.getCreatedTime());
////            System.out.println(cmt.getParent());
////            System.out.println("=====================");
//        }
//        System.out.println("Likes count: " + post.getLikesCount());
//        System.out.println("Shares count: " + post.getSharesCount());
//        System.out.println("Comments count: " + post.getCommentsCount());
    }
}
