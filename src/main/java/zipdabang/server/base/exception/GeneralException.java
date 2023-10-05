//package zipdabang.server.base.exception;
//
//import lombok.Getter;
//import zipdabang.server.apiPayload.code.CommonStatus;
//
//@Getter
//public class GeneralException extends RuntimeException{
//
//    private final CommonStatus errorCommonStatus;
//
//    public GeneralException() {
//        super(CommonStatus.INTERNAL_ERROR.getMessage());
//        this.errorCommonStatus = CommonStatus.INTERNAL_ERROR;
//    }
//
//    public GeneralException(String message) {
//        super(CommonStatus.INTERNAL_ERROR.getMessage(message));
//        this.errorCommonStatus = CommonStatus.INTERNAL_ERROR;
//    }
//
//    public GeneralException(String message, Throwable cause) {
//        super(CommonStatus.INTERNAL_ERROR.getMessage(message), cause);
//        this.errorCommonStatus = CommonStatus.INTERNAL_ERROR;
//    }
//
//    public GeneralException(Throwable cause) {
//        super(CommonStatus.INTERNAL_ERROR.getMessage(cause));
//        this.errorCommonStatus = CommonStatus.INTERNAL_ERROR;
//    }
//
//    public GeneralException(CommonStatus errorCommonStatus) {
//        super(errorCommonStatus.getMessage());
//        this.errorCommonStatus = errorCommonStatus;
//    }
//
//    public GeneralException(CommonStatus errorCommonStatus, String message) {
//        super(errorCommonStatus.getMessage(message));
//        this.errorCommonStatus = errorCommonStatus;
//    }
//
//    public GeneralException(CommonStatus errorCommonStatus, String message, Throwable cause) {
//        super(errorCommonStatus.getMessage(message), cause);
//        this.errorCommonStatus = errorCommonStatus;
//    }
//
//    public GeneralException(CommonStatus errorCommonStatus, Throwable cause) {
//        super(errorCommonStatus.getMessage(cause), cause);
//        this.errorCommonStatus = errorCommonStatus;
//    }
//}
