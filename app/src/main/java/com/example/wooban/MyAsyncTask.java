package com.example.wooban;

import android.os.AsyncTask;
import android.util.Log;

public class MyAsyncTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = " MyAsyncTask";
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        MailSend mail = new MailSend();
        // asyncTask.execute() 에 입력된 첫번째 값이 params
        mail.MailSend(params[0], params[1]);
        Log.d(TAG, params[0]);
        Log.d(TAG, params[1]);
        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
    }
}