package com.hc.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hc.common.enums.HttpCodeEnum;
import com.hc.common.exception.ServiceException;
import com.hc.common.lang.Constants;
import com.hc.component.RedisComponent;
import com.hc.entity.EmailCode;
import com.hc.entity.UserInfo;
import com.hc.entity.dto.SysSettingsDto;
import com.hc.mapper.EmailCodeMapper;
import com.hc.mapper.UserInfoMapper;
import com.hc.service.EmailCodeService;
import com.hc.utils.RandomNumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author: 何超
 * @date: 2023-06-07 20:16
 * @description:
 */
@Service
public class EmailCodeServiceImpl extends ServiceImpl<EmailCodeMapper, EmailCode> implements EmailCodeService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    EmailCodeMapper emailCodeMapper;

    @Autowired
    JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    RedisComponent redisComponent;

    /**
     * 发送邮箱验证码
     *
     * @param email
     * @param type
     */
    @Override
    public void sendEmailCode(String email, Integer type) {
        if (type.equals(Constants.ZERO)) {
            UserInfo userInfo = userInfoMapper.selectByEmail(email);
            if (null != userInfo) {
                throw new ServiceException(HttpCodeEnum.CODE_407);
            }
        }
        String code = RandomNumberUtil.getRandomNumber(Constants.LENGTH_6);
        // 发送验证码
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        SysSettingsDto sysSettingsDto = redisComponent.sysSettingsDto();
        mailMessage.setTo(email);
        mailMessage.setFrom(from);
        mailMessage.setSubject(sysSettingsDto.getRegisterEMailTitle());
        mailMessage.setText(String.format(sysSettingsDto.getRegisterEmailContent(), code));
        mailMessage.setSentDate(new Date());
        javaMailSender.send(mailMessage);
        // 将之前的验证码值为无效
        emailCodeMapper.disableEmailCode(email);
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(code);
        emailCode.setEmail(email);
        emailCode.setStatus(Constants.ZERO);
        emailCode.setCreateTime(new Date());
        emailCodeMapper.insert(emailCode);
    }

    /**
     * 校验验证码
     *
     * @param email
     * @param code
     */
    @Override
    public void checkCode(String email, String code) {
        EmailCode emailCode = emailCodeMapper.selectByEmailAndCode(email, code);
        if (null == emailCode) {
            throw new ServiceException(HttpCodeEnum.CODE_411);
        }
        if (emailCode.getStatus() == 1 || System.currentTimeMillis() - emailCode.getCreateTime().getTime() > Constants.LENGTH_5 * 1000 * 60) {
            throw new ServiceException(HttpCodeEnum.CODE_412);
        }
        emailCodeMapper.disableEmailCode(email);
    }
}
