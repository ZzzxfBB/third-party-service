package com.common.thirdpartyservice.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.live.model.v20161101.*;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName LiveUtil
 * @Description: 直播相关
 * @Author zhouxiangfu
 * @Date 2020-06-15 14:14
 * @Version V1.0
 **/
@Slf4j
public class LiveUtil {


    private static String regionId;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String pushDomain;
    private static String pullDomain;
    private static String urlKey;

    static {
        regionId = YmlUtil.get("aliyun.regionId");
        accessKeyId = YmlUtil.get("aliyun.accessKeyId");
        accessKeySecret = YmlUtil.get("aliyun.accessKeySecret");
        pushDomain = YmlUtil.get("aliyun.live.pushDomain");
        pullDomain = YmlUtil.get("aliyun.live.pullDomain");
        urlKey = YmlUtil.get("aliyun.live.urlKey");
    }

    //初始化客户端
    public static DefaultAcsClient initVodClient() throws ClientException {
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);
        return client;
    }

    /**
     * 根据源id创建该id的推流url
     *
     * @param identUrlValidTime 鉴权url的有效时间（秒），默认30分钟，1800秒
     * @param appName           直播测试appName
     * @param streamName        直播测试s直播测试streamNametreamName
     * @return
     */
    public static String createPushUrl(String appName, String streamName, Integer identUrlValidTime) {
        // 计算过期时间
        if (Objects.isNull(identUrlValidTime)) {
            identUrlValidTime = 1800;
        }
        String timestamp = String.valueOf((System.currentTimeMillis() / 1000) + identUrlValidTime);


        // 组合推流域名前缀
        String rtmpUrl = StrUtil.format("rtmp://{}/{}/{}", pushDomain, appName, streamName);

        String md5Url = StrUtil.format("/{}/{}-{}-0-0-{}", appName, streamName, timestamp, urlKey);
        String md5Str = DigestUtil.md5Hex(md5Url);
        //分隔字符串
        /*******************************/

        int i = rtmpUrl.lastIndexOf("/");
        String pre = rtmpUrl.substring(0, i + 1);
        String after = rtmpUrl.substring(i + 1);

        /*******************************/
        Map<Object, Object> finallyPushUrl = new HashMap<>();
        finallyPushUrl.put("url", pre);
        finallyPushUrl.put("streamName", after + "?auth_key=" + timestamp + "-0-0-" + md5Str);
        log.info("生成推流：{}", finallyPushUrl);
        //存入redis并生成过期时间
        return pre + after + "?auth_key=" + timestamp + "-0-0-" + md5Str;
    }


    /**
     * 创建拉流域名，key=rtmpUrl、flvUrl、m3u8Url，代表三种拉流类型域名
     *
     * @param appName           应用名称
     * @param streamName        流名称
     * @param identUrlValidTime 鉴权url的有效时间（秒），默认30分钟，1800秒
     */
    private static String createPullUrl(String appName, String streamName,  Integer identUrlValidTime) {
        // 计算过期时间
        if (Objects.isNull(identUrlValidTime)) {
            identUrlValidTime = 1800;
        }
        String timestamp = String.valueOf((System.currentTimeMillis() / 1000) + identUrlValidTime);
        // 组合通用域名
        // {pullDomain}/{appName}/{streamName}
        String pullUrl = StrUtil.format("{}/{}/{}", pullDomain, appName, streamName);
        log.info("组合通用域名，pullUrl:{}", pullUrl);

        // 组合md5加密串
        // /{appName}/{streamName}-{timestamp}-0-0-{pullIdentKey}
        String md5Url = StrUtil.format("/{}/{}-{}-0-0-{}", appName, streamName, timestamp, urlKey);

        // md5加密
        String md5Str = DigestUtil.md5Hex(md5Url);

        // 组合三种拉流域名前缀
        // rtmp://{pullUrl}?auth_key={timestamp}-0-0-{md5Str}
        String rtmpUrl = StrUtil.format("rtmp://{}?auth_key={}-0-0-{}", pullUrl, timestamp, md5Str);
        log.info("生成播流url：{}", rtmpUrl);
        //存入redis并设置过期时间
        return rtmpUrl;
    }

    /**
     * 功能描述:获取某个域名（或域名下某应用或某个流）的推流记录
     *
     * @param
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 18:05
     */
    public static void describeLiveStreamsOnlineList(String domianName, String appName) throws ClientException {

        DefaultAcsClient client = initVodClient();
        DescribeLiveStreamsOnlineListRequest request = new DescribeLiveStreamsOnlineListRequest();
        request.setDomainName(domianName);
        if (!StringUtils.isEmpty(appName)) {
            request.setAppName(appName);
        }

        try {
            DescribeLiveStreamsOnlineListResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
    }

    /**
     * 功能描述: 获取某一时间段内某个域名（或域名下某应用或某个流）的推流记录
     *
     * @param domianName
     * @param appName
     * @param startTime  UTC 格式，例如：2016-06-29T19:00:00Z。
     * @param endTime
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 18:13
     */
    public static void describeLiveStreamsPublishList(String domianName, String appName, String startTime, String endTime) throws ClientException {
        DefaultAcsClient client = initVodClient();

        DescribeLiveStreamsPublishListRequest request = new DescribeLiveStreamsPublishListRequest();
//        request.setRegionId("cn-hangzhou");
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setDomainName(domianName);
        if (!StringUtils.isEmpty(appName)) {
            request.setAppName(appName);
        }

        try {
            DescribeLiveStreamsPublishListResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }

    }

    /**
     * 功能描述:调用ForbidLiveStream禁止某条流的推送，可以预设某个时刻将流恢复
     *
     * @param domianName
     * @param appName
     * @param streamName
     * @param resumeTime 恢复流的时间。UTC时间，格式：2015-12-01T17:37:00Z。
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 18:20
     */
    public static void forbidLiveStream(String domianName, String appName, String streamName,
                                        String resumeTime) throws ClientException {

        DefaultAcsClient client = initVodClient();

//        request.setRegionId("cn-hangzhou");

        ForbidLiveStreamRequest request = new ForbidLiveStreamRequest();
//        request.setRegionId("cn-hangzhou");
        request.setAppName(appName);
        request.setStreamName(streamName);
        request.setLiveStreamType("publisher");
        request.setDomainName(domianName);
        if (!StringUtils.isEmpty(resumeTime)) {
            request.setResumeTime(resumeTime);
        }

        try {
            ForbidLiveStreamResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
    }

    public static void resumeLiveStream(String domianName, String appName, String streamName) throws ClientException {

        DefaultAcsClient client = initVodClient();

        ResumeLiveStreamRequest request = new ResumeLiveStreamRequest();
//        request.setRegionId("cn-hangzhou");
        request.setAppName(appName);
        request.setStreamName(streamName);
        request.setLiveStreamType("publisher");
        request.setDomainName(domianName);

        try {
            ResumeLiveStreamResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
    }

    public static void main(String[] args) throws ClientException {
        createPullUrl("test", "test", null);
    }

}