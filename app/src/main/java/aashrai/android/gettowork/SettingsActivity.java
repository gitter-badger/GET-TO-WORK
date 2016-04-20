package aashrai.android.gettowork;

import aashrai.android.gettowork.adapter.PackageListAdapter;
import aashrai.android.gettowork.presenter.SettingsActivityPresenter;
import aashrai.android.gettowork.view.SettingsView;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import com.jakewharton.rxbinding.widget.RxTextView;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class SettingsActivity extends BaseActivity
    implements SettingsView, Action1<CharSequence>, TextView.OnEditorActionListener {

  @Bind(R.id.rv_packages) RecyclerView packageList;
  @Bind(R.id.progress) ProgressBar progressBar;
  @Bind(R.id.et_search) EditText search;
  @Inject SettingsActivityPresenter presenter;
  PackageListAdapter adapter;
  CompositeSubscription compositeSubscription;
  boolean firstSearchFlag = true;
  private static final String TAG = "SettingsActivity";

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((GoToWorkApplication) getApplication()).getApplicationComponent()
        .getSettingsComponent()
        .inject(this);
    presenter.setView(this);
    search.setOnEditorActionListener(this);
    compositeSubscription = new CompositeSubscription();
    compositeSubscription.add(RxTextView.textChanges(search)
        .debounce(100, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this));
  }

  @Override public int getLayoutId() {
    return R.layout.activity_settings;
  }

  @Override public void setPackageListAdapter(PackageListAdapter adapter) {
    this.adapter = adapter;
    packageList.setLayoutManager(new LinearLayoutManager(this));
    packageList.setAdapter(this.adapter);
  }

  @Override public void updatePackageListAdapter(List<ApplicationInfo> packageList) {
    Log.d(TAG, "updatePackageListAdapter: " + packageList.size());
    adapter.updatePackageList(packageList);
  }

  @Override public void startProgressBar() {
    progressBar.setVisibility(View.VISIBLE);
  }

  @Override public void stopProgressBar() {
    progressBar.setVisibility(View.GONE);
  }

  @Override public void call(CharSequence charSequence) {
    if (!firstSearchFlag) {
      presenter.onSearch(charSequence.toString());
    } else {
      firstSearchFlag = false;
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    compositeSubscription.unsubscribe();
    presenter.onDestroy();
  }

  @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
      return true;
    }
    return false;
  }
}


