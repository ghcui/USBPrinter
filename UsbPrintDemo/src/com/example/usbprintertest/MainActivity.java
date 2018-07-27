package com.example.usbprintertest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import com.printsdk.cmd.PrintCmd; 
import com.printsdk.usbsdk.UsbDriver;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("SimpleDateFormat") 
@SuppressWarnings("unused")
public class MainActivity extends Activity{
	private static final int FILE_SELECT_CODE = 0;  
	private Context mContext;
	final int SERIAL_BAUDRATE = UsbDriver.BAUD115200;
	UsbDriver mUsbDriver; 
	UsbDevice mUsbDev1;		//打印机1
	UsbDevice mUsbDev2;		//打印机2
	// Control definition 控件定义
	private Button mPrintSelfPage,mPrinterInfo,mPrintTest,mPrintTicket,mClear;
	private Button mBmpLoad,mBmpPrint,mClearPath,mAddBmpFilePath,mAddImgFilePath;
	private EditText etWrite,editRecDisp,mBmpPath_et,mTestTimes,mImgPath_et;
	private CheckBox IsAddLoadBmpPath;
	// 代表性的支持安卓USB口打印机QrCode函数调用：MS-D347、MS-D245（N58V）、T500II
	private Button D347QrBtn,D245QrBtn,T500IIQrBtn; 
	private Button print1DBarBtn;
	private Button printImgfileBtn,printConverImgBtn,printSeatBtn;
	private static ImageView printImg;
	private EditText HTColumn1,HTColumn2,HTColumn3; // 水平制表输入内容与列信息
	private TextView mHTSeatStr; // 水平制表结果显示
	// Common variables 常用变量全局
	private int rotate = 0;       // 默认为:0, 0 正常、1 90度旋转 
	private int align = 0;        // 默认为:1, 0 靠左、1  居中、2:靠右
	private int underLine = 0;    // 默认为:0, 0 取消、   1 下划1、 2 下划2 
	private int linespace = 40;   // 默认40, 常用：30 40 50 60
	private int cutter = 0;       // 默认0，  0 全切、1 半切
	static int Number = 1000;
	private int QrSize = 1;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 国际化标志时间格式类
	SimpleDateFormat m_sdfDate = new SimpleDateFormat("HH:mm:ss ");     // 国际化标志时间格式类
	private String title = "", strData = "", num = "",codeStr = "";
	
