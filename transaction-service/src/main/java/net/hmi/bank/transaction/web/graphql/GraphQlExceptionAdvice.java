package net.hmi.bank.transaction.web.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import net.hmi.bank.transaction.exception.BusinessRuleException;
import net.hmi.bank.transaction.exception.RemoteServiceUnavailableException;
import net.hmi.bank.transaction.exception.ResourceNotFoundException;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GraphQlExceptionAdvice {

    @GraphQlExceptionHandler
    public GraphQLError handle(ResourceNotFoundException ex, DataFetchingEnvironment env) {
        return build(ErrorType.NOT_FOUND, ex.getMessage(), env);
    }

    @GraphQlExceptionHandler
    public GraphQLError handle(BusinessRuleException ex, DataFetchingEnvironment env) {
        return build(ErrorType.BAD_REQUEST, ex.getMessage(), env);
    }

    @GraphQlExceptionHandler
    public GraphQLError handle(RemoteServiceUnavailableException ex, DataFetchingEnvironment env) {
        return build(ErrorType.INTERNAL_ERROR, "Account service unavailable: " + ex.getMessage(), env);
    }

    private GraphQLError build(ErrorType type, String message, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env).errorType(type).message(message).build();
    }
}