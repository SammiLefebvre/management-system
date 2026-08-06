package edu.cdut.aiback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cdut.aiback.entity.CodeTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CodeTableMapper extends BaseMapper<CodeTable> {

    @Select("SELECT * FROM code_table WHERE code_type = #{codeType} AND status = 1 ORDER BY sort_order")
    List<CodeTable> selectByType(@Param("codeType") String codeType);
}
