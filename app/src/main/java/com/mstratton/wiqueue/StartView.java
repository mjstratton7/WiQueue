package com.mstratton.wiqueue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A login screen that offers login via email/password.
 */
public class StartView extends AppCompatActivity {
    private static final String TAG = "StartView";

    // UI references.
    private LinearLayout mLayout;
    private Spinner serverList;
    private ArrayList<String> serverListData;

    private View mLoginFormView;
    private AutoCompleteTextView mNameView;
    private EditText mPasswordView;
    private TextView mPasswordHint;
    private Button mSignInButton;
    private View mProgressView;

    private ImageView mLoadImageView;
    private TextView mLoadTextView;

    private boolean isHost;
    private boolean reqPassword = false;

    public Network server;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setup UI Elements
        initUI();

        // Create new network object
        server = new Network(this);

        // Define thread for peer discovery
        final Thread discoverThread = new Thread() {
            @Override
            public void run() {

                serverListData = server.discover();

            }
        };

        // Start the  discovery thread
        discoverThread.start();

        // Hold main thread until discovery is complete. Needed for start screen
        // list population.
        try {
            discoverThread.join();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Set adapter for spinner list data loading, using a given layout
        ArrayAdapter<String> serverListAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, serverListData);

        // Set drop down layout style, using a given layout
        serverListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        serverList.setAdapter(serverListAdapter);

        serverList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = parent.getItemAtPosition(pos).toString();

                if (selected.equals(getString(R.string.server_default))) {
                    // No Server Chosen, hide input widgets
                    mLoginFormView.setVisibility(View.GONE);

                } else if (selected.equals(getString(R.string.server_create))) {
                    // New Server Chosen

                    // For UI on PlaylistView
                    isHost = true;

                    // Update Prompts for Create
                    mPasswordView.setVisibility(View.VISIBLE);
                    mPasswordHint.setVisibility(View.VISIBLE);

                    // Show login elements
                    mLoginFormView.setVisibility(View.VISIBLE);
                    mPasswordHint.setVisibility(View.VISIBLE);

                } else if (!selected.equals(getString(R.string.server_default)) && !selected.equals(getString(R.string.server_create))) {
                    // Existing Server Chosen

                    // For UI on PlaylistView
                    isHost = false;

                    // Determine if Server Needs Password
                    // reqPassword = true;

                    // Ipdate prompts for Join
                    mPasswordHint.setVisibility(View.GONE);
                    if (!reqPassword) {
                        mPasswordView.setVisibility(View.GONE);
                    } else if (reqPassword) {
                        mPasswordView.setVisibility(View.VISIBLE);
                    }

                    // Show login elements
                    mLoginFormView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "No selection", Toast.LENGTH_SHORT).show();

            }

        });

        mNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.password || id == EditorInfo.IME_NULL) {

                    Toast.makeText(getApplicationContext(), "Hit Enter on Name View?", Toast.LENGTH_SHORT).show();
                    attemptStart();
                    return true;
                }
                return false;
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.password || id == EditorInfo.IME_NULL) {

                    Toast.makeText(getApplicationContext(), "Hit Enter on Password View?", Toast.LENGTH_SHORT).show();
                    attemptStart();
                    return true;
                }
                return false;
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptStart();
            }
        });

    }

    // Setup Login Form Elements
    private void initUI() {
        mLayout = (LinearLayout) findViewById(R.id.main_layout);

        serverList = (Spinner) findViewById(R.id.server_list);
        serverList.setVisibility(View.VISIBLE);

        mLoginFormView = findViewById(R.id.login_form);
        mLoginFormView.setEnabled(false);

        mNameView = (AutoCompleteTextView) findViewById(R.id.name);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordHint = (TextView) findViewById(R.id.password_optional_label);
        mPasswordHint.setEnabled(false);
        mSignInButton = (Button) findViewById(R.id.sign_in_button);

        mProgressView = (ProgressBar) findViewById(R.id.login_progress);
        mLoadImageView = (ImageView) findViewById(R.id.load_screen);
        mLoadTextView = (TextView) findViewById(R.id.load_screen_info);
    }

    private boolean isNameValid(String name) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            serverList.setVisibility(show ? View.GONE : View.VISIBLE);
            serverList.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    serverList.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            serverList.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptStart() {

        // Reset errors.
        mNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String servername = serverList.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid name address.
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            mNameView.setError(getString(R.string.error_invalid_name));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);


            Intent i = new Intent(StartView.this, PlaylistView.class);

            if (isHost) {
                i.putExtra("type", "host");
                i.putExtra("name", name);
                i.putExtra("password", password);
            } else if (!isHost) {
                i.putExtra("type", "user");
                i.putExtra("servername", servername);
                i.putExtra("name", name);
                i.putExtra("password", password);
            }

            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }
}

