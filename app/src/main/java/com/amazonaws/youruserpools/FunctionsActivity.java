package com.amazonaws.youruserpools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
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
 * Created by zemoso on 4/8/17.
 */

public class FunctionsActivity extends AppCompatActivity {
    private final String TAG = " Functions Activity ";

        private Button getAllParents;
    private Button getParent;
    private Button getChildren;
    private TextView listContent;
    private Toolbar toolbar;

    private CognitoUser user;
    private CognitoUserSession session;
    private CognitoUserDetails details;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;


    private  String parentsList="No entries";

    private String uri ="https://pr0kx7v4dg.execute-api.us-east-2.amazonaws.com/dev/";
    private final String default_uri= uri;
    private String username;
    private ListView attributesList;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG,"In callFunctions");
        setContentView(R.layout.call_functions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d(TAG,"In callFunctions");
        init();
        // region Set toolbar for this screen
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("Account");
        setSupportActionBar(toolbar);
        //endregion

        listContent = (TextView) findViewById(R.id.listContent);
        listContent.setMovementMethod(new ScrollingMovementMethod());
        listContent.setSingleLine(false);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), //context
                "us-east-2:12aea453-5bdd-4df9-a13d-32d3f4510709", // Identity pool ID
                Regions.US_EAST_2 // Region
        );
        // Set up as a credentials provider.
        Map<String, String> logins = new HashMap<>();
        Log.d(TAG,"Check logins "+ credentialsProvider);
        logins.put("cognito-idp.us-east-2.amazonaws.com/us-east-2_Bkjf1CqAH",
                session.getIdToken().getJWTToken());
        credentialsProvider.setLogins(logins);

        getAllParents = (Button) findViewById(R.id.getAllParent);
        getAllParents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"getAllParents");
                listContent.clearComposingText();
                uri=default_uri;
                uri +="allparents";
                getList();
                //new CallFunctionsUseHttp(getApplicationContext(),listContent).getList(uri,session);
                //listContent.setText(result);
            }
        });

        getParent = (Button) findViewById(R.id.get_parent);
        getParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listContent.clearComposingText();
                uri=default_uri;
                uri+="parents";
                getList();
                //performAction();
            }
        });
        getChildren = (Button) findViewById(R.id.get_children);
        getChildren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listContent.clearComposingText();
                uri=default_uri;
                uri+="children";
                getList();
            }
        });


    }

    // Initialize this activity
    private void init() {
        // Get the user name
        Bundle extras = getIntent().getExtras();
        username = AppHelper.getCurrUser();
        user = AppHelper.getPool().getUser(username);
        session= AppHelper.getCurrSession();
        getDetails();
    }



