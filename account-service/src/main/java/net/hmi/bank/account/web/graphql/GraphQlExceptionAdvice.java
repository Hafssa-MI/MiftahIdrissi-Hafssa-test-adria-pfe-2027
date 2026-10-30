package net.hmi.bank.account.web.graphql;

import graphql.GraphQLError;
import net.hmi.bank.account.exception.AccountNotFoundException;
import net.hmi.bank.account.exception.BusinessRuleException;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GraphQlExceptionAdvice {

    @GraphQlExceptionHandler
    public GraphQLError notFound(AccountNotFoundException ex) {
        return GraphQLError.newError().errorType(ErrorType.NOT_FOUND).message(ex.getMessage()).build();
    }

    @GraphQlExceptionHandler
    public GraphQLError businessRule(BusinessRuleException ex) {
        return GraphQLError.newError().errorType(ErrorType.BAD_REQUEST).message(ex.getMessage()).build();
    }
}