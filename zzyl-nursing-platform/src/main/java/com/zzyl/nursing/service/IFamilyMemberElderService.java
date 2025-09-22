package com.zzyl.nursing.service;

import java.util.List;
import com.zzyl.nursing.domain.FamilyMemberElder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzyl.nursing.vo.ElderInfoVo;

/**
 * 家庭成员与老人关联Service接口
 * 
 * @author alexis
 * @date 2025-09-21
 */
public interface IFamilyMemberElderService extends IService<FamilyMemberElder>
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
     * 批量删除家庭成员与老人关联
     * 
     * @param ids 需要删除的家庭成员与老人关联主键集合
     * @return 结果
     */
    public int deleteFamilyMemberElderByIds(Long[] ids);

    /**
     * 删除家庭成员与老人关联信息
     * 
     * @param id 家庭成员与老人关联主键
     * @return 结果
     */
    public int deleteFamilyMemberElderById(Long id);

    List<ElderInfoVo> listByPage(Integer pageNum, Integer pageSize);
}
