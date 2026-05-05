package org.example.myadminjavaeight.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.myadminjavaeight.domain.entity.sys.SysRefreshToken;

@Mapper
public interface RefreshTokenMapper {

    int insert(SysRefreshToken token);

    SysRefreshToken findByTokenHash(@Param("tokenHash") String tokenHash);

    int deleteByUserId(@Param("userId") Long userId);

    int deleteByJtiId(@Param("jtiId") String jtiId);
}
