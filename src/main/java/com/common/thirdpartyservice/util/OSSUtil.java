package com.common.thirdpartyservice.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.common.thirdpartyservice.listener.GetObjectProgressListener;
import com.common.thirdpartyservice.listener.PutObjectProgressListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

/**
 * @ClassName OSSUtil
 * @Description: 阿里云OSS相关服务
 * @Author zhouxiangfu
 * @Date 2020-06-08 16:47
 * @Version V1.0
 **/

@Slf4j
public class OSSUtil {

    private static String accessId;
    private static String accessKey;
    private static String endpoint;
    private static String bucket;

    static {
        accessId = YmlUtil.get("aliyun.accessKeyId");
        accessKey = YmlUtil.get("aliyun.accessKeySecret");
        endpoint = YmlUtil.get("aliyun.oss.endpoint");
        bucket = YmlUtil.get("aliyun.oss.bucket");
    }


    /**
     * 功能描述: 获取oss签名
     *
     * @param dir 指定保存目录
     * @return: java.util.Map<java.lang.String, java.lang.String>
     * @Author: zhouxiangfu
     * @Date: 2020-06-08 17:35
     */
    public static Map<String, String> getOSSSignature(String dir) {

        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        //        String callbackUrl = "http://88.88.88.88:8888";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));

//            JSONObject jasonCallback = new JSONObject();
//            jasonCallback.put("callbackUrl", callbackUrl);
//            jasonCallback.put("callbackBody",
//                    "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
//            jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
//            String base64CallbackBody = BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
//            respMap.put("callback", base64CallbackBody);
//
//            JSONObject ja1 = JSONObject.fromObject(respMap);
//            // System.out.println(ja1.toString());
//            response.setHeader("Access-Control-Allow-Origin", "*");
//            response.setHeader("Access-Control-Allow-Methods", "GET, POST");
//            response(request, response, ja1.toString());

            return respMap;
        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            log.info(e.getMessage());
            return null;
        } finally {
            ossClient.shutdown();
        }
    }


    /**
     * 功能描述: 简单上传
     *
     * @param bucketName
     * @param objectName
     * @param fileName
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 16:15
     */
    public static void simpleUpload(String bucketName, String objectName, String fileName) {
// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

// 创建PutObjectRequest对象。
// <yourObjectName>表示上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName,
                new File(fileName));

// 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
        ObjectMetadata metadata = new ObjectMetadata();

        // 指定上传文件操作时是否覆盖同名Object。
// 不指定x-oss-forbid-overwrite时，默认覆盖同名Object。
// 指定x-oss-forbid-overwrite为false时，表示允许覆盖同名Object。
// 指定x-oss-forbid-overwrite为true时，表示禁止覆盖同名Object，如果同名Object已存在，程序将报错。
        metadata.setHeader("x-oss-forbid-overwrite", "true");


// metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
// metadata.setObjectAcl(CannedAccessControlList.Private);
        putObjectRequest.setMetadata(metadata);

// 上传字符串。
//        ossClient.putObject(putObjectRequest);

        try {
            // 带进度条的上传。
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, new File(fileName)).
                    <PutObjectRequest>withProgressListener(new PutObjectProgressListener()));

        } catch (Exception e) {
            e.printStackTrace();
        }


// 关闭OSSClient。
        ossClient.shutdown();
    }


    /**
     * 功能描述: 断点续传
     *
     * @param bucketName
     * @param objectName
     * @param fileName
     * @param partSize         分片大小 默认值为文件大小/10000
     * @param taskNum          分片下载的并发数 默认值1
     * @param enableCheckpoint 是否开启断点续传功能。 默认关闭
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 16:35
     */
    public static void offAndOnUpload(String bucketName, String objectName, String fileName, Long partSize, Integer taskNum,
                                      Boolean enableCheckpoint) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

        ObjectMetadata meta = new ObjectMetadata();
// 指定上传的内容类型。
        meta.setContentType("text/plain");
        meta.setHeader("x-oss-forbid-overwrite", "true");

// 通过UploadFileRequest设置多个参数。
        UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, objectName);

// 通过UploadFileRequest设置单个参数。
// 设置存储空间名称。
//uploadFileRequest.setBucketName("<yourBucketName>");
// 设置文件名称。
        uploadFileRequest.setKey(objectName);
// 指定上传的本地文件。
        uploadFileRequest.setUploadFile(fileName);
// 指定上传并发线程数，默认为1。
        if (!Objects.isNull(taskNum)) {
            uploadFileRequest.setTaskNum(taskNum);
        }

// 指定上传的分片大小。
        if (!Objects.isNull(partSize)) {
            uploadFileRequest.setPartSize(partSize);
        }

// 开启断点续传，默认关闭。
        if (!Objects.isNull(enableCheckpoint)) {
            uploadFileRequest.setEnableCheckpoint(enableCheckpoint);
        }
