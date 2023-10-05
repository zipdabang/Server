//package zipdabang.server.base.exception;
//
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//import zipdabang.server.apiPayload.code.CommonStatus;
//
//import javax.validation.ConstraintViolationException;
//
//@RestControllerAdvice(annotations = {RestController.class})
//public class ExceptionHandler extends ResponseEntityExceptionHandler {
//
//
//    @org.springframework.web.bind.annotation.ExceptionHandler
//    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
//        if(e.getConstraintViolations().size() >= 1)
//            return handleExceptionInternal(e, CommonStatus.valueOf(e.getMessage().split(":")[1].substring(1)), request);
//        return handleExceptionInternal(e, CommonStatus.UNAUTHORIZED, request);
//    }
//
//    @org.springframework.web.bind.annotation.ExceptionHandler
//    public ResponseEntity<Object> general(GeneralException e, WebRequest request) {
//        return handleExceptionInternal(e, e.getErrorCommonStatus(), request);
//    }
//
//    @org.springframework.web.bind.annotation.ExceptionHandler
//    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
//        e.printStackTrace();
//        return handleExceptionInternalFalse(e, CommonStatus.INTERNAL_ERROR, HttpHeaders.EMPTY, CommonStatus.INTERNAL_ERROR.getHttpStatus(),request);
//    }
//
//    @Override
//    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
//                                                             HttpHeaders headers, HttpStatus status, WebRequest request) {
//        return handleExceptionInternal(ex, CommonStatus.valueOf(status), headers, status, request);
//    }
//
//
//    private ResponseEntity<Object> handleExceptionInternal(Exception e, CommonStatus errorCommonStatus,
//                                                           WebRequest request) {
//        return handleExceptionInternal(e, errorCommonStatus, HttpHeaders.EMPTY, errorCommonStatus.getHttpStatus(),
//                request);
//    }
//
//    private ResponseEntity<Object> handleExceptionInternal(Exception e, CommonStatus errorCommonStatus,
//                                                           HttpHeaders headers, HttpStatus status, WebRequest request) {
//        ResponseDto<Object> body = ResponseDto.of(true, errorCommonStatus, null);
//        e.printStackTrace();
//        return super.handleExceptionInternal(
//                e,
//                body,
//                headers,
//                status,
//                request
//        );
//    }
//
//    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, CommonStatus errorCommonStatus,
//                                                                HttpHeaders headers, HttpStatus status, WebRequest request) {
//        ResponseDto<Object> body = ResponseDto.of(false, errorCommonStatus, null);
//        return super.handleExceptionInternal(
//                e,
//                body,
//                headers,
//                status,
//                request
//        );
//    }
//}
