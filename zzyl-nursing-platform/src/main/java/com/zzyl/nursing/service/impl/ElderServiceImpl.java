package com.zzyl.nursing.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.nursing.domain.Elder;
import com.zzyl.nursing.dto.ElderPageQueryDto;
import com.zzyl.nursing.mapper.ElderMapper;
import com.zzyl.nursing.service.IElderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 老人Service业务层处理
 *
 * @author alexis
 * @date 2025-09-04
 */
@Service
public class ElderServiceImpl extends ServiceImpl<ElderMapper, Elder> implements IElderService {
    @Autowired
    private ElderMapper elderMapper;

    /**
     * 查询老人
     *
     * @param id 老人主键
     * @return 老人
     */
    @Override
    public Elder selectElderById(Long id) {
        return elderMapper.selectById(id);
    }

    /**
     * 查询老人列表
     *
     * @param elder 老人
     * @return 老人
     */
    @Override
    public List<Elder> selectElderList(Elder elder) {
        return elderMapper.selectElderList(elder);
    }

    /**
     * 新增老人
     *
     * @param elder 老人
     * @return 结果
     */
    @Override
    public int insertElder(Elder elder) {
        return elderMapper.insert(elder);
    }

    /**
     * 修改老人
     *
     * @param elder 老人
     * @return 结果
     */
    @Override
    public int updateElder(Elder elder) {
        return elderMapper.updateById(elder);
    }

    /**
     * 批量删除老人
     *
     * @param ids 需要删除的老人主键
     * @return 结果
     */
    @Override
    public int deleteElderByIds(Long[] ids) {
        return elderMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除老人信息
     *
     * @param id 老人主键
     * @return 结果
     */
    @Override
    public int deleteElderById(Long id) {
        return elderMapper.deleteById(id);
    }

    @Override
    public TableDataInfo<Elder> pageQuery(ElderPageQueryDto dto) {
        LambdaQueryWrapper<Elder> qw = new LambdaQueryWrapper<>();
        qw.like(StrUtil.isNotEmpty(dto.getName()),Elder::getName, dto.getName())
                .eq(StrUtil.isNotEmpty(dto.getIdCardNo()), Elder::getIdCardNo, dto.getIdCardNo())
                .eq(dto.getStatus()!=null,Elder::getStatus, dto.getStatus());
        IPage<Elder> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<Elder> elderIPage = elderMapper.selectPage(page, qw);
        TableDataInfo<Elder> tableDataInfo = new TableDataInfo<>();
        tableDataInfo.setTotal(elderIPage.getTotal());
        tableDataInfo.setRows(elderIPage.getRecords());
        return tableDataInfo;
    }
}
