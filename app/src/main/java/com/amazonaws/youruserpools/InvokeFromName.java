package com.amazonaws.youruserpools;

/**
 * Created by zemoso on 3/8/17.
 */
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;

import java.util.HashMap;
import java.util.Map;

/*
 * A holder for lambda functions
 */
interface MyInterface {

    /**
     * Invoke lambda function "echo". The function name is the method name
     */
    @LambdaFunction
    String listParents();

    /**
     * Invoke lambda function "echo". The functionName in the annotation
     * overrides the default which is the method name
     */
    @LambdaFunction(functionName = "listParents")
    void getParents();
}
public class InvokeFromName  {
    private final String TAG = "InvokeFromName";

    private String funcResult="";

    public String performAction(final Context context) {

        Log.d(TAG,"In function performAction");
        CognitoUserSession session = new AppHelper().getCurrSession();
        Log.d(TAG,"Session :  "+session);
        String idToken = session.getIdToken().getJWTToken();
        Log.d(TAG,"idToken :  "+idToken);
        // Create an instance of CognitoCachingCredentialsProvider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-2:12aea453-5bdd-4df9-a13d-32d3f4510709",
                Regions.US_EAST_2);
        Log.d(TAG,"Credential Provider before logins :  "+credentialsProvider);
        Map<String, String> logins = new HashMap<>();
        //Log.d("Check logins", String.valueOf(idToken));
        logins.put("cognito-idp.us-east-2.amazonaws.com/us-east-2_Bkjf1CqAH",
                idToken);
        credentialsProvider.setLogins(logins);
        Log.d(TAG,"Credential Provider after login :  "+credentialsProvider);


//        Log.d(TAG,"Context :  "+getApplicationContext());
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                context,
                Regions.US_EAST_2,
                credentialsProvider);

        Log.d(TAG,"LambdaInvokeFactory factory = "+factory);
        // Create the Lambda proxy object with default Json data binder.
// You can provide your own data binder by implementing
// LambdaDataBinder
        final MyInterface myInterface = factory.build(MyInterface.class);
        Log.d(TAG,"final MyInterface myInterface = "+ myInterface);
        //NameInfo nameInfo = new NameInfo("John", "Doe");

// The Lambda function invocation results in a network call
// Make sure it is not called from the main thread
        AsyncTask getResult = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
//             invoke "getParent" method. In case it fails, it will throw a
//             LambdaFunctionException.
                try {
                    return myInterface.listParents();
                } catch (LambdaFunctionException lfe) {
                    Log.e("TAG", "Failed to invoke echo", lfe);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    return;
                }
                else {
                    funcResult = result;
                }
                // Do a toast
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            }
        }.execute();
        return funcResult;
//    LambdaInvokerFactory factory = new LambdaInvokerFactory(
//            Context context,//myActivity.getApplicationContext(),
//            REGION,
//            credentialsProvider);
    }


}
