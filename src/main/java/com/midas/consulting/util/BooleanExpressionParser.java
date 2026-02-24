package com.midas.consulting.util;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.regex.Pattern;

public class BooleanExpressionParser {
    public Criteria parse(String expression) {
        expression = expression.trim();

        // If expression has no logical operators, return a single criteria directly
        if (!expression.contains(" AND ") && !expression.contains(" OR ") && !expression.startsWith("NOT ")) {
            return Criteria.where("fullText").regex("\\b" + Pattern.quote(expression) + "\\b", "i");
        }

        // Otherwise, parse the expression with logical operators
        return parseExpression(expression);
    }
    private Criteria parseExpression(String expression) {
        expression = expression.trim();
        
        if (expression.contains(" AND ")) {
            String[] andParts = expression.split(" AND ");
            List<Criteria> andCriteria = new ArrayList<>();
            for (String part : andParts) {
                andCriteria.add(parseExpression(part));
            }
            return new Criteria().andOperator(andCriteria.toArray(new Criteria[0]));
        } else if (expression.contains(" OR ")) {
            String[] orParts = expression.split(" OR ");
            List<Criteria> orCriteria = new ArrayList<>();
            for (String part : orParts) {
                orCriteria.add(parseExpression(part));
            }
            return new Criteria().orOperator(orCriteria.toArray(new Criteria[0]));
        } else if (expression.startsWith("NOT ")) {
            String notPart = expression.substring(4);  // Remove "NOT "
            return Criteria.where("fullText").not().regex("\\b" + Pattern.quote(notPart) + "\\b", "i");
        } else {
            // Handle exact match or phrase
            return Criteria.where("fullText").regex("\\b" + Pattern.quote(expression) + "\\b", "i");
        }
    }
}
