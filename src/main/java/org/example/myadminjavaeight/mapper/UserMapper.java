package org.example.myadminjavaeight.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.myadminjavaeight.domain.entity.sys.SysUser;

@Mapper
public interface UserMapper {
    
    SysUser findByUsername(@Param("username") String username);

    /* 查询用户信息并计算账户锁定剩余时间 */
    SysUser findByUsernameWithLockInfo(@Param("username") String username);

    SysUser findById(@Param("id") Long id);

    int insert(SysUser user);

    int incrementFailedAttempts(
      @Param("username") String username,
      @Param("maxAttempts") int maxAttempts,
      @Param("lockDurationSeconds") long lockDurationSeconds);

    int resetFailedAttempts(@Param("id") Long id);
    
    int unlockUser(@Param("id") Long id);
}
