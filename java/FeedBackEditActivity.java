package com.rlk.feedback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ApplicationErrorReport;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.rlk.misdk.account.UserAccount;
import com.rlk.misdk.account.UserCredential;
import com.rlk.feedback.adapter.GridImageAdapter;
import com.rlk.feedback.domain.PhoneDeviceInfo;
import com.rlk.feedback.util.ContantUtil;
import com.rlk.feedback.util.CustomToast;
import com.rlk.feedback.util.FileUtils;
import com.rlk.feedback.util.HttpUtil;
import com.rlk.feedback.util.ImageUtils;
import com.rlk.feedback.util.PreferencesHelper;
import com.rlk.feedback.util.TextUtil;
import com.rlk.feedback.util.Utils;

public class FeedBackEditActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

	private final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0;
	private final int DIALOG_BACK = 1000;
	private EditText mEt_problem;
	private GridView mGridView;
	private Button mBnCommit;
	private UserCredential mCredential;
	private ArrayList<String> mDataList = new ArrayList<String>();
	private GridImageAdapter mGridImageAdapter;
	private ProgressDialog mProgressDialog;
	private int mCatalogId, mFbsortId;
	private String mTitle, mPackageName, mFeedbackId;
	private File mLogFile;
	private MyApplication mApplication;
	private EditText mEt_phoneNumber;
	private ApplicationErrorReport mReport;
	private TextView mTv_text_limit;
	private PreferencesHelper mPreferencesHelper;
	public static final int PHOTO_COUNT = 4;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				mEt_problem.setText(mTitle + ".log");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferencesHelper = new PreferencesHelper(this, Configure.SP_CONFIG_NAME);
		mApplication = MyApplication.getInstance();
		mApplication.addActivities(this);
		PhoneDeviceInfo.newPhoneDeviceInfo(this);
		mReport = (ApplicationErrorReport) getIntent().getExtra(Intent.EXTRA_BUG_REPORT);
		if (mReport != null) {
			setContentView(R.layout.layout_feedback_log);
			mFbsortId = Configure.CATEGORY_BUG;
			mCatalogId = Configure.LOG_CATLOGID;
			initView_log();
			mTitle = mReport.packageName;
			saveErroLog(mReport);
		} else {
			setContentView(R.layout.activity_feedback_edit_layout);

			TextView tv_tip = (TextView) findViewById(R.id.tv_text);
			mFeedbackId = getIntent().getStringExtra("feedbackId");
			mFbsortId = getIntent().getIntExtra(Configure.PARMA_FBSORTID, -1);
			mCatalogId = getIntent().getIntExtra(Configure.PARMA_CATALOGID, -1);
			if (mFbsortId == Configure.CATEGORY_ADVICE) {
				mTitle = getString(R.string.advice);
				tv_tip.setText(R.string.advice_descir);
			} else if (mFbsortId == Configure.CATEGORY_BUG) {
				if (mCatalogId == Configure.APP) {
					mTitle = getIntent().getStringExtra(Configure.APP_NAME);
					mPackageName = getIntent().getStringExtra(Configure.APP_PACKAGE);
				} else {
					mTitle = getTitle(mCatalogId);
				}
				tv_tip.setText(R.string.problem_descir);
			} else if (mFbsortId == Configure.CATEGORY_HELP) {
				mTitle = getString(R.string.help);
				tv_tip.setText(R.string.help_descir);
			} else if (mFbsortId == Configure.REPLY) {
				mTitle = getString(R.string.reply);
				tv_tip.setText(R.string.reply_descir);
			}

			if (mCatalogId == 9 && mFbsortId == -1) { // Account title
				mTitle = getString(R.string.app_account);
				mFbsortId = Configure.CATEGORY_BUG;
				mCatalogId = Configure.APP;
				mPackageName = "com.rlk.mi";
			}
			initView();
		}
		this.setTitle(mTitle);
	}

	public String getTitle(int position) {
		switch (position) {
		case 1:
			return getString(R.string.err_type_reboot);
		case 2:
			if (Configure.LAN_FR.equals(TextUtil.getLocalLanguage(this))) {
				return getString(R.string.err_type_power_consuming_title);
			} else {
				return getString(R.string.err_type_power_consuming);
			}
		case 3:
			return getString(R.string.err_type_signal);
		case 4:
			return getString(R.string.err_type_wifi);
		case 5:
			return getString(R.string.err_type_bluetooth);
		case 6:
			return getString(R.string.err_type_camera);
		case 10:
			return getString(R.string.err_type_lag);

		default:
			getString(R.string.app_name);
			break;
		}
		return null;
	}

	public void initView_log() {
		mEt_problem = (EditText) findViewById(R.id.et_probleam);
		mBnCommit = (Button) findViewById(R.id.bn_commit);
		mBnCommit.setOnClickListener(this);
		this.mTitleBtnLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String body = mEt_problem.getText().toString().trim();
				if (!("".equals(body))) {
					showDialog(DIALOG_BACK);
				} else {
					FeedBackEditActivity.this.finish();
				}
			}
		});
	}

	public void initView() {
		mEt_problem = (EditText) findViewById(R.id.et_probleam);
		mTv_text_limit = (TextView) findViewById(R.id.tv_text_limit);
		mTv_text_limit.setText("0/300" + getString(R.string.chartmater));

		mEt_phoneNumber = (EditText) findViewById(R.id.et_phone);
		if (mFbsortId == Configure.REPLY || mFbsortId == Configure.CATEGORY_BUG) {
			findViewById(R.id.tv_tip).setVisibility(View.GONE);
			mEt_phoneNumber.setVisibility(View.GONE);
		}

		mbn.setVisibility(View.VISIBLE);
		mGridView = (GridView) this.findViewById(R.id.gv_show);
		mbn.setOnClickListener(this);

		mDataList.add(Configure.ADD_PICTURE_DEFAULT);
		mGridImageAdapter = new GridImageAdapter(this, mDataList);
		mGridView.setAdapter(mGridImageAdapter);
		mGridView.setOnItemClickListener(this);

		this.mTitleBtnLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String body = mEt_problem.getText().toString().trim();
				if (!("".equals(body)) || getOriginFiles().size() > 0) {
					showDialog(DIALOG_BACK);
				} else {
					mPreferencesHelper.putValue(Configure.KEY_PREFERENCE_IS_REFRESH, false);
					FeedBackEditActivity.this.finish();
				}
			}
		});

		InputFilter[] textFilters = new InputFilter[1];
		textFilters[0] = new InputFilter.LengthFilter(Configure.INPUTMAX_LIMIT) {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				if (source.length() > 0 && dest.length() == Configure.INPUTMAX_LIMIT) {
					Toast.makeText(FeedBackEditActivity.this, R.string.input_text_limit, 0).show();
				}
				return super.filter(source, start, end, dest, dstart, dend);
			}
		};
		mEt_problem.setFilters(textFilters);
		mEt_problem.addTextChangedListener(new EditChangedListener());
	}

	public void saveErroLog(final ApplicationErrorReport report) {
		mProgressDialog = ProgressDialog.show(this, "", getString(R.string.log_loading));
		new Thread() {
			public void run() {
				/*
				 * Log.d(TAG,"installerPackageName=" +
				 * report.installerPackageName); Log.d(TAG,"packageName=" +
				 * report.packageName); Log.d(TAG,"processName=" +
				 * report.processName); Log.d(TAG,"systemApp=" +
				 * report.systemApp); Log.d(TAG,"time=" + report.time);
				 * Log.d(TAG,"type=" + report.type);
				 */

				StringBuilder builder = new StringBuilder();
				builder.append("packageName=" + report.packageName);
				builder.append("\r\n");
				builder.append("versionName=" + FileUtils.getVersion(FeedBackEditActivity.this, report.packageName));
				builder.append("\r\n");
				switch (report.type) {
				case ApplicationErrorReport.TYPE_CRASH:
					String throwFileName = report.crashInfo.throwFileName;
					String throwClassName = report.crashInfo.throwClassName;
					String throwMethodName = report.crashInfo.throwMethodName;
					String throwLineNumber = report.crashInfo.throwLineNumber + "";
					String exceptionMessage = report.crashInfo.exceptionMessage;
					String exceptionClassName = report.crashInfo.exceptionClassName;
					String stackTrace = report.crashInfo.stackTrace;
					if (throwFileName != null && !("".equals(throwFileName))) {
						builder.append("throwFileName=" + throwFileName);
						builder.append("\r\n");
					}
					if (throwClassName != null && !("".equals(throwClassName))) {
						builder.append("throwClassName=" + throwClassName);
						builder.append("\r\n");
					}
					if (throwMethodName != null && !("".equals(throwMethodName))) {
						builder.append("throwMethodName=" + throwMethodName);
						builder.append("\r\n");
					}
					if (throwLineNumber != null && !("".equals(throwLineNumber))) {
						builder.append("throwLineNumber=" + throwLineNumber);
						builder.append("\r\n");
					}
					if (exceptionMessage != null && !("".equals(exceptionMessage))) {
						builder.append("exceptionMessage=" + exceptionMessage);
						builder.append("\r\n");
					}
					if (exceptionClassName != null && !("".equals(exceptionClassName))) {
						builder.append("exceptionClassName=" + exceptionClassName);
						builder.append("\r\n");
					}
					if (stackTrace != null && !("".equals(stackTrace))) {
						builder.append("stackTrace=");
						builder.append("\r\n" + stackTrace);
					}
					/*
					 * Log.d(TAG,"throwFileName=" + throwFileName);
					 * Log.d(TAG,"throwClassName=" + throwClassName);
					 * Log.d(TAG,"throwMethodName=" + throwMethodName);
					 * Log.d(TAG,"throwLineNumber=" + throwLineNumber);
					 * Log.d(TAG,"exceptionMessage=" + exceptionMessage);
					 * Log.d(TAG,"exceptionClassName=" + exceptionClassName);
					 * Log.d(TAG,"stackTrace=" + stackTrace);
					 */
					break;
				case ApplicationErrorReport.TYPE_ANR:
					String activity = report.anrInfo.activity;
					String cause = report.anrInfo.cause;
					String info = report.anrInfo.info;
					if (activity != null && !("".equals(activity))) {
						builder.append("activity=" + activity);
						builder.append("\r\n");
					}
					if (cause != null && !("".equals(cause))) {
						builder.append("cause=" + cause);
						builder.append("\r\n");
					}
					if (info != null && !("".equals(info))) {
						builder.append("info=" + info);
					}
					/*
					 * Log.d(TAG,"activity=" + report.anrInfo.activity);
					 * Log.d(TAG,"cause=" + report.anrInfo.cause);
					 * Log.d(TAG,"info=" + report.anrInfo.info);
					 */
					break;
				case ApplicationErrorReport.TYPE_BATTERY:
					String durationMicros = report.batteryInfo.durationMicros + "";
					String usagePercent = report.batteryInfo.usagePercent + "";
					String checkinDetails = report.batteryInfo.checkinDetails;
					String usageDetails = report.batteryInfo.usageDetails;
					if (durationMicros != null && !("".equals(durationMicros))) {
						builder.append("durationMicros=" + durationMicros);
						builder.append("\r\n");
					}
					if (usagePercent != null && !("".equals(usagePercent))) {
						builder.append("usagePercent=" + usagePercent);
						builder.append("\r\n");
					}
					if (checkinDetails != null && !("".equals(checkinDetails))) {
						builder.append("checkinDetails=" + checkinDetails);
						builder.append("\r\n");
					}
					if (usageDetails != null && !("".equals(usageDetails))) {
						builder.append("usageDetails=" + usageDetails);
					}
					/*
					 * Log.d(TAG,"durationMicros=" +
					 * report.batteryInfo.durationMicros);
					 * Log.d(TAG,"usagePercent=" +
					 * report.batteryInfo.usagePercent);
					 * Log.d(TAG,"checkinDetails=" +
					 * report.batteryInfo.checkinDetails);
					 * Log.d(TAG,"usageDetails=" +
					 * report.batteryInfo.usageDetails);
					 */
					break;
				case ApplicationErrorReport.TYPE_RUNNING_SERVICE:
					String durationMillis = report.runningServiceInfo.durationMillis + "";
					String serviceDetails = report.runningServiceInfo.serviceDetails;
					if (durationMillis != null && !("".equals(durationMillis))) {
						builder.append("durationMillis=" + durationMillis);
						builder.append("\r\n");
					}
					if (serviceDetails != null && !("".equals(serviceDetails))) {
						builder.append("serviceDetails=" + serviceDetails);
					}
					/*
					 * Log.d(TAG,"durationMillis=" +
					 * report.runningServiceInfo.durationMillis);
					 * Log.d(TAG,"serviceDetails=" +
					 * report.runningServiceInfo.serviceDetails);
					 */
					break;
				case ApplicationErrorReport.TYPE_NONE:
				default:
					break;
				}
				mLogFile = FileUtils.newFile(Configure.DIR_FEEDBACK, mTitle + ".log");
				try {
					FileUtils.writeStringToFile(builder.toString(), mLogFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Message msg = mHandler.obtainMessage();
				msg.what = 0;
				msg.sendToTarget();
			}
		}.start();

	}

	private void createProgressDialog() {
		mProgressDialog = ProgressDialog.show(this, "", getString(R.string.now_commit));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_commit:
			if (!HttpUtil.checkNetworkState(FeedBackEditActivity.this)) {
				Toast.makeText(FeedBackEditActivity.this, R.string.network_error, 0).show();
				return;
			}
			UserAccount userAccount = new UserAccount(this);
			mCredential = userAccount.getUserCredential();
			if (mCredential == null) {
				if (!Utils.isExistsProcess(this)) {
					Toast.makeText(FeedBackEditActivity.this, R.string.not_found_account, 0).show();
					return;
				}
				try {
					Intent intent = new Intent("com.rlk.mi.ACCOUNT");
					intent.putExtra("isFinish", true);
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(FeedBackEditActivity.this, R.string.not_found_account, 0).show();
					return;
				}
			} else {
				if (checkContetnValid()) {
					new CommitResultTask().execute();
				}
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Adapter adapter = parent.getAdapter();
		String path = (String) adapter.getItem(position);
		if (path.contains(Configure.ADD_PICTURE_DEFAULT)) {
			if (adapter.getCount() > PHOTO_COUNT) {
				Toast.makeText(this, R.string.limit_picture, 0).show();
				// Toast.makeText(FeedBackEditActivity.this,
				// R.string.add_picture_repeat, 0).show();
				return;
			}
			Intent intent = new Intent(FeedBackEditActivity.this, PhotoAlbumActivity.class);
			intent.putExtra("LeftCount", PHOTO_COUNT - adapter.getCount());
			startActivity(intent);
//			 Intent intent = new Intent(Intent.ACTION_PICK, null);
//			 intent.get
//			 intent.setDataAndType(MediaStore.Images.Media.TITLE,
//			 "image/*");
//			 startActivityForResult(intent, REQUEST_CODE_GETIMAGE_BYSDCARD);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		ArrayList<String> paths = intent.getStringArrayListExtra("paths");
		if (mGridImageAdapter.getCount() + paths.size() <= PHOTO_COUNT) {
			paths.add(Configure.ADD_PICTURE_DEFAULT);
		}
		mGridImageAdapter.addAll(paths);
	}

	public String selectImage(Intent data) {
		Uri selectedImage = data.getData();
		if (selectedImage != null) {
			String uriStr = selectedImage.toString();
			String path = uriStr.substring(10, uriStr.length());
			if (path.startsWith("com.sec.android.gallery3d")) {
				return null;
			}
		}
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;
	}

	public boolean analysisResult(String result) {
		if (result == null) {
			Toast.makeText(this, R.string.discoon_server, 0).show();
			return false;
		}
		try {
			JSONObject object = new JSONObject(result);
			int status = object.getInt("status");
			switch (status) {
			case 0:
				Toast.makeText(this, R.string.user_not_exists, 0).show();
				break;
			case 1:
				return true;
			case 13:
				Toast.makeText(this, R.string.save_error, 0).show();
				break;
			case 14:
				Toast.makeText(this, R.string.user_not_validate, 0).show();
				break;
			case 15:
				Toast.makeText(this, R.string.param_error, 0).show();
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean checkContetnValid() {
		String body = mEt_problem.getText().toString().trim();
		if ("".equals(body)) {
			Toast.makeText(this, R.string.feed_content_none, 0).show();
			return false;
		}
		if (TextUtil.length(body) < 7) {
			Toast.makeText(this, R.string.feed_back_limit, 0).show();
			return false;
		}
		return true;
	}

	@SuppressLint("DefaultLocale")
	public Map<String, String> getParams() {
		Map<String, String> parms = new HashMap<String, String>();
		parms.put(Configure.PARMA_USERID, mCredential.mUserId);
		parms.put(Configure.PARMA_TOKEN, mCredential.mToken);
		parms.put(Configure.PARMA_CATALOGID, mCatalogId + "");
		parms.put(Configure.PARMA_ITEMID, "-1");
		parms.put(Configure.PARMA_EMAIL, mCredential.mEmail);
		parms.put(Configure.PARMA_FEEDBACK, mEt_problem.getText().toString().trim());

		if (mCatalogId == Configure.APP) {
			parms.put(Configure.PARMA_TITLE, mPackageName);
		} else if (mCatalogId == Configure.LOG_CATLOGID && mReport != null) {
			parms.put(Configure.PARMA_TITLE, mReport.processName);
		}

		if (mReport != null) {
			parms.put(Configure.PARMA_ERROR_TYPE, mReport.type + "");
		}

		parms.put(Configure.PARMA_LAN, TextUtil.getLocalLanguage(FeedBackEditActivity.this));
		parms.put(Configure.PARMA_VERSIONNUMBER, PhoneDeviceInfo.sVersionNumber);
		parms.put(Configure.PARMA_MODEN, PhoneDeviceInfo.sModel);
		parms.put(Configure.PARMA_BRAND, PhoneDeviceInfo.sBrand);
		parms.put(Configure.PARMA_FBSORTID, mFbsortId + "");
		parms.put(Configure.PARMA_XUIVERSION, PhoneDeviceInfo.sXuiVersion);

		if (mEt_phoneNumber != null) {
			String phoneNumber = mEt_phoneNumber.getText().toString().trim();
			if (phoneNumber != null && !("".equals(phoneNumber))) {
				String countryCode = ContantUtil.getCountryCode(FeedBackEditActivity.this);
				if (countryCode == null) {
					parms.put(Configure.PARMA_PHONE_NUMBER, phoneNumber);
				} else {
					parms.put(Configure.PARMA_PHONE_NUMBER, "+" + countryCode + phoneNumber);
				}
			}
		}

		return parms;
	}

	public Map<String, String> getReplytParams() {
		Map<String, String> parms = new HashMap<String, String>();
		parms.put("userId", mCredential.mUserId);
		parms.put("feedbackId", mFeedbackId);
		parms.put("content", mEt_problem.getText().toString().trim());
		return parms;
	}

	public ArrayList<String> getPhotoList() {
		ArrayList<String> files = getOriginFiles();
		return getCompressFile(files);
	}

	public ArrayList<String> getCompressFile(ArrayList<String> files) {
		ArrayList<String> filesdes = null;
		File appDataFile = new File(Configure.DIR_FEEDBACK);
		if (!appDataFile.exists()) {
			appDataFile.mkdir();
		}
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int reqWidth = metric.widthPixels;
		int reqHeight = metric.heightPixels;
		ImageUtils.getCompressImage(appDataFile, files, reqWidth, reqHeight);
		File[] listFiles = appDataFile.listFiles();
		if (listFiles == null) {
			return filesdes;
		}
		if (listFiles.length > 0) {
			filesdes = new ArrayList<String>();
			for (int i = 0; i < listFiles.length; i++) {
				filesdes.add(listFiles[i].getAbsolutePath());
			}
		}
		return filesdes;
	}

	public void deleteFile() {
		File appDataFile = new File(Configure.DIR_FEEDBACK);
		if (!appDataFile.exists()) {
			return;
		}
		File[] listFiles = appDataFile.listFiles();
		if (listFiles.length > 0) {
			for (int i = 0; i < listFiles.length; i++) {
				File file = listFiles[i];
				if (file.exists() && file.isFile()) {
					file.delete();
				}
			}
		}
		if (mLogFile != null) {
			FileUtils.deleteFile(Configure.DIR_FEEDBACK);
		}
	}

	class CommitResultTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			createProgressDialog();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				String result = null;
				if (mFbsortId == Configure.REPLY) {
					result = HttpUtil.postRequest(Configure.REPLY_URL, getReplytParams(), getPhotoList(), null);
				} else {
					result = HttpUtil.postRequest(Configure.FEEDBACK_URL, getParams(), getPhotoList(), mLogFile);
				}
				deleteFile();
				return result;
			} catch (Exception e) {
				mProgressDialog.dismiss();
				deleteFile();
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mProgressDialog.dismiss();
			if (analysisResult(result)) {
				Toast.makeText(FeedBackEditActivity.this, R.string.feed_ok, 0).show();
				mPreferencesHelper.putValue(Configure.KEY_PREFERENCE_IS_REFRESH, true);
				mApplication.finishActivities();
			}
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_BACK:
			return new AlertDialog.Builder(this).setTitle(getString(R.string.back_dialog_title))
					.setMessage(getString(R.string.back_dialog_msg))
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							mPreferencesHelper.putValue(Configure.KEY_PREFERENCE_IS_REFRESH, false);
							disMiss();
							FeedBackEditActivity.this.finish();
							pointToHide();
						}
					}).setNegativeButton(getString(R.string.no), null).create();
		}
		return null;
	}

	private void pointToHide() {
		((InputMethodManager) mEt_problem.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public ArrayList<String> getOriginFiles() {
		ArrayList<String> files = new ArrayList<String>();
		for (String path : mDataList) {
			if (!path.contains(Configure.ADD_PICTURE_DEFAULT)) {
				files.add(path);
			}
		}
		return files;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		String body = mEt_problem.getText().toString().trim();
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!("".equals(body)) || getOriginFiles().size() > 0) {
				showDialog(DIALOG_BACK);
			} else {
				mPreferencesHelper.putValue(Configure.KEY_PREFERENCE_IS_REFRESH, false);
				return super.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Countly.sharedInstance().onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Countly.sharedInstance().onStop();
	}

	class EditChangedListener implements TextWatcher {
		private CharSequence temp;
		private int editStart;
		private int editEnd;
		private final int charMaxNum = 300;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			temp = s;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mTv_text_limit.setText(s.length() + "/300" + getString(R.string.chartmater));
		}

		@Override
		public void afterTextChanged(Editable s) {
			editStart = mEt_problem.getSelectionStart();
			editEnd = mEt_problem.getSelectionEnd();
			if (temp.length() > charMaxNum) {
				CustomToast.showTextToast(R.string.input_text_limit, FeedBackEditActivity.this);
				s.delete(editStart - 1, editEnd);
				int tempSelection = editStart;
				mEt_problem.setText(s);
				mEt_problem.setSelection(tempSelection);
			}

		}
	};

	private void disMiss() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			dismissDialog(DIALOG_BACK);
		}
	}
}
