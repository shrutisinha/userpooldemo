package com.amazonaws.youruserpools;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * Created by zemoso on 3/8/17.
 */

public class CallFunctionsUseHttp extends AppCompatActivity {


    private CognitoCachingCredentialsProvider credentialsProvider;
    private String func_uri;
    private CognitoUserSession session;
    private Context context;
    private TextView listContent;
    private String parentsList;
    private String uri ="https://pr0kx7v4dg.execute-api.us-east-2.amazonaws.com/dev/";
    private final String default_uri= uri;
    private Button getAllParents;

//    public CallFunctionsUseHttp(Context context,TextView listContent){
//        Log.d("CallFunctionsUseHttp"," entered the function");
//        this.context=context;
//        this.listContent = listContent;
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.d("call_functions","new page");

        getAllParents = (Button) findViewById(R.id.getAllParent);
        getAllParents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listContent.clearComposingText();
                uri=default_uri;
                uri +="allparents";
                //new CallFunctionsUseHttp(getApplicationContext(),listContent).getParentsList(uri,session);
                //listContent.setText(result);
                getParentsList();
                //setContentView(R.layout.call_functions);

            }
        });
    }
        private class AsyncGetResult extends AsyncTask<Void, Void, String> {
        //private String func_uri;
        //CognitoCachingCredentialsProvider credentialsProvider;
        @Override
        public String doInBackground(Void... voids) {


            String result = callFuncCreateSig();
            return result;
        }
        @Override
        public void onPostExecute(String result){
            listContent.setText(result);
        }
        private void getCredentialProvider(){
            String idToken = session.getIdToken().getJWTToken();
            Log.d("idtoken",idToken);


            AppHelper x = new AppHelper();
            x.getCurrUser();x.getCurrSession();
            // Set up as a credentials provider.
            Map<String, String> logins = new HashMap<>();
            //Log.d("Check logins", String.valueOf(idToken));
            logins.put("cognito-idp.us-east-2.amazonaws.com/us-east-2_Bkjf1CqAH",
                    idToken);
            credentialsProvider.setLogins(logins);

        }
        private String generateIdentityId(){
            //CognitoCachingCredentialsProvider credentialsProvider = getCredentialProvider();
            String identityId = credentialsProvider.getIdentityId();
            Log.d("identityId ",identityId);
            String AccessKey = credentialsProvider.getCredentials().getAWSAccessKeyId();
            String SecretKey = credentialsProvider.getCredentials().getAWSSecretKey();
            String SessionKey = credentialsProvider.getCredentials().getSessionToken();

            Log.d("AccessKey = " , AccessKey);
            Log.d("SecretKey = " , SecretKey);
            Log.d("SessionKey = " , SessionKey);

            return identityId;
        }
        private String callFuncCreateSig(){

            getCredentialProvider();
            String identityId = generateIdentityId();
            //Step 3: Create an aws requets and sign by using AWS4Signer class (API_GATEWAY_SERVICE_NAME);

            String AccessKey = credentialsProvider.getCredentials().getAWSAccessKeyId();
            String SecretKey = credentialsProvider.getCredentials().getAWSSecretKey();
            String SessionKey = credentialsProvider.getCredentials().getSessionToken();

            Log.d("AccessKey = " , AccessKey);
            Log.d("SecretKey = " , SecretKey);
            Log.d("SessionKey = " , SessionKey);
            AmazonWebServiceRequest amazonWebServiceRequest = new AmazonWebServiceRequest() {
            };

            //ClientConfiguration clientConfiguration = new ClientConfiguration();

            String API_GATEWAY_SERVICE_NAME = "execute-api";

            DefaultRequest request = new DefaultRequest(amazonWebServiceRequest,API_GATEWAY_SERVICE_NAME);
            request.setEndpoint(URI.create(func_uri));
            request.setHttpMethod(HttpMethodName.GET);

            Log.d("Method name", String.valueOf(request.getHttpMethod()));
            Log.d("Endpoint", String.valueOf(request.getEndpoint()));

            //Create the AWS4 Signature
            AWS4Signer signer = new AWS4Signer();
            signer.setServiceName(API_GATEWAY_SERVICE_NAME);
            signer.setRegionName(Region.getRegion(Regions.US_EAST_2).getName());
            signer.sign(request, credentialsProvider.getCredentials());

            Log.d("Request header " ,request.getHeaders().toString());
            Log.d("AWS4Signer ", String.valueOf(signer));

            //Step 4: Create new request with authorization headers
            OkHttpClient httpClient = new OkHttpClient();
            Map<String, String> headers = request.getHeaders();
            List<String> key = new ArrayList<>();
            List<String> value = new ArrayList<>();
            for (Map.Entry<String, String> entry : headers.entrySet())
            {
                key.add(entry.getKey());
                value.add(entry.getValue());
            }
            Log.d("headers map", String.valueOf(headers));
            try {
                okhttp3.Request request2 = new okhttp3.Request.Builder()
                        .url(func_uri+"/") // remember to add / to the end of the uri, otherwise the signature will be different
                        .addHeader(key.get(0), value.get(0))
                        .addHeader(key.get(1), value.get(1))
                        .addHeader(key.get(2), value.get(2))
                        .addHeader(key.get(3), value.get(3))
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();
                okhttp3.Response response = null;
                Log.d("HTTP Request 2", String.valueOf(request2));
                String body;
                response = httpClient.newCall(request2).execute();
                if(response!=null) {
                    body = response.body().string();
                    parentsList = body;
                }
                //cant directly use listContent.setText(body); as setText must be run in UIThread

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        listContent.setText(parentsList);
//                    }
//                });
                Log.d("response " , parentsList);
            } catch (Exception e) {
                Log.d("error " , String.valueOf(e));
            }
            return parentsList;
        }

    }

    public void getParentsList() {
        Log.d("getParentsList","1");
        //Call the Async function
        //this.session=session;
        //this.context= context;
        //this.listContent=listContent;
        func_uri=uri;
        AsyncGetResult callAllParents = new AsyncGetResult();
        callAllParents.execute();
        //listContent.setText("I AM WRITING");
        //listContent.setText(parentsList);
        //SEssion expiry time: 3600
        Log.d("SEssion expiry time", String.valueOf(credentialsProvider.getSessionDuration()));

    }
}
