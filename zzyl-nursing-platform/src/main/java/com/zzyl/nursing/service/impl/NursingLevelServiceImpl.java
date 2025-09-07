package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.service.INursingLevelService;
import com.zzyl.nursing.vo.NursingLevelVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 护理等级Service业务层处理
 *
 * @author alexis
 * @date 2024-12-30
 */
@Service
public class NursingLevelServiceImpl extends ServiceImpl<NursingLevelMapper, NursingLevel> implements INursingLevelService {
    @Autowired
    private NursingLevelMapper nursingLevelMapper;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "nursingLevel:all";

    /**
     * 查询护理等级
     *
     * @param id 护理等级主键
     * @return 护理等级
     */
    @Override
    public NursingLevel selectNursingLevelById(Long id) {
        return nursingLevelMapper.selectById(id);
    }

    /**
     * 查询护理等级列表
     *
     * @param nursingLevel 护理等级
     * @return 护理等级
     */
    @Override
    public List<NursingLevel> selectNursingLevelList(NursingLevel nursingLevel) {
        return nursingLevelMapper.selectNursingLevelList(nursingLevel);
    }

    /**
     * 新增护理等级
     *
     * @param nursingLevel 护理等级
     * @return 结果
     */
    @Override
    public int insertNursingLevel(NursingLevel nursingLevel) {
        int num = nursingLevelMapper.insert(nursingLevel);
        redisTemplate.delete(CACHE_KEY_PREFIX);
        return num;
    }

    /**
     * 修改护理等级
     *
     * @param nursingLevel 护理等级
     * @return 结果
     */
    @Override
    public int updateNursingLevel(NursingLevel nursingLevel) {
        int num = nursingLevelMapper.updateById(nursingLevel);
        redisTemplate.delete(CACHE_KEY_PREFIX);
        return num;
    }

    /**
     * 批量删除护理等级
     *
     * @param ids 需要删除的护理等级主键
     * @return 结果
     */
    @Override
    public int deleteNursingLevelByIds(Long[] ids) {
        int num = nursingLevelMapper.deleteBatchIds(Arrays.asList(ids));
        redisTemplate.delete(CACHE_KEY_PREFIX);
        return num;
    }

    /**
     * 删除护理等级信息
     *
     * @param id 护理等级主键
     * @return 结果
     */
    @Override
    public int deleteNursingLevelById(Long id) {
        int num = nursingLevelMapper.deleteById(id);
        redisTemplate.delete(CACHE_KEY_PREFIX);
        return num;
    }

    /**
     * 查询护理等级Vo列表
     *
     * @param nursingLevel 条件
     * @return 结果
     */
    @Override
    public List<NursingLevelVo> selectNursingLevelVoList(NursingLevel nursingLevel) {
        return nursingLevelMapper.selectNursingLevelVoList(nursingLevel);
    }

    /**
     * 查询所有护理等级
     *
     * @return 护理等级列表
     */
    @Override
    public List<NursingLevel> getAll() {
        List<NursingLevel> list = (List<NursingLevel>) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX);
        if (!CollectionUtils.isEmpty(list)) {
            return list;
        }
        LambdaQueryWrapper<NursingLevel> qw = new LambdaQueryWrapper<>();
        qw.eq(NursingLevel::getStatus, 1).orderByDesc(NursingLevel::getCreateTime);
        List<NursingLevel> nursingLevelList = nursingLevelMapper.selectList(qw);
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX, nursingLevelList);
        return nursingLevelList;

    }
}
