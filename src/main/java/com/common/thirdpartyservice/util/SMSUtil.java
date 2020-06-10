package com.common.thirdpartyservice.util;

import cn.hutool.core.util.RandomUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName SMSUtil
 * @Description: 阿里云短信服务
 * @Author zhouxiangfu
 * @Date 2020-06-08 11:41
 * @Version V1.0
 **/

@Slf4j
@Component
public class SMSUtil {

    private static String regionId;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String signName;
    private static String templateCode;

    static {
        regionId = YmlUtil.get("aliyun.regionId");
        accessKeyId = YmlUtil.get("aliyun.accessKeyId");
        accessKeySecret = YmlUtil.get("aliyun.accessKeySecret");
        signName = YmlUtil.get("aliyun.sms.signName");
        templateCode = YmlUtil.get("aliyun.sms.templateCode");
    }

    /**
     * 功能描述: 发送短信
     *
     * @param phoneNumbers 电话号码，多个号码","分隔
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-08 11:55
     */
    public static void sendSMS(String phoneNumbers) {

        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        // 自定义纯数字的验证码（随机4位数字，可重复）
        String captcha = RandomUtil.randomNumbers(4);


        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", regionId);
        request.putQueryParameter("PhoneNumbers", phoneNumbers);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", "{\"code\":\""+captcha+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能描述: 查询短信发送详细列表
     *
     * @param phoneNumber 手机号
     * @param sendDate    发送时间，形如yyyyMMdd
     * @param pageSize
     * @param currentPage
     * @return: java.lang.String
     * @Author: zhouxiangfu
     * @Date: 2020-06-08 15:29
     */
    public static String querySendDetails(String phoneNumber, String sendDate, String pageSize, String currentPage) {
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("QuerySendDetails");
        request.putQueryParameter("RegionId", regionId);
        request.putQueryParameter("PhoneNumber", phoneNumber);
        request.putQueryParameter("SendDate", sendDate);
        request.putQueryParameter("PageSize", pageSize);
        request.putQueryParameter("CurrentPage", currentPage);
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info(response.getData());
            return response.getData();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        sendSMS("13219641151");
    }
}