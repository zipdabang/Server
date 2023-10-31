package zipdabang.server.apiPayload.exception;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.code.Reason;
import zipdabang.server.apiPayload.exception.base.GeneralException;
import zipdabang.server.apiPayload.reponse.ResponseDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {


    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(constraintViolation -> constraintViolation.getMessage())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

        return handleExceptionInternalConstraint(e, CommonStatus.valueOf(errorMessage),HttpHeaders.EMPTY,request);
    }

    @NotNull
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().stream()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage, (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        return handleExceptionInternalArgs(e,HttpHeaders.EMPTY,CommonStatus.valueOf("BAD_REQUEST"),request,errors);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        e.printStackTrace();

        return handleExceptionInternalFalse(e, CommonStatus.INTERNAL_ERROR, HttpHeaders.EMPTY, CommonStatus.INTERNAL_ERROR.getHttpStatus(),request, e.getMessage());
    }

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity onThrowException(GeneralException generalException,
                                           @AuthenticationPrincipal User user, HttpServletRequest request) {
        getExceptionStackTrace(generalException, user, request);
        Reason errorReasonHttpStatus = generalException.getErrorReasonHttpStatus();
        System.out.println(generalException.getMessage());
        System.out.println(generalException.getCode());
        return handleExceptionInternal(generalException,errorReasonHttpStatus,null,request);
    }

//    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Reason reason,
//                                                             HttpHeaders headers, HttpServletRequest request) {
//        return handleExceptionInternal(ex, reason, headers, request);
//    }


//    private ResponseEntity<Object> handleExceptionInternal(Exception e, Reason errorReason,
//                                                           WebRequest request) {
//        return handleExceptionInternal(e, errorReason, HttpHeaders.EMPTY, request);
//    }

//    private ResponseEntity<Object> handleExceptionInternal(Exception e, CommonStatus commonStatus,
//                                                           WebRequest request) {
//        return handleExceptionInternal(e, Reason, HttpHeaders.EMPTY, request);
//    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, Reason reason,
                                                           HttpHeaders headers, HttpServletRequest request) {

        ResponseDto<Object> body = ResponseDto.onFailure(reason.getCode(),reason.getMessage(),null);
//        e.printStackTrace();

        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                reason.getHttpStatus(),
                webRequest
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, CommonStatus errorCommonStatus,
                                                                HttpHeaders headers, HttpStatus status, WebRequest request, String errorPoint) {
        ResponseDto<Object> body = ResponseDto.onFailure(errorCommonStatus.getCode(),errorCommonStatus.getMessage(),errorPoint);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers, CommonStatus errorCommonStatus,
                                                               WebRequest request, Map<String, String> errorArgs) {
        ResponseDto<Object> body = ResponseDto.onFailure(errorCommonStatus.getCode(),errorCommonStatus.getMessage(),errorArgs);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, CommonStatus errorCommonStatus,
                                                                HttpHeaders headers, WebRequest request) {
        ResponseDto<Object> body = ResponseDto.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), null);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    private void getExceptionStackTrace(Exception e, @AuthenticationPrincipal User user,
                                        HttpServletRequest request) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.append("\n==========================!!!ERROR TRACE!!!==========================\n");
        pw.append("uri: " + request.getRequestURI() + " " + request.getMethod() + "\n");
        if (user != null) {
            pw.append("uid: " + user.getUsername() + "\n");
        }
        pw.append(e.getMessage());
        System.out.println(e.getMessage());
        pw.append("\n=====================================================================");
        log.error(sw.toString());
    }
}
