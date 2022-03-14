package com.hhp227.yu_minigroup.app;

public interface EndPoint {
    String YU_PORTAL_LOGIN_URL = "https://portal.yu.ac.kr/sso/login_process.jsp";
    String BASE_URL = "http://lms.yu.ac.kr";
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
    String GROUP_ARTICLE_LIST = BASE_URL + "/ilos/community/share_list.acl";
    String WRITE_ARTICLE = BASE_URL + "/ilos/community/share_insert.acl";
    String IMAGE_UPLOAD = BASE_URL + "/ilos/tinymce/file_upload_pop.acl";
    String DELETE_ARTICLE = BASE_URL + "/ilos/community/share_delete.acl";
    String MODIFY_ARTICLE = BASE_URL + "/ilos/community/share_update.acl";
    String INSERT_REPLY = BASE_URL + "/ilos/community/share_comment_insert.acl";
    String DELETE_REPLY = BASE_URL + "/ilos/community/share_comment_delete.acl";
    String MODIFY_REPLY = BASE_URL + "/ilos/community/share_comment_update.acl";
    String MEMBER_LIST = BASE_URL + "/ilos/community/share_member_list.acl";
    String USER_IMAGE = BASE_URL + "/ilos/mp/user_image_view.acl?id={UID}&ext=.jpg";
    String GET_USER_IMAGE = BASE_URL + "/ilos/mp/myinfo_update_photo.acl";
    String TIMETABLE = BASE_URL + "/ilos/st/main/pop_academic_timetable_form.acl";
    String MY_INFO = BASE_URL + "/ilos/mp/myinfo_form.acl";
    String SEND_MESSAGE = BASE_URL + "/ilos/co/club_send_msg_insert.acl";
    String SYNC_PROFILE = BASE_URL + "/ilos/mp/myinfo_sync.acl";
    String PROFILE_IMAGE_PREVIEW = BASE_URL + "/ilos/mp/myinfo_file_update.acl";
    String PROFILE_IMAGE_UPDATE = BASE_URL + "/ilos/mp/myinfo_insert.acl";

    // 로그기록
    String CREATE_LOG = "http://knu.dothome.co.kr/knu/v1/register";

    // 학교 URL
    String URL_YU = "https://www.yu.ac.kr";
    String URL_YU_MOBILE = "http://m.yu.ac.kr";
    String URL_YU_NOTICE = URL_YU + "/main/intro/yu-news.do?mode={MODE}";
    String URL_YU_LIBRARY_SEAT = "https://slib.yu.ac.kr";
    String URL_YU_LIBRARY_SEAT_ROOMS = URL_YU_LIBRARY_SEAT + "/Clicker/GetClickerReadingRooms";
    String URL_YU_LIBRARY_SEAT_DETAIL = URL_YU_LIBRARY_SEAT + "/clicker/UserSeat/{ID}";
    String URL_YU_SHUTTLE_BUS = "https://hcms.yu.ac.kr/main/life/information-on-the-school-bus.do";
    String URL_SCHEDULE = URL_YU + "/main/bachelor/calendar.do?mode=calendar&srYear={YEAR}";

    // 유튜브 API
    String URL_YOUTUBE_API = "https://www.googleapis.com/youtube/v3/search";
}
