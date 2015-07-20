package br.com.snippet.cordova.twitterclient;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

class TwitterClientApiClient extends TwitterApiClient {
    public TwitterClientApiClient(TwitterSession session) {
        super(session);
    }

    public FriendsService getFriendsService() {
        return getService(FriendsService.class);
    }

    interface FriendsService {
        @GET("/1.1/friends/list.json")
        void friends(@Query("user_id") long id,
                     @Query("screen_name") String screen_name,
                     @Query("cursor") Integer cursor,
                     @Query("count") Integer count,
                     @Query("skip_status") boolean skip_status,
                     @Query("include_user_entities") boolean include_user_entities,
                     Callback<UsersCursor> cb);
    }

    class UsersCursor {
        @SerializedName("previous_cursor")
        public final int previousCursor;

        @SerializedName("previous_cursor_str")
        public final String previousCursorStr;

        @SerializedName("users")
        public final List<User> users;

        @SerializedName("next_cursor")
        public final int nextCursor;

        public UsersCursor(int previousCursor, String previousCursorStr, int nextCursor, List<User> users) {
            this.previousCursor = previousCursor;
            this.nextCursor = nextCursor;
            this.users = users;
            this.previousCursorStr = previousCursorStr;
        }
    }
}