	private static final String ACTION_USB_PERMISSION =  "com.usb.sample.USB_PERMISSION";
	private UsbManager mUsbManager;
	private UsbDevice m_Device;
	private boolean shareFlag = false;
	private int clickFlag = 1; // 1：BMP；0：IMG
	private final static int PID11 = 8211;
    private final static int PID13 = 8213;
    private final static int PID15 = 8215;
    private final static int VENDORID = 1305;
    private int densityValue = 80; // 默认：80,浓度范围：70-200
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        init();// 初始化
    }
    /**
     * 初始化
     */
    private void init() {
    	findView();
    	getUsbDriverService();
//    	getUsbDriver();
    	setListener();
    	getMsgByLanguage();
	}
	// 绑定控件
	private void findView() {
		// 20170518 路径选择监听及输入框控件Add
		mAddBmpFilePath = (Button) findViewById(R.id.SelectBmpFile);
		mBmpPath_et = (EditText) findViewById(R.id.Nvbmp_path_et);
		mClearPath = (Button) findViewById(R.id.Clear_Path_Btn);
		mAddImgFilePath = (Button) findViewById(R.id.SelectImgFile);
		mImgPath_et = (EditText) findViewById(R.id.Img_path_et);
		// 二维码内容 + 一维码类型选择输入框
		etWrite = (EditText) findViewById(R.id.InputContent_et);
		mTestTimes =  (EditText) findViewById(R.id.TestTimes_et);
		mPrintSelfPage = (Button) findViewById(R.id.PrintSeflPage_btn);
		mPrinterInfo = (Button) findViewById(R.id.PrinterInfo_btn);
		mPrintTest = (Button) findViewById(R.id.PrintTest_btn);
		mPrintTicket = (Button) findViewById(R.id.PrintTicket_btn);
		editRecDisp = (EditText) findViewById(R.id.Get_State_et); // 打印机状态显示框、打印机信息显示框
		mClear = (Button) findViewById(R.id.Clear_btn);           // 打印机状态清除按钮
		QrSize = Integer.valueOf(mTestTimes.getText().toString().trim());
		etWrite.setText(Constant.WebAddress);
		// 20160826 Add
		mBmpLoad = (Button) findViewById(R.id.Load_nvbmp_btn);
		mBmpPrint = (Button) findViewById(R.id.Print_nvbmp_btn);
		IsAddLoadBmpPath = (CheckBox) findViewById(R.id.Is_AddLoadBmpPath);
		IsAddLoadBmpPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){ 
					mBmpLoad.setVisibility(View.VISIBLE);
					IsAddLoadBmpPath.setText(getString(R.string.Close));
                }else{ 
                	mBmpLoad.setVisibility(View.INVISIBLE);
					IsAddLoadBmpPath.setText(getString(R.string.Open));
                } 
			}
		});
		D347QrBtn = (Button)findViewById(R.id.D347QrBtn);
		D245QrBtn = (Button)findViewById(R.id.D245QrBtn);
		T500IIQrBtn = (Button)findViewById(R.id.T500IIQrBtn);
		print1DBarBtn =  (Button)findViewById(R.id.Print_1DBar_Btn);
		printImgfileBtn = (Button)findViewById(R.id.Print_Imgfile_Btn);
		printConverImgBtn = (Button)findViewById(R.id.Print_ConverImg_Btn);
		// 水平制表
		HTColumn1 =  (EditText)findViewById(R.id.SetColumn1_et);
		HTColumn2 =  (EditText)findViewById(R.id.SetColumn2_et);
		HTColumn3 =  (EditText)findViewById(R.id.SetColumn3_et);
		printSeatBtn = (Button)findViewById(R.id.Print_Seat_Btn);
		mHTSeatStr = (TextView)findViewById(R.id.HTSeat_tv);
	}
	
	
	private void getUsbDriverService(){
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mUsbDriver = new UsbDriver(mUsbManager, this);
		PendingIntent permissionIntent1 = PendingIntent.getBroadcast(this, 0,
				new Intent(ACTION_USB_PERMISSION), 0);
		mUsbDriver.setPermissionIntent(permissionIntent1);
		
		// Broadcast listen for new devices
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		this.registerReceiver(mUsbReceiver, filter);
	}

	// 设置监听
	private void setListener() {
		mPrintSelfPage.setOnClickListener(new PrintClickListener());
		mPrinterInfo.setOnClickListener(new PrintClickListener());
		mPrintTest.setOnClickListener(new PrintClickListener());
		mPrintTicket.setOnClickListener(new PrintClickListener());
		mClear.setOnClickListener(new PrintClickListener());
		mBmpLoad.setOnClickListener(new PrintClickListener());
		mBmpPrint.setOnClickListener(new PrintClickListener());
		// 打印机各型号QrCode二维码
		D347QrBtn.setOnClickListener(new PrintClickListener());
		D245QrBtn.setOnClickListener(new PrintClickListener());
		T500IIQrBtn.setOnClickListener(new PrintClickListener());
		// 打印机各型号一维码各种类型
		print1DBarBtn.setOnClickListener(new PrintClickListener());
		printImgfileBtn.setOnClickListener(new PrintClickListener());
		printConverImgBtn.setOnClickListener(new PrintClickListener());
		printSeatBtn.setOnClickListener(new PrintClickListener());
		mAddBmpFilePath.setOnClickListener(new BmpBrowerClickListener());
		mAddImgFilePath.setOnClickListener(new BmpBrowerClickListener());
		mClearPath.setOnClickListener(new BmpBrowerClickListener());
	}
	
	// Get UsbDriver(UsbManager) service
	private boolean printConnStatus() {

		boolean blnRtn = false;
		try {
			if (!mUsbDriver.isConnected()) {
//				UsbManager m_UsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
				// USB线已经连接
				for (UsbDevice device : mUsbManager.getDeviceList().values()) {
					if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
							|| (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
							|| (device.getProductId() == PID15 && device.getVendorId() == VENDORID)) {
						if (!mUsbManager.hasPermission(device)) {
							break;
						}
						blnRtn = mUsbDriver.usbAttached(device);
						if (blnRtn == false) {
							break;
						}
						blnRtn = mUsbDriver.openUsbDevice(device);
						// 打开设备
						if (blnRtn) {
							if (device.getProductId() == PID11) {
								mUsbDev1 = device;
							} else {
								mUsbDev2 = device;
							}
							setClean();// 清理缓存，初始化
							T.showShort(this, getString(R.string.USB_Driver_Success));
							break;
						} else {
							T.showShort(this, getString(R.string.USB_Driver_Failed));
							break;
						}
					}
				}
			} else {
				blnRtn = true;
			}
		} catch (Exception e) {
			T.showShort(this, e.getMessage());
		}

		return blnRtn;
	}
	
    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
 	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
 				if(mUsbDriver.usbAttached(intent)) 	
 				{
 					UsbDevice device = (UsbDevice) intent
	 						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
							|| (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
							|| (device.getProductId() == PID15 && device.getVendorId() == VENDORID))
					{
						if(mUsbDriver.openUsbDevice(device))
						{
		 					if(device.getProductId()==PID11)
		 						mUsbDev1 = device;
		 					else
		 						mUsbDev2 = device;
	 					 }
					}
 				}
 			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = (UsbDevice) intent
 						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
						|| (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
						|| (device.getProductId() == PID15 && device.getVendorId() == VENDORID)) 
				{
	 				mUsbDriver.closeUsbDevice(device);
					if(device.getProductId()==PID11)
						mUsbDev1 = null;
					else
						mUsbDev2 = null;
				}
			} else if (ACTION_USB_PERMISSION.equals(action)) {
	             synchronized (this) 
	             {
	                 UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	                 if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) 
	                 {
						if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
								|| (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
								|| (device.getProductId() == PID15 && device.getVendorId() == VENDORID)) 
						{ 	                	 
 		 				 if(mUsbDriver.openUsbDevice(device))
 		 				 {
 		 					if(device.getProductId()==PID11)
 		 						mUsbDev1 = device;
 		 					else
 		 						mUsbDev2 = device;
 		 				 }
						}
	                 }
	                 else {
	                	 T.showShort(MainActivity.this, "permission denied for device");
	                     //Log.d(TAG, "permission denied for device " + device);
	                 }
	             }
	         }  
 		}
 	};
 	class BmpBrowerClickListener implements OnClickListener{

		@Override
		public void onClick(View view) {
			try {
				switch (view.getId()) {
				case R.id.SelectBmpFile:
					selectBmpFile();
					break;
				case R.id.SelectImgFile:
					selectImgFile();
					break;
				case R.id.Clear_Path_Btn:
					clearAllPath();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
 	}
 	// 选择bmp图片文件
	private void selectBmpFile() {
		clickFlag = 1;
		showFileChooser();
		String bmpPath = mBmpPath_et.getText().toString().trim();
		Utils.putValue(MainActivity.this, "path", bmpPath);
	}
	// 选择img图片文件
	private void selectImgFile() {
		clickFlag = 0;
		showFileChooser();
	}
	// 清除输入框获取路径
	private void clearAllPath(){
		mBmpPath_et.setText("");
		mImgPath_et.setText("");
	}
 	
	class PrintClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			QrSize = Integer.valueOf(mTestTimes.getText().toString().trim());
			if(!printConnStatus()){
				return;
			}
			try {
				switch (view.getId()) {
				case R.id.PrintSeflPage_btn:
					printSelfPageTest();
					break;
				case R.id.PrinterInfo_btn:
					getPrinterInfo();
					break;
				case R.id.PrintTest_btn:
					int status = getPrinterStatus2(mUsbDev1);
					if(status != 3){ 
						getPrintTestData(mUsbDev1);
					}else{
						T.showShort(MainActivity.this, "打印机状态值2：" + String.valueOf(status));
					}
					break;
				case R.id.PrintTicket_btn:
					getPrintTicketData(mUsbDev1);
					break;
				case R.id.Clear_btn:        // 清除状态按钮
					editRecDisp.setText("");
					break;
				case R.id.Load_nvbmp_btn:   // 下载指定路径的位图保存到打印机
					if(downloadNvBmp())
						T.showShort(MainActivity.this, getString(R.string.Download_bmp_prompt));
					break;
				case R.id.Print_nvbmp_btn:    // 打印位图
					setPrintNvBmp();	
					break;
				case R.id.Print_Imgfile_Btn:  // 打印其他格式图片（jpg/png/bmp等）
					printDiskImgFile();
					break;
				case R.id.Print_ConverImg_Btn:// 转换后的图片打印（jpg/png/bmp等）
					printConverImg();
					break;
				case R.id.Print_Seat_Btn:    // 打印水平制表
					printSeat(mUsbDev1);
					break;
				case R.id.D347QrBtn:       // D347QrCode
					printD347QrCode();	
					break;
				case R.id.D245QrBtn:       // D245QrCode
					printD245QrCode();
					break;
				case R.id.T500IIQrBtn:     // T500IIQrCode
					printT500IIQrCode();	
					break;
				case R.id.Print_1DBar_Btn:  // Print1DBar
					print1DBarByType(QrSize);
					break;
				default:
					break;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// 打印机信息【指定下载位图的索引】
	String printerInfo = "Get Printer Info failed!";
	private void getPrinterInfo() {
		String[] iFstypeNums = null;
		iFstypeNums = new String[] { 
				getString(R.string.iFstype_1).toString(),
				getString(R.string.iFstype_2).toString(),
				getString(R.string.iFstype_3).toString(),
				getString(R.string.iFstype_4).toString(),
				getString(R.string.iFstype_5).toString(),
				getString(R.string.iFstype_6).toString() }; // 对齐方式数组
		AlertDialog.Builder b = new Builder(this);
		b.setTitle(getString(R.string.PrinterInfo_btn));
		b.setSingleChoiceItems(iFstypeNums, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String title = "";
						switch (which) {
						case 0:
							title = getString(R.string.iFstype_1).toString() + ":";
							printerInfo = title + checkPrinterInfo(1); 
							break;
						case 1:
							title = getString(R.string.iFstype_2).toString() + ":";
							printerInfo = title + checkPrinterInfo(2); 
							break;
						case 2:
							title = getString(R.string.iFstype_3).toString() + ":";
							printerInfo = title + checkPrinterInfo(3); 
							break;
						case 3:
							title = getString(R.string.iFstype_4).toString() + ":";
							printerInfo = title + checkPrinterInfo(4); 
							break;
						case 4:
							title = getString(R.string.iFstype_5).toString() + ":";
							printerInfo = title + checkPrinterInfo(5); 
							break;
						case 5:
							title = getString(R.string.iFstype_6).toString() + ":";
							printerInfo = title + checkPrinterInfo(6); 
							break;
						default:
							break;
						}
						editRecDisp.setText(printerInfo);
						dialog.dismiss();
					}
				});
		b.show();
	}
	
	/**
	 * 打印机信息解析
	 * 参数  iFstype： 1.打印头型号ID,2.类型ID,3.固件版本,4.生产厂商信息,5.打印机型号,6.支持的中文编码格式
	 */
	private String checkPrinterInfo(int iFstype) {
		String iRet = "0";
		byte[] bRead1 = new byte[50];
		byte[] bWrite1 = PrintCmd.GetProductinformation(iFstype);
		if (mUsbDriver.read(bRead1, bWrite1) > 0) {
			iRet = PrintCmd.CheckProductinformation(bRead1);
		}
		if (iRet != "0")
			return iRet;
		return iRet;
	}
	
	// 水平制表符行列数据转换
	ArrayList<String> list1 = null;
	ArrayList<String> list2 = null;
	ArrayList<String> list3 = null;
	ArrayList<String> list4 = null;
	String[] str1 = {"语文","数学","英语","物理","化学","政治"}; 
	String[] str2 = {"88","100","96","100","95","65"}; 
	String[] str3 = {"A-","A+","A+","A+","A-","B-"}; 
	String[] str4 = {"陈老师","周老师","吴老师","张老师","冯老师","李老师"}; 
	private ArrayList<String> getSeatColHtData(String[] str) {
		ArrayList<String> list = new ArrayList<String>();
		if (str != null) {
			for (int i = 0; i < 6; i++) {
				list.add(str[i]);
			}
		}
		return list;
	}
	String HTSeatStr1 = "";
	String HTSeatStr2 = "";
	String HTSeatStr3 = "";
	String HTSeatStr4 = "";
	String ht1,ht2,ht3,ht4 = "";
	private void setTransData(String col1,String col2,String col3,UsbDevice usbDev){
		list1 = getSeatColHtData(str1);
		list2 = getSeatColHtData(str2);
		list3 = getSeatColHtData(str3);
		list4 = getSeatColHtData(str4);
		String Col1 = Utils.intToHexString(Integer.valueOf(col1), 1)+ " ";// 转换第1列
		String Col2 = Utils.intToHexString(Integer.valueOf(col2), 1)+ " ";// 转换第2列
		String Col3 = Utils.intToHexString(Integer.valueOf(col3), 1)+ " ";// 转换第3列
		for(int i = 0;i<6;i++){
			HTSeatStr1 = list1.get(i);
			ht1 = Utils.stringTo16Hex(HTSeatStr1);
			HTSeatStr2 = list2.get(i);
			ht2 = Utils.stringTo16Hex(HTSeatStr2);
			HTSeatStr3 = list3.get(i);
			ht3 = Utils.stringTo16Hex(HTSeatStr3);
			HTSeatStr4 = list4.get(i);
			ht4 = Utils.stringTo16Hex(HTSeatStr4);
			mHTSeatStr.setText(Col1 + Col2 + Col3 + "00 " + ht1 + "09 " +
								ht2 + "09 " + ht3 + "09 " + ht4 + "0A 0A");
			String etstring = mHTSeatStr.getText().toString();
			byte[] seat = Utils.hexStr2Bytesnoenter(etstring);
			if (etstring != null && !"".equals(etstring)) {
				mUsbDriver.write(PrintCmd.SetAlignment(align));
				mUsbDriver.write(PrintCmd.SetLinespace(linespace));
				mUsbDriver.write(PrintCmd.SetHTseat(seat, seat.length),
						seat.length,usbDev);
				mUsbDriver.write(PrintCmd.PrintFeedline(0),usbDev);      // 走纸换行
			}
		}
	}
	
	// 打印水平制表
	private void printSeat(UsbDevice usbDev) {
		// 获取输入数据
		String col1 = HTColumn1.getText().toString().trim();
		String col2 = HTColumn2.getText().toString().trim();
		String col3 = HTColumn3.getText().toString().trim();
		if(Integer.valueOf(col1)>Integer.valueOf(col2)){
			T.showShort(MainActivity.this, "第1列值不能大于第2列，请重新输入！");
			return;
		}
		if(Integer.valueOf(col2)>Integer.valueOf(col3)){
			T.showShort(MainActivity.this, "第2列值不能大于第3列，请重新输入！");
			return;
		}
		setTransData(col1,col2,col3,usbDev);
		setFeedCut(cutter,usbDev);
	}
	
	// 打印图片文件（png/jpg/bmp）
	private void printDiskImgFile(){
		String imgPath = mImgPath_et.getText().toString().trim();
		if("".endsWith(imgPath)){
 			showMessage(getString(R.string.The_path_cannot_be_empty));
 			return;
 		}
		Bitmap bm = Utils.getBitmapData(imgPath);
		if(bm == null)
 			return;
		int[] data = Utils.getPixelsByBitmap(bm);
		mUsbDriver.write(PrintCmd.PrintDiskImagefile(data,bm.getWidth(),bm.getHeight()));
		setFeedCut(cutter);
	}
	// 转换后的图片打印（png/jpg/bmp）20171018
	private void printConverImg() {
		String imgPath = mImgPath_et.getText().toString().trim();
		if("".endsWith(imgPath)){
			showMessage(getString(R.string.The_path_cannot_be_empty));
 			return;
 		}
		Bitmap inputBmp = Utils.getBitmapData(imgPath);
		if(inputBmp == null)
 			return;
		Bitmap bm = Utils.getSinglePic(inputBmp);
		int[] data = Utils.getPixelsByBitmap(bm);
		mUsbDriver.write(PrintCmd.PrintDiskImagefile(data, bm.getWidth(), bm.getHeight()));
		setFeedCut(cutter);
	}
	
	// 通过类型打印一维码
	private void print1DBarByType(int iBarType) {
		mUsbDriver.write(PrintCmd.SetAlignment(align));
		// CODE39:14809966841053 测试数据  k >= 1 
		// 类型 0 （11 =< k <= 12） 、 1(11 =< k <= 12) 、2(12 =< k <= 13) 、7(12 =< k <= 13) 字符个数 k = 12均适用 
		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, iBarType, "012345678900")); 
		System.out.println("一维码类型：" + iBarType);
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "XSC0330000000062323"));// * 10 CODE128 / *4 CODE39
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		mUsbDriver.write(PrintCmd.Print1Dbar(1, 100, 0, 2, 10, "XSC0330000000062323"));// * 10 CODE128 / *4 CODE39
		setFeedCut(cutter);
	}

	// 【1】MS-D347,13 52指令二维码接口，环绕模式1
	private void printD347QrCode() {
		getStrDataByLanguage();	
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 25, 6, 1));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));// 走纸换行
		setFeedCut(cutter);
	}

	// 【2】MS-D245|MS-N58V|MSP-100 二维码，左边距、size、环绕模式0
	private void printD245QrCode() {
		getStrDataByLanguage();	
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 12, 2, 0));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		setFeedCut(cutter);
	}

	// 【3】MS-532II+T500II二维码接口
	private void printT500IIQrCode() {
		getStrDataByLanguage();	
		mUsbDriver.write(PrintCmd.SetAlignment(0));
		mUsbDriver.write(PrintCmd.PrintQrcode("欢迎光临", 15, 4, 1));
		mUsbDriver.write(PrintCmd.PrintFeedline(2));
		mUsbDriver.write(PrintCmd.PrintQrcode("欢迎光临", 5, 4, 1));
		mUsbDriver.write(PrintCmd.PrintFeedline(2));
		mUsbDriver.write(PrintCmd.PrintQrCodeT500II(QrSize,Constant.WebAddress_zh));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));// 走纸换行
		setFeedCut(cutter);
	}
	
	// 打印位图【指定下载位图的索引】
	private void setPrintNvBmp() {
		String[] NvBmpNums = null;
		NvBmpNums = new String[] { getString(R.string.bmp_1).toString(),
				getString(R.string.bmp_2).toString(),
				getString(R.string.bmp_3).toString(),
				getString(R.string.bmp_4).toString(),
				getString(R.string.bmp_5).toString(),
				getString(R.string.bmp_6).toString() }; // 对齐方式数组
		AlertDialog.Builder b = new Builder(this);
		b.setTitle(getString(R.string.Print_Bmp_btn));
		b.setSingleChoiceItems(NvBmpNums, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							getPrintNvBmp(1);
							break;
						case 1:
							getPrintNvBmp(2);
							break;
						case 2:
							getPrintNvBmp(3);
							break;
						case 3:
							getPrintNvBmp(4);
							break;
						case 4:
							getPrintNvBmp(5);
							break;
						case 5:
							getPrintNvBmp(6);
							break;
						default:
							break;
						}
					}
				});
		b.show();
	}
	
	// 打印下载位图
	private void getPrintNvBmp(int iNums) {
		byte[] etBytes = PrintCmd.PrintNvbmp(iNums, 48);
		mUsbDriver.write(etBytes);
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		setFeedCut(cutter);
	}
	// 设置下载位图
	private boolean downloadNvBmp() {
		String loadPath = mBmpPath_et.getText().toString().trim();
		if(!"".equalsIgnoreCase(loadPath)){
			int inums = Utils.Count(loadPath, ";");
			byte[] bValue = PrintCmd.SetNvbmp(inums,loadPath);
			if(bValue != null){
				mUsbDriver.write(bValue, bValue.length);
				return true;
			}
		}
		return false;
	}
	
	// 显示文件选择路径
	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*.bin");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent, "Select a BIN file"),FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Please install a File Manager.",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				// Get the Uri of the selected file
				Uri uri = data.getData();
				String path = Utils.getPath(MainActivity.this, uri);
				if(clickFlag==1){
					String sharePath = Utils.getValue(MainActivity.this, "path", "").toString().trim();
					if(!"".equalsIgnoreCase(sharePath)){
						mBmpPath_et.setText(sharePath + path + ";");
					}else{
						mBmpPath_et.setText(path + ";");
					}
				} else {
					mImgPath_et.setText(path);
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 1.测试文本打印
	 * @throws UnsupportedEncodingException 
	 */
	private void getPrintTestData(UsbDevice usbDev) throws UnsupportedEncodingException{
		int iStatus = getPrinterStatus(usbDev);
		if(checkStatus(iStatus)!=0)
			return; 
		String etstring = Constant.TESTDATA_CN;
		if (etstring != null && !"".equals(etstring)) {
			getCommonSettings(usbDev); // 常规设置
			byte[] etBytes = PrintCmd.PrintString(etstring, 0);
			mUsbDriver.write(etBytes, etBytes.length,usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(align),usbDev);
			mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "AB1-CD2-EF3"),usbDev);// * 10 CODE128 / *4 CODE39
//			setFeedCut(cutter,usbDev);
			mUsbDriver.write(PrintCmd.PrintMarkcutpaper(0));
		}
	}
	
	/**
	 * 2.小票打印
	 */
	private void getPrintTicketData(UsbDevice usbDev) {
		getStrDataByLanguage();		
		int iStatus = getPrinterStatus(usbDev);
		if(checkStatus(iStatus)!=0)
			return; 
		try{
//			mUsbDriver.write(PrintCmd.SetClean(),usbDev);  // 初始化，清理缓存
			// 小票标题
			mUsbDriver.write(PrintCmd.SetBold(0),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(1),usbDev);
			mUsbDriver.write(PrintCmd.SetSizetext(1, 1),usbDev);
			mUsbDriver.write(PrintCmd.PrintString(title, 0),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			mUsbDriver.write(PrintCmd.SetSizetext(0, 0),usbDev);
			// 小票号码
			mUsbDriver.write(PrintCmd.SetBold(1),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(1),usbDev);
			mUsbDriver.write(PrintCmd.SetSizetext(1, 1),usbDev);
			mUsbDriver.write(PrintCmd.PrintString(num, 0),usbDev);
			mUsbDriver.write(PrintCmd.SetBold(0),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			mUsbDriver.write(PrintCmd.SetSizetext(0, 0),usbDev);
			// 小票主要内容
			mUsbDriver.write(PrintCmd.PrintString(strData, 0),usbDev); 
			mUsbDriver.write(PrintCmd.PrintFeedline(2),usbDev); // 打印走纸2行
			// 二维码
			mUsbDriver.write(PrintCmd.SetAlignment(1),usbDev);   
			mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 25, 6, 1),usbDev);           // 【1】MS-D347,13 52指令二维码接口，环绕模式1
//			mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 12, 2, 0),usbDev);           // 【2】MS-D245,MSP-100二维码，左边距、size、环绕模式0
//			mUsbDriver.write(PrintCmd.PrintQrCodeT500II(5,Constant.WebAddress_zh),usbDev);// 【3】MS-532II+T500II二维码接口
			mUsbDriver.write(PrintCmd.PrintFeedline(2),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			// 日期时间
			mUsbDriver.write(PrintCmd.SetAlignment(2),usbDev);
			mUsbDriver.write(PrintCmd.PrintString(sdf.format(new Date()).toString()
					+ "\n\n", 1),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			// 一维条码
			mUsbDriver.write(PrintCmd.SetAlignment(1),usbDev);
			mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "A12345678Z"),usbDev);// 一维条码打印
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			// 走纸换行、切纸、清理缓存
			setFeedCut(cutter,usbDev);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	
	// 根据系统语言获取测试文本
	private void getStrDataByLanguage(){
		codeStr = etWrite.getText().toString().trim();
		if("".equalsIgnoreCase(codeStr))
			codeStr = Constant.WebAddress;
		if(Utils.isZh(this)){
			title = Constant.TITLE_CN;
			strData = Constant.STRDATA_CN;
		}else {
			title = Constant.TITLE_US;
			strData = Constant.STRDATA_US;
		}
		num = String.valueOf(Number) + "\n\n";
		Number++;
	}
	// 走纸换行、切纸、清理缓存
	private void setFeedCut(int iMode,UsbDevice usbDev) {
		mUsbDriver.write(PrintCmd.PrintFeedline(5),usbDev);      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode),usbDev);  // 切纸类型
	}
	// 走纸换行、切纸、清理缓存
	private void setFeedCut(int iMode) {
		mUsbDriver.write(PrintCmd.PrintFeedline(5));      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode));  // 切纸类型
	}
	// 常规设置
	private void getCommonSettings(UsbDevice usbDev){
		mUsbDriver.write(PrintCmd.SetAlignment(align),usbDev);    // 对齐方式
		mUsbDriver.write(PrintCmd.SetRotate(rotate),usbDev);      // 字体旋转
		mUsbDriver.write(PrintCmd.SetUnderline(underLine),usbDev);// 下划线
		mUsbDriver.write(PrintCmd.SetLinespace(linespace),usbDev);// 行间距
	}

	// 常规设置
	private void setClean() {
		mUsbDriver.write(PrintCmd.SetClean());// 清除缓存,初始化
	}
	
 	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.font_0:// 文字方向
			rotate = 0;
			break;
		case R.id.font_1:
			rotate = 1;
			break;
		case R.id.align_0:// 对齐方式
			align = 0;
			break;
		case R.id.align_1:
			align = 1;
			break;
		case R.id.align_2:
			align = 2;
			break;
		case R.id.under_0:// 下划线
			underLine = 0;
			break;
		case R.id.under_1:
			underLine = 1;
			break;
		case R.id.under_2:
			underLine = 2;
			break;
		case R.id.linespace_30:// 下划线
			linespace = 30;
			break;
		case R.id.linespace_40:
			linespace = 40;
			break;
		case R.id.linespace_50:
			linespace = 50;
			break;
		case R.id.linespace_60:
			linespace = 60;
			break;
		case R.id.linespace_70:
			linespace = 70;
			break;
		case R.id.cutter_0:// 切刀
			cutter = 0;
			break;
		case R.id.cutter_1:
			cutter = 1;
			break;
		case R.id.system_0:// 退出系统
			System.exit(0);
			break;
		case R.id.system_1:// 系统版本
			T.showShort(MainActivity.this, "当前版本USB2.2");
			break;
		case R.id.system_2:// 系统版本
			mUsbDriver.write(openBlackMark(0));
			break;
		case R.id.system_3:// 系统版本
			mUsbDriver.write(openBlackMark(1));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// -------------------显示消息-----------------------
	private void showMessage(String sMsg) {
		StringBuilder sbMsg = new StringBuilder();
		sbMsg.append(editRecDisp.getText());
		sbMsg.append(m_sdfDate.format(new Date()));
		sbMsg.append(sMsg);
		sbMsg.append("\r\n");
		editRecDisp.setText(sbMsg);
		editRecDisp.setSelection(sbMsg.length(), sbMsg.length());
	}
	
	// 检测打印机状态
	private int getPrinterStatus(UsbDevice usbDev) {
		int iRet = -1;

		byte[] bRead1 = new byte[1];
		byte[] bWrite1 = PrintCmd.GetStatus1();		
		if(mUsbDriver.read(bRead1,bWrite1,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus1(bRead1[0]);
		}
		
		if(iRet!=0)
			return iRet;
		
		byte[] bRead2 = new byte[1];
		byte[] bWrite2 = PrintCmd.GetStatus2();		
		if(mUsbDriver.read(bRead2,bWrite2,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus2(bRead2[0]);
		}

		if(iRet!=0)
			return iRet;
		
		byte[] bRead3 = new byte[1];
		byte[] bWrite3 = PrintCmd.GetStatus3();		
		if(mUsbDriver.read(bRead3,bWrite3,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus3(bRead3[0]);
		}

		if(iRet!=0)
			return iRet;
		
		byte[] bRead4 = new byte[1];
		byte[] bWrite4 = PrintCmd.GetStatus4();		
		if(mUsbDriver.read(bRead4,bWrite4,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus4(bRead4[0]);
		}

		
		return iRet;
	}
	
 	
	private int checkStatus(int iStatus)
	{ 
		int iRet = -1;

		StringBuilder sMsg = new StringBuilder();
		 
  
		//0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配 
		//3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
		switch (iStatus) {
			case 0: 
				sMsg.append(normal);       // 正常
				iRet = 0;
				break;
			case 8:
				sMsg.append(paperWillExh); // 纸将尽
				iRet = 0;
				break;			
			case 3:
				sMsg.append(printerHeadOpen); //打印头打开 
				break;   
			case 4:
				sMsg.append(cutterNotReset);      
				break;
			case 5:
				sMsg.append(printHeadOverheated);      
				break;
			case 6:
				sMsg.append(blackMarkError);      
				break;			
			case 7:
				sMsg.append(paperExh);     // 纸尽==缺纸
				break;
			case 1:
				sMsg.append(notConnectedOrNotPopwer);
				break;
			default:
				sMsg.append(abnormal);     // 异常
				break;
		} 
		showMessage(sMsg.toString());
		return iRet;
		
	}
 
	public synchronized void sleep(long msec) {
		try {
			wait(msec);
		} catch (InterruptedException e) {
		}
	}
	// 通过系统语言判断Message显示
	String receive = "", state = ""; // 接收提示、状态类型
	String normal = "",notConnectedOrNotPopwer = "",notMatch = "",
			printerHeadOpen = "", cutterNotReset = "", printHeadOverheated = "", 
			blackMarkError = "",paperExh = "",paperWillExh = "",abnormal = "";
	private void getMsgByLanguage() {
		if (Utils.isZh(this)) {
			receive = Constant.Receive_CN;
			state = Constant.State_CN;
			normal = Constant.Normal_CN;
			notConnectedOrNotPopwer = Constant.NoConnectedOrNoOnPower_CN;
			notMatch = Constant.PrinterAndLibraryNotMatch_CN;
			printerHeadOpen = Constant.PrintHeadOpen_CN;
			cutterNotReset = Constant.CutterNotReset_CN;
			printHeadOverheated = Constant.PrintHeadOverheated_CN;
			blackMarkError = Constant.BlackMarkError_CN;
			paperExh = Constant.PaperExhausted_CN;
			paperWillExh = Constant.PaperWillExhausted_CN;
			abnormal = Constant.Abnormal_CN;
		} else {
			receive = Constant.Receive_US;
			state = Constant.State_US;
			normal = Constant.Normal_US;
			notConnectedOrNotPopwer = Constant.NoConnectedOrNoOnPower_US;
			notMatch = Constant.PrinterAndLibraryNotMatch_US;
			printerHeadOpen = Constant.PrintHeadOpen_US;
			cutterNotReset = Constant.CutterNotReset_US;
			printHeadOverheated = Constant.PrintHeadOverheated_US;
			blackMarkError = Constant.BlackMarkError_US;
			paperExh = Constant.PaperExhausted_US;
			paperWillExh = Constant.PaperWillExhausted_US;
			abnormal = Constant.Abnormal_US;
		}
	}
	
	/**
	 * 自检页打印
	 */
	private void printSelfPageTest() {
		try {
			byte[] spCmd = PrintCmd.PrintSelfcheck();
			if (spCmd != null) {
				mUsbDriver.write(spCmd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 检测打印机状态
	private int getPrinterStatus2(UsbDevice usbDev) {
		int iRet = -1;
		
		byte[] bRead5 = new byte[1];
		byte[] bWrite5 = sendCommand();		
		if(mUsbDriver.read(bRead5,bWrite5,usbDev)>0)
		{
			T.showShort(MainActivity.this, "返回值："+String.valueOf(bRead5[0]));
			iRet = checkStatus(bRead5[0]);
		}
		if(iRet == 0 || iRet > 0)
			return iRet;
		return iRet;
	}
	// 发送打印完成指令 1D 72 01
	private byte[] sendCommand() {
		byte[] b_send = new byte[3];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x1D;
		b_send[(iIndex++)] = 0x72;
		b_send[(iIndex++)] = 0x01;
		return b_send;
	}
	// 解析
	public static int checkStatus(byte bRecv) {
		if ((bRecv & 0x00) == 0x00) 
			return 0;  // 打印纸充足
		if ((bRecv & 0x03) == 0x03) 
			return 1;  // 打印纸将尽
		if ((bRecv & 0x60) != 0x60) 
			return 2;  // 打印机非空闲状态
		return 3;      // 空闲状态
	}
	
	/**
	 * 开启黑标功能指令【MS-530i控制板指令】  "0x13 0x74 0x22 0x23"，设置黑标功能有效  / A3无效
	 * @param bmType  0  黑标有效, 1 黑标无效
	 * @return byte[]
	 */
	private byte[] openBlackMark(int bmType) {
		byte[] b_send = new byte[4];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x13;
		b_send[(iIndex++)] = 0x74;
		b_send[(iIndex++)] = 0x22;
		if(bmType == 0)
			b_send[(iIndex++)] = 0x23;
		else
			b_send[(iIndex++)] = (byte) 0xA3;
		return b_send;
	}
	
//	private void getPrintDessity(UsbDevice usbDev){
//		densityValue = Integer.valueOf(mTestTimes.getText().toString().trim());
//		if(densityValue >= 70 && densityValue <= 200){
//			mUsbDriver.write(setPrintConcentration(densityValue), usbDev);
//		}else{
//			T.showShort(MainActivity.this, "浓度值超出范围！正常范围为：70-200");
//		}
//	}
//	/*设置打印的浓度
//	指令十六进制 12 7e  N(70 <= N <= 200，十进制数)
//	N的值在这个范围内才有效，其他值无效*/
//	/**
//	 * 设置打印浓度  mTestTimes 输入框
//	 * @param concentrationValue
//	 * @return
//	 */
//	private byte[] setPrintConcentration(int concentrationValue) {
//		byte[] b_send = new byte[3];
//		int iIndex = 0;
//		b_send[(iIndex++)] = 0x12;
//		b_send[(iIndex++)] = 0x7e;
//		b_send[(iIndex++)] = (byte) concentrationValue;
//		return b_send;
//	}
	
}
