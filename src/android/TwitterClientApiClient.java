package br.com.snippet.cordova.twitterclient;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import retrofit.http.GET;
import retrofit.http.Query;

class TwitterClientApiClient extends TwitterApiClient {
    public TwitterClientApiClient(TwitterSession session) {
        super(session);
    }

    public FriendsService getFriendsService() {
        return getService(FriendsService.class);
    }
}

interface FriendsService {
    @GET("/1.1/friends/list.json")
    void friends(@Query("user_id") long id,
                 @Query("screen_name") String screen_name,
                 @Query("cursor") Long cursor,
                 @Query("count") Integer count,
                 @Query("skip_status") boolean skip_status,
                 @Query("include_user_entities") boolean include_user_entities,
                 Callback<User> cb);
}