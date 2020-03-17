package kaaes.spotify.webapi.android;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsCursorPager;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.AudioFeaturesTracks;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * These are functional tests that make actual requests to the Spotify Web API endpoints and
 * compare raw JSON responses with the ones received with the interface crated by this library.
 * They require an access token to run, which is currently pretty manual and annoying but hopefully
 * we'll solve that in the future.
 * <p>
 * Running the tests:
 * ./gradlew :spotify-api:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.access_token=valid_access_token
 */
public class SpotifyServiceAndroidTest {

    private OkHttpClient mClient = new OkHttpClient();
    private SpotifyService mService;
    private Headers mAuthHeader;

    @Before
    public void setUp() throws Exception {
        Bundle arguments = InstrumentationRegistry.getArguments();
        String accessToken = arguments.getString("access_token");
        if (accessToken == null) {
            Assert.fail("Access token can't be null");
        }

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(accessToken);
        mService = spotifyApi.getService();

        mAuthHeader = Headers.of("Authorization", "Bearer " + accessToken);
    }

    private Request getGenericRequest(String url) {
        return new Request.Builder()
                .get()
                .url(url)
                .headers(mAuthHeader)
                .build();
    }

    @Test
    public void getAlbum() throws Exception {
        Call<Album> call = mService.getAlbum("4Mewe6A62ZpJKmVzcaOixy");
        retrofit2.Response<Album> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/albums/4Mewe6A62ZpJKmVzcaOixy");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());
        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getTrack() throws Exception {
        Call<Track> call = mService.getTrack("6Fer9IcKzs4G3nu0MYQmn4");
        retrofit2.Response<Track> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/tracks/6Fer9IcKzs4G3nu0MYQmn4");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getArtist() throws Exception {
        Call<Artist> call = mService.getArtist("54KCNI7URCrG6yjQK3Ukow");
        retrofit2.Response<Artist> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/artists/54KCNI7URCrG6yjQK3Ukow");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getPlaylist() throws Exception {
        Call<Playlist> call = mService.getPlaylist("spotify", "3Jlo5JoAA9pMUQfhLaLG5u");
        retrofit2.Response<Playlist> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/users/spotify/playlists/3Jlo5JoAA9pMUQfhLaLG5u");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getArtistTopTracks() throws Exception {
        Call<Tracks> call = mService.getArtistTopTrack("54KCNI7URCrG6yjQK3Ukow", "SE");
        retrofit2.Response<Tracks> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/artists/54KCNI7URCrG6yjQK3Ukow/top-tracks?country=SE");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getAlbumSearch() throws Exception {
        Call<AlbumsPager> call = mService.searchAlbums("XX");
        retrofit2.Response<AlbumsPager> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/search?type=album&q=XX");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }


    @Test
    public void getMyPlaylist() throws Exception {
        Call<Pager<PlaylistSimple>> call = mService.getMyPlaylists();
        retrofit2.Response<Pager<PlaylistSimple>> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/me/playlists");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getFollowedArtists() throws Exception {

        Call<ArtistsCursorPager> call = mService.getFollowedArtists();
        retrofit2.Response<ArtistsCursorPager> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/me/following?type=artist");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getFollowedArtistsWithOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("limit", 10);
        Call<ArtistsCursorPager> call = mService.followedArtists;
        retrofit2.Response<ArtistsCursorPager> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/me/following?type=artist&limit=10");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getAudioFeaturesForTrack() throws Exception {
        Call<AudioFeaturesTrack> call = mService.getTrackAudioFeatures("6Fer9IcKzs4G3nu0MYQmn4");
        retrofit2.Response<AudioFeaturesTrack> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/audio-features/6Fer9IcKzs4G3nu0MYQmn4");

        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getAudioFeaturesForTracks() throws Exception {
        Call<AudioFeaturesTracks> call = mService.getTracksAudioFeatures("6Fer9IcKzs4G3nu0MYQmn4,24NwBd5vZ2CK8VOQVnqdxr,7cy1bEJV6FCtDaYpsk8aG6");
        retrofit2.Response<AudioFeaturesTracks> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/audio-features?ids=6Fer9IcKzs4G3nu0MYQmn4,24NwBd5vZ2CK8VOQVnqdxr,7cy1bEJV6FCtDaYpsk8aG6");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getUserTopArtists() throws Exception {
        Call<Pager<Artist>> call = mService.getTopArtists();
        retrofit2.Response<Pager<Artist>> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/me/top/artists");

        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }

    @Test
    public void getUserTopTracks() throws Exception {
        Call<Pager<Track>> call = mService.getTopTracks();
        retrofit2.Response<Pager<Track>> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/me/top/tracks");

        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());

        JsonAssert.areEqual(payload.body(), response.body().string());
    }


    @Ignore("We dont test this one because the api gives different results.")
    @Test
    public void getRecommendations() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("seed_artists", "4cJKxS7uOPhwb5UQ70sYpN,6UUrUCIZtQeOf8tC0WuzRy");

        Call<Recommendations> call = mService.getRecommendations(options);
        retrofit2.Response<Recommendations> payload = call.execute();

        Request request = getGenericRequest("https://api.spotify.com/v1/recommendations?seed_artists=4cJKxS7uOPhwb5UQ70sYpN,6UUrUCIZtQeOf8tC0WuzRy");
        Response response = mClient.newCall(request).execute();
        assertEquals(200, response.code());
    }

    private static class JsonAssert {
        private static ObjectMapper mapper = SpotifyApi.newObjectMapper();

        private static void areEqual(Object actualRaw, String json2) {
            try {
                String json1 = mapper.writeValueAsString(actualRaw);
                JsonNode tree1 = mapper.readTree(json1);
                JsonNode tree2 = mapper.readTree(json2);
                assertTrue(tree1.equals(tree2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
