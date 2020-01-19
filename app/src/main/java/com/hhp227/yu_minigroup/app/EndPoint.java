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
    String GROUP_MEMBER_LIST = BASE_URL + "/ilos/community/share_group_member_list.acl";
    String GROUP_IMAGE_UPDATE = BASE_URL + "/ilos/community/share_group_image_update.acl";
    String NEW_MESSAGE = BASE_URL + "/ilos/message/received_new_message_check.acl";
    String GET_USER_IMAGE = BASE_URL + "/ilos/mp/myinfo_update_photo.acl";
    String TIMETABLE = BASE_URL + "/ilos/st/main/pop_academic_timetable_form.acl";

    // 로그기록
    String CREATE_LOG = "http://knu.dothome.co.kr/knu/v1/register";

    // 학교 URL
    String URL_YU = "https://www.yu.ac.kr";
    String URL_YU_MOBILE = "http://m.yu.ac.kr";
    String URL_YU_NOTICE = URL_YU + "/_korean/about/index.php?c=about_08_a_list&page={PAGE}";
    String URL_YU_MOBILE_NOTICE = URL_YU_MOBILE + "/_mobile/notice/?c=notice_01_view&seq={ID}";
}
