package com.hhp227.yu_minigroup.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * 게시글·댓글·채팅 신고. 별도 서버가 없으므로 신고 대상 식별 정보를 채운 메일 작성 화면을 연다.
 * 아동 안전 표준(CSAE) 정책 페이지의 신고 창구와 같은 주소를 쓴다.
 */
public final class ReportHelper {
    public static final String REPORT_EMAIL = "hong227@naver.com";

    private static final String APP_NAME = "영남대 소모임";

    private ReportHelper() {
    }

    /**
     * @param target  신고 대상 종류 (게시글, 댓글, 채팅방)
     * @param details 신고 대상을 찾을 수 있는 식별 정보 (줄 단위)
     */
    public static void report(Context context, String target, String details) {
        String subject = "[" + APP_NAME + "] " + target + " 신고";
        String body = "신고 사유를 적어 주세요.\n"
                + "(아동 성착취·학대, 음란물, 괴롭힘, 혐오 표현, 스팸, 개인정보 노출 등)\n\n"
                + "사유: \n\n"
                + "---- 아래 정보는 지우지 말아 주세요 ----\n"
                + "신고 대상: " + target + "\n"
                + details;
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + REPORT_EMAIL));

        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {REPORT_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            context.startActivity(Intent.createChooser(intent, target + " 신고"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "메일 앱이 없습니다. " + REPORT_EMAIL + " 으로 신고 내용을 보내 주세요.", Toast.LENGTH_LONG).show();
        }
    }
}
