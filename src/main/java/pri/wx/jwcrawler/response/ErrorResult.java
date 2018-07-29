package pri.wx.jwcrawler.response;

import java.io.Serializable;
import java.util.Date;

public class ErrorResult implements Serializable {

    //异常发生时间
    private Date exceptionDate;

    //异常类名
    private String exceptionType;

    //异常描述
    private String exceptionMessage;

    //异常堆栈
    private String exceptionStackTrace;

    public ErrorResult(String exceptionType, String exceptionMessage, String exceptionStackTrace) {
        this.exceptionDate = new Date();
        this.exceptionType = exceptionType;
        this.exceptionMessage = exceptionMessage;
        this.exceptionStackTrace = exceptionStackTrace;
    }

    public Date getExceptionDate() {
        return exceptionDate;
    }

    public void setExceptionDate(Date exceptionDate) {
        this.exceptionDate = exceptionDate;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionStackTrace() {
        return exceptionStackTrace;
    }

    public void setExceptionStackTrace(String exceptionStackTrace) {
        this.exceptionStackTrace = exceptionStackTrace;
    }
}