//    //region lambda call
//    //----------------------------------------------------------------------------------------------------------
//    interface MyInterface {
//
//        /**
//         * Invoke lambda function "echo". The function name is the method name
//         */
//        @LambdaFunction
//        String
//        listParents();
//
//        /**
//         * Invoke lambda function "echo". The functionName in the annotation
//         * overrides the default which is the method name
//         */
////        @LambdaFunction(functionName = "listParents")
////        void listParents();
//    }
//    private final String TAG = "InvokeFromName";
//
//    private String funcResult="";
//
//    public void performAction() {
//
//        Log.d(TAG,"In function performAction");
//        //CognitoUserSession session = new AppHelper().getCurrSession();
//        Log.d(TAG,"Session :  "+session);
//        String idToken = session.getIdToken().getJWTToken();
//        Log.d(TAG,"idToken :  "+idToken);
//        // Create an instance of CognitoCachingCredentialsProvider
//        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                getApplicationContext(),
//                "us-east-2:12aea453-5bdd-4df9-a13d-32d3f4510709",
//                Regions.US_EAST_2);
//        Log.d(TAG,"Credential Provider before logins :  "+credentialsProvider);
//        Map<String, String> logins = new HashMap<>();
//        //Log.d("Check logins", String.valueOf(idToken));
//        logins.put("cognito-idp.us-east-2.amazonaws.com/us-east-2_Bkjf1CqAH",
//                idToken);
//        credentialsProvider.setLogins(logins);
//        Log.d(TAG,"Credential Provider after login :  "+credentialsProvider);
//
//
//
//
//// The Lambda function invocation results in a network call
//// Make sure it is not called from the main thread
//        UserActivity.AsyncCallFunc call= new UserActivity.AsyncCallFunc();
//        call.execute();
//
//
//    }
//    private class AsyncCallFunc extends AsyncTask<Void,Void,String> {
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            LambdaInvokerFactory factory = new LambdaInvokerFactory(
//                    getApplicationContext(),
//                    Regions.US_EAST_2,
//                    credentialsProvider);
//
//            Log.d(TAG,"LambdaInvokeFactory factory = "+factory);
////             invoke "getParent" method. In case it fails, it will throw a
////             LambdaFunctionException.
//            UserActivity.MyInterface myInterface = factory.build(UserActivity.MyInterface.class);
//            Log.d(TAG,"final MyInterface myInterface = "+ myInterface);
//
//            try {
//                return myInterface.
//                        listParents();
//            } catch (LambdaFunctionException lfe) {
//                Log.e("TAG", "Failed to invoke echo", lfe);
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            if (result == null) {
//                return;
//            } else {
//                funcResult = result;
//            }
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    listContent.setText(funcResult);
//                }
//            });
//            // Do a toast
//            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//        }
//    }
//
////-------------------------------------------------------------------------------------------------------------
//    //endregion


    //region Async Function
    private class AsyncGetResult extends AsyncTask<Void, Void, Void> {
        private String func_uri;
        //CognitoCachingCredentialsProvider credentialsProvider;
        @Override
        public Void doInBackground(Void... voids) {
            func_uri=uri;

            callFuncCreateSig();
            return null;
        }
        private void getCredentialProvider(){
            String idToken = session.getIdToken().getJWTToken();
            Log.d("idtoken",idToken);

            credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(), //context
                    "us-east-2:12aea453-5bdd-4df9-a13d-32d3f4510709", // Identity pool ID
                    Regions.US_EAST_2 // Region
            );
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
        private void callFuncCreateSig(){

            //getCredentialProvider();
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

                response = httpClient.newCall(request2).execute();
                if(response!=null) {
                    String body = response.body().string();
                    parentsList = body;
                }
                //cant directly use listContent.setText(body); as setText must be run in UIThread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listContent.setText(parentsList);
                    }
                });
                Log.d("response " , parentsList);
            } catch (Exception e) {
                Log.d("error " , String.valueOf(e));
            }
        }

    }
    //endregion

    private void getList() {
        //Call the Async function
        AsyncGetResult callAllParents = new AsyncGetResult();
        callAllParents.execute();
        //SEssion expiry time: 3600
        Log.d("SEssion expiry time", String.valueOf(credentialsProvider.getSessionDuration()));
    }




    //region default operations
    private void getDetails() {
        AppHelper.getPool().getUser(username).getDetailsInBackground(detailsHandler);
    }
    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            AppHelper.setUserDetails(cognitoUserDetails);
            showAttributes();
            // Trusted devices?
            handleTrustedDevice();
        }
        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Could not fetch user details!", AppHelper.formatException(exception), true);
        }
    };
    private void showAttributes() {
        final UserAttributesAdapter attributesAdapter = new UserAttributesAdapter(getApplicationContext());
        final ListView attributesListView;
        attributesListView = (ListView) findViewById(R.id.listViewUserAttributes);
        attributesListView.setAdapter(attributesAdapter);
        attributesList = attributesListView;

        attributesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView data = (TextView) view.findViewById(R.id.editTextUserDetailInput);
                String attributeType = data.getHint().toString();
                String attributeValue = data.getText().toString();
                showUserDetail(attributeType, attributeValue);
            }
        });
    }


    private void handleTrustedDevice() {
        CognitoDevice newDevice = AppHelper.getNewDevice();
        if (newDevice != null) {
            AppHelper.newDevice(null);
            trustedDeviceDialog(newDevice);
        }
    }

    private void updateDeviceStatus(CognitoDevice device) {
        device.rememberThisDeviceInBackground(trustedDeviceHandler);
    }

    private void trustedDeviceDialog(final CognitoDevice newDevice) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remember this device?");
        //final EditText input = new EditText(UserActivity.this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        //input.setLayoutParams(lp);
        //input.requestFocus();
        //builder.setView(input);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //String newValue = input.getText().toString();
                    showWaitDialog("Remembering this device...");
                    updateDeviceStatus(newDevice);
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    // Callback handlers

    UpdateAttributesHandler updateHandler = new UpdateAttributesHandler() {
        @Override
        public void onSuccess(List<CognitoUserCodeDeliveryDetails> attributesVerificationList) {
            // Update successful
            if(attributesVerificationList.size() > 0) {
                showDialogMessage("Updated", "The updated attributes has to be verified",  false);
            }
            getDetails();
        }

        @Override
        public void onFailure(Exception exception) {
            // Update failed
            closeWaitDialog();
            showDialogMessage("Update failed", AppHelper.formatException(exception), false);
        }
    };

    GenericHandler deleteHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            closeWaitDialog();
            // Attribute was deleted
            Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT);

            // Fetch user details from the the service
            getDetails();
        }

        @Override
        public void onFailure(Exception e) {
            closeWaitDialog();
            // Attribute delete failed
            showDialogMessage("Delete failed", AppHelper.formatException(e), false);

            // Fetch user details from the service
            getDetails();
        }
    };

    GenericHandler trustedDeviceHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            // Close wait dialog
            closeWaitDialog();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Failed to update device status", AppHelper.formatException(exception), true);
        }
    };

    private void showUserDetail(final String attributeType, final String attributeValue) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(attributeType);
        final EditText input = new EditText(FunctionsActivity.this);
        input.setText(attributeValue);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        input.requestFocus();
        builder.setView(input);

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String newValue = input.getText().toString();
                    if(!newValue.equals(attributeValue)) {
                        showWaitDialog("Updating...");
                        updateAttribute(AppHelper.getSignUpFieldsC2O().get(attributeType), newValue);
                    }
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }
    private void updateAttribute(String attributeType, String attributeValue) {

        if(attributeType == null || attributeType.length() < 1) {
            return;
        }
        CognitoUserAttributes updatedUserAttributes = new CognitoUserAttributes();
        updatedUserAttributes.addAttribute(attributeType, attributeValue);
        Toast.makeText(getApplicationContext(), attributeType + ": " + attributeValue, Toast.LENGTH_LONG);
        showWaitDialog("Updating...");
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).updateAttributesInBackground(updatedUserAttributes, updateHandler);
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exit) {
                        exit();
                    }
                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG,"Dialog dismiss failed");
                    if(exit) {
                        exit();
                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }

    private void exit () {
        Intent intent = new Intent();
        if(username == null)
            username = "";
        intent.putExtra("name",username);
        setResult(RESULT_OK, intent);
        finish();
    }
    //endregion


}

