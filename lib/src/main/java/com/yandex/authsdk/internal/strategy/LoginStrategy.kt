package com.yandex.authsdk.internal.strategy

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdkParams
import com.yandex.authsdk.YandexAuthToken
import com.yandex.authsdk.internal.Constants

internal abstract class LoginStrategy {

    abstract val type: LoginType

    abstract val contract: ActivityResultContract<YandexAuthSdkParams, Result<YandexAuthToken?>>

    internal abstract class LoginContract(
        private val extractor: ResultExtractor
    ) : ActivityResultContract<YandexAuthSdkParams, Result<YandexAuthToken?>>() {

        override fun parseResult(resultCode: Int, intent: Intent?): Result<YandexAuthToken?> {
            if (intent == null || resultCode != Activity.RESULT_OK) {
                return Result.success(null)
            }

            val yandexAuthToken = extractor.tryExtractToken(intent)
            if (yandexAuthToken != null) {
                return Result.success(yandexAuthToken)
            }

            val error = extractor.tryExtractError(intent)
            if (error != null) {
                return Result.failure(error)
            }

            return Result.success(null)
        }
    }

    interface ResultExtractor {

        fun tryExtractToken(data: Intent): YandexAuthToken?

        fun tryExtractError(data: Intent): YandexAuthException?
    }

    companion object {

        fun putExtras(
            intent: Intent,
            options: YandexAuthOptions,
            loginOptions: YandexAuthLoginOptions
        ): Intent {
            intent.putExtra(Constants.EXTRA_OPTIONS, options)
            intent.putExtra(Constants.EXTRA_LOGIN_OPTIONS, loginOptions)
            return intent
        }

        fun putExtrasNative(
            intent: Intent,
            options: YandexAuthOptions,
            loginOptions: YandexAuthLoginOptions
        ): Intent {
            if (!loginOptions.requiredScopes.isNullOrEmpty()) {
                intent.putExtra(Constants.EXTRA_REQUIRED_SCOPES, loginOptions.requiredScopes)
            }
            if (!loginOptions.optionalScopes.isNullOrEmpty()) {
                intent.putExtra(Constants.EXTRA_OPTIONAL_SCOPES, loginOptions.optionalScopes)
            }
            intent.putExtra(Constants.EXTRA_CLIENT_ID, options.clientId)
            if (loginOptions.uid != null) {
                intent.putExtra(Constants.EXTRA_UID_VALUE, loginOptions.uid)
            }
            if (loginOptions.loginHint != null) {
                intent.putExtra(Constants.EXTRA_LOGIN_HINT, loginOptions.loginHint)
            }
            intent.putExtra(Constants.EXTRA_USE_TESTING_ENV, options.isTesting)
            intent.putExtra(Constants.EXTRA_FORCE_CONFIRM, loginOptions.isForceConfirm)
            return intent
        }
    }
}
