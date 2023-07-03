package com.admin4j.framework.mybatis.service;

import com.admin4j.framework.mybatis.IDataScopeInfoHandler;
import com.admin4j.framework.mybatis.constant.DataScope;
import com.admin4j.framework.mybatis.constant.DataScopeEnum;
import com.admin4j.framework.mybatis.entity.DeptInfoDTO;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;
import lombok.Data;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * @author andanyang
 * @since 2023/7/3 13:32
 */
@Service
@Data
public class DataScopeInfoHandler implements IDataScopeInfoHandler {

    private DataScopeEnum dataScopeEnum = DataScopeEnum.CUSTOM_DEPARTMENT;
    private UserDataScopeBO userDataScopeBO;

    @PostConstruct
    public void initUser() {
        userDataScopeBO = new UserDataScopeBO();
        userDataScopeBO.setType(dataScopeEnum);
        userDataScopeBO.setUserId(new LongValue(1L));

        DeptInfoDTO deptInfoDTO1 = new DeptInfoDTO(168L, "168,123,1");
        DeptInfoDTO deptInfoDTO2 = new DeptInfoDTO(169L, "168,123,2");
        DeptInfoDTO deptInfoDTO3 = new DeptInfoDTO(170L, "168,123,3");
        DeptInfoDTO deptInfoDTO4 = new DeptInfoDTO(171L, "168,123,4");
        userDataScopeBO.setDeptInfos(Arrays.asList(deptInfoDTO1, deptInfoDTO2, deptInfoDTO3, deptInfoDTO4));
        userDataScopeBO.setManagerDeptInfos(Arrays.asList(deptInfoDTO1, deptInfoDTO2, new DeptInfoDTO(172L, "168,123,5")));


        DeptInfoDTO deptInfoDTO11 = new DeptInfoDTO(1681L, "168,123,11");
        DeptInfoDTO deptInfoDTO12 = new DeptInfoDTO(1691L, "168,123,12");
        DeptInfoDTO deptInfoDTO13 = new DeptInfoDTO(1701L, "168,123,13");
        DeptInfoDTO deptInfoDTO14 = new DeptInfoDTO(1711L, "168,123,14");
        userDataScopeBO.setCustomDeptInfos(Arrays.asList(deptInfoDTO11, deptInfoDTO12, deptInfoDTO13, deptInfoDTO14));
    }

    public void setDataScopeEnum(DataScopeEnum dataScopeEnum) {
        this.dataScopeEnum = dataScopeEnum;
        userDataScopeBO.setType(dataScopeEnum);
    }

    /**
     * @return 返回当前数据权限信息
     */
    @Override
    public UserDataScopeBO currentDataScope(DataScope dataScope) {
        return userDataScopeBO;
    }
}
