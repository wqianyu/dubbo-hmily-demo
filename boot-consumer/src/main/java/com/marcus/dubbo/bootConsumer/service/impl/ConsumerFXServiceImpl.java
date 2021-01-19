package com.marcus.dubbo.bootConsumer.service.impl;

import com.marcus.dubbo.boot.api.fxAccount.dto.FXAccountDTO;
import com.marcus.dubbo.boot.api.fxAccount.entity.FXAccountDO;
import com.marcus.dubbo.boot.api.fxAccount.mapper.FXAccountMapper;
import com.marcus.dubbo.boot.api.fxAccount.service.FXAccountService;
import com.marcus.dubbo.bootConsumer.service.ConsumerFXService;
import com.marcus.dubbo.bootConsumer.service.FXRateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ConsumerFXServiceImpl implements ConsumerFXService {

    @DubboReference
    private FXAccountService fxAccountService;

    private final FXAccountMapper fxAccountMapper;

    private final FXRateService fxRateService;

    @Autowired(required = false)
    public ConsumerFXServiceImpl(FXAccountMapper fxAccountMapper, FXRateService fxRateService) {
        this.fxAccountMapper = fxAccountMapper;
        this.fxRateService = fxRateService;
    }

    @Override
    public List<FXAccountDO> findAllFXAccount() {
        return fxAccountMapper.findAll();
    }

    @Override
    public FXAccountDO findFXAccountByUserId(long order_id) {
        return fxAccountMapper.findByUserId(order_id);
    }

    @Override
    @HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
    public int updateFXAccount(FXAccountDTO fxAccountDTO) {
        log.info("consumer update FXAccount called");
        fxAccountMapper.update(buildUpdateFXAccount(fxAccountDTO));
        //失败会引起HmilyTCC回滚，但是fxAccountService.updateFXAccount不一定回回滚
        //一段时间后也会回滚，类似超时cancel，因为一直没有confirm
        //HmilyTransactionSelfRecoveryScheduled：hmily tcc transaction begin self recovery: HmilyParticipant(participantId=6354364505338306560, participantRefId=null, transId=6354364427492024320, transType=TCC, status=1, appName=user-vip-dubbo, role=3, retry=1, targetClass=com.marcus.dubbo.bootProvider.service.FXAccountServiceImpl, targetMethod=updateFXAccount, confirmMethod=confirm, cancelMethod=cancel, version=2, createTime=Tue Jan 19 19:45:04 CST 2021, updateTime=Tue Jan 19 19:45:04 CST 2021, confirmHmilyInvocation=HmilyInvocation(targetClass=interface com.marcus.dubbo.boot.api.fxAccount.service.FXAccountService, methodName=updateFXAccount, parameterTypes=[class com.marcus.dubbo.boot.api.fxAccount.entity.FXAccountDO], args=[FXAccountDO(user_id=2, ccy_USD=1.0, ccy_CNH=-7.0, freeze_amount=7.0, freeze_ccy=CNH)]), cancelHmilyInvocation=HmilyInvocation(targetClass=interface com.marcus.dubbo.boot.api.fxAccount.service.FXAccountService, methodName=updateFXAccount, parameterTypes=[class com.marcus.dubbo.boot.api.fxAccount.entity.FXAccountDO], args=[FXAccountDO(user_id=2, ccy_USD=1.0, ccy_CNH=-7.0, freeze_amount=7.0, freeze_ccy=CNH)]))
        fxAccountService.updateFXAccount(buildCtpyFXAccount(fxAccountDTO));
        return 1;
    }


    public boolean confirm(FXAccountDTO fxAccountDTO) {
        log.info("consumer update FXAccount confirmed");
        FXAccountDO fxAccountDO = buildConfirmFXAccount(fxAccountDTO);
        log.info("{}",fxAccountDO.toString());
        return fxAccountMapper.update(fxAccountDO) >= 0;

    }

    public boolean cancel(FXAccountDTO fxAccountDTO) {
        log.info("consumer update FXAccount cancelled");
        FXAccountDO fxAccountDO = buildCancelFXAccount(fxAccountDTO);
        log.info("{}",fxAccountDO.toString());
        return fxAccountMapper.update(fxAccountDO) >= 0;
    }

    private FXAccountDO buildUpdateFXAccount(FXAccountDTO fxAccountDTO) {
        double priAmount = fxAccountDTO.getAmount();
        double secAmount = priAmount * 7;
        FXAccountDO fxAccountDO ;
        if (fxAccountDTO.isBuySellFlag()) {
            fxAccountDO = new FXAccountDO(fxAccountDTO.getUser_id(),0, secAmount * -1, secAmount, "CNH");
        } else {
            fxAccountDO = new FXAccountDO(fxAccountDTO.getUser_id(), priAmount * -1, 0, priAmount, "USD");
        }
        return fxAccountDO;
    }

    private FXAccountDO buildConfirmFXAccount(FXAccountDTO fxAccountDTO) {
        double priAmount = fxAccountDTO.getAmount();
        double secAmount = priAmount * 7;
        FXAccountDO fxAccountDO ;
        if (fxAccountDTO.isBuySellFlag()) {
            fxAccountDO = new FXAccountDO(fxAccountDTO.getUser_id(),priAmount, 0, 0, null);
        } else {
            fxAccountDO = new FXAccountDO(fxAccountDTO.getUser_id(), 0, secAmount, 0, null);
        }
        log.info(fxAccountDO.toString());
        return fxAccountDO;
    }

    private FXAccountDO buildCancelFXAccount(FXAccountDTO fxAccountDTO) {
        FXAccountDO fxAccountDO ;
        if (fxAccountDTO.isBuySellFlag()) {
            fxAccountDO = new FXAccountDO(fxAccountDTO.getUser_id(),0, fxAccountDTO.getAmount(), 0, null);
        } else {
            fxAccountDO = new FXAccountDO(fxAccountDTO.getUser_id(), fxAccountDTO.getAmount(), 0, 0, null);
        }
        return fxAccountDO;
    }

    private FXAccountDO buildCtpyFXAccount(FXAccountDTO fxAccountDTO) {
        double priAmount = fxAccountDTO.getAmount();
        double secAmount = priAmount * 7;
        FXAccountDO fxAccountDO;
        if (fxAccountDTO.isBuySellFlag()) {
            fxAccountDO = new FXAccountDO(fxAccountDTO.getUser_id(), priAmount * -1, secAmount, priAmount, "USD");
        } else {
            fxAccountDO = new FXAccountDO(fxAccountDTO.getUser_id(), priAmount, secAmount * -1, secAmount, "CNH");
        }
        return fxAccountDO;
    }
}
