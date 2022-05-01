package com.github.hcsp.course.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/api/v1")
public class VideoController {

    /**
     * @api {get} /api/v1/course/{id}/token 获取上传视频所需token
     * @apiName 获取在指定课程下上传视频所需token等验证信息
     * @apiGroup 视频管理
     * @apiDescription
     *  验证信息不止包括token。详见 https://help.aliyun.com/document_detail/31927.html
     *
     *  当客户端上传成功时，应调用createVideo接口发起一个新的POST请求将视频URL发给应用。
     *
     * @apiHeader {String} Accept application/json
     * @apiParam {Number} id 课程id
     *
     * @apiParamExample Request-Example:
     *   GET /api/v1/course/12345/token
     * @apiSuccess {String} accessid
     * @apiSuccess {String} host
     * @apiSuccess {String} policy
     * @apiSuccess {String} signature
     * @apiSuccess {Number} expire
     * @apiSuccess {String} dir
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "accessid":"6MKO******4AUk44",
     *       "host":"http://post-test.oss-cn-hangzhou.aliyuncs.com",
     *       "policy":"MCwxMDQ4NTc2MDAwXSxbInN0YXJ0cy13aXRoIiwiJGtleSIsInVzZXItZGlyXC8iXV19",
     *       "signature":"VsxOcOudx******z93CLaXPz+4s=",
     *       "expire":1446727949,
     *       "dir":"user-dirs/"
     *     }
     * @apiError 400 Bad Request 若请求中包含错误
     * @apiError 401 Unauthorized 若未登录
     * @apiError 403 Forbidden 若无权限
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @return
     */
    @GetMapping("/course/{id}/token")
    public Token getToken(@PathVariable("id") Integer courseId) {
        String accessKeyId = "yourAccessKeyId"; // 请填写您的AccessKeyId。
        String accessKeySecret = "yourAccessKeySecret"; // 请填写您的AccessKeySecret。
        String endpoint = "oss-cn-guangzhou.aliyuncs.com"; // 请填写您的 endpoint。
        String bucket = "bing-course"; // 请填写您的 bucketname 。
        String host = "http://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
        String dir = "course-" + courseId + "/"; // 用户上传文件时指定的前缀。

        OSSClient client = new OSSClient(endpoint, new DefaultCredentialProvider(accessKeyId, accessKeySecret), null);

        // 1970年来经过的秒数
        long expireTimeSeconds = 30;
        long expireEndTimeMillSeconds = System.currentTimeMillis() + expireTimeSeconds * 1000;
        Date expiration = new Date(expireEndTimeMillSeconds);
        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

        String postPolicy = client.generatePostPolicy(expiration, policyConds);
        byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = client.calculatePostSignature(postPolicy);

        Token token = new Token();
        token.setAccessid(accessKeyId);
        token.setPolicy(encodedPolicy);
        token.setSignature(postSignature);
        token.setDir(dir);
        token.setHost(host);
        token.setExpire(expireEndTimeMillSeconds / 1000);

        return token;
    }

    @GetMapping("/video/{id}")
    public String getVideo(@PathVariable("id") String id) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-guangzhou.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "yourAccessKeyId";
        String accessKeySecret = "yourAccessKeySecret";
        // 从STS服务获取的安全令牌（SecurityToken）。
        //String securityToken = "yourSecurityToken";
        // 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = "course-12345/" + id + ".jpg";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 设置签名URL过期时间，单位为毫秒。
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        // 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
        URL url = ossClient.generatePresignedUrl("bing-course", objectName, expiration);

        return "<html><body><a href='" + url + "'>打开视频</></body></html>";
    }

    static class Token {
        private String accessid;
        private String host;
        private String policy;
        private String signature;
        private long expire;
        private String dir;

        public String getAccessid() {
            return accessid;
        }

        public void setAccessid(String accessid) {
            this.accessid = accessid;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public long getExpire() {
            return expire;
        }

        public void setExpire(long expire) {
            this.expire = expire;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }
}
