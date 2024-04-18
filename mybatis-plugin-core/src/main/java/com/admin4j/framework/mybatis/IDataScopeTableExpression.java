package com.admin4j.framework.mybatis;

import com.admin4j.framework.mybatis.entity.DeptInfoDTO;
import com.admin4j.framework.mybatis.entity.PlainValue;
import com.admin4j.framework.mybatis.entity.UserDataScopeBO;
import com.admin4j.framework.mybatis.exception.NoDataException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author andanyang
 * @since 2023/7/3 16:37
 */
public class IDataScopeTableExpression {

    /**
     * 构建数据权限为 All的查询条件
     *
     * @throws NoDataException
     */
    public Expression buildAll(Column field, UserDataScopeBO userDataScopeBO) throws NoDataException {
        return null;
    }

    /**
     * 构建数据权限为 本人的查询条件
     *
     * @throws NoDataException
     */
    public Expression buildSelf(Column field, UserDataScopeBO userDataScopeBO) throws NoDataException {
        
        if (!userDataScopeBO.hasManagerDept()) {
            return new EqualsTo(field, userDataScopeBO.getUserId());
        }

        StringBuilder sql = new StringBuilder("select user_id from sys_user_dept where ");

        PlainValue plainValue = null;
        boolean isFirst = true;
        // ** 要求-自己也在自己管理的部门之下 否则需要添加 userId = userDataScopeBO.getUserId()
        // 加上自己管理的部门
        // 处理子部门数据
        isFirst = true;
        for (DeptInfoDTO dept : userDataScopeBO.getManagerDeptInfos()) {
            if (!isFirst) {
                sql.append(" OR ");
            }
            sql.append("dept_tree like '");
            sql.append(dept.getDeptTree());
            sql.append("%'");
            isFirst = false;
        }
        ItemsList itemsList = new ExpressionList(new PlainValue(sql.toString()));
        return new InExpression(field, itemsList);
    }


