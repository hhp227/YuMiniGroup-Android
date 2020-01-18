package com.hhp227.yu_minigroup.app;

public interface EndPoint {
    String SMS_URL = "http://sms.yu.ac.kr/module/index.php/api";
    String BASE_URL = "http://lms.yu.ac.kr";
    String LOGIN = SMS_URL + "/login";
    String LOGIN_LMS = BASE_URL + "/ilos/lo/login_sso.acl";
    String GROUP_LIST = BASE_URL + "/ilos/m/community/share_group_list.acl";
    String CREATE_GROUP = BASE_URL + "/ilos/community/share_group_insert.acl";
    String REGISTER_GROUP = BASE_URL + "/ilos/community/share_group_register.acl";
    String WITHDRAWAL_GROUP = BASE_URL + "/ilos/community/share_auth_drop_me.acl";
    String MODIFY_GROUP = BASE_URL + "/ilos/community/share_group_modify.acl";
    String UPDATE_GROUP = BASE_URL + "/ilos/community/share_group_update.acl";
    String DELETE_GROUP = BASE_URL + "/ilos/community/share_group_delete.acl";
    String NEW_MESSAGE = BASE_URL + "/ilos/message/received_new_message_check.acl";
    String GET_USER_IMAGE = BASE_URL + "/ilos/mp/myinfo_update_photo.acl";

    // 로그기록
    String CREATE_LOG = "http://knu.dothome.co.kr/knu/v1/register";
}
