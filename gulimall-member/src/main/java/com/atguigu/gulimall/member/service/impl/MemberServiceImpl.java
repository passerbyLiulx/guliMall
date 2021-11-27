package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo memberRegistVo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = new MemberEntity();
        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        // 校验用户名和手机号唯一
        checkPhoneUnique(memberRegistVo.getPhone());
        checkUserNameUnique(memberRegistVo.getUserName());

        memberEntity.setMobile(memberRegistVo.getPhone());
        memberEntity.setUsername(memberRegistVo.getUserName());
        // 密码加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(memberRegistVo.getPassword());
        memberEntity.setPassword(encode);

        memberDao.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAcct = vo.getLoginAcct();
        String password = vo.getPassword();
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, loginAcct)
                .or().eq(MemberEntity::getMobile, loginAcct));
        if (memberEntity != null) {
            String passwordDb = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, passwordDb);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public MemberEntity oauthLogin(SocialUserVo socialUserVo) throws Exception {
        //登录和注册合并逻辑
        String uid = socialUserVo.getUid();
        MemberEntity memberEntity = baseMapper.selectOne(new LambdaQueryWrapper<MemberEntity>()
                .eq(MemberEntity::getSocialUid, uid));
        if (memberEntity != null) {
            // 用户已经注册
            MemberEntity updateMemberEntity = new MemberEntity();
            updateMemberEntity.setId(memberEntity.getId());
            updateMemberEntity.setAccessToken(socialUserVo.getAccess_token());
            updateMemberEntity.setExpiresIn(socialUserVo.getExpires_in());
            baseMapper.updateById(updateMemberEntity);

            memberEntity.setAccessToken(socialUserVo.getAccess_token());
            memberEntity.setExpiresIn(socialUserVo.getExpires_in());
            return memberEntity;
        } else {
            // 用户没有注册
            MemberEntity saveMemberEntity = new MemberEntity();
            try {
                // 查询当前社交用户的社交账号信息
                Map<String, String> query = new HashMap<>();
                query.put("access_token", socialUserVo.getAccess_token());
                query.put("uid", socialUserVo.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), null);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String jsonStr = EntityUtils.toString((response.getEntity()));
                    JSONObject jsonObject = JSON.parseObject(jsonStr);
                    saveMemberEntity.setNickname(jsonObject.getString("name"));
                    saveMemberEntity.setGender("m".equals(jsonObject.getString("gender")) ? 1 : 0);
                }
            } catch (Exception e) {

            }
            saveMemberEntity.setSocialUid(socialUserVo.getUid());
            saveMemberEntity.setAccessToken(socialUserVo.getAccess_token());
            saveMemberEntity.setExpiresIn(socialUserVo.getExpires_in());
            baseMapper.insert(saveMemberEntity);

        }
        return null;
    }

}