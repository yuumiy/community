package com.community.controller;

import com.community.entity.Comment;
import com.community.entity.DiscussPost;
import com.community.entity.Page;
import com.community.entity.User;
import com.community.service.*;
import com.community.util.CommunityConstant;
import com.community.util.CommunityUtil;
import com.community.util.HostHolder;
import com.community.util.RedisKeyUtil;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //从hostHolder能得到当前的用户是谁
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

//    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

//    @LoginRequired
    //废弃
    @RequestMapping(path = "/upload", method = {RequestMethod.GET,RequestMethod.POST})
    //model变量用来向页面返回数据
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        if(fileName.lastIndexOf(".")==-1){
            model.addAttribute("error", "图片的格式不正确!");
            return "/site/setting";
        }

        //suffix表示后缀名，从.开始截取，如果加1，就是png
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        if(!suffix.equals("png")||!suffix.equals("jpg")||!suffix.equals("jpeg")){
            model.addAttribute("error", "图片仅支持png、jpg、jpeg格式");
            return "/site/setting";
        }
        suffix = fileName.substring(fileName.lastIndexOf("."));

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径(web访问路径)，从hostholder得到当前用户
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //本地磁盘的图片，映射到服务器上也能访问
    //http://localhost:8080/community/user/header/ce3a80b67a994cf9b08167827bb2c006.jpg
    //废弃
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀，得到png
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

//    @LoginRequired
    @RequestMapping(path = "/changePassword", method = {RequestMethod.GET,RequestMethod.POST})
    //修改密码，model变量用来向页面返回数据
    public String changePassword(String oldPassword,String newPassword,String confirmPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.changePassword(user,oldPassword, newPassword, confirmPassword);
        if(map == null || map.isEmpty()){
            return "redirect:/index";
        }else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
            return "/site/setting";
        }
    }

    @RequestMapping(path = "/changePasswordByCode", method = {RequestMethod.GET,RequestMethod.POST})
    //修改密码，model变量用来向页面返回数据
    public String changePasswordByCode(String email,String code,String password, Model model,
            @CookieValue("codeOwner") String codeOwner) {
        String kaptcha = null;
        try {
            if (StringUtils.isNotBlank(codeOwner)) {
                String redisKey = RedisKeyUtil.getCodeKey(codeOwner);
                kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
            }
        }catch (Exception e) {
            model.addAttribute("codeMsg", "验证码失效!");
            return "/site/forget";
        }


        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equals(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/forget";
        }
        Map<String, Object> map = userService.changePasswordByCode(email, password);
        if (map.containsKey("success")) {
            return "redirect:/login";
        } else {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }

    @RequestMapping(path = "/forgetPassword", method = RequestMethod.GET)
    //忘记密码
    public String forgetPassword() {
        return "/site/forget";
    }

    @RequestMapping(path = "/sendCode", method = RequestMethod.POST)
    //向邮箱发送验证码
    @ResponseBody
    public String sendCode(String email,HttpServletResponse response){
        User user=userService.findUserByEmail(email);
        if(user==null){
            return CommunityUtil.getJSONString(1,"您输入的邮箱有误");
        }
        userService.sendCode(email,response);
        return CommunityUtil.getJSONString(0);
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model,
                                 @RequestParam(name = "infoMode", defaultValue = "0") int infoMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        model.addAttribute("infoMode", infoMode);

        return "/site/profile";
    }

   //跳转到我的帖子页面
    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String toMyPost(@PathVariable("userId") int userId,Model model, Page page,
                           @RequestParam(name = "infoMode", defaultValue = "1") int infoMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        // 设置分页信息
        page.setLimit(5);
        page.setRows(discussPostService.findDiscussPostRows(user.getId()));
        page.setPath("/user/mypost/"+userId);


        // 查询某用户发布的帖子
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(user.getId(), page.getOffset(), page.getLimit(),0);
        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost post : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                // 点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                list.add(map);
            }
            model.addAttribute("discussPosts", list);
        }
        // 帖子数量
        int postCount = discussPostService.findDiscussPostRows(user.getId());
        model.addAttribute("postCount", postCount);
        model.addAttribute("infoMode", infoMode);

        return "site/my-post";
    }

    //跳转到我的评论页面
    @RequestMapping(path = "/mycomment/{userId}", method = RequestMethod.GET)
    public String toMyReply(@PathVariable("userId") int userId,Model model, Page page,
                            @RequestParam(name = "infoMode", defaultValue = "2") int infoMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        // 设置分页信息
        page.setLimit(5);
        page.setRows(commentService.findCommentCountById(user.getId()));
        page.setPath("/user/mycomment/"+userId);

        // 获取用户所有评论 (而不是回复,所以在 sql 里加一个条件 entity_type = 1)
        List<Comment> comments = commentService.findCommentsByUserId(user.getId(),page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);

                // 根据实体 id 查询对应的帖子标题
                String discussPostTitle = discussPostService.findDiscussPostById(comment.getEntityId()).getTitle();
                map.put("discussPostTitle", discussPostTitle);

                list.add(map);
            }
            model.addAttribute("comments", list);
        }

        // 回复的数量
        int commentCount = commentService.findCommentCountById(user.getId());
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("infoMode", infoMode);

        return "site/my-comment";
    }



}
