package com.fsy.task.selenium;

import com.fsy.task.domain.ImportUser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * selenium自动化支持程度不高  , 故不采用
 */
public class SeleniumUtil {
    public ImportUser getUserInfo(String username , String password){
        // TODO Auto-generated method stub
        //如果测试的浏览器没有安装在默认目录，那么必须在程序中设置
        //bug1:System.setProperty("webdriver.chrome.driver", "C://Program Files (x86)//Google//Chrome//Application//chrome.exe");
        //bug2:System.setProperty("webdriver.chrome.driver", "C://Users//Yoga//Downloads//chromedriver_win32//chromedriver.exe");
        //System.setProperty("webdriver.chrome.driver", "D://tanzhenTest//chromedriver_win32//chromedriver.exe");
        FirefoxDriver firefoxDriver = new FirefoxDriver();
//        String username = "gyu-160308401009";
//
//        String passowrd = "123456";

//        String loginDomain = "gyu";


//		WebDriver driver = new ChromeDriver();

        firefoxDriver.get("http://null.wnssedu.com/");
        // 获取 网页的 title
        //System.out.println("The testing page title is: " + firefoxDriver.getTitle());

        WebDriverWait webDriverWait=new WebDriverWait(firefoxDriver,5);

        WebElement loginButton= firefoxDriver.findElement(By.id("btnLogin"));

        loginButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        firefoxDriver.switchTo().frame(firefoxDriver.findElement(By.id("layui-layer-iframe100001")));

        WebElement e1= firefoxDriver.findElement(By.className("loginBtn"));

        WebElement e= firefoxDriver.findElementByXPath("//*[@id=\"password\"]");

        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.id("loginId"))).sendKeys(username);

        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.className("loginBtn"))).sendKeys(username);

        webDriverWait.until(ExpectedConditions.elementToBeClickable(e)).sendKeys(username);



        e.clear();

        //System.out.println("清空密码框内容----------------------------------------------------------------------------------------------------------");

        e.sendKeys(password);

        e1.click();





        //userId 不导入 程序直接自动获取

        //schoolId  不导入 程序直接自动获取

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }

        String schoolToken = firefoxDriver.manage().getCookieNamed("schoolToken").getValue();

        //String schoolUserStr = firefoxDriver.findElementByXPath("/html/body/script[20]").getText();


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }

        //nickname person_nickname_25
        WebElement nickNameEle = firefoxDriver.switchTo().defaultContent().findElement(By.className("person_nickname_25"));
        String nickName = nickNameEle.getText();

//        Stirng nickName = firefoxDriver.findElementByName()
       // firefoxDriver.quit();
//        firefoxDriver.close();

        return ImportUser.builder()
                .username(username)
                .password(password)
                .schoolToken(schoolToken)
                .loginDomain(username.substring(0 , 4 ))
                .nickName(nickName)
                .build();
    }

}
