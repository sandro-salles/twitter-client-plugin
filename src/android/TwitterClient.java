package br.com.snippet.twitterclient;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class TwitterClient extends CordovaPlugin {

	private static final String LOG_TAG = "Twitter Client";
	private String action;

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Fabric.with(cordova.getActivity().getApplicationContext(), new Twitter(new TwitterAuthConfig(getTwitterKey(), getTwitterSecret())));
		Log.v(LOG_TAG, "Initialize TwitterClient");
	}

	private String getTwitterKey() {
		return preferences.getString("TwitterConsumerKey", "");
	}

	private String getTwitterSecret() {
		return preferences.getString("TwitterConsumerSecret", "");
	}

	public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		Log.v(LOG_TAG, "Received: " + action);
		this.action = action;
		final Activity activity = this.cordova.getActivity();
		final Context context = activity.getApplicationContext();
		cordova.setActivityResultCallback(this);
		if (action.equals("login")) {
			login(activity, callbackContext);
			return true;
		}
		if (action.equals("logout")) {
			logout(callbackContext);
			return true;
		}
		return false;
	}

	private void login(final Activity activity, final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Twitter.logIn(activity, new Callback<TwitterSession>() {
					@Override
					public void success(Result<TwitterSession> twitterSessionResult) {
						Log.v(LOG_TAG, "Successful login session!");
						callbackContext.success(handleResult(twitterSessionResult.data));

					}

					@Override
					public void failure(TwitterException e) {
						Log.v(LOG_TAG, "Failed login session");
						callbackContext.error("Failed login session");
					}
				});
			}
		});
	}

	private void logout(final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Twitter.logOut();
				Log.v(LOG_TAG, "Logged out");
				callbackContext.success();
			}
		});
	}

	private void get(final Activity activity, final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Twitter.logIn(activity, new Callback<TwitterSession>() {
					@Override
					public void success(Result<TwitterSession> twitterSessionResult) {
						Log.v(LOG_TAG, "Successful login session!");
						callbackContext.success(handleResult(twitterSessionResult.data));

					}

					@Override
					public void failure(TwitterException e) {
						Log.v(LOG_TAG, "Failed login session");
						callbackContext.error("Failed login session");
					}
				});
			}
		});
	}

	private JSONObject handleResult(TwitterSession result) {
		JSONObject response = new JSONObject();
		try {
			response.put("userName", result.getUserName());
			response.put("userId", result.getUserId());
			response.put("secret", result.getAuthToken().secret);
			response.put("token", result.getAuthToken().token);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response;
	}

	private void handleLoginResult(int requestCode, int resultCode, Intent intent) {
		TwitterLoginButton twitterLoginButton = new TwitterLoginButton(cordova.getActivity());
		twitterLoginButton.onActivityResult(requestCode, resultCode, intent);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.v(LOG_TAG, "activity result: " + requestCode + ", code: " + resultCode);
		if (action.equals("login")) {
			handleLoginResult(requestCode, resultCode, intent);
		}
	}
}
