package org.example.myadminjavaeight.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.myadminjavaeight.domain.entity.sys.SysLoginLog;

@Mapper
public interface LoginLogMapper {
    
    int insert(SysLoginLog log);
}
