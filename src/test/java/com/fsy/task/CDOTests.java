package com.fsy.task;

import com.fsy.task.dto.CDO;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

public class CDOTests {

    @Test
    public void tests(){
        String cdoXmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<CDO>\n" +
                "  <CDOF N=\"cdoReturn\">\n" +
                "    <CDO>\n" +
                "      <NF N=\"nCode\" V=\"0\"/>\n" +
                "      <STRF N=\"strText\" V=\"登录成功\"/>\n" +
                "      <STRF N=\"strInfo\" V=\"UserService.Login.OK\"/>\n" +
                "    </CDO>\n" +
                "  </CDOF>\n" +
                "  <CDOF N=\"cdoResponse\">\n" +
                "    <CDO>\n" +
                "      <CDOF N=\"cdoUser\">\n" +
                "        <CDO>\n" +
                "          <LF N=\"lId\" V=\"43500000134\"/>\n" +
                "          <STRF N=\"strLoginId\" V=\"sdnu201715030108\"/>\n" +
                "          <STRF N=\"strHash\" V=\"1000:a1a8962ff3eb66c0b72e5b846c59a6b077f93a5bd746c79a:9671e0927b4dea4ded4b563199f2cd10209874aa32bf5629\"/>\n" +
                "          <STRF N=\"strName\" V=\"聂春云\"/>\n" +
                "          <STRF N=\"strNickName\" V=\"\"/>\n" +
                "          <BYF N=\"nVerifyStage\" V=\"0\"/>\n" +
                "          <DTF N=\"dtLastLoginTime\" V=\"2018-11-07 16:38:54\"/>\n" +
                "          <STRF N=\"strHeadURL\" V=\"\"/>\n" +
                "          <BF N=\"bLocked\" V=\"true\"/>\n" +
                "          <BF N=\"bIsRegUser\" V=\"false\"/>\n" +
                "          <STRF N=\"strEmail\" V=\"\"/>\n" +
                "        </CDO>\n" +
                "      </CDOF>\n" +
                "      <LF N=\"lSchoolId\" V=\"24\"/>\n" +
                "      <NF N=\"nRecordCount\" V=\"1\"/>\n" +
                "      <STRAF N=\"arrLoginUrl\">\n" +
                "        <STR>http://sdnu.wnssedu.com/teacher/Servlet/doLogin.svl?key=30eed315961a5d6e0e39299ddc3993f27054721f33ee1b168e777514c20cf540955da3504b40be37c52058000a1dc482865c25e96ad29495dfec8ee435371253cfd80660b4f0f6925f5cb08e56048220f273f5e2aa45f842879bd4b10db73b3e72ef03c1bdb8c55e0d4ae5912f0562375a797c2a0d4ad4f8f8ada9ea2dd4db71ad11381a58c7fc7ea17420a6ad668353311f64b15e71df7472c85d923ea0a9d4fd4f0553915f8e79efe5ce3e496ed7db%7C6D51D545C1E103E00CAAA1017FA812E6</STR>\n" +
                "      </STRAF>\n" +
                "      <NF N=\"nType\" V=\"1\"/>\n" +
                "    </CDO>\n" +
                "  </CDOF>\n" +
                "</CDO>\n";
        SAXReader reader = new SAXReader();
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
            return;
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }
}