// 记录本地分片上传结果的文件。
//        uploadFileRequest.setCheckpointFile("<yourCheckpointFile>");
// 文件的元数据。
        uploadFileRequest.setObjectMetadata(meta);
// 设置上传成功回调，参数为Callback类型。
//        uploadFileRequest.setCallback(Callback);

// 断点续传上传。
        try {
            ossClient.uploadFile(uploadFileRequest);
        } catch (Throwable throwable) {

            log.error("上传文件失败", throwable);
        }

// 关闭OSSClient。
        ossClient.shutdown();
    }


    /**
     * 功能描述: 分片上传，可用于较大文件
     *
     * @param bucketName
     * @param objectName
     * @param fileName
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 16:52
     */
    public static void sliceUpload(String bucketName, String objectName, String fileName) throws IOException {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

// 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);

// 如果需要在初始化分片时设置文件存储类型，请参考以下示例代码。
        ObjectMetadata metadata = new ObjectMetadata();
// metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        metadata.setHeader("x-oss-forbid-overwrite", "true");
        request.setObjectMetadata(metadata);

// 初始化分片。
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
// 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upresult.getUploadId();

// partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags = new ArrayList<PartETag>();
// 计算文件有多少个分片。
        final long partSize = 1 * 1024 * 1024L;   // 1MB
        final File sampleFile = new File(fileName);
        long fileLength = sampleFile.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }
// 遍历分片上传。
        for (int i = 0; i < partCount; i++) {
            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
            InputStream instream = new FileInputStream(sampleFile);
            // 跳过已经上传的分片。
            instream.skip(startPos);
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(objectName);
            uploadPartRequest.setUploadId(uploadId);
            uploadPartRequest.setInputStream(instream);
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
            uploadPartRequest.setPartSize(curPartSize);
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
            uploadPartRequest.setPartNumber(i + 1);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
            partETags.add(uploadPartResult.getPartETag());
        }


// 创建CompleteMultipartUploadRequest对象。
// 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

// 如果需要在完成文件上传的同时设置文件访问权限，请参考以下示例代码。
// completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.PublicRead);

// 完成上传。
        CompleteMultipartUploadResult completeMultipartUploadResult =
                ossClient.completeMultipartUpload(completeMultipartUploadRequest);

// 关闭OSSClient。
        ossClient.shutdown();
    }

    /**
     * 功能描述: 流式下载，可自定义流数据的处理，并可指定下载范围（兼容范围超限）
     *
     * @param bucketName
     * @param objectName 文件名
     * @param startIndex 开始索引（字节） 要么指定始末，要么不指定
     * @param endIndex   结束索引
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 14:42
     */
    public static void streamingDownload(String bucketName, String objectName, Long startIndex, Long endIndex) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);
        // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
        OSSObject ossObject = null;
        GetObjectRequest getObjectRequest;

        //是否指定始末字节数
        if (Objects.isNull(startIndex) && Objects.isNull(endIndex)) {
            ossObject = ossClient.getObject(bucketName, objectName);
        } else if (!Objects.isNull(startIndex) && !Objects.isNull(endIndex)) {
            // 指定兼容行为。
            // 范围末端取值不在有效区间，返回start~end字节范围内容，且HTTP Code为206。
            try {
                getObjectRequest = new GetObjectRequest(bucketName, objectName);
                getObjectRequest.setRange(startIndex, endIndex);
                getObjectRequest.addHeader("x-oss-range-behavior", "standard");
                ossObject = ossClient.getObject(getObjectRequest);
            } catch (OSSException e) {
                log.error("范围错误", e.getErrorCode());
            }
        } else {
            log.error("禁止只指定一端范围！");
            return;
        }

        // 读取文件内容。
        log.info("读取文件内容:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
        while (true) {
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                log.error("读取文件时发生错误", e);
                break;
            }
            if (line == null) {
                break;
            }
            System.out.println("\n" + line);
        }
        // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
        try {
            reader.close();
        } catch (IOException e) {
            log.error("文件读取流关闭时发生错误", e);
        }
        // 关闭OSSClient。
        ossClient.shutdown();
    }

    /**
     * 功能描述: 下载到本地
     *
     * @param bucketName
     * @param objectName oss中存储文件名
     * @param fileName   本地保存文件名
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 12:37
     */
    public static void downloadLocal(String bucketName, String objectName, String fileName) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

        // 下载OSS文件到本地文件。如果指定的本地文件存在会覆盖，不存在则新建。
