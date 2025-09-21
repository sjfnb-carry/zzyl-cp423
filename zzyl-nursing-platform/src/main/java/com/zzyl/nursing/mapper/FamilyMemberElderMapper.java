package com.zzyl.nursing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.zzyl.nursing.domain.FamilyMemberElder;

/**
 * 老人-家属关联中间Mapper接口
 * 
 * @author alexis
 * @date 2025-09-20
 */
@Mapper
public interface FamilyMemberElderMapper extends BaseMapper<FamilyMemberElder>
{
    /**
     * 查询老人-家属关联中间
     * 
     * @param id 老人-家属关联中间主键
     * @return 老人-家属关联中间
     */
    public FamilyMemberElder selectFamilyMemberElderById(Long id);

    /**
     * 查询老人-家属关联中间列表
     * 
     * @param familyMemberElder 老人-家属关联中间
     * @return 老人-家属关联中间集合
     */
    public List<FamilyMemberElder> selectFamilyMemberElderList(FamilyMemberElder familyMemberElder);

    /**
     * 新增老人-家属关联中间
     * 
     * @param familyMemberElder 老人-家属关联中间
     * @return 结果
     */
    public int insertFamilyMemberElder(FamilyMemberElder familyMemberElder);

    /**
     * 修改老人-家属关联中间
     * 
     * @param familyMemberElder 老人-家属关联中间
     * @return 结果
     */
    public int updateFamilyMemberElder(FamilyMemberElder familyMemberElder);

    /**
     * 删除老人-家属关联中间
     * 
     * @param id 老人-家属关联中间主键
     * @return 结果
     */
    public int deleteFamilyMemberElderById(Long id);

    /**
     * 批量删除老人-家属关联中间
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFamilyMemberElderByIds(Long[] ids);
}
