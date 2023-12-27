package com.biggergames.backend.logstorageservice.config.time;

import io.micrometer.common.annotation.ValueExpressionResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.NonNull;

@Slf4j
public class SpelValueExpressionResolver implements ValueExpressionResolver {
    @Override
    @NonNull
    public String resolve(@NonNull String expression, @NonNull Object parameter) {
        try {
            ExpressionParser expressionParser = new SpelExpressionParser();
            Expression spelExpression = expressionParser.parseExpression(expression);

            Object propertyValue = spelExpression.getValue(parameter);
            return propertyValue != null ? propertyValue.toString() : parameter.toString();
        } catch (Exception ex) {
            log.error("Exception occurred while trying to evaluate the SpEL expression [" + expression + "]", ex);
        }
        return parameter.toString();
    }
}