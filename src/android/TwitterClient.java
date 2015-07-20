package br.com.snippet.cordova.twitterclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

		if (action.equals("friends")) {
			friends(activity, callbackContext);
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
					public void success(final Result<TwitterSession> twitterSessionResult) {
						Log.v(LOG_TAG, "Successful login session!");

						TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
						twitterApiClient.getAccountService().verifyCredentials(false, false, new Callback<User>() {
							@Override
							public void success(Result<User> userResult) {
								callbackContext.success(handleSessionResult(twitterSessionResult.data, userResult.data));
							}

							@Override
							public void failure(TwitterException e) {
								Log.v(LOG_TAG, "Failed credentials verification");
								callbackContext.error("Failed credentials verification");
							}
						});
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

	private void friends(final Activity activity, final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {

				final ArrayList<User> friends = new ArrayList<User>();
				final TwitterClientApiClient twitterApiClient = new TwitterClientApiClient(Twitter.getSessionManager().getActiveSession());
				twitterApiClient.getFriendsService().friends(Twitter.getSessionManager().getActiveSession().getUserId(), null, null, 200, true, false, new Callback<TwitterClientApiClient.UsersCursor>() {
					@Override
					public void success(Result<TwitterClientApiClient.UsersCursor> result) {

						friends.addAll(result.data.users);

						if (result.data.nextCursor > 0){
							twitterApiClient.getFriendsService().friends(Twitter.getSessionManager().getActiveSession().getUserId(), null, result.data.nextCursor, 200, true, false, this);
						} else {
							Log.v(LOG_TAG, "Successful friends list loaded!");
							callbackContext.success(handleFriendsResult(friends));
						}
					}

					@Override
					public void failure(TwitterException e) {
						Log.v(LOG_TAG, "Failed to load friends list");
						callbackContext.error("Failed to load friends list");
					}
				});
			}
		});
	}


	private JSONArray handleFriendsResult(List<User> users) {

		JSONArray response = new JSONArray();

		for (User user: users) {
			JSONObject object = new JSONObject();
			try {
				object.put("name", user.name);
				object.put("screenName", user.screenName);
				object.put("id", user.id);
				object.put("profileImageUrl", user.profileImageUrl);
				response.put(object);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return response;
	}


	private JSONObject handleSessionResult(TwitterSession session, User user) {
		JSONObject response = new JSONObject();

		try {
			response.put("name", user.name);
			response.put("screenName", user.screenName);
			response.put("id", user.id);
			response.put("secret", session.getAuthToken().secret);
			response.put("token", session.getAuthToken().token);
			response.put("profileImageUrl", user.profileImageUrl);
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