//        ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(fileName));

        try {
            // 带进度条的下载。
            ossClient.getObject(new GetObjectRequest(bucketName, objectName).
                            <GetObjectRequest>withProgressListener(new GetObjectProgressListener()),
                    new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 关闭OSSClient。
        ossClient.shutdown();
    }

    /**
     * 功能描述: 断点续传（可针对网络不好的情况）
     *
     * @param bucketName
     * @param objectName
     * @param fileName
     * @param partSize         分片大小 默认值为文件大小/10000
     * @param taskNum          分片下载的并发数 默认值1
     * @param enableCheckpoint 是否开启断点续传功能。 默认关闭
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 15:42
     */
    public static void offAndOnDownload(String bucketName, String objectName, String fileName, Long partSize, Integer taskNum,
                                        Boolean enableCheckpoint) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

        // 下载请求，10个任务并发下载，启动断点续传。
        DownloadFileRequest downloadFileRequest = new DownloadFileRequest(bucketName, objectName);
        downloadFileRequest.setDownloadFile(fileName);
        if (!Objects.isNull(partSize)) {
            downloadFileRequest.setPartSize(partSize);
        }
        if (!Objects.isNull(taskNum)) {
            downloadFileRequest.setTaskNum(taskNum);
        }
        if (!Objects.isNull(enableCheckpoint)) {
            downloadFileRequest.setEnableCheckpoint(enableCheckpoint);
        }
        //downloadFileRequest.setCheckpointFile("<yourCheckpointFile>");
        // 下载文件。

        DownloadFileResult downloadRes = null;
        try {
            downloadRes = ossClient.downloadFile(downloadFileRequest);
        } catch (Throwable e) {
            log.error("下载文件失败", e);
        }
        // 下载成功时，会返回文件元信息。
        downloadRes.getObjectMetadata();

        // 关闭OSSClient。
        ossClient.shutdown();

    }


    /**
     * 功能描述: 文件是否存在于oss
     *
     * @param bucketName
     * @param objectName
     * @return: boolean
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 17:13
     */
    public static boolean fileExists(String bucketName, String objectName) {
// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

// 判断文件是否存在。doesObjectExist还有一个参数isOnlyInOSS，如果为true则忽略302重定向或镜像；如果为false，则考虑302重定向或镜像。
        boolean found = ossClient.doesObjectExist(bucketName, objectName);
        System.out.println(found);

// 关闭OSSClient。
        ossClient.shutdown();
        return found;
    }


    /**
     * 功能描述: 设置文件权限
     *
     * @param bucketName
     * @param objectName
     * @param cannedAccessControlList
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 17:20
     */
    public static void setFileAcl(String bucketName, String objectName, CannedAccessControlList cannedAccessControlList) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

// 设置文件的访问权限为公共读。
        ossClient.setObjectAcl(bucketName, objectName, cannedAccessControlList);

// 关闭OSSClient。
        ossClient.shutdown();
    }

    /**
     * 功能描述: 获取文件权限
     *
     * @param bucketName
     * @param objectName
     * @return: com.aliyun.oss.model.ObjectPermission
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 17:21
     */
    public static ObjectPermission getFileAcl(String bucketName, String objectName) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, accessKey);

// 获取文件的访问权限。
        ObjectAcl objectAcl = ossClient.getObjectAcl(bucketName, objectName);
        log.info(objectAcl.getPermission().toString());

// 关闭OSSClient。
        ossClient.shutdown();

        return objectAcl.getPermission();
    }

    /**
     * 功能描述: 列举bucket中的文件
     *
     * @param bucketName
     * @param prefix             根据前缀
     * @param listObjectsRequest 根据规则
     * @return: java.util.List
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 17:25
     */
    public static List listObjects(String bucketName, String prefix, ListObjectsRequest listObjectsRequest) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

        // 列举文件。 如果不设置KeyPrefix，则列举存储空间下所有的文件。KeyPrefix，则列举包含指定前缀的文件。
        ObjectListing objectListing = null;
        //如果指定规则，则前两个参数不生效
        if (listObjectsRequest != null) {
            objectListing = ossClient.listObjects(listObjectsRequest);
        } else {
            if (StringUtils.isEmpty(prefix)) {
                objectListing = ossClient.listObjects(bucketName);
            } else {
                objectListing = ossClient.listObjects(bucketName, prefix);
            }
        }


        List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
        for (OSSObjectSummary s : sums) {
            log.info("\t" + s.getKey());
        }

// 关闭OSSClient。
        ossClient.shutdown();

        return sums;

    }

    /**
     * 功能描述: 删除单个文件
     *
     * @param bucketName
     * @param objectName
     * @return: void
     * @Author: zhouxiangfu
     * @Date: 2020-06-10 17:37
     */
    public static void removeFile(String bucketName, String objectName) {

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);

// 删除文件。如需删除文件夹，请将ObjectName设置为对应的文件夹名称。如果文件夹非空，则需要将文件夹下的所有object删除后才能删除该文件夹。
        ossClient.deleteObject(bucketName, objectName);

// 关闭OSSClient。
        ossClient.shutdown();
    }




}