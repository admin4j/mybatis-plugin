package com.admin4j.framework.mybatis.interceptor;

import lombok.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * @author andanyang
 * @since 2023/7/3 9:56
 */
@Data
public class PlainValue extends ASTNodeAccessImpl implements Expression {


    private String plainValue;

    public PlainValue(String plainValue) {
        this.plainValue = plainValue;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {

    }

    @Override
    public String toString() {
        return plainValue;
    }
}
