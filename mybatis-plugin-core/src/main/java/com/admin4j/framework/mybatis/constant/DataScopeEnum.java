package com.admin4j.framework.mybatis.constant;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限类型。；1：本人；2：本部门；3本部门及下级部门；4自定义部门；5全部
 *
 * @author andanyang
 * @since 2023/6/27 15:34
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {
    /**
     * 本人
     */
    SELF(1),
    /**
     * 自己部门
     */
    DEPARTMENT(2),
    /**
     * 本部门及下级部门
     */
    DEPARTMENT_SU(3),
    CUSTOM_DEPARTMENT(4),
    ALL(5);

    //@EnumValue
    @JsonValue
    private final int value;

    @JsonCreator
    public static DataScopeEnum forValue(Integer value) {
        for (DataScopeEnum dataScopeEnum : values()) {
            if (dataScopeEnum.value == value) {
                return dataScopeEnum;
            }
        }
        return null;
    }
}
