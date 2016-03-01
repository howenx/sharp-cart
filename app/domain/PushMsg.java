package domain;

import java.io.Serializable;

/**
 * Created by sibyl.sun on 16/3/1.
 */
public class PushMsg implements Serializable {
    private static final long serialVersionUID = 1L;
    private String alert;
    private String title;
    private String url;
    private String targetType;
    private String platform;
    private String audience;




}
