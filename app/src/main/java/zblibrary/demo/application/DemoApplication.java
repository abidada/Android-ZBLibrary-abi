/*Copyright ©2015 TommyLemon(https://github.com/TommyLemon)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package zblibrary.demo.application;

import android.content.Intent;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.WebView;

import zblibrary.demo.manager.DataManager;
import zblibrary.demo.model.User;
import zblibrary.demo.util.X5ProcessInitService;
import zuo.biao.library.base.BaseApplication;
import zuo.biao.library.util.StringUtil;

/**Application
 * @author Lemon
 */
public class DemoApplication extends BaseApplication {
	private static final String TAG = "DemoApplication";

	private static DemoApplication context;
	public static DemoApplication getInstance() {
		return context;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		/* [new] 独立Web进程演示 */
		if (!startX5WebProcessPreinitService()) {
			return;
		}

		QbSdk.setDownloadWithoutWifi(true);

		QbSdk.setCoreMinVersion(QbSdk.CORE_VER_ENABLE_202112);

		/* SDK内核初始化周期回调，包括 下载、安装、加载 */
		QbSdk.setTbsListener(new TbsListener() {

			/**
			 * @param stateCode 用户可处理错误码请参考{@link com.tencent.smtt.sdk.TbsCommonCode}
			 */
			@Override
			public void onDownloadFinish(int stateCode) {
				Log.i(TAG, "onDownloadFinished: " + stateCode);
			}

			/**
			 * @param stateCode 用户可处理错误码请参考{@link com.tencent.smtt.sdk.TbsCommonCode}
			 */
			@Override
			public void onInstallFinish(int stateCode) {
				Log.i(TAG, "onInstallFinished: " + stateCode);
			}

			/**
			 * 首次安装应用，会触发内核下载，此时会有内核下载的进度回调。
			 * @param progress 0 - 100
			 */
			@Override
			public void onDownloadProgress(int progress) {
				Log.i(TAG, "Core Downloading: " + progress);
			}
		});

		/* 此过程包括X5内核的下载、预初始化，接入方不需要接管处理x5的初始化流程，希望无感接入 */
		QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
			@Override
			public void onCoreInitFinished() {
				// 内核初始化完成，可能为系统内核，也可能为系统内核
			}

			/**
			 * 预初始化结束
			 * 由于X5内核体积较大，需要依赖wifi网络下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
			 * 内核下发请求发起有24小时间隔，卸载重装、调整系统时间24小时后都可重置
			 * 调试阶段建议通过 WebView 访问 debugtbs.qq.com -> 安装线上内核 解决
			 * @param isX5 是否使用X5内核
			 */
			@Override
			public void onViewInitFinished(boolean isX5) {
				Log.i(TAG, "onViewInitFinished: " + isX5);
				QbSdk.getX5CoreLoadHelp(context);
				// hint: you can use QbSdk.getX5CoreLoadHelp(context) anytime to get help.
			}
		});


	}

	/**
	 * 启动X5 独立Web进程的预加载服务。优点：
	 * 1、后台启动，用户无感进程切换
	 * 2、启动进程服务后，有X5内核时，X5预加载内核
	 * 3、Web进程Crash时，不会使得整个应用进程crash掉
	 * 4、隔离主进程的内存，降低网页导致的App OOM概率。
	 *
	 * 缺点：
	 * 进程的创建占用手机整体的内存，demo 约为 150 MB
	 */
	private boolean startX5WebProcessPreinitService() {
		String currentProcessName = QbSdk.getCurrentProcessName(this);
		// 设置多进程数据目录隔离，不设置的话系统内核多个进程使用WebView会crash，X5下可能ANR
		WebView.setDataDirectorySuffix(QbSdk.getCurrentProcessName(this));
		Log.i(TAG, currentProcessName);
		if (currentProcessName.equals(this.getPackageName())) {
			this.startService(new Intent(this, X5ProcessInitService.class));
			return true;
		}
		return false;
	}

	
	/**获取当前用户id
	 * @return
	 */
	public long getCurrentUserId() {
		currentUser = getCurrentUser();
		Log.d(TAG, "getCurrentUserId  currentUserId = " + (currentUser == null ? "null" : currentUser.getId()));
		return currentUser == null ? 0 : currentUser.getId();
	}
	/**获取当前用户phone
	 * @return
	 */
	public String getCurrentUserPhone() {
		currentUser = getCurrentUser();
		return currentUser == null ? null : currentUser.getPhone();
	}


	private static User currentUser = null;
	public User getCurrentUser() {
		if (currentUser == null) {
			currentUser = DataManager.getInstance().getCurrentUser();
		}
		return currentUser;
	}

	public void saveCurrentUser(User user) {
		if (user == null) {
			Log.e(TAG, "saveCurrentUser  currentUser == null >> return;");
			return;
		}
		if (user.getId() <= 0 && StringUtil.isNotEmpty(user.getName(), true) == false) {
			Log.e(TAG, "saveCurrentUser  user.getId() <= 0" +
					" && StringUtil.isNotEmpty(user.getName(), true) == false >> return;");
			return;
		}

		currentUser = user;
		DataManager.getInstance().saveCurrentUser(currentUser);
	}

	public void logout() {
		currentUser = null;
		DataManager.getInstance().saveCurrentUser(currentUser);
	}
	
	/**判断是否为当前用户
	 * @param userId
	 * @return
	 */
	public boolean isCurrentUser(long userId) {
		return DataManager.getInstance().isCurrentUser(userId);
	}

	public boolean isLoggedIn() {
		return getCurrentUserId() > 0;
	}



}
