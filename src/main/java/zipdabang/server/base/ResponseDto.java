//package zipdabang.server.base;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.ToString;
//import zipdabang.server.apiPayload.code.CommonStatus;
//
//@Getter
//@ToString
//@RequiredArgsConstructor
//public class ResponseDto<T> {
//    private final Boolean isSuccess;
//    private final Integer code;
//    private final String message;
//    private final T result;
//
//    public static<T> ResponseDto<T> of(Boolean isSuccess, CommonStatus commonStatus, T result) {
//        return new ResponseDto<>(isSuccess, commonStatus.getCode(), commonStatus.getMessage(), result);
//    }
//
//    public static <T> ResponseDto<T> of(CommonStatus commonStatus, T result){
//        return new ResponseDto<>(true, commonStatus.getCode(), commonStatus.getMessage(), result);
//    }
//
//    public static <T> ResponseDto<T> of(T result){
//        return new ResponseDto<>(true, CommonStatus.OK.getCode(), CommonStatus.OK.getMessage(), result);
//    }
//
//    public static<T> ResponseDto<T> of(Boolean isSuccess, CommonStatus errorCommonStatus, Exception e, T result) {
//        return new ResponseDto<>(isSuccess, errorCommonStatus.getCode(), errorCommonStatus.getMessage(e), result);
//    }
//
//    public static<T> ResponseDto<T> of(Boolean isSuccess, CommonStatus errorCommonStatus, String message, T result) {
//        return new ResponseDto<>(isSuccess, errorCommonStatus.getCode(), errorCommonStatus.getMessage(message), result);
//    }
//
//    public static <T> ResponseDto<T> empty(){
//        return new ResponseDto<>(true, CommonStatus.OK.getCode(), CommonStatus.OK.getMessage(), null);
//    }
//}
//a