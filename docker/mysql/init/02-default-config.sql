SET NAMES utf8mb4;
USE chen_blog;

INSERT IGNORE INTO tb_website_config (id, config, create_time, update_time)
VALUES (
  1,
  '{"websiteAvatar":"","websiteName":"Renzs Blog","websiteAuthor":"Renzs","websiteIntro":"一个基于 Spring Boot 和 Vue 的博客","websiteNotice":"","websiteCreateTime":"","websiteVerse":"","websiteRecordNo":"","websitePoliceRecordNo":"","websiteBgAddress":"","socialLoginList":[],"socialUrlList":[],"qq":"","github":"","gitee":"","touristAvatar":"","userAvatar":"","isCommentReview":0,"isMessageReview":0,"isEmailNotice":0,"isEmailRegister":0,"isReward":0,"weiXinQRCode":"","alipayQRCode":"","articleCover":"","isChatRoom":0,"websocketUrl":"","isMusicPlayer":0,"isAiSummary":0,"aiApiUrl":"","aiApiKey":"","aiModel":"","aiApiType":"chat_completions","aiReasoningEffort":"","aiDisableResponseStorage":1,"aiSummaryPrompt":""}',
  NOW(),
  NOW()
);
