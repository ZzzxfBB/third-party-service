package com.common.thirdpartyservice.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.*;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

/**
 * @ClassName VODUtil
 * @Description: 视频点播工具
 * @Author zhouxiangfu
 * @Date 2020-06-15 10:44
 * @Version V1.0
 **/

public class VODUtil {


    private static String regionId;
    private static String accessKeyId;
    private static String accessKeySecret;

    static {
        regionId = YmlUtil.get("aliyun.regionId");
        accessKeyId = YmlUtil.get("aliyun.accessKeyId");
        accessKeySecret = YmlUtil.get("aliyun.accessKeySecret");
    }

    //初始化客户端
    public static DefaultAcsClient initVodClient() throws ClientException {
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);
        return client;
    }


    /**
     * 功能描述: 获取视频上传地址和凭证
     * @param client
     * @return: com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 10:48
    */
    public static CreateUploadVideoResponse createUploadVideo(DefaultAcsClient client) throws Exception {
        CreateUploadVideoRequest request = new CreateUploadVideoRequest();
        request.setTitle("this is a sample");
        request.setFileName("filename.mp4");
        //UserData，用户自定义设置参数，用户需要单独回调URL及数据透传时设置(非必须)
        //JSONObject userData = new JSONObject();
        //UserData回调部分设置
        //JSONObject messageCallback = new JSONObject();
        //messageCallback.put("CallbackURL", "http://xxxxx");
        //messageCallback.put("CallbackType", "http");
        //userData.put("MessageCallback", messageCallback.toJSONString());
        //UserData透传数据部分设置
        //JSONObject extend = new JSONObject();
        //extend.put("MyId", "user-defined-id");
        //userData.put("Extend", extend.toJSONString());
        //request.setUserData(userData.toJSONString());
        return client.getAcsResponse(request);
    }

    /**
     * 功能描述: 刷新上传地址和凭证
     * @param client
     * @return: com.aliyuncs.vod.model.v20170321.RefreshUploadVideoResponse
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 10:51
    */
    public static RefreshUploadVideoResponse refreshUploadVideo(DefaultAcsClient client) throws Exception {
        RefreshUploadVideoRequest request = new RefreshUploadVideoRequest();
        request.setVideoId("VideoId");
        return client.getAcsResponse(request);
    }


    /**
     * 功能描述: 获取图片上传地址和凭证
     * @param client
     * @return: com.aliyuncs.vod.model.v20170321.CreateUploadImageResponse
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 10:52
    */
    public static CreateUploadImageResponse createUploadImage(DefaultAcsClient client) throws Exception {
        CreateUploadImageRequest request = new CreateUploadImageRequest();
        request.setImageType("default");
        request.setImageExt("gif");
        request.setTitle("this is a sample");
        JSONObject userData = new JSONObject();
        JSONObject messageCallback = new JSONObject();
        messageCallback.put("CallbackURL", "http://xxxxx");
        messageCallback.put("CallbackType", "http");
        userData.put("MessageCallback", messageCallback.toJSONString());
        JSONObject extend = new JSONObject();
        extend.put("MyId", "user-defined-id");
        userData.put("Extend", extend.toJSONString());
        request.setUserData(userData.toJSONString());
        return client.getAcsResponse(request);
    }


    /** 
     * 功能描述: 获取辅助媒资上传地址和凭证
     * @param client
     * @return: com.aliyuncs.vod.model.v20170321.CreateUploadAttachedMediaResponse
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 10:53
    */
    public static CreateUploadAttachedMediaResponse createUploadAttachedMedia(DefaultAcsClient client) throws Exception {
        CreateUploadAttachedMediaRequest request = new CreateUploadAttachedMediaRequest();
        request.setBusinessType("watermark");
        request.setMediaExt("gif");
        request.setTitle("this is a sample");
        JSONObject userData = new JSONObject();
        JSONObject messageCallback = new JSONObject();
        messageCallback.put("CallbackURL", "http://xxxxx");
        messageCallback.put("CallbackType", "http");
        userData.put("MessageCallback", messageCallback.toJSONString());
        JSONObject extend = new JSONObject();
        extend.put("MyId", "user-defined-id");
        userData.put("Extend", extend.toJSONString());
        request.setUserData(userData.toJSONString());
        return client.getAcsResponse(request);
    }

    /**
     * URL批量拉取上传
     * @param client 发送请求客户端
     * @return UploadMediaByURLResponse URL批量拉取上传响应数据
     * @throws Exception
     */
    public static UploadMediaByURLResponse uploadMediaByURL(DefaultAcsClient client) throws Exception {
        UploadMediaByURLRequest request = new UploadMediaByURLRequest();
        String url = "http://xxxx.mp4";
        String encodeUrl = URLEncoder.encode(url, "UTF-8");
        request.setUploadURLs(encodeUrl);
        JSONObject uploadMetadata = new JSONObject();
        uploadMetadata.put("SourceUrl", encodeUrl);
        uploadMetadata.put("Title", "upload by url sample");
        JSONArray uploadMetadataList = new JSONArray();
        uploadMetadataList.add(uploadMetadata);
        request.setUploadMetadatas(uploadMetadataList.toJSONString());
        JSONObject userData = new JSONObject();
        JSONObject messageCallback = new JSONObject();
        messageCallback.put("CallbackURL", "http://xxxxx");
        messageCallback.put("CallbackType", "http");
        userData.put("MessageCallback", messageCallback.toJSONString());
        JSONObject extend = new JSONObject();
        extend.put("MyId", "user-defined-id");
        userData.put("Extend", extend.toJSONString());
        request.setUserData(userData.toJSONString());
        return client.getAcsResponse(request);
    }

    /**
     * 注册媒资信息
     * @param client 发送请求客户端
     * @return RegisterMediaResponse 注册媒资信息响应数据
     * @throws Exception
     */
    public static RegisterMediaResponse registerMedia(DefaultAcsClient client) throws Exception {
        RegisterMediaRequest request = new RegisterMediaRequest();
        JSONArray metaDataArray = new JSONArray();
        JSONObject metaData = new JSONObject();
        metaData.put("Title", "this is a sample");
        metaData.put("FileURL", "https://xxxxxx.oss-cn-shanghai.aliyuncs.com/vod_sample.mp4");
        metaDataArray.add((metaData));
        request.setRegisterMetadatas(metaDataArray.toJSONString());
        return client.getAcsResponse(request);
    }

    /**
     * 获取URL上传信息
     * @param client 发送请求客户端
     * @return GetURLUploadInfosResponse 获取URL上传信息响应数据
     * @throws Exception
     */
    public static GetURLUploadInfosResponse getURLUploadInfos(DefaultAcsClient client) throws Exception {
        GetURLUploadInfosRequest request = new GetURLUploadInfosRequest();
        String[] urls = {
                "http://xxx.cn-shanghai.aliyuncs.com/sample1.mp4",
                "http://xxx.cn-shanghai.aliyuncs.com/sample2.flv"
        };
        List<String> encodeUrlList = new ArrayList<String>();
        for(String url : urls){
            encodeUrlList.add(URLEncoder.encode(url, "UTF-8"));
        }
        request.setUploadURLs(StringUtils.join(encodeUrlList, ','));
        //request.setJobIds("xxx1,xxx2");
        return client.getAcsResponse(request);
    }

    /**
     * 功能描述: 获取播放地址函数
     * @param client
     * @return: com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 11:03
    */
    public static GetPlayInfoResponse getPlayInfo(DefaultAcsClient client) throws Exception {
        GetPlayInfoRequest request = new GetPlayInfoRequest();
        request.setVideoId("db12e917f08e4ccfa847f6fa6f8e4092");
        return client.getAcsResponse(request);
    }

    /**
     * 功能描述: 获取播放地址函数
     * @param client
     * @return: com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse
     * @Author: zhouxiangfu
     * @Date: 2020-06-15 11:03
    */
    public static GetVideoPlayAuthResponse getVideoPlayAuth(DefaultAcsClient client) throws Exception {
        GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();
        request.setVideoId("db12e917f08e4ccfa847f6fa6f8e4092");
        return client.getAcsResponse(request);
    }

    /**
     * 搜索媒资信息
     * @param client 发送请求客户端
     * @return SearchMediaResponse 搜索媒资信息响应数据
     * @throws Exception
     */
    public static SearchMediaResponse searchMedia(DefaultAcsClient client) throws Exception {
        SearchMediaRequest request = new SearchMediaRequest();
        request.setFields("Title,CoverURL,Status");
        request.setMatch("Status in ('Normal','Checking') and CreationTime = ('2018-07-01T08:00:00Z','2018-08-01T08:00:00Z')");
        request.setPageNo(1);
        request.setPageSize(10);
        request.setSearchType("video");
        request.setSortBy("CreationTime:Desc");
        return client.getAcsResponse(request);
    }


    /**
     * 获取视频信息
     * @param client 发送请求客户端
     * @return GetVideoInfoResponse 获取视频信息响应数据
     * @throws Exception
     */
    public static GetVideoInfoResponse getVideoInfo(DefaultAcsClient client) throws Exception {
        GetVideoInfoRequest request = new GetVideoInfoRequest();
        request.setVideoId("VideoId");
        return client.getAcsResponse(request);
    }

    /**
     * 批量获取视频信息函数
     * @param client 发送请求客户端
     * @return GetVideoInfosResponse 获取视频信息响应数据
     * @throws Exception
     */
    public static GetVideoInfosResponse getVideoInfos(DefaultAcsClient client) throws Exception {
        GetVideoInfosRequest request = new GetVideoInfosRequest();
        request.setVideoIds("VideoId1,VideoId2");
        return client.getAcsResponse(request);
    }

    /**
     * 修改视频信息
     * @param client 发送请求客户端
     * @return UpdateVideoInfoResponse 修改视频信息响应数据
     * @throws Exception
     */
    public static UpdateVideoInfoResponse updateVideoInfo(DefaultAcsClient client) throws Exception {
        UpdateVideoInfoRequest request = new UpdateVideoInfoRequest();
        request.setVideoId("db12e917f08e4ccfa847f6fa6f8e4092");
        request.setTitle("[清晰 480P] 世上有几人能活到这短片结束.flv");
        return client.getAcsResponse(request);
    }

    /**
     * 批量修改视频信息
     * @param client 发送请求客户端
     * @return UpdateVideoInfosResponse 批量修改视频信息响应数据
     * @throws Exception
     */
    public static UpdateVideoInfosResponse updateVideoInfos(DefaultAcsClient client) throws Exception {
        UpdateVideoInfosRequest request = new UpdateVideoInfosRequest();
        JSONArray updateContentArray = new JSONArray();
        JSONObject updateContent1 = new JSONObject();
        updateContent1.put("VideoId", "VideoId1");
        // updateContent1.put("Title", "new Title");
        // updateContent1.put("Tags", "new Tag1,new Tag2");
        updateContentArray.add((updateContent1));
        JSONObject updateContent2 = new JSONObject();
        updateContent2.put("VideoId", "VideoId2");
        // updateContent2.put("Title", "new Title");
        // updateContent2.put("Tags", "new Tag1,new Tag2");
        updateContentArray.add((updateContent2));
        request.setUpdateContent(updateContentArray.toJSONString());
        return client.getAcsResponse(request);
    }



    /**
     * 删除视频
     * @param client 发送请求客户端
     * @return DeleteVideoResponse 删除视频响应数据
     * @throws Exception
     */
    public static DeleteVideoResponse deleteVideo(DefaultAcsClient client) throws Exception {
        DeleteVideoRequest request = new DeleteVideoRequest();
        //支持传入多个视频ID，多个用逗号分隔
        request.setVideoIds("VideoId1,VideoId2");
        return client.getAcsResponse(request);
    }


    /**
     * 获取源文件信息（含源片下载地址）
     * @param client 发送请求客户端
     * @return GetMezzanineInfoResponse 获取源文件信息响应数据
     * @throws Exception
     */
    public static GetMezzanineInfoResponse getMezzanineInfo(DefaultAcsClient client) throws Exception {
        GetMezzanineInfoRequest request = new GetMezzanineInfoRequest();
        request.setVideoId("VideoId");
        //源片下载地址过期时间
        request.setAuthTimeout(3600L);
        return client.getAcsResponse(request);
    }

    // 根据Date时间生成UTC时间函数
    public static String generateUTCTime(Date time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        dateFormat.setLenient(false);
        return dateFormat.format(time);
    }
    /**
     * 获取视频列表
     * @param client 发送请求客户端
     * @return GetVideoListResponse 获取视频列表响应数据
     * @throws Exception
     */
    public static GetVideoListResponse getVideoList(DefaultAcsClient client) throws Exception {
        GetVideoListRequest request = new GetVideoListRequest();
        // 分别取一个月前、当前时间的UTC时间作为筛选视频列表的起止时间
        String monthAgoUTCTime = generateUTCTime(new Date(System.currentTimeMillis() - 30 * 86400*1000L));
        String nowUTCTime = generateUTCTime(new Date(System.currentTimeMillis()));
        System.out.println(monthAgoUTCTime);
        System.out.println(nowUTCTime);
        // 视频创建的起始时间，为UTC格式
        request.setStartTime(monthAgoUTCTime);
        // 视频创建的结束时间，为UTC格式
        request.setEndTime(nowUTCTime);
        // 视频状态，默认获取所有状态的视频，多个用逗号分隔
        // request.setStatus("Uploading,Normal,Transcoding");
        request.setPageNo(1);
        request.setPageSize(20);
        return client.getAcsResponse(request);
    }

    /**
     * 获取图片信息函数
     *
     * @param client 发送请求客户端
     * @return GetImageInfoResponse 获取图片信息响应数据
     * @throws Exception
     */
    public static GetImageInfoResponse getImageInfo(DefaultAcsClient client) throws Exception {
        GetImageInfoRequest request = new GetImageInfoRequest();
        request.setImageId("ImageId");
        return client.getAcsResponse(request);
    }

    /**
     * 删除图片函数
     *
     * @param client 发送请求客户端
     * @return DeleteImageResponse 删除图片响应数据
     * @throws Exception
     */
    public static DeleteImageResponse deleteImage(DefaultAcsClient client) throws Exception {
        DeleteImageRequest request = new DeleteImageRequest();
        //根据ImageURL删除图片文件
        request.setDeleteImageType("ImageURL");
        String url = "http://sample.aliyun.com/cover.jpg";
        String encodeUrl = URLEncoder.encode(url, "UTF-8");
        request.setImageURLs(encodeUrl);
        //根据ImageId删除图片文件
        //request.setDeleteImageType("ImageId");
        //request.setImageIds("ImageId1,ImageId2");
        //根据VideoId删除指定ImageType的图片文件
        //request.setDeleteImageType("VideoId");
        //request.setVideoId("VideoId");
        //request.setImageType("SpriteSnapshot");
        return client.getAcsResponse(request);
    }


    /**
     * 删除媒体流函数
     * @param client 发送请求客户端
     * @return DeleteMezzaninesResponse 删除媒体流响应数据
     * @throws Exception
     */
    public static DeleteStreamResponse deleteStream(DefaultAcsClient client) throws Exception {
        DeleteStreamRequest request = new DeleteStreamRequest();
        request.setVideoId("VideoId");
        request.setJobIds("JobId1,JobId2");
        return client.getAcsResponse(request);
    }


    /**
     * 批量删除源文件函数
     * @param client 发送请求客户端
     * @return DeleteMezzaninesResponse 批量删除源文件响应数据
     * @throws Exception
     */
    public static DeleteMezzaninesResponse deleteMezzanines(DefaultAcsClient client) throws Exception {
        DeleteMezzaninesRequest request = new DeleteMezzaninesRequest();
        //支持传入多个视频ID，多个用逗号分隔
        request.setVideoIds("VideoId1,VideoId2");
        request.setForce(false);
        return client.getAcsResponse(request);
    }


    /**
     * 批量更新图片信息函数
     * @param client 发送请求客户端
     * @return UpdateImageInfosResponse 批量更新图片信息响应数据
     * @throws Exception
     */
    public static UpdateImageInfosResponse updateImageInfos(DefaultAcsClient client) throws Exception{
        UpdateImageInfosRequest request = new UpdateImageInfosRequest();
        JSONArray updateContentArray = new JSONArray();
        JSONObject updateContent1 = new JSONObject();
        updateContent1.put("ImageId", "ImageId1");
//        updateContent1.put("Title", "new Title");
//        updateContent1.put("Tags", "new Tag1,new Tag2");
        updateContentArray.add((updateContent1));
        JSONObject updateContent2 = new JSONObject();
        updateContent2.put("ImageId", "ImageId2");
//        updateContent2.put("Title", "new Title");
//        updateContent2.put("Tags", "new Tag1,new Tag2");
        updateContentArray.add((updateContent2));
        request.setUpdateContent(updateContentArray.toJSONString());
        return client.getAcsResponse(request);
    }

}