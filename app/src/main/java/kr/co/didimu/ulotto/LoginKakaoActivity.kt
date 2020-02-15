package kr.co.didimu.ulotto

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kakao.auth.AuthType
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.exception.KakaoException


class LoginKakaoActivity : AppCompatActivity() {

    private var isNeedLogin = true


    /** * 카카오 로그인 UserProfile 호출 */
    private fun kakaoRequestMe(){
        UserManagement.getInstance().me(object: MeV2ResponseCallback()
        {
            override fun onSessionClosed(errorResult: ErrorResult?) {
                Log.d("onSessionClosed", "failed to update profile. msg = $errorResult")
                //todo: finishWithError(errorResult.getErrorMessage());
            }
            override fun onSuccess(result: MeV2Response?)
            {
                Log.d("onSessionClosed", "success to update profile. msg = $result")

//                val msg =  Message.obtain()
//                msg?.what = Constants.MSG_USE_AUDIO
//
//                MessageHandler(this@LoginKakaoActivity, this@LoginKakaoActivity as Handler.Callback).sendMessage(msg)

                var kakaoId : String? = "" + result?.getId()
                if(kakaoId == null){
                    finishWithError(" failed no ID")
                    return
                }

                var email : String? = result?.getKakaoAccount()?.getEmail()
                if(email == null){
                    email = ""
                }

                var name : String? = result?.nickname
                if(name == null){
                    name = ""
                }

                var koAccessToken : String?= Session.getCurrentSession ().getTokenInfo().getAccessToken()
                if(koAccessToken == null){
                    koAccessToken = ""
                }
//                var photoUrl = result.getKakaoAccount ().getProfile().getProfileImageUrl()
//                var name = result.getKakaoAccount ().getProfile().getNickname()

                finishWithSuccess(name!!, email!!, "", kakaoId, koAccessToken!!);

                //String kakaoId = result.getId() +"";
                //                    String email = result.getKakaoAccount().getEmail();
                //                    String koAccessToken = Session.getCurrentSession().getTokenInfo().getAccessToken();
                //                    String photoUrl = result.getKakaoAccount().getProfile().getProfileImageUrl();
                //                    String name = result.getKakaoAccount().getProfile().getNickname();
                //
//                val moveUrl = uri.getQueryParameter("url")
//                val msg = Message.obtain()
//                msg?.what = Constants.MSG_MOVE_URL
//                msg?.obj = moveUrl
//
//                MessageHandler(this, this as Handler.Callback).sendMessage(msg)                //todo :finishWithSuccess(name, email, photoUrl, kakaoId, koAccessToken);
            }


        })
    }

    /**
     * 카카오 세션 콜백.
     */
    private val mKakaoSessionCallback = object : ISessionCallback {
        override fun onSessionOpened() {

            if (Session.getCurrentSession().isOpened) {
                Log.e("LoginActivity", "카카오 로그인 성공")
                kakaoRequestMe()
            }

            //            List<String> keys = new ArrayList<>();
            //            keys.add("kakao_account.profile");
            //            keys.add("kakao_account.email");
            //
            //            UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            //                @Override
            //                public void onSessionClosed(ErrorResult errorResult) {
            //                    finishWithError(errorResult.getErrorMessage());
            //                }
            //
            //                @Override
            //                public void onSuccess(MeV2Response result) {
            //                    Log.e("LoginActivity", "카카오톡 프로필 가져오기 성공했다.");
            //                    String kakaoId = result.getId() +"";
            //                    String email = result.getKakaoAccount().getEmail();
            //                    String koAccessToken = Session.getCurrentSession().getTokenInfo().getAccessToken();
            //                    String photoUrl = result.getKakaoAccount().getProfile().getProfileImageUrl();
            //                    String name = result.getKakaoAccount().getProfile().getNickname();
            //
            //                    finishWithSuccess(name, email, photoUrl, kakaoId, koAccessToken);
            //                }
            //
            //                @Override
            //                public void onFailure(ErrorResult errorResult) {
            //                    super.onFailure(errorResult);
            //                    finishWithError(errorResult.getErrorMessage());
            //                }
            //            });
        }

        override fun onSessionOpenFailed(ex: KakaoException) {
            Log.e("LoginActivity", "온 쎄션 오픈 페일드.")
            finishWithError(ex.localizedMessage)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Session.getCurrentSession().removeCallback(mKakaoSessionCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Session.getCurrentSession().close()
        Session.getCurrentSession().addCallback(mKakaoSessionCallback)
    }

    override fun onResume() {
        super.onResume()
        if (isNeedLogin) {
            isNeedLogin = false
            Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, this)
        }
    }


    private fun finishWithError(err: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(" 로그인")
        builder.setMessage(err)
        builder.setCancelable(false)
        builder.setPositiveButton(
            "확인",
            DialogInterface.OnClickListener { dialogInterface, i -> finish() }).show()
    }

//    private fun finishWithSuccess(){
//        val intent = Intent()
//        setResult(Activity.RESULT_OK, intent)
//        finish()
//    }

    private fun finishWithSuccess(
        name: String,
        email: String,
        photoUrl: String,
        kkId: String,
        kkAccessToken: String
    ) {
        val intent = Intent()
        intent.putExtra("name", name)
        intent.putExtra("email", email)
        intent.putExtra("photoUrl", photoUrl)
        intent.putExtra("kkId", kkId)
        intent.putExtra("kkAccessToken", kkAccessToken)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