    /**
     * 构建自己部门的数据权限
     *
     * @return
     * @throws NoDataException
     */
    public Expression buildDepartment(Column field, UserDataScopeBO userDataScopeBO) throws NoDataException {
        // user_id in (select user_id from sys_user_dept where dept_id in (1,2))
        if (userDataScopeBO.getDeptInfos() == null || userDataScopeBO.getDeptInfos().isEmpty()) {
            throw new NoDataException("no any DeptInfo");
        }

        PlainValue plainValue = null;
        boolean isFirst = true;
        StringBuilder sql = null;

        List<DeptInfoDTO> deptInfos = userDataScopeBO.getDeptInfos();
        // 去除重复部门
        if (userDataScopeBO.hasManagerDept()) {
            deptInfos = userDataScopeBO.getDeptInfos().stream().filter(i -> {
                for (DeptInfoDTO info : userDataScopeBO.getManagerDeptInfos()) {
                    if (info.getDeptId().equals(i.getDeptId())) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());
        }

        boolean needOr = false;
        sql = new StringBuilder("select user_id from sys_user_dept where ");
        if (deptInfos.size() == 1) {
            needOr = true;
            sql.append("dept_id = " + deptInfos.get(0).getDeptId());
        } else if (deptInfos.size() > 1) {
            needOr = true;
            isFirst = true;

            for (DeptInfoDTO dept : deptInfos) {
                if (!isFirst) {
                    sql.append(" OR ");
                }
                sql.append("dept_id = ");
                sql.append(dept.getDeptId());
                isFirst = false;
            }
        }

        // 又有管理的部门，需要权限穿透
        if (userDataScopeBO.hasManagerDept()) {
            // 处理子部门数据
            if (needOr) {
                sql.append(" OR ");
            }
            isFirst = true;
            for (DeptInfoDTO dept : userDataScopeBO.getManagerDeptInfos()) {
                if (!isFirst) {
                    sql.append(" OR ");
                }
                sql.append("dept_tree like '");
                sql.append(dept.getDeptTree());
                sql.append("%'");
                isFirst = false;
            }
        }
        ItemsList itemsList = new ExpressionList(new PlainValue(sql.toString()));
        return new InExpression(field, itemsList);
    }

    /**
     * 构建自己部门和下级部门sql
     *
     * @return
     * @throws NoDataException
     */
    public Expression buildDepartmentSub(Column field, UserDataScopeBO userDataScopeBO) throws NoDataException {

        // user_id in (select user_id from sys_data_scope where dept_tree like '1,2,3,%' or dept_tree like '1,2,4,%')
        if (userDataScopeBO.getDeptInfos() == null || userDataScopeBO.getDeptInfos().isEmpty()) {
            throw new NoDataException("no any DeptInfos");
        }

        PlainValue plainValue = null;
        boolean isFirst = true;
        StringBuilder sql = null;

        List<DeptInfoDTO> deptInfos = userDataScopeBO.getDeptInfos();
        if (userDataScopeBO.hasManagerDept()) {
            // 合并数组
            deptInfos = (List<DeptInfoDTO>) CollectionUtils.union(deptInfos, userDataScopeBO.getManagerDeptInfos());
        }

        isFirst = true;
        sql = new StringBuilder("select user_id from sys_user_dept where ");
        for (DeptInfoDTO deptInfo : deptInfos) {
            if (!isFirst) {
                sql.append(" OR ");
            }
            sql.append("dept_tree like '");
            sql.append(deptInfo.getDeptTree());
            sql.append("%'");
            isFirst = false;
        }
        plainValue = new PlainValue(sql.toString());

        ExpressionList itemsList = new ExpressionList(plainValue);
        return new InExpression(field, itemsList);
    }

    /**
     * 自定义权限
     *
     * @return
     * @throws NoDataException
     */
    public Expression buildDepartmentCustom(Column field, UserDataScopeBO userDataScopeBO) throws NoDataException {

        // user_id in (select user_id from sys_data_scope where dept_id in (1,2,3,4)
        if (userDataScopeBO.getCustomDeptInfos() == null || userDataScopeBO.getCustomDeptInfos().isEmpty()) {
            throw new NoDataException("no any CustomDeptInfos");
        }


        PlainValue plainValue = null;
        boolean isFirst = true;
        StringBuilder sql = null;

        List<DeptInfoDTO> deptInfos = userDataScopeBO.getCustomDeptInfos();
        // 去除重复部门
        if (userDataScopeBO.hasManagerDept()) {
            deptInfos = userDataScopeBO.getCustomDeptInfos().stream().filter(i -> {
                for (DeptInfoDTO info : userDataScopeBO.getManagerDeptInfos()) {
                    if (info.getDeptId().equals(i.getDeptId())) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());
        }

        boolean needOr = !deptInfos.isEmpty();
        sql = new StringBuilder("select user_id from sys_user_dept where ");
        if (!deptInfos.isEmpty()) {

            isFirst = true;
            for (DeptInfoDTO dept : deptInfos) {
                if (!isFirst) {
                    sql.append(" OR ");
                }
                sql.append("dept_id = ");
                sql.append(dept.getDeptId());
                isFirst = false;
            }
        }

        // 又有管理的部门，需要权限穿透
        if (userDataScopeBO.hasManagerDept()) {
            // 处理子部门数据
            if (needOr) {
                sql.append(" OR ");
            }
            isFirst = true;
            for (DeptInfoDTO dept : userDataScopeBO.getManagerDeptInfos()) {
                if (!isFirst) {
                    sql.append(" OR ");
                }
                sql.append("dept_tree like '");
                sql.append(dept.getDeptTree());
                sql.append("%'");
                isFirst = false;
            }
        }
        ItemsList itemsList = new ExpressionList(new PlainValue(sql.toString()));
        return new InExpression(field, itemsList);
    }
}
