package net.hmi.bank.transaction.client;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import net.hmi.bank.transaction.exception.BusinessRuleException;
import net.hmi.bank.transaction.exception.RemoteServiceUnavailableException;
import net.hmi.bank.transaction.exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AccountClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String detail = extractDetail(readBody(response));
        if (status == 404) {
            return new ResourceNotFoundException(detail);
        }
        if (status >= 400 && status < 500) {
            return new BusinessRuleException(detail);
        }
        return new RemoteServiceUnavailableException("account-service returned HTTP " + status);
    }

    private String readBody(Response response) {
        if (response.body() == null) {
            return "";
        }
        try {
            return Util.toString(response.body().asReader(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return "";
        }
    }

    // account-service answers with {"detail":"..."}: keep only the message
    private String extractDetail(String body) {
        int i = body.indexOf("\"detail\":\"");
        if (i >= 0) {
            int start = i + 10;
            int end = body.indexOf('"', start);
            if (end > start) {
                return body.substring(start, end);
            }
        }
        return body.isBlank() ? "account-service rejected the request" : body;
    }
}