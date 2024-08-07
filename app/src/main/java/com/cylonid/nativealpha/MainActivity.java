package com.cylonid.nativealpha;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.cylonid.nativealpha.activities.NewsActivity;
import com.cylonid.nativealpha.fragments.webapplist.WebAppListFragment;
import com.cylonid.nativealpha.model.DataManager;
import com.cylonid.nativealpha.model.WebApp;
import com.cylonid.nativealpha.util.Const;
import com.cylonid.nativealpha.util.EntryPointUtils;
import com.cylonid.nativealpha.util.LocaleUtils;
import com.cylonid.nativealpha.util.Utility;
import com.cylonid.nativealpha.util.WebViewLauncher;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import static android.widget.LinearLayout.HORIZONTAL;

public class MainActivity extends AppCompatActivity {
//    private LinearLayout mainScreen;
    private WebAppListFragment webAppListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webAppListFragment = (WebAppListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
        EntryPointUtils.entryPointReached(this);

        if (DataManager.getInstance().getWebsites().size() == 0) {
            buildAddWebsiteDialog(getString(R.string.welcome_msg));
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> buildAddWebsiteDialog(getString(R.string.add_webapp)));
        personalizeToolbar();

    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(Const.INTENT_BACKUP_RESTORED, false)) {

            webAppListFragment.updateWebAppList();

            buildImportSuccessDialog();
            intent.putExtra(Const.INTENT_BACKUP_RESTORED, false);
            intent.putExtra(Const.INTENT_REFRESH_NEW_THEME, false);
        }
        if (intent.getBooleanExtra(Const.INTENT_WEBAPP_CHANGED, false)) {
            webAppListFragment.updateWebAppList();
            intent.putExtra(Const.INTENT_WEBAPP_CHANGED, false);
        }
    }
    private void personalizeToolbar()  {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.native_alpha_white);
        @StringRes int appName = !BuildConfig.FLAVOR.equals("extended") ? R.string.app_name : R.string.app_name_plus;
        toolbar.setTitle(appName);
        setSupportActionBar(toolbar);
    }

    private void buildImportSuccessDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String message =  getString(R.string.import_success_dialog_txt2) + "\n\n" + getString(R.string.import_success_dialog_txt3);

        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.import_success, DataManager.getInstance().getActiveWebsitesCount()));
        builder.setPositiveButton(getString(android.R.string.yes), (dialog, id) -> {

            ArrayList<WebApp> webapps = DataManager.getInstance().getActiveWebsites();

            for (int i = webapps.size() - 1; i >= 0; i--) {
                WebApp webapp = webapps.get(i);
                boolean last_webapp = i == webapps.size() - 1;
                Spanned msg = Html.fromHtml(getString(R.string.restore_shortcut, webapp.getTitle()), Html.FROM_HTML_MODE_COMPACT);
                final AlertDialog addition_dialog = new AlertDialog.Builder(this)
                        .setMessage(msg)
                        .setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                            ShortcutDialogFragment frag = ShortcutDialogFragment.newInstance(webapp);
                            frag.show(getSupportFragmentManager(), "SCFetcher-" + webapp.getID());
                        })
                        .setNegativeButton(android.R.string.no, (dialog1, which) -> {
                        })
                        .create();

                addition_dialog.show();

            }

        });
        builder.setNegativeButton(getString(android.R.string.no),  (dialog, id) -> { });
        builder.create().show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void buildAddWebsiteDialog(String title) {
        final View inflated_view = getLayoutInflater().inflate(R.layout.add_website_dialogue, null);
        final EditText url = inflated_view.findViewById(R.id.websiteUrl);
        final Switch create_shortcut = inflated_view.findViewById(R.id.switchCreateShortcut);

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(inflated_view)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            url.requestFocus();
            positive.setOnClickListener(view -> {
                String str_url = url.getText().toString().trim();
                if(str_url.equals("")) {
                    Utility.showInfoSnackbar(this, getString(R.string.no_url_entered), Snackbar.LENGTH_SHORT);
                } else {
                    if (!(str_url.startsWith("https://")) && !(str_url.startsWith("http://"))) {
                        str_url = "https://" + str_url;
                    }
                    WebApp new_site = new WebApp(str_url, DataManager.getInstance().getIncrementedID());
                    new_site.applySettingsForNewWebApp();
                    DataManager.getInstance().addWebsite(new_site);

                    webAppListFragment.updateWebAppList();
                    dialog.dismiss();
                    if (create_shortcut.isChecked()) {
                        ShortcutDialogFragment frag = ShortcutDialogFragment.newInstance(new_site);
                        frag.show(getSupportFragmentManager(), "SCFetcher-" + new_site.getID());
                    }
                }
            });
        });
        dialog.show();
    }



}


