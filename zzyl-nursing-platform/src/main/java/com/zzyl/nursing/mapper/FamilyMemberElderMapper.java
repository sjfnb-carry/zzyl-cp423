package com.zzyl.nursing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzyl.nursing.vo.ElderInfoVo;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.zzyl.nursing.domain.FamilyMemberElder;

/**
 * 家庭成员与老人关联Mapper接口
 * 
 * @author alexis
 * @date 2025-09-21
 */
@Mapper
public interface FamilyMemberElderMapper extends BaseMapper<FamilyMemberElder>
{
    /**
     * 查询家庭成员与老人关联
     * 
     * @param id 家庭成员与老人关联主键
     * @return 家庭成员与老人关联
     */
    public FamilyMemberElder selectFamilyMemberElderById(Long id);

    /**
     * 查询家庭成员与老人关联列表
     * 
     * @param familyMemberElder 家庭成员与老人关联
     * @return 家庭成员与老人关联集合
     */
    public List<FamilyMemberElder> selectFamilyMemberElderList(FamilyMemberElder familyMemberElder);

    /**
     * 新增家庭成员与老人关联
     * 
     * @param familyMemberElder 家庭成员与老人关联
     * @return 结果
     */
    public int insertFamilyMemberElder(FamilyMemberElder familyMemberElder);

    /**
     * 修改家庭成员与老人关联
     * 
     * @param familyMemberElder 家庭成员与老人关联
     * @return 结果
     */
    public int updateFamilyMemberElder(FamilyMemberElder familyMemberElder);

    /**
     * 删除家庭成员与老人关联
     * 
     * @param id 家庭成员与老人关联主键
     * @return 结果
     */
    public int deleteFamilyMemberElderById(Long id);

    /**
     * 批量删除家庭成员与老人关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFamilyMemberElderByIds(Long[] ids);

    List<ElderInfoVo> listByPage(IPage<FamilyMemberElder> page);
}
