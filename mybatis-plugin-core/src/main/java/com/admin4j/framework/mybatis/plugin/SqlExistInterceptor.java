package com.admin4j.framework.mybatis.plugin;

import com.admin4j.framework.mybatis.IAppendExistSqlHandler;
import com.admin4j.framework.mybatis.constant.SqlExist;
import com.admin4j.framework.mybatis.process.SelectSqlProcess;
import com.admin4j.framework.mybatis.util.MapperAnnotationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * 自动拼接 exist 子查询
 *
 * @author andanyang
 * @since 2024/4/2 17:23
 */
@Slf4j
@RequiredArgsConstructor
public class SqlExistInterceptor extends SelectSqlProcess {

    final IAppendExistSqlHandler sqlExistService;

    /**
     * 处理之前调用
     *
     * @param ms
     * @return false 不执行
     */
    @Override
    public boolean processBefore(MappedStatement ms) {

        try {
            SqlExist annotationByIdNoCache = MapperAnnotationUtil.getAnnotationById((ms.getId()), SqlExist.class);
            return annotationByIdNoCache != null && !annotationByIdNoCache.ignore();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // return true;
    }

    /**
     * 结束处理
     * 清理资源
     */
    @Override
    public void processEnd() {

    }

    /**
     * 构建数据库表的查询条件
     *
     * @param table        表对象
     * @param where        当前where条件
     * @param whereSegment 所属Mapper对象全路径
     * @return 需要拼接的新条件（不会覆盖原有的where条件，只会在原有条件上再加条件），为 null 则不加入新的条件
     */
    @Override
    public Expression buildTableExpression(Table table, Expression where, String whereSegment) {

        PlainSelect plainSelect = sqlExistService.getSelectSql(table);
        if (plainSelect != null) {

            SubSelect subSelect = new SubSelect();
            subSelect.setSelectBody(plainSelect);
            return new ExistsExpression().withRightExpression(subSelect);
        }
        return null;
    }
}
