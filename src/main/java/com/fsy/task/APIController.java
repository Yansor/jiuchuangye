package com.fsy.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fsy.task.domain.ImportUser;
import com.fsy.task.domain.QuestionOption;
import com.fsy.task.domain.enums.AnswerOption;
import com.fsy.task.dto.ResultDTO.*;
import com.fsy.task.selenium.SeleniumUtil;
import com.fsy.task.util.HttpClientUtil;
import com.fsy.task.util.MD5Util;
import lombok.extern.log4j.Log4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class APIController {
    private HashMap<String, String> cookieMap = new HashMap<String, String>();

    private static SeleniumUtil seleniumUtil = new SeleniumUtil();

    public static List<ImportUser> userList ;

    private List<Plan> plans = new ArrayList<Plan>();

    // key planId value List<ExerciseDTO>
    private HashMap<String ,List<ExerciseDTO>> planExerciseMap = new HashMap<String, List<ExerciseDTO>>();


    private String lUserId;

    private String nickName;

    private String schoolToken ;

    private String username;

    private String password;

    private String schoolId;

    private String loginDomain ;

    public APIController(ImportUser user){
        //this.lUserId = user.getUserId();
        this.username = user.getUsername();

        this.password = user.getPassword();

        ImportUser wholeUser = seleniumUtil.getUserInfo(user.getUsername() , user.getPassword());

        this.nickName = wholeUser.getNickName();

        this.password = wholeUser.getPassword();

        this.schoolToken = wholeUser.getSchoolToken();

        //this.schoolId = user.getSchoolId();

        this.loginDomain = wholeUser.getLoginDomain();

        appendSession2CookieMap();

        appendSchoolToken2CookieMap();

        //准备看视频
        preWatch();

        //准备做职业测评
        try {
            preTest();
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }

    private void appendSchoolToken2CookieMap() {

        cookieMap.put(CookieConstant.SCHOOL_TOKEN , this.schoolToken);
    }

    private void preTest() throws ParserException {
        //获取测评主页面
        String resp = getTestMainPage();

        //setUserId
        setUserId(resp);


        //setSchoolId
        setSchoolId(resp);

        Parser parser = Parser.createParser(resp, Charset.defaultCharset().toString());
        //缓冲层 parser解析一次之后，再次解析为空
        NodeList cacheNodeList = parser.parse(new NodeFilter() {
            public boolean accept(Node node) {
                return true;
            }
        });
        //startevaluation.htm
        NodeFilter testFilter = new NodeFilter() {
            public boolean accept(Node node) {
                if (node instanceof LinkTag
                        && ((LinkTag) node).getAttribute("href") != null
                        && ((LinkTag) node).getAttribute("href").contains("startevaluation.htm")
                 )
                    return true;
                else return false;
            }
        };
        NodeList thatMatch = cacheNodeList.extractAllNodesThatMatch(testFilter);
        if(thatMatch != null && thatMatch.size()>0) {
            for (int matchIndex = 0; matchIndex < thatMatch.size(); matchIndex++) {
                Node[] linkNode = thatMatch.toNodeArray();
                if (linkNode[matchIndex] instanceof LinkTag) {
                    LinkTag linkTags = (LinkTag) linkNode[matchIndex];
                    //测评ID startevaluation.htm?id=25
                    String testId = linkTags.getAttribute("href");
                    String testIdPage = doTestId(testId);
                    List<QuestionOption> questionIds = getQuestionIds( testIdPage);
                    //String questionOptionCount = questionIds.remove(questionIds.size() -1 );
                    publishTestEvent(schoolId, lUserId, this.nickName, testId, questionIds);
                    System.out.println(this.nickName + "  测评" + testId + " 通过");
                }
            }
        }
        return;
    }

    private void setUserId(String resp) {
        String userIdPattern = "arr[\"lUserId\"]=";
        int userIndexStart = resp.indexOf(userIdPattern);
        int userIndexEnd = resp.indexOf(";"  , userIndexStart);
        this.lUserId = resp.substring(userIndexStart +userIdPattern.length()  , userIndexEnd  ) ;
    }

    private void setSchoolId(String resp) {
        String userIdPattern = "arr[\"lSchoolId\"]=";
        int schoolIndexStart = resp.indexOf(userIdPattern);
        int schoolIndexEnd = resp.indexOf(";"  , schoolIndexStart);
        this.schoolId = resp.substring(schoolIndexStart  +userIdPattern.length()  , schoolIndexEnd  ) ;
    }

    private List<QuestionOption> getQuestionIds( String testIdPage) {

        List<QuestionOption> questionIds = new ArrayList<QuestionOption>();


        Parser parser = Parser.createParser(testIdPage, Charset.defaultCharset().toString());
        //缓冲层 parser解析一次之后，再次解析为空
        NodeList cacheNodeList = null;
        try {
            cacheNodeList = parser.parse(new NodeFilter() {
                public boolean accept(Node node) {
                    return true;
                }
            });
        } catch (ParserException e) {
            e.printStackTrace();
        }
        //startevaluation.htm
        NodeFilter questionIdFilter = new NodeFilter() {
            public boolean accept(Node node) {
                if (node instanceof BulletList
                        && ((BulletList) node).getAttribute("class") != null
                        && ((BulletList) node).getAttribute("class").equals("answer")
                        && ((BulletList) node).getAttribute("questionId") != null
                        )
                    return true;
                else return false;
            }
        };



        NodeList thatMatch = cacheNodeList.extractAllNodesThatMatch(questionIdFilter);
        if(thatMatch != null && thatMatch.size()>0) {
            for (int matchIndex = 0; matchIndex < thatMatch.size(); matchIndex++) {
                Node[] questionNode = thatMatch.toNodeArray();
                if (questionNode[matchIndex] instanceof BulletList) {
                    BulletList questionIdNode = (BulletList) questionNode[matchIndex];
                    int questionOptionCount = questionIdNode.getChildCount();
                    String questionId = questionIdNode.getAttribute("questionId");
                    questionIds.add(
                            QuestionOption.builder()
                            .questionId(questionId)
                            .questionOptionCount(questionOptionCount + "")
                            .build()
                    );
                }
            }
        }
        return questionIds;

    }

    /**
     *
     * @param schoolId
     * @param userId
     * @param nickName
     * @param testId
     * @param options 选项的个数 影响答题 该题目的id
     */
    private void publishTestEvent(String schoolId , String userId , String nickName , String testId ,List<QuestionOption> options){
        String url = "http://sdnu.wnssedu.com/student/tc/careerPlaning/handleTrans.cdo?strServiceName=EvalutionService&strTransName=addEvaluationResult";
        String cookie = getCookie();

        HashMap postParam = new HashMap();
        StringBuffer postValue = new StringBuffer();
        String originalId = testId.split("=")[1];
        postValue.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<CDO>\n" +
                "  <STRF N=\"strServiceName\" V=\"EvalutionService\"/>\n" +
                "  <STRF N=\"strTransName\" V=\"addEvaluationResult\"/>\n" +
                "  <LF N=\"lSchoolId\" V=\""+schoolId+"\"/>\n" +
                "  <LF N=\"lUserId\" V=\""+userId+"\"/>\n" +
                "  <STRF N=\"strUserName\" V=\""+nickName+"\"/>\n" +
                "  <LF N=\"lEvaluationId\" V=\""+originalId+"\"/>\n" +
                "  <STRF N=\"strAnswer\" V=\"{answers:[\n");





        StringBuffer wrapValue = new StringBuffer();
        for(int i = 0 ; i <options.size() ; i++)
        {

            QuestionOption currentOption = options.get(i);
            String option = null;
            String answer = null;
            if(currentOption.getQuestionOptionCount().equals("2") ){
                option = "A,B";
                answer = "1,0";
            }else if(currentOption.getQuestionOptionCount().equals("5")){
                option = "A,B,C,D,E";
                answer = "1,0,0,0,0";
            }else if(currentOption.getQuestionOptionCount().equals("6")){
                option = "A,B,C,D,E,F";
                answer = "1,0,0,0,0,0";
            }else if(currentOption.getQuestionOptionCount().equals("8")){
                option = "A,B,C,D,E,F,G,H";
                answer = "1,0,0,0,0,0,0,0";
            }else{
                System.out.print("测评时，目前仅支持有2,5,6,8个项，请通知管理员 ！" + currentOption.getQuestionOptionCount());
            }

            //{index:"1",lQuestionId:"2005",type:"0",options:"A,B,C,D,E",answer:"1,0,0,0,0",checkOptions:"A",checkAnswers:"1"}
            wrapValue.append("{index:\""+(i+1)+"\",lQuestionId:\""+currentOption.getQuestionId()+"\",type:\"0\",options:\""+option+"\",answer:\""+answer+"\",checkOptions:\"A\",checkAnswers:\"1\"}" + ",");
        }
        //" to &quot;
        wrapValue = new StringBuffer(wrapValue.toString().replaceAll("\"" , "&quot;"));
        postValue.append(wrapValue.toString());

        postValue.deleteCharAt(postValue.length() -1 );
        postValue.append("]}\"/>\n" +
                "  <STRF N=\"strToken\" V=\"\"/>\n" +
                "</CDO>\n");

        postParam.put("$$CDORequest$$" , postValue );
        HttpClientUtil.postResByUrlAndCookie(url , cookie , postParam , false  );

    }

    private String doTestId(String testId) {
        String url = "http://sdnu.wnssedu.com/student/tc/careerPlaning/"+testId;
        String cookie = getCookie();
        HashMap<String,String> headerParam = new HashMap<String, String>();
        headerParam.put("Referer", "http://sdnu.wnssedu.com/student/tc/careerPlaning/evaluationlist.htm");
        String respStr = HttpClientUtil.getResByUrlAndCookie(url , headerParam , cookie  , false);
        return respStr;
    }

    private String getTestMainPage(){

        String url = "http://sdnu.wnssedu.com/student/tc/careerPlaning/evaluationlist.htm";
        String cookie = getCookie();
        HashMap<String,String> headerParam = new HashMap<String, String>();
        headerParam.put("Referer", "http://sdnu.wnssedu.com/student/tc/careerPlaning/evaluationlist.htm");
        String respStr = HttpClientUtil.getResByUrlAndCookie(url , headerParam , cookie  , false);
        return respStr;
    }


    private void preWatch()  {
        //需要登录
        System.out.println(username + "  开始看视频：");
        getTeachPlans(getCookie());
        String blankStyle = "      ";
        if (plans != null && plans.size() > 0) {
            for (Plan plan : plans) {
                System.out.println(blankStyle + plan.getStrPlanName());
                if (plan.getStrState().equals("进行中")) {
                    //http://sdnu.wnssedu.com/student/rest/v1/study/getPlanCourseList  lPlanId=2400000001

                    List<ExerciseDTO> exercises = getExerciseList(plan.getLPlanId());

                    long startTimeMills = 0l;
                    float hasCompleteExercise = 0.0f;
                    List<ExerciseDTO> exerciseDTOS = this.planExerciseMap.get(plan.getLPlanId());

                    //TODO JDK 8
                    if (exerciseDTOS != null && exerciseDTOS.size() > 0) {
                        startTimeMills = System.currentTimeMillis();
                        for (ExerciseDTO exerciseDTO : exerciseDTOS) {
                            String strName = exerciseDTO.getStrName();
                            System.out.println(blankStyle + blankStyle + " " + strName);
                            List<SectionDTO> cdoSectionList = exerciseDTO.getCdoSectionList();
                            if (cdoSectionList != null && cdoSectionList.size() > 0) {
                                for (SectionDTO sectionDTO : cdoSectionList) {
                                    hasCompleteExercise++;
                                    if (sectionDTO.getNViewTimeLength() != sectionDTO.getNTimeLength()) {
                                        //进行中的任务 自动看视频
                                        int needCount = sectionDTO.getNTimeLength() - sectionDTO.getNViewTimeLength();
                                        for (int count = 0; count < needCount; count++) {
                                            this.watchVideo(sectionDTO.getLCoursewareId());
                                            System.out.print(".");
                                            //解决看视频太慢的问题
                                            //        Thread.sleep(30000);
                                            //支持秒刷的业务逻辑
                                        }
                                    }
                                    else {
                                        System.out.println(blankStyle + blankStyle + blankStyle + " " + sectionDTO.getStrName() + "已完成 ， 已自动跳过。");
                                    }
                                }
                            }
                        }
                    }

                    //服务器30秒内只允许请求一次看视频接口 , 故在此准备所有的请求视频的session 下一次全部请求
                    utilNextRequest30s(startTimeMills);
                    //支持秒刷的业务
                    List<ExerciseDTO> exerciseDTOS2 = this.planExerciseMap.get(plan.getLPlanId());
                    if (exerciseDTOS2 != null && exerciseDTOS2.size() > 0) {
                        for (ExerciseDTO exerciseDTO : exerciseDTOS2) {
                            String strName = exerciseDTO.getStrName();
                            System.out.println(blankStyle + blankStyle + " " + strName);
                            List<SectionDTO> cdoSectionList = exerciseDTO.getCdoSectionList();
                            if (cdoSectionList != null && cdoSectionList.size() > 0) {
                                for (SectionDTO sectionDTO : cdoSectionList) {
                                        int needCount = sectionDTO.getNTimeLength() - sectionDTO.getNViewTimeLength();
                                        if(needCount <=0){
                                            System.out.println(sectionDTO.getStrName() +" 已完成 ， 跳过。。");
                                            continue;
                                        }
                                        String courseId = sectionDTO.getLCoursewareId();
                                        for (int count = 0; count < needCount; count++) {
                                            //看视频接口
                                            doWatch(courseId);
                                            System.out.print(".");
                                        }
                                        hasCompleteExercise++;
                                        System.out.println(blankStyle + blankStyle + blankStyle + sectionDTO.getStrName() + hasCompleteExercise / cdoSectionList.size() * 100 + "%");

                                }
                            }
                        }

                    } else {
                        System.out.println("看视频 " + plan.getStrPlanName() + " 已完成 ，自动跳过。");
                    }
                }
            }
        }
    }

    private void doWatch(String courseId) {

        String tempUrl = "http://course.wnssedu.com/Servlet/recordStudy.svl?lCoursewareId=" + courseId + "&strStartTime=0";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httPost3 = new HttpPost(tempUrl);
        //lCoursewareId=227
        //strStartTime=0
        httPost3.setEntity(new StringEntity("lCoursewareId=" + courseId + "&strStartTime=0", Charset.defaultCharset()));
        //过滤出已完成的
        httPost3.setHeader(new BasicHeader(CookieConstant.COOKIE, getCookie()));

        try {
            CloseableHttpResponse response3 = httpclient.execute(httPost3);
            HttpEntity tempEntity = response3.getEntity();
            String respStrr = EntityUtils.toString(tempEntity, Charset.defaultCharset());

            Header[] cookies1 = response3.getHeaders("Set-Cookie");
            StringBuffer cookieStr3 = new StringBuffer();
            if (cookies1 != null && cookies1.length != 0) {
                for (Header cookHeader : cookies1) {
                    cookieStr3.append(cookHeader.getValue() + ";");
                }
            }
            rebuildCookieMap(cookieStr3.toString());
            response3.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void utilNextRequest30s(long startTimeMills) {

        long endTimeMills = System.currentTimeMillis();
        long periodTime = endTimeMills - startTimeMills;
        long periodTimeSecond = periodTime / 1000;
        if (periodTimeSecond < 30) {
            try {
                Thread.sleep((30 - periodTimeSecond) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<ExerciseDTO> getExerciseList(String planId) {
        List<ExerciseDTO> exercises = new ArrayList<ExerciseDTO>();
        String url = "http://sdnu.wnssedu.com/student/rest/v1/study/getPlanCourseList";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httPost = new HttpPost(url);
        httPost.setEntity(new StringEntity("{\n" +
                "\t\"lPlanId\":\"" +  planId + "\"\n" +
                "}", Charset.defaultCharset()));
        //过滤出已完成的
        httPost.setHeader(new BasicHeader(CookieConstant.COOKIE, getCookie()));
        httPost.addHeader(new BasicHeader("Content-Type", "application/json"));
        CloseableHttpResponse response2 = null;
        try {
            response2 = httpclient.execute(httPost);
            Header[] cookies = response2.getHeaders("Set-Cookie");
            StringBuffer cookieStr = new StringBuffer();
            if (cookies != null && cookies.length != 0) {
                for (Header cookHeader : cookies) {
                    cookieStr.append(cookHeader.getValue() + ";");
                }
            }
            rebuildCookieMap(cookieStr.toString());
            HttpEntity entity2 = response2.getEntity();
            String respStr = EntityUtils.toString(entity2, Charset.defaultCharset());
            if (respStr != null && respStr.length() > 0) {
                JSONArray courseArray = JSONObject.parseObject(respStr).getJSONObject("response").getJSONArray("cdoCourseList");
                if (courseArray != null && courseArray.size() > 0) {
                    for (int index = 0; index < courseArray.size(); index++) {
                        JSONObject courseJson = courseArray.getJSONObject(index);
                        ExerciseDTO exercise = courseJson.toJavaObject(ExerciseDTO.class);
                        exercises.add(exercise);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exercises;

    }

    public void appendSession2CookieMap() {
        String loginUrl = "http://sdnu.wnssedu.com/modal/handleTrans.cdo?strServiceName=UserService&strTransName=SSOLogin";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("$$CDORequest$$", buildLoginParam(this.username , this.password));
        String respAndCookie = HttpClientUtil.postResByUrlAndCookie(loginUrl, getCookie(), params, true);
        try{
            String cookie = respAndCookie.split("#")[1];
            rebuildCookieMap(cookie);
        }catch (IndexOutOfBoundsException e){
            System.out.println(username +" 密码错误");
            throw new IllegalArgumentException("");
        }

    }

    private String buildLoginParam(String username , String password ) {
        String schooldId0 = null;
        if(this.schoolId == null ){
            schooldId0 = "0";
        }else{
            schooldId0 = this.schoolId;
        }
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<CDO>\n" +
                "  <STRF N=\"strServiceName\" V=\"UserService\"/>\n" +
                "  <STRF N=\"strLoginId\" V=\"" + username + "\"/>\n" +
                "  <STRF N=\"strTransName\" V=\"SSOLogin\"/>\n" +
                "  <STRF N=\"strPassword\" V=\"" + MD5Util.MD5Encode(password, Charset.defaultCharset().toString()) + "\"/>\n" +
                "  <STRF N=\"strVerifyCode\" V=\"\"/>\n" +
                "  <STRF N=\"bIsCookieLogin\" V=\"change\"/>\n" +
                "  <STRF N=\"Sessioncheck\" V=\"sessionErr\"/>\n" +
                "  <LF N=\"lSchoolId\" V=\""+schooldId0+"\"/>\n" +
                "  <LF N=\"lEduId\" V=\"0\"/>\n" +
                "</CDO>\n";
    }

    public void getTeachPlans(String cookie) {
        String teachPlanUrl = "http://sdnu.wnssedu.com/student/rest/v1/study/getTeachPlanList";
        String respStr = HttpClientUtil.getResByUrlAndCookie(teachPlanUrl,  null , cookie, false);
        JSONObject jsonObject = JSONObject.parseObject(respStr);
        JSONArray cdosPlanList = jsonObject.getJSONObject("response").getJSONArray("cdosPlanList");
        List<Plan> plans = new ArrayList<Plan>();
        if(cdosPlanList != null && cdosPlanList.size() >0){
            for(int index = 0 ; index< cdosPlanList.size() ; index++ ){
                JSONObject planJson = cdosPlanList.getJSONObject(index);
                Plan plan = planJson.toJavaObject(Plan.class);
                plans.add(plan);
            }
        }
        this.plans = plans;
    }

    /**
     * 后台看视频 进度 +2 系统需要30s的延迟接口请求的时间 30s请求两次 返回超时
     *
     * @param courseId http://course.njcedu.com/Servlet/recordStudy.svl?lCourseId=1286&lSchoolId=539&strStartTime=0

     **/
    public void watchVideo(String courseId)  {
        //数据准备
        getCourseWareCookie(courseId);
        //解决看视频太慢的问题
//        Thread.sleep(30000);
        //支持秒刷的业务逻辑 将代码迁移出去
//        String url = "http://course.njcedu.com/Servlet/recordStudy.svl?lCourseId=" + courseId +
//                "&lSchoolId=" + this.schoolCodeMap.get(this.loginDomain) + "&strStartTime=0";
//        HttpClientUtil.getResByUrlAndCookie(url, null , getCookieByMap(cookieMap), false);

        return;

    }

    public String getCookieByMap(HashMap<String, String> cookieMap) {
        StringBuffer cookieStr = new StringBuffer();
        if (cookieMap != null && cookieMap.size() != 0) {
            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                cookieStr.append(key + "=" + value + ";");
            }
        }
        return cookieStr.toString();
    }

    public String setSchoolToken(String schoolToken){
            cookieMap.put(CookieConstant.SCHOOL_TOKEN , schoolToken);
            return null;
    }

    public String getCookie(){
        return this.getCookieByMap(this.cookieMap);
    }

    public void getCourseWareCookie(String courseId) {
        String url = "http://course.wnssedu.com/rest/v1/coursestudy/setCourseCookie";
        HashMap<String,String> valueMap = new HashMap<String, String>();
        valueMap.put("lCoursewareId" , courseId);
        String respAndCookie = HttpClientUtil.postResByUrlAndCookie(url , getCookie() , valueMap , true );

        if(respAndCookie.split("#").length >=2){
            rebuildCookieMap(respAndCookie.split("#")[1]);
        }
    }

    private void rebuildCookieMap(String cookie) {
        //remove Path=/ HttpOnly Domain=njcedu.com
        cookie = cookie.replaceAll("Path=/", "")
                .replaceAll("HttpOnly", "")
                .replaceAll("Domain=njcedu.com", "")
                .replaceAll("Domain=wnssedu.com" , "")
                .replaceAll(" Domain","")
                .replaceAll(";;;", ";;")
                .replaceAll(";;", ";")
                .replaceAll(";", "")
                .replaceAll("  ", ";");
        String[] nameValuePair = cookie.split(";");
        if (nameValuePair != null && nameValuePair.length > 0) {
            for (String nameValue : nameValuePair) {
                if(nameValue.contains("cpwd")){
                    continue;
                }
                if (nameValue != null && nameValue.length() > 0) {
                    //luserid=44670010423
                    String key = nameValue.split("=")[0].trim();
                    String value = nameValue.split("=")[1];
                    cookieMap.put(key, value);
                }

            }
        }
        //remove cpwd key value
        if(cookieMap.containsKey("cpwd")){
            cookieMap.remove("cpwd");
        }

        if(cookieMap.get("JSESSIONID") != null
                && cookieMap.get("JSESSIONID").contains(" ")){
            String splitedJession = cookieMap.get("JSESSIONID").split(" ")[0];
            cookieMap.put("JSESSIONID" , splitedJession);
        }
    }

}

class CookieConstant {
    public static String COOKIE = "Cookie";
    public static String SCHOOL_TOKEN = "schoolToken";

}




