package com.fsy.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fsy.task.config.WebHeader;
import com.fsy.task.domain.User;
import com.fsy.task.domain.QuestionOption;
import com.fsy.task.dto.*;
import com.fsy.task.util.MD5Util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class APIController {
//    private HashMap<String, String> cookieMap = new HashMap<String, String>();

//    private static SeleniumUtil seleniumUtil = new SeleniumUtil();

//    public static List<User> userList ;

    private List<Plan> plans = new ArrayList<Plan>();

    // key planId value List<ExerciseDTO>
//    @Deprecated
//    private HashMap<String ,List<ExerciseDTO>> planExerciseMap = new HashMap<String, List<ExerciseDTO>>();


//    private String lUserId;

//    private String nickName;

//    private String schoolToken ;

    private String username;

    private String password;

//    private String schoolId;

//    @Deprecated
//    private String loginDomain ;

    private List<ExamDto> exams;

    //版本二 修改cookie管理方式 由httpclient的核心cookieStore来维护
    private CookieStore cookieStore = new BasicCookieStore();

    private HttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

    private String baseUrl = "http://sdnu.wnssedu.com";

    private CDO cdo;

    public APIController(String username , String password){
        this.username = username;

        this.password = password;

        String loginXml =  login();

        cdo = parseLoginXml(loginXml);

        //二次授权 获取schoolToken token
        String resp = secondAuth();

        System.out.println( "应该返回 callback({res:1}) ， 二次登录授权 ,实际  " + resp);

        String teachPlanJson = getTeachPlansJson();
        this.plans = parseTeachPlanJson(teachPlanJson);

        //做课程
        doPlanV2();

        //看测评
        doTestV2();

        String examListJson = getExamListJson();

        ExamListDto examListDto = JSONObject.parseObject(examListJson , ExamListDto.class);

        //nStart 2是已结束 1是进行中
        exams = examListDto.getResponse().getCdosUserExaminList()
                .stream().filter((ExamDto examDto)->{return "1".equals(examDto.getNStart());}).collect(Collectors.toList());

        return;
    }

    private String getExamListJson() {
        String url = baseUrl + "/student/rest/v1/study/getExaminList";

        HttpPost post = new HttpPost(url);

        post.addHeader(new BasicHeader(WebHeader.Refer_Key , "http://sdnu.wnssedu.com/student/prese/studycenter.htm"));
        post.addHeader(new BasicHeader(WebHeader.USERAGENT_KEY , WebHeader.USERAGENT_VALUE));
        post.addHeader(new BasicHeader(WebHeader.CONTENTTYPE_KEY , WebHeader.CONTENTTYPE_VALUE));

        try {
            HttpResponse httpResponse = client.execute(post);
            HttpEntity httpEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(httpEntity ,Charset.defaultCharset());
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ExerciseDTO> parseCourseListJson(String planCourseListJson) {
        List<ExerciseDTO> exercises = new ArrayList<>();
        if (!StringUtils.isEmpty(planCourseListJson)) {
            JSONArray courseArray = JSONObject.parseObject(planCourseListJson).getJSONObject("response").getJSONArray("cdoCourseList");
            if (courseArray != null && courseArray.size() > 0) {
                for (int index = 0; index < courseArray.size(); index++) {
                    JSONObject courseJson = courseArray.getJSONObject(index);
                    ExerciseDTO exercise = courseJson.toJavaObject(ExerciseDTO.class);
                    exercises.add(exercise);
                }
            }
        }
        return exercises;
    }

//    private void doPlan() {
//        if (CollectionUtils.isEmpty(plans)) {
//            String blankStyle = "      ";
//            for (Plan plan : plans) {
//                if (plan.getStrState().equals("进行中")) {
//                    System.out.println(blankStyle + plan.getStrPlanName());
//                    //http://sdnu.wnssedu.com/student/rest/v1/study/getPlanCourseList  lPlanId=2400000001
//                    setPlanExerciseMap(plan.getLPlanId());
//                    long startTimeMills = 0l;
//                    List<ExerciseDTO> exerciseDTOS = this.planExerciseMap.get(plan.getLPlanId());
//
//                    //TODO JDK 8
//                    if (exerciseDTOS != null && exerciseDTOS.size() > 0) {
//                        startTimeMills = System.currentTimeMillis();
//                        for (ExerciseDTO exerciseDTO : exerciseDTOS) {
//                            String strName = exerciseDTO.getStrName();
//                            List<SectionDTO> cdoSectionList = exerciseDTO.getCdoSectionList();
//                            if (cdoSectionList != null && cdoSectionList.size() > 0) {
//                                for (SectionDTO sectionDTO : cdoSectionList) {
//                                    if (sectionDTO.getNViewTimeLength() != sectionDTO.getNTimeLength()) {
//                                        //进行中的任务 自动看视频
//                                        int needCount = sectionDTO.getNTimeLength() - sectionDTO.getNViewTimeLength();
//                                        for (int count = 0; count < needCount; count++) {
//                                            this.watchVideo(sectionDTO.getLCoursewareId());
//                                            System.out.print(".");
//                                            //解决看视频太慢的问题
//                                            //        Thread.sleep(30000);
//                                            //支持秒刷的业务逻辑
//                                        }
//                                    }
//                                    else {
//                                        System.out.println(blankStyle + blankStyle + blankStyle + " " + sectionDTO.getStrName() + "已完成 ， 已自动跳过。");
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    //服务器30秒内只允许请求一次看视频接口 , 故在此准备所有的请求视频的session 下一次全部请求
//                    utilNextRequest30s(startTimeMills);
//                    //支持秒刷的业务
//                    List<ExerciseDTO> exerciseDTOS2 = this.planExerciseMap.get(plan.getLPlanId());
//                    if (exerciseDTOS2 != null && exerciseDTOS2.size() > 0) {
//                        for (ExerciseDTO exerciseDTO : exerciseDTOS2) {
//                            String strName = exerciseDTO.getStrName();
//                            System.out.println(blankStyle + blankStyle + " " + strName);
//                            List<SectionDTO> cdoSectionList = exerciseDTO.getCdoSectionList();
//                            if (cdoSectionList != null && cdoSectionList.size() > 0) {
//                                for (SectionDTO sectionDTO : cdoSectionList) {
//                                    int needCount = sectionDTO.getNTimeLength() - sectionDTO.getNViewTimeLength();
//                                    if(needCount <=0){
//                                        System.out.println(sectionDTO.getStrName() +" 已完成 ， 跳过。。");
//                                        continue;
//                                    }
//                                    String courseId = sectionDTO.getLCoursewareId();
//                                    for (int count = 0; count < needCount; count++) {
//                                        //看视频接口
//                                        doWatch(courseId);
//                                        System.out.print(".");
//                                    }
//                                    System.out.println(blankStyle + blankStyle + blankStyle + sectionDTO.getStrName());
//
//                                }
//                            }
//                        }
//
//                    }
//                }else {
//                    System.out.println("看视频 " + plan.getStrPlanName() + " 已完成 ，自动跳过。");
//                }
//            }
//        }
//    }

    private void doPlanV2() {
        if (!CollectionUtils.isEmpty(plans)) {
            String blankStyle = "      ";
            for (Plan plan : plans) {
                if (plan.getStrState().equals("进行中")) {
                    System.out.println(blankStyle + plan.getStrPlanName());
                    //http://sdnu.wnssedu.com/student/rest/v1/study/getPlanCourseList  lPlanId=2400000001
                    String planCourseListJson = getPlanCourseListJson(plan.getLPlanId());
                    List<ExerciseDTO> exerciseDTOS = parseCourseListJson(planCourseListJson);
                    long startTimeMills = 0l;

                    //TODO JDK 8
                    if (!CollectionUtils.isEmpty(exerciseDTOS)) {
                        startTimeMills = System.currentTimeMillis();
                        for (ExerciseDTO exerciseDTO : exerciseDTOS) {
                            String strName = exerciseDTO.getStrName();
                            List<SectionDTO> cdoSectionList = exerciseDTO.getCdoSectionList();
                            if (!CollectionUtils.isEmpty(cdoSectionList)) {
                                for (SectionDTO sectionDTO : cdoSectionList) {
                                    if (sectionDTO.getNViewTimeLength() != sectionDTO.getNTimeLength()) {
                                        //进行中的任务 自动看视频
                                        int needCount = sectionDTO.getNTimeLength() - sectionDTO.getNViewTimeLength();
                                        for (int count = 0; count < needCount; count++) {
                                            watchVideoBefore(sectionDTO.getLCoursewareId());
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
                    List<ExerciseDTO> exerciseDTOS2 = exerciseDTOS;
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
                                        String watchResp = doWatchV2(courseId);
                                        System.out.println(watchResp);
                                        System.out.print(".");
                                    }
                                    System.out.println(blankStyle + blankStyle + blankStyle + sectionDTO.getStrName());

                                }
                            }
                        }

                    }
                }else {
                    System.out.println("看视频 " + plan.getStrPlanName() + " 已完成 ，自动跳过。");
                }
            }
        }
    }

    private List<Plan> parseTeachPlanJson(String teachPlanJson) {
        JSONObject jsonObject = JSONObject.parseObject(teachPlanJson);
        JSONArray cdosPlanList = jsonObject.getJSONObject("response").getJSONArray("cdosPlanList");
        List<Plan> plans = new ArrayList<Plan>();
        if(cdosPlanList != null && cdosPlanList.size() >0){
            for(int index = 0 ; index< cdosPlanList.size() ; index++ ){
                JSONObject planJson = cdosPlanList.getJSONObject(index);
                Plan plan = planJson.toJavaObject(Plan.class);
                plans.add(plan);
            }
        }
        return plans;
    }

    private String secondAuth() {
        //jsonpCallback=callback
        //_=1541580117494
        String secondAuthUrl = cdo.getArrLoginUrl()+"jsonpCallback=callback&_="+System.currentTimeMillis();

        HttpGet get = new HttpGet(secondAuthUrl);

        get.addHeader(new BasicHeader(WebHeader.USERAGENT_KEY, WebHeader.USERAGENT_VALUE));
        get.addHeader(new BasicHeader(WebHeader.Refer_Key , "http://sdnu.wnssedu.com/modal/…Mo.htm?aTargetURL=%2Findex.htm"));

        try {
            HttpResponse httpResponse = client.execute(get);
            HttpEntity httpEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(httpEntity ,Charset.defaultCharset());
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private CDO parseLoginXml(String cdoXmlStr) {
        try {
            Document doc = DocumentHelper.parseText(cdoXmlStr);
            Element root = doc.getRootElement();
            String nCode = ((Element)root.selectSingleNode("//NF[@N='nCode']")).attribute("V").getValue();
            String strText = ((Element)root.selectSingleNode("//STRF[@N='strText']")).attribute("V").getValue();
            String strInfo = ((Element)root.selectSingleNode("//STRF[@N='strInfo']")).attribute("V").getValue();
            String lId = ((Element)root.selectSingleNode("//LF[@N='lId']")).attribute("V").getValue();
            String strLoginId = ((Element)root.selectSingleNode("//STRF[@N='strLoginId']")).attribute("V").getValue();
            String strHash = ((Element)root.selectSingleNode("//STRF[@N='strHash']")).attribute("V").getValue();
            String strName = ((Element)root.selectSingleNode("//STRF[@N='strName']")).attribute("V").getValue();
            String strNickName = ((Element)root.selectSingleNode("//STRF[@N='strNickName']")).attribute("V").getValue();
            String nVerifyStage = ((Element)root.selectSingleNode("//BYF[@N='nVerifyStage']")).attribute("V").getValue();
            String dtLastLoginTime = ((Element)root.selectSingleNode("//DTF[@N='dtLastLoginTime']")).attribute("V").getValue();
            String strHeadURL = ((Element)root.selectSingleNode("//STRF[@N='strHeadURL']")).attribute("V").getValue();
            String bLocked = ((Element)root.selectSingleNode("//BF[@N='bLocked']")).attribute("V").getValue();
            String bIsRegUser = ((Element)root.selectSingleNode("//BF[@N='bIsRegUser']")).attribute("V").getValue();
            String strEmail = ((Element)root.selectSingleNode("//STRF[@N='strEmail']")).attribute("V").getValue();
            String lSchoolId = ((Element)root.selectSingleNode("//LF[@N='lSchoolId']")).attribute("V").getValue();
            String nRecordCount = ((Element)root.selectSingleNode("//NF[@N='nRecordCount']")).attribute("V").getValue();
            String arrLoginUrl = ((Element)root.selectSingleNode("//STR")).getText();
            String nType = ((Element)root.selectSingleNode("//NF[@N='nType']")).attribute("V").getValue();

            CDO cdo  = CDO.builder()
                    .nCode(nCode)
                    .strText(strText)
                    .strInfo(strInfo)
                    .lId(lId)
                    .strLoginId(strLoginId)
                    .strHash(strHash)
                    .strName(strName)
                    .strNickName(strNickName)
                    .nVerifyStage(nVerifyStage)
                    .dtLastLoginTime(dtLastLoginTime)
                    .strHeadURL(strHeadURL)
                    .bLocked(bLocked)
                    .bIsRegUser(bIsRegUser)
                    .strEmail(strEmail)
                    .lSchoolId(lSchoolId)
                    .nRecordCount(nRecordCount)
                    .arrLoginUrl(arrLoginUrl)
                    .nType(nType)
                    .build();
            return cdo;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String login() {
        String loginUrl = baseUrl + "/modal/handleTrans.cdo?strServiceName=UserService&strTransName=SSOLogin";
        HttpPost post = new HttpPost(loginUrl);

        post.addHeader(new BasicHeader(WebHeader.CONTENTTYPE_KEY,WebHeader.CONTENTTYPE_VALUE));

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("$$CDORequest$$" , buildLoginParam(this.username , this.password)));

        post.setEntity(new UrlEncodedFormEntity(nvps , Charset.defaultCharset()));

        try {
            HttpResponse httpResponse = client.execute(post);
            HttpEntity httpEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(httpEntity ,Charset.defaultCharset());
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public APIController(User user){

//        this.username = user.getUsername();
//
//        this.password = user.getPassword();
//
//        User wholeUser = seleniumUtil.getUserInfo(user.getUsername() , user.getPassword());
//
//        this.nickName = wholeUser.getNickName();
//
//        this.password = wholeUser.getPassword();
//
//        this.schoolToken = wholeUser.getSchoolToken();
//
//        //this.schoolId = user.getSchoolId();
//
//        this.loginDomain = wholeUser.getLoginDomain();
//
////        appendSession2CookieMap();
//        login();

//        appendSchoolToken2CookieMap();

        //准备看视频
//        preWatch();
//
//        //准备做职业测评
//        doTest();
    }

//    private void appendSchoolToken2CookieMap() {
//
//        cookieMap.put(CookieConstant.SCHOOL_TOKEN , this.schoolToken);
//    }

//    private void doTest() {
//        try{
//        //获取测评主页面
//        String resp = getTestMainPage();
//
//        //setUserId
////        setUserId(resp);
////
////        //setSchoolId
////        setSchoolId(resp);
//
//        Parser parser = Parser.createParser(resp, Charset.defaultCharset().toString());
//        //缓冲层 parser解析一次之后，再次解析为空
//        NodeList cacheNodeList = parser.parse(new NodeFilter() {
//            public boolean accept(Node node) {
//                return true;
//            }
//        });
//        //startevaluation.htm
//        NodeFilter testFilter = new NodeFilter() {
//            public boolean accept(Node node) {
//                if (node instanceof LinkTag
//                        && ((LinkTag) node).getAttribute("href") != null
//                        && ((LinkTag) node).getAttribute("href").contains("startevaluation.htm")
//                 )
//                    return true;
//                else return false;
//            }
//        };
//        NodeList thatMatch = cacheNodeList.extractAllNodesThatMatch(testFilter);
//        if(thatMatch != null && thatMatch.size()>0) {
//            for (int matchIndex = 0; matchIndex < thatMatch.size(); matchIndex++) {
//                Node[] linkNode = thatMatch.toNodeArray();
//                if (linkNode[matchIndex] instanceof LinkTag) {
//                    LinkTag linkTags = (LinkTag) linkNode[matchIndex];
//                    //测评ID startevaluation.htm?id=25
//                    String testId = linkTags.getAttribute("href");
//                    String testIdPage = doTestId(testId);
//                    List<QuestionOption> questionIds = getQuestionIds( testIdPage);
//                    //String questionOptionCount = questionIds.remove(questionIds.size() -1 );
//                    publishTestEvent(schoolId, lUserId, this.nickName, testId, questionIds);
//                    System.out.println(this.nickName + "  测评" + testId + " 通过");
//                }
//            }
//        }}catch (Exception e){
//            e.printStackTrace();
//        }
//        return;
//    }

    private void doTestV2() {
        try{
            //获取测评主页面
            String resp = getTestMainPage();

            //setUserId
//            setUserId(resp);

            //setSchoolId
//            setSchoolId(resp);

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
                        String testIdPage = doTestIdV2(testId);
                        List<QuestionOption> questionIds = getQuestionIds( testIdPage);
                        //String questionOptionCount = questionIds.remove(questionIds.size() -1 );
                        publishTestEvent(cdo.getLSchoolId(), cdo.getLId(), cdo.getStrName(), testId, questionIds);
                        System.out.println(cdo.getStrName() + "  测评" + testId + " 通过");
                    }
                }
            }}catch (Exception e){
            e.printStackTrace();
        }
        return;
    }

//    private void setUserId(String resp) {
//        String userIdPattern = "arr[\"lUserId\"]=";
//        int userIndexStart = resp.indexOf(userIdPattern);
//        int userIndexEnd = resp.indexOf(";"  , userIndexStart);
//        this.lUserId = resp.substring(userIndexStart +userIdPattern.length()  , userIndexEnd  ) ;
//    }
//
//    private void setSchoolId(String resp) {
//        String userIdPattern = "arr[\"lSchoolId\"]=";
//        int schoolIndexStart = resp.indexOf(userIdPattern);
//        int schoolIndexEnd = resp.indexOf(";"  , schoolIndexStart);
//        this.schoolId = resp.substring(schoolIndexStart  +userIdPattern.length()  , schoolIndexEnd  ) ;
//    }

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
                        && ((BulletList) node).getAttribute("class").contains("nswer")
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
                    int questionOptionCount = (int) Arrays.asList(questionIdNode.getChildren().toNodeArray()).stream()
                            .filter((Node node)->{
                                return node instanceof Bullet;
                            }).count();
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
    private String publishTestEvent(String schoolId , String userId , String nickName , String testId ,List<QuestionOption> options){
        if(options == null || options.size()==0){
            System.out.println("获取该测评id选项失败:"+ testId  + "\n请联系管理员排除bug");
            throw new IllegalArgumentException("获取该测评id选项失败:"+ testId );
        }

        String url = "http://sdnu.wnssedu.com/student/tc/careerPlaning/handleTrans.cdo?strServiceName=EvalutionService&strTransName=addEvaluationResult";
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
            }else if(currentOption.getQuestionOptionCount().equals("3")){
                option = "A,B,C";
                answer = "1,0,0";
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
        HttpPost post = new HttpPost(url);
        post.addHeader(new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8"));
        post.addHeader(new BasicHeader("Referer","http://sdnu.wnssedu.com"));

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("$$CDORequest$$" , postValue.toString()));
        post.setEntity(new UrlEncodedFormEntity(nvps ,Charset.forName("UTF-8")));
        try {
            HttpResponse httpResponse = client.execute(post);
            HttpEntity tempEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(tempEntity, Charset.defaultCharset());

            return respStr;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private String doTestIdV2(String testId) {
        String url = baseUrl + "/student/tc/careerPlaning/"+testId;
        HttpGet get = new HttpGet(url);
        get.addHeader(new BasicHeader("Referer", "http://sdnu.wnssedu.com/student/tc/careerPlaning/evaluationlist.htm"));
        try {
            HttpResponse httpResponse = client.execute(get);
            HttpEntity httpEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(httpEntity ,Charset.defaultCharset());
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
//    private String doTestId(String testId) {
//        String url = "http://sdnu.wnssedu.com/student/tc/careerPlaning/"+testId;
//        String cookie = getCookie();
//        HashMap<String,String> headerParam = new HashMap<String, String>();
//        headerParam.put("Referer", "http://sdnu.wnssedu.com/student/tc/careerPlaning/evaluationlist.htm");
//        String respStr = HttpClientUtil.getResByUrlAndCookie(url , headerParam , cookie  , false);
//        return respStr;
//    }

    private String getTestMainPage(){
        String url = baseUrl + "/student/tc/careerPlaning/evaluationlist.htm";

        HttpGet get = new HttpGet(url);
        get.addHeader(new BasicHeader( "Referer" , "http://sdnu.wnssedu.com/student/tc/careerPlaning/evaluationlist.htm" ));
        try {
            HttpResponse httpResponse = client.execute(get);
            HttpEntity httpEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(httpEntity ,Charset.defaultCharset());
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


//    private void preWatch()  {
//        System.out.println(username + "  开始看视频：");
////        setTeachPlans(getCookie());
//        String blankStyle = "      ";
//        if (plans != null && plans.size() > 0) {
//            for (Plan plan : plans) {
//                if (plan.getStrState().equals("进行中")) {
//                    System.out.println(blankStyle + plan.getStrPlanName());
//                    //http://sdnu.wnssedu.com/student/rest/v1/study/getPlanCourseList  lPlanId=2400000001
//                    setPlanExerciseMap(plan.getLPlanId());
//                    long startTimeMills = 0l;
//                    List<ExerciseDTO> exerciseDTOS = this.planExerciseMap.get(plan.getLPlanId());
//
//                    //TODO JDK 8
//                    if (exerciseDTOS != null && exerciseDTOS.size() > 0) {
//                        startTimeMills = System.currentTimeMillis();
//                        for (ExerciseDTO exerciseDTO : exerciseDTOS) {
//                            String strName = exerciseDTO.getStrName();
//                            List<SectionDTO> cdoSectionList = exerciseDTO.getCdoSectionList();
//                            if (cdoSectionList != null && cdoSectionList.size() > 0) {
//                                for (SectionDTO sectionDTO : cdoSectionList) {
//                                    if (sectionDTO.getNViewTimeLength() != sectionDTO.getNTimeLength()) {
//                                        //进行中的任务 自动看视频
//                                        int needCount = sectionDTO.getNTimeLength() - sectionDTO.getNViewTimeLength();
//                                        for (int count = 0; count < needCount; count++) {
//                                            this.watchVideo(sectionDTO.getLCoursewareId());
//                                            System.out.print(".");
//                                            //解决看视频太慢的问题
//                                            //        Thread.sleep(30000);
//                                            //支持秒刷的业务逻辑
//                                        }
//                                    }
//                                    else {
//                                        System.out.println(blankStyle + blankStyle + blankStyle + " " + sectionDTO.getStrName() + "已完成 ， 已自动跳过。");
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    //服务器30秒内只允许请求一次看视频接口 , 故在此准备所有的请求视频的session 下一次全部请求
//                    utilNextRequest30s(startTimeMills);
//                    //支持秒刷的业务
//                    List<ExerciseDTO> exerciseDTOS2 = this.planExerciseMap.get(plan.getLPlanId());
//                    if (exerciseDTOS2 != null && exerciseDTOS2.size() > 0) {
//                        for (ExerciseDTO exerciseDTO : exerciseDTOS2) {
//                            String strName = exerciseDTO.getStrName();
//                            System.out.println(blankStyle + blankStyle + " " + strName);
//                            List<SectionDTO> cdoSectionList = exerciseDTO.getCdoSectionList();
//                            if (cdoSectionList != null && cdoSectionList.size() > 0) {
//                                for (SectionDTO sectionDTO : cdoSectionList) {
//                                        int needCount = sectionDTO.getNTimeLength() - sectionDTO.getNViewTimeLength();
//                                        if(needCount <=0){
//                                            System.out.println(sectionDTO.getStrName() +" 已完成 ， 跳过。。");
//                                            continue;
//                                        }
//                                        String courseId = sectionDTO.getLCoursewareId();
//                                        for (int count = 0; count < needCount; count++) {
//                                            //看视频接口
//                                            doWatch(courseId);
//                                            System.out.print(".");
//                                        }
//                                        System.out.println(blankStyle + blankStyle + blankStyle + sectionDTO.getStrName());
//
//                                }
//                            }
//                        }
//
//                    }
//                }else {
//                    System.out.println("看视频 " + plan.getStrPlanName() + " 已完成 ，自动跳过。");
//                }
//            }
//        }
//    }


    private String doWatchV2(String courseId) {

        String url = "http://course.wnssedu.com/Servlet/recordStudy.svl?lCoursewareId=" + courseId + "&strStartTime=0";
        HttpPost post = new HttpPost(url);
        //lCoursewareId=227
        //strStartTime=0
        post.setEntity(new StringEntity("lCoursewareId=" + courseId + "&strStartTime=0", Charset.defaultCharset()));
        //过滤出已完成的
//        httPost3.setHeader(new BasicHeader(CookieConstant.COOKIE, getCookie()));

        try {
            HttpResponse httpResponse = client.execute(post);
            HttpEntity tempEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(tempEntity, Charset.defaultCharset());

            return respStr;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

//    private void doWatch(String courseId) {
//
//        String tempUrl = "http://course.wnssedu.com/Servlet/recordStudy.svl?lCoursewareId=" + courseId + "&strStartTime=0";
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpPost httPost3 = new HttpPost(tempUrl);
//        //lCoursewareId=227
//        //strStartTime=0
//        httPost3.setEntity(new StringEntity("lCoursewareId=" + courseId + "&strStartTime=0", Charset.defaultCharset()));
//        //过滤出已完成的
////        httPost3.setHeader(new BasicHeader(CookieConstant.COOKIE, getCookie()));
//
//        try {
//            CloseableHttpResponse response3 = httpclient.execute(httPost3);
//            HttpEntity tempEntity = response3.getEntity();
//            String respStrr = EntityUtils.toString(tempEntity, Charset.defaultCharset());
//
//            Header[] cookies1 = response3.getHeaders("Set-Cookie");
//            StringBuffer cookieStr3 = new StringBuffer();
//            if (cookies1 != null && cookies1.length != 0) {
//                for (Header cookHeader : cookies1) {
//                    cookieStr3.append(cookHeader.getValue() + ";");
//                }
//            }
////            rebuildCookieMap(cookieStr3.toString());
//            response3.close();
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }

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

    private String getPlanCourseListJson(String planId){
        String url = "http://sdnu.wnssedu.com/student/rest/v1/study/getPlanCourseList";
        HttpPost httPost = new HttpPost(url);
        httPost.setEntity(new StringEntity("{\n" +
                "\t\"lPlanId\":\"" +  planId + "\"\n" +
                "}", Charset.defaultCharset()));
        httPost.addHeader(new BasicHeader("Content-Type", "application/json"));

        try {
            HttpResponse httpResponse = client.execute(httPost);
            HttpEntity httpEntity= httpResponse.getEntity();
            String respStr = EntityUtils.toString(httpEntity, Charset.defaultCharset());
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
//    private void setPlanExerciseMap(String planId) {
//        List<ExerciseDTO> exercises = new ArrayList<ExerciseDTO>();
//        String url = "http://sdnu.wnssedu.com/student/rest/v1/study/getPlanCourseList";
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpPost httPost = new HttpPost(url);
//        httPost.setEntity(new StringEntity("{\n" +
//                "\t\"lPlanId\":\"" +  planId + "\"\n" +
//                "}", Charset.defaultCharset()));
//        //过滤出已完成的
////        httPost.setHeader(new BasicHeader(CookieConstant.COOKIE, getCookie()));
//        httPost.addHeader(new BasicHeader("Content-Type", "application/json"));
//        CloseableHttpResponse response2 = null;
//        try {
//            response2 = httpclient.execute(httPost);
//            Header[] cookies = response2.getHeaders("Set-Cookie");
//            StringBuffer cookieStr = new StringBuffer();
//            if (cookies != null && cookies.length != 0) {
//                for (Header cookHeader : cookies) {
//                    cookieStr.append(cookHeader.getValue() + ";");
//                }
//            }
////            rebuildCookieMap(cookieStr.toString());
//            HttpEntity entity2 = response2.getEntity();
//            String respStr = EntityUtils.toString(entity2, Charset.defaultCharset());
//            if (respStr != null && respStr.length() > 0) {
//                JSONArray courseArray = JSONObject.parseObject(respStr).getJSONObject("response").getJSONArray("cdoCourseList");
//                if (courseArray != null && courseArray.size() > 0) {
//                    for (int index = 0; index < courseArray.size(); index++) {
//                        JSONObject courseJson = courseArray.getJSONObject(index);
//                        ExerciseDTO exercise = courseJson.toJavaObject(ExerciseDTO.class);
//                        exercises.add(exercise);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        this.planExerciseMap.put(planId , exercises);
//
//    }

//    public void appendSession2CookieMap() {
//        String loginUrl = "http://sdnu.wnssedu.com/modal/handleTrans.cdo?strServiceName=UserService&strTransName=SSOLogin";
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("$$CDORequest$$", buildLoginParam(this.username , this.password));
//        String respAndCookie = HttpClientUtil.postResByUrlAndCookie(loginUrl, getCookie(), params, true);
//        try{
//            String cookie = respAndCookie.split("#")[1];
//            rebuildCookieMap(cookie);
//        }catch (IndexOutOfBoundsException e){
//            System.out.println(username +" 密码错误");
//            throw new IllegalArgumentException("");
//        }
//
//    }

    private String buildLoginParam(String username , String password ) {
        String schooldId0 = "0";
//        if(this.schoolId == null ){
//            schooldId0 = "0";
//        }else{
//            schooldId0 = this.schoolId;
//        }
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

//    public void setTeachPlans(String cookie) {
//        String teachPlanUrl = "http://sdnu.wnssedu.com/student/rest/v1/study/getTeachPlanList";
//        String respStr = HttpClientUtil.getResByUrlAndCookie(teachPlanUrl,  null , cookie, false);
//        JSONObject jsonObject = JSONObject.parseObject(respStr);
//        JSONArray cdosPlanList = jsonObject.getJSONObject("response").getJSONArray("cdosPlanList");
//        List<Plan> plans = new ArrayList<Plan>();
//        if(cdosPlanList != null && cdosPlanList.size() >0){
//            for(int index = 0 ; index< cdosPlanList.size() ; index++ ){
//                JSONObject planJson = cdosPlanList.getJSONObject(index);
//                Plan plan = planJson.toJavaObject(Plan.class);
//                plans.add(plan);
//            }
//        }
//        this.plans = plans;
//    }

    public String getTeachPlansJson() {
        String teachPlanUrl = baseUrl + "/student/rest/v1/study/getTeachPlanList";
        HttpGet get = new HttpGet(teachPlanUrl);

        try {
            HttpResponse httpResponse = client.execute(get);
            HttpEntity httpEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(httpEntity ,Charset.defaultCharset());
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

    /**
     * 后台看视频 进度 +2 系统需要30s的延迟接口请求的时间 30s请求两次 返回超时
     *
     * @param courseId http://course.njcedu.com/Servlet/recordStudy.svl?lCourseId=1286&lSchoolId=539&strStartTime=0

     **/
//    public void watchVideo(String courseId)  {
//        //数据准备
//        getCourseWareCookie(courseId);
//        //解决看视频太慢的问题
////        Thread.sleep(30000);
//        //支持秒刷的业务逻辑 将代码迁移出去
////        String url = "http://course.njcedu.com/Servlet/recordStudy.svl?lCourseId=" + courseId +
////                "&lSchoolId=" + this.schoolCodeMap.get(this.loginDomain) + "&strStartTime=0";
////        HttpClientUtil.getResByUrlAndCookie(url, null , getCookieByMap(cookieMap), false);
//
//        return;
//
//    }

//    public String getCookieByMap(HashMap<String, String> cookieMap) {
//        StringBuffer cookieStr = new StringBuffer();
//        if (cookieMap != null && cookieMap.size() != 0) {
//            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
//                String key = entry.getKey();
//                String value = entry.getValue();
//                cookieStr.append(key + "=" + value + ";");
//            }
//        }
//        return cookieStr.toString();
//    }

//    public String setSchoolToken(String schoolToken){
////            cookieMap.put(CookieConstant.SCHOOL_TOKEN , schoolToken);
//            return null;
//    }

//    public String getCookie(){
//        return this.getCookieByMap(this.cookieMap);
//    }

    /**
     * 看视频前置需要获取cookie
     */
    public void watchVideoBefore(String courseId){
        String url = "http://course.wnssedu.com/rest/v1/coursestudy/setCourseCookie";
        HttpPost post = new HttpPost(url);
        post.addHeader(new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8"));
        post.addHeader(new BasicHeader("Referer","http://sdnu.wnssedu.com"));

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("lCoursewareId" , courseId));

        try {
            HttpResponse httpResponse = client.execute(post);
            HttpEntity httpEntity = httpResponse.getEntity();
            String respStr = EntityUtils.toString(httpEntity ,Charset.defaultCharset());
            return ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ;
    }
//    public void getCourseWareCookie(String courseId) {
//        String url = "http://course.wnssedu.com/rest/v1/coursestudy/setCourseCookie";
//        HashMap<String,String> valueMap = new HashMap<String, String>();
//        valueMap.put("lCoursewareId" , courseId);
//        String respAndCookie = HttpClientUtil.postResByUrlAndCookie(url , getCookie() , valueMap , true );
//
//        if(respAndCookie.split("#").length >=2){
////            rebuildCookieMap(respAndCookie.split("#")[1]);
//        }
//    }


}





