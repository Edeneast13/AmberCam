// Generated code from Butter Knife. Do not modify!
package com.ambercam.android.camera2basic.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.ambercam.android.camera2basic.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class CreateAccountActivity_ViewBinding<T extends CreateAccountActivity> implements Unbinder {
  protected T target;

  private View view2131624059;

  @UiThread
  public CreateAccountActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.mEmailEditText = Utils.findRequiredViewAsType(source, R.id.create_account_email, "field 'mEmailEditText'", EditText.class);
    target.mConfirmEmailEditText = Utils.findRequiredViewAsType(source, R.id.create_account_confirm_email, "field 'mConfirmEmailEditText'", EditText.class);
    target.mPasswordEditText = Utils.findRequiredViewAsType(source, R.id.create_account_password, "field 'mPasswordEditText'", EditText.class);
    target.mConfirmPasswordEditText = Utils.findRequiredViewAsType(source, R.id.create_account_confirm_password, "field 'mConfirmPasswordEditText'", EditText.class);
    view = Utils.findRequiredView(source, R.id.create_account_create, "field 'mCreateButton' and method 'createButtonListener'");
    target.mCreateButton = Utils.castView(view, R.id.create_account_create, "field 'mCreateButton'", Button.class);
    view2131624059 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.createButtonListener();
      }
    });
    target.mToolbar = Utils.findRequiredViewAsType(source, R.id.create_toolbar, "field 'mToolbar'", Toolbar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.mEmailEditText = null;
    target.mConfirmEmailEditText = null;
    target.mPasswordEditText = null;
    target.mConfirmPasswordEditText = null;
    target.mCreateButton = null;
    target.mToolbar = null;

    view2131624059.setOnClickListener(null);
    view2131624059 = null;

    this.target = null;
  }
}
