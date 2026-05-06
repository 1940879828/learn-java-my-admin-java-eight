package org.example.myadminjavaeight.security;

import org.example.myadminjavaeight.domain.entity.sys.SysUser;
import org.example.myadminjavaeight.exception.UserDisabledException;
import org.example.myadminjavaeight.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.findByUsernameWithLockInfo(username);

        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在"+username);
        }

        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) {
            throw new UserDisabledException();
        }

        if (sysUser.getLockTime() != null) {
            Long lockRemaining = sysUser.getLockRemainingSeconds();
            if (lockRemaining != null && lockRemaining > 0) {
                log.warn("[UserDetails] 账户已锁定: {}, 剩余锁定时间: {}秒", username, lockRemaining);
                // 不抛出异常，通过 UserDetails.isAccountNonLocked() 返回锁定状态
                // Spring Security 会自动处理并抛出标准的 LockedException
            } else {
                userMapper.unlockUser(sysUser.getId());
                sysUser.setFailedAttempts(0);
                sysUser.setLockTime(null);
            }
        }
        return new JwtUserDetails(sysUser);
    }
}
