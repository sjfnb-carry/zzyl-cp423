package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.nursing.domain.Contract;
import com.zzyl.nursing.mapper.ContractMapper;
import com.zzyl.nursing.service.IContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 合同Service业务层处理
 *
 * @author alexis
 * @date 2025-09-04
 */
@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements IContractService {
    @Autowired
    private ContractMapper contractMapper;

    /**
     * 查询合同
     *
     * @param id 合同主键
     * @return 合同
     */
    @Override
    public Contract selectContractById(Long id) {
        return contractMapper.selectById(id);
    }

    /**
     * 查询合同列表
     *
     * @param contract 合同
     * @return 合同
     */
    @Override
    public List<Contract> selectContractList(Contract contract) {
        return contractMapper.selectContractList(contract);
    }

    /**
     * 新增合同
     *
     * @param contract 合同
     * @return 结果
     */
    @Override
    public int insertContract(Contract contract) {
        return contractMapper.insert(contract);
    }

    /**
     * 修改合同
     *
     * @param contract 合同
     * @return 结果
     */
    @Override
    public int updateContract(Contract contract) {
        return contractMapper.updateById(contract);
    }

    /**
     * 批量删除合同
     *
     * @param ids 需要删除的合同主键
     * @return 结果
     */
    @Override
    public int deleteContractByIds(Long[] ids) {
        return contractMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除合同信息
     *
     * @param id 合同主键
     * @return 结果
     */
    @Override
    public int deleteContractById(Long id) {
        return contractMapper.deleteById(id);
    }

    /**
     * 更新合同状态
     */
    @Override
    public void updateContractStatus() {
        LambdaUpdateWrapper<Contract> uw = new LambdaUpdateWrapper<>();
        uw.set(Contract::getStatus, 1)
                .set(Contract::getUpdateBy, "system")
                .set(Contract::getUpdateTime, LocalDateTime.now())
                .eq(Contract::getStatus, 0)
                .le(Contract::getStartDate, LocalDateTime.now());
        update(uw);


//        List<Contract> contractList = list(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, 0).le(Contract::getStartDate, LocalDateTime.now()));
//        if (CollectionUtil.isNotEmpty(contractList)) {
//            LambdaUpdateWrapper<Contract> uqw = new LambdaUpdateWrapper<>();
//            List<Long> ids = contractList.stream().map(Contract::getId).collect(Collectors.toList());
//            uqw.set(Contract::getStatus, 1)
//                    .set(Contract::getUpdateBy,"system")
//                    .set(Contract::getUpdateTime, LocalDateTime.now())
//                    .in(Contract::getId, ids);
//            update(uqw);
//
//            contractList.forEach(contract -> {
//                contract.setStatus(1);
//            });
//            updateBatchById(contractList);
//        }


    }
}
