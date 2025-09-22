package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.nursing.domain.FamilyMemberElder;
import com.zzyl.nursing.mapper.FamilyMemberElderMapper;
import com.zzyl.nursing.service.IFamilyMemberElderService;
import com.zzyl.nursing.vo.ElderInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 家庭成员与老人关联Service业务层处理
 *
 * @author alexis
 * @date 2025-09-21
 */
@Service
public class FamilyMemberElderServiceImpl extends ServiceImpl<FamilyMemberElderMapper, FamilyMemberElder> implements IFamilyMemberElderService {
    @Autowired
    private FamilyMemberElderMapper familyMemberElderMapper;

    /**
     * 查询家庭成员与老人关联
     *
     * @param id 家庭成员与老人关联主键
     * @return 家庭成员与老人关联
     */
    @Override
    public FamilyMemberElder selectFamilyMemberElderById(Long id) {
        return familyMemberElderMapper.selectById(id);
    }

    /**
     * 查询家庭成员与老人关联列表
     *
     * @param familyMemberElder 家庭成员与老人关联
     * @return 家庭成员与老人关联
     */
    @Override
    public List<FamilyMemberElder> selectFamilyMemberElderList(FamilyMemberElder familyMemberElder) {
        return familyMemberElderMapper.selectFamilyMemberElderList(familyMemberElder);
    }

    /**
     * 新增家庭成员与老人关联
     *
     * @param familyMemberElder 家庭成员与老人关联
     * @return 结果
     */
    @Override
    public int insertFamilyMemberElder(FamilyMemberElder familyMemberElder) {
        return familyMemberElderMapper.insert(familyMemberElder);
    }

    /**
     * 修改家庭成员与老人关联
     *
     * @param familyMemberElder 家庭成员与老人关联
     * @return 结果
     */
    @Override
    public int updateFamilyMemberElder(FamilyMemberElder familyMemberElder) {
        return familyMemberElderMapper.updateById(familyMemberElder);
    }

    /**
     * 批量删除家庭成员与老人关联
     *
     * @param ids 需要删除的家庭成员与老人关联主键
     * @return 结果
     */
    @Override
    public int deleteFamilyMemberElderByIds(Long[] ids) {
        return familyMemberElderMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除家庭成员与老人关联信息
     *
     * @param id 家庭成员与老人关联主键
     * @return 结果
     */
    @Override
    public int deleteFamilyMemberElderById(Long id) {
        return familyMemberElderMapper.deleteById(id);
    }

    @Override
    public List<ElderInfoVo> listByPage(Integer pageNum, Integer pageSize) {
        IPage<FamilyMemberElder> page = new Page<>(pageNum, pageSize);
        List<ElderInfoVo> list = familyMemberElderMapper.listByPage(page);
        return list;

    }
}
