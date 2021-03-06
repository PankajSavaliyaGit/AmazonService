package socialinfotech.amazonservice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import socialinfotech.amazonservice.aws.awsauth.AuthHeaderModel;
import socialinfotech.amazonservice.aws.http.HTTPRequest;
import socialinfotech.amazonservice.main.GitHubService;
import socialinfotech.amazonservice.main.ResponseModel;

/**
 * Created by pankaj on 4/07/16.
 */
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mai);
        HTTPRequest httpRequest = new HTTPRequest();
        AuthHeaderModel authHeaderModel = httpRequest.AWSGetData(Credentials.LOGIN_URL, false);
        volly(authHeaderModel);
        retrofit(authHeaderModel);
    }


    private void volly(final AuthHeaderModel authHeaderModel) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Credentials.LOGIN_URL, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response + "");
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error + " ");
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //  headers.put("Content-Type", "application/json");
                headers.put("Authorization", authHeaderModel.getAuthorization());
                headers.put("Content-Type", Credentials.CONTENT_TYPE);
                headers.put("X-Amz-Date", authHeaderModel.getDate());
                headers.put("Host", Credentials.HOST);
                Log.e("header", headers + "");
                return headers;
            }

        };

// Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, "first");

    }


    private void retrofit(final AuthHeaderModel authHeaderModel) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {

                        try {
                            request.addHeader("Authorization", authHeaderModel.getAuthorization());
                            request.addHeader("Content-Type", Credentials.CONTENT_TYPE);
                            request.addHeader("Host", Credentials.HOST);
                            request.addHeader("X-Amz-Date", authHeaderModel.getDate());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).setEndpoint(Credentials.HOST).build();
        GitHubService service = restAdapter.create(GitHubService.class);
        service.login("login", new Callback<ResponseModel>() {
            @Override
            public void success(ResponseModel responseModel, Response response) {
                Log.e("response", response.getReason() + " " + response.getStatus() + "    ");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error + " " + error.getUrl());
            }
        });
    }


}
