package com.hhp227.yu_minigroup.app;

public interface EndPoint {
    String BASE_URL = "http://lms.yu.ac.kr";
    String SMS_URL = "http://sms.yu.ac.kr/module/index.php/api";
    String LOGIN = SMS_URL + "/login";
    String LOGIN_LMS = BASE_URL + "/ilos/lo/login_sso.acl";
    String GROUP_LIST = BASE_URL + "/ilos/m/community/share_group_list.acl";
}
