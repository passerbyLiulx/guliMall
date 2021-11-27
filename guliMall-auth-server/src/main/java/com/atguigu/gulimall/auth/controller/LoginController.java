package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    /*@GetMapping("/login.html")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/reg.html")
    public String regPage() {
        return "reg";
    }*/

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotBlank(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        String codeSpell = code + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, codeSpell, 120, TimeUnit.SECONDS);
        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            /*Map<String, String> errorMap = new HashMap<>();
            List<Map<String, String>> collect = result.getFieldErrors().stream().map(fieldError -> {
                String field = fieldError.getField();
                String message = fieldError.getDefaultMessage();
                errorMap.put(field, message);
                return errorMap;
            }).collect(Collectors.toList());*/
            /*Map<String, String> errorMap = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, fieldError -> {
                return fieldError.getDefaultMessage();
            }));*/
            Map<String, String> errorMap = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            //model.addAttribute("errors", errorMap);
            redirectAttributes.addFlashAttribute("errors", errorMap);
            // 校验出错
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        // 校验验证码
        String code = userRegistVo.getCode();
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
        if (StringUtils.isNotBlank(s)) {
            if (code.equals(s.split("_")[0])) {
                // 删除验证码
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
                // 验证通过
                R r = memberFeignService.regist(userRegistVo);
                if (r.getCode() == 0) {
                    return "redirect:/login.html";
                } else {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("msg", r.getData(new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errorMap);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errorMap);
                // 校验出错
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errorMap);
            // 校验出错
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes) {
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
