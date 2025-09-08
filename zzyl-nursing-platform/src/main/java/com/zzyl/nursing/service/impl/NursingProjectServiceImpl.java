package com.zzyl.nursing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.nursing.domain.NursingProject;
import com.zzyl.nursing.mapper.NursingProjectMapper;
import com.zzyl.nursing.service.INursingProjectService;
import com.zzyl.nursing.vo.NursingProjectVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 护理项目Service业务层处理
 *
 * @author alexis
 * @date 2024-12-30
 */
@Service
public class NursingProjectServiceImpl extends ServiceImpl<NursingProjectMapper, NursingProject> implements INursingProjectService {
    @Autowired
    private NursingProjectMapper nursingProjectMapper;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "nursingProject:all";

    /**
     * 查询护理项目
     *
     * @param id 护理项目主键
     * @return 护理项目
     */
    @Override
    public NursingProject selectNursingProjectById(Long id) {
        return nursingProjectMapper.selectById(id);
    }

    /**
     * 查询护理项目列表
     *
     * @param nursingProject 护理项目
     * @return 护理项目
     */
    @Override
    public List<NursingProject> selectNursingProjectList(NursingProject nursingProject) {
        return nursingProjectMapper.selectNursingProjectList(nursingProject);
    }

    /**
     * 新增护理项目
     *
     * @param nursingProject 护理项目
     * @return 结果
     */
    @Override
    public int insertNursingProject(NursingProject nursingProject) {
        int num = nursingProjectMapper.insert(nursingProject);
        redisTemplate.delete(CACHE_KEY_PREFIX);
        redisTemplate.opsForSet().remove(CacheConstants.GARBAGE_FILE,nursingProject.getImage());
        return num;
    }

    /**
     * 修改护理项目
     *
     * @param nursingProject 护理项目
     * @return 结果
     */
    @Override
    public int updateNursingProject(NursingProject nursingProject) {
        int num = nursingProjectMapper.updateById(nursingProject);
        redisTemplate.opsForSet().remove(CacheConstants.GARBAGE_FILE,nursingProject.getImage());
        redisTemplate.delete(CACHE_KEY_PREFIX);
        return num;
    }

    /**
     * 批量删除护理项目
     *
     * @param ids 需要删除的护理项目主键
     * @return 结果
     */
    @Override
    public int deleteNursingProjectByIds(Long[] ids) {
        int num = nursingProjectMapper.deleteBatchIds(Arrays.asList(ids));
        redisTemplate.delete(CACHE_KEY_PREFIX);

        return num;
    }

    /**
     * 删除护理项目信息
     *
     * @param id 护理项目主键
     * @return 结果
     */
    @Override
    public int deleteNursingProjectById(Long id) {
        int num = nursingProjectMapper.deleteById(id);
        redisTemplate.delete(CACHE_KEY_PREFIX);
        return num;
    }

    /**
     * 查询所有护理项目VO列表
     *
     * @return 结果
     */
    @Override
    public List<NursingProjectVo> getAllProjects() {
        List<NursingProjectVo> voList = (List<NursingProjectVo>) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX);
        if (CollectionUtil.isNotEmpty(voList)) {
            return voList;
        }
        List<NursingProjectVo> nursingProjectVoList = nursingProjectMapper.getAllProjects();
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX, nursingProjectVoList);
        return nursingProjectVoList;
    }
}
