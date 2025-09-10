package com.zzyl.oss;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Slf4j
@Component
public class AliyunOSSOperator {
    @Autowired
    private AliyunOSSProperties ossProperties;

    /**
     * 上传文件到OSS存储
     *
     * @param content          文件内容的字节数组
     * @param originalFilename 原始文件名
     * @return 上传文件的完整访问URL
     * @throws Exception 上传过程中可能抛出的异常
     */
    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = ossProperties.getEndpoint();
        String bucketName = ossProperties.getBucketName();
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 构造OSS存储路径和文件名
        // 获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        // 根据原始文件名originalFilename, 生成一个新的不重复的文件名
        String newFileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        // 文件上传
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 构造并返回文件访问URL
        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

    public void deleteFile(List<String> keys) throws Exception {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ossProperties.getEndpoint();
        String bucketName = ossProperties.getBucketName();
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();


        String region = ossProperties.getRegion();

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 删除文件。
            // 填写需要删除的多个文件完整路径。文件完整路径中不能包含Bucket名称。
            List<String> fileName = keys.stream().map(this::getPathFromUrl).collect(Collectors.toList());

            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(fileName).withEncodingType("url"));
            List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();

//            for (String obj : deletedObjects) {
//                String deleteObj = URLDecoder.decode(obj, StandardCharsets.UTF_8);
//                System.out.println(deleteObj);
//            }
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    private  String getPathFromUrl(String fileUrl) {
        // 1. 前置条件检查，保证代码健壮性
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            // 在实际项目中，你可能希望在这里打印日志或抛出自定义异常
            return null;
        }

        try {
            // 2. 使用Java内置的URL类进行专业解析
            URL url = new URL(fileUrl);
            String path = url.getPath();

            // 3. 处理返回结果
            // url.getPath() 返回的路径会以'/'开头, 例如 "/2025/09/...", 通常我们需要移除它
            if (path != null && path.startsWith("/")) {
                return path.substring(1);
            }

            return path;

        } catch (MalformedURLException e) {
            // 如果传入的字符串不是一个合法的URL，URL的构造函数会抛出此异常
            log.info("提供的字符串不是一个有效的URL: {}", fileUrl);
            e.printStackTrace(); // 打印异常堆栈，便于调试
            return null;
        }
    }

}
