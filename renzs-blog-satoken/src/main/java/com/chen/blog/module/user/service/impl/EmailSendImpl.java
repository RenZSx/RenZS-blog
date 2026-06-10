package com.chen.blog.module.user.service.impl;


import com.aliyun.dm20151123.Client;
import com.aliyun.dm20151123.models.SingleSendMailRequest;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.chen.blog.module.user.dto.EmailDTO;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.user.service.EmailSendService;
import com.chen.blog.common.service.RedisService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import static com.aliyun.teautil.Common.assertAsString;
import static com.chen.blog.common.constant.RedisPrefixConst.CODE_EXPIRE_TIME;
import static com.chen.blog.common.constant.RedisPrefixConst.USER_CODE_KEY;
import static com.chen.blog.common.util.CommonUtils.checkEmail;
import static com.chen.blog.common.util.CommonUtils.getRandomCode;

@Data
@Service
@ConfigurationProperties(prefix = "aliyunmail")
@Slf4j
public class EmailSendImpl implements EmailSendService {

    @Autowired
    private RedisService redisService;

    private  String accessKey ;
    private  String secretKey;
    private  String endpoint;
    //邮件回复的人
    private  String replyAddress;
    //发送邮件地址
    private  String accountName;
    public Client createClient() throws Exception {
        Config config = new Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(accessKey)
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(secretKey);
        // Endpoint 请参考 https://api.aliyun.com/product/Dm
        config.endpoint = endpoint;
        return new Client(config);
    }
    @Override
    public void sendCode(String username) {
        // 校验账号是否合法
        if (!checkEmail(username)) {
            throw new BizException("请输入正确邮箱");
        }
        // 生成六位随机验证码发送
        String code = getRandomCode();
        // 发送验证码
//        EmailDTO emailDTO = EmailDTO.builder()
//                .email(username)
//                .subject("验证码")
//                .content("您的验证码为 " + code + " 有效期15分钟，请不要告诉他人哦！")
//                .build();
//        rabbitTemplate.convertAndSend(MQPrefixConst.EMAIL_EXCHANGE, "*", new Message(JSON.toJSONBytes(emailDTO), new MessageProperties()));
        //阿里发送验证码
        Client client = null;
        try {
            client = createClient();
        } catch (Exception e) {
            throw new BizException("createClient出错了！请联系博主！");
        }
        SingleSendMailRequest singleSendMailRequest = new SingleSendMailRequest()
                .setAccountName(accountName)
                .setAddressType(1)
                //接受验证码的人
                .setToAddress(username)
                .setReplyToAddress(true)
                //邮件标签
                .setTagName("test")
                //发送的标题
                .setSubject("验证码")
                .setFromAlias("任子双博客网站")
                //发送的内容
                .setHtmlBody("您的验证码为 " + code + " 有效期15分钟，请不要告诉他人哦！")
                //收到邮件回复的人的邮箱
                .setReplyAddress(replyAddress);
        RuntimeOptions runtime = new RuntimeOptions();
        System.out.println("发送成功！");
        try {
            // 复制代码运行请自行打印 API 的返回值
            client.singleSendMailWithOptions(singleSendMailRequest, runtime);
        } catch (TeaException error) {
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            assertAsString(error.message);
            throw new BizException("出错了！请联系博主！");
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            assertAsString(error.message);
            throw new BizException("出错了！请联系博主！");
        }
        // 将验证码存入redis，设置过期时间为15分钟
        redisService.set(USER_CODE_KEY + username, code, CODE_EXPIRE_TIME);
    }

    @Override
    public void sendEmails(EmailDTO emailDTO) {
        log.info("开始发送邮件通知");
        // 校验账号是否合法
        if (!checkEmail(emailDTO.getEmail())) {
            throw new BizException("用户未填写邮箱，用户无法收到回复通知");
        }
        Client client = null;
        try {
            client = createClient();
        } catch (Exception e) {
            throw new BizException("createClient出错了！请联系博主！");
        }
        SingleSendMailRequest singleSendMailRequest = new SingleSendMailRequest()
                //发件地址
                .setAccountName(accountName)
                .setAddressType(1)
                //接受验证码的人的邮箱
                .setToAddress(emailDTO.getEmail())
                .setReplyToAddress(true)
                //邮件标签
                .setTagName("网站评论")
                //发送的标题
                .setSubject(emailDTO.getSubject())
                .setFromAlias("任子双博客网站")
                //发送的内容
                .setHtmlBody(emailDTO.getContent()+"<br>"+"评论内容："+emailDTO.getCommentContent())
                //收到邮件回复的人的邮箱
                .setReplyAddress(replyAddress);
        RuntimeOptions runtime = new RuntimeOptions();
        System.out.println("发送成功！");
        try {
            // 复制代码运行请自行打印 API 的返回值
            client.singleSendMailWithOptions(singleSendMailRequest, runtime);
            log.info("发送成功");
        } catch (TeaException error) {
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            assertAsString(error.message);
            throw new BizException("出错了！请联系博主！");
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            assertAsString(error.message);
            throw new BizException("出错了！请联系博主！");
        }
    }
}
