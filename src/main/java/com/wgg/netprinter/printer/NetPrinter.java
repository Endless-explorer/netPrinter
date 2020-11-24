package com.wgg.netprinter.printer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.TaskCallback;
import net.posprinter.service.PosprinterService;

/***
 create by johnseg
 contact:320838955@qq.com
 time:2020/11/24
 ***/
public class NetPrinter {
    private static  NetPrinter instance;
    private Context context;
    public static IMyBinder netService;
    private boolean isConnectService=false;

    private String ip;//网络打印机的ip
    private final int port=9100;//固定打印机端口号

    public static  final int conn_success=10001;
    public static  final int conn_failure=10002;
    public static  final int disConn_success=10003;
    public static  final int disConn_failure=10004;
    public static  final int print_success=10005;
    public static  final int print_failure=10006;

    private boolean isConnectPrinter=false;



    public static NetPrinter getInstance(){
        if(instance==null){
            synchronized (NetPrinter.class){
                if(instance==null){
                    instance=new NetPrinter();
                }
            }
        }
        return instance;
    }


    public boolean isConnectPrinter() {
        return isConnectPrinter;
    }

    public void  initService(Context context){
        Log.d("jmy", "正在连接打印机服务");
        this.context=context.getApplicationContext();
        Intent intent=new Intent(this.context, PosprinterService.class);
        context.bindService(intent,connection,Context.BIND_AUTO_CREATE);
    }


    ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            netService= (IMyBinder) service;
            isConnectService=true;
            Log.d("jmy", "打印机服务连接成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnectService=false;
            Log.d("jmy", "打印机服务连接失败");
            initService(context);

        }
    };

    public void connect(String ip, final OnPrinterCall onPrinterCall){
        this.ip=ip;
        if(netService!=null&&isConnectService&&this.ip!=null){
            Log.d("jmy", "正在连接"+ip+":"+port+"的网络打印机");
            netService.ConnectNetPort(ip, port, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    isConnectPrinter=true;
                    if(onPrinterCall!=null){
                        onPrinterCall.onCallBack(conn_success,"连接网络打印机成功!");
                    }
                }

                @Override
                public void OnFailed() {
                    isConnectPrinter=false;
                    if(onPrinterCall!=null){
                        onPrinterCall.onCallBack(conn_failure,"连接网络打印机失败!");
                    }
                }
            });
        }
    }


    public  void disconnect(final OnPrinterCall onPrinterCall){
        if(netService!=null&&isConnectService&&isConnectPrinter){
            Log.d("jmy", "正在断开连接"+ip+":"+port+"的网络打印机");
            netService.DisconnetNetPort( new TaskCallback() {
                @Override
                public void OnSucceed() {
                    isConnectPrinter=false;
                    if(onPrinterCall!=null){
                        onPrinterCall.onCallBack(disConn_success,"断开网络打印机成功!");
                    }
                }

                @Override
                public void OnFailed() {
                    if(onPrinterCall!=null){
                        onPrinterCall.onCallBack(disConn_failure,"断开网络打印机失败!");
                    }
                }
            });
        }
    }




    private void print(ProcessData processData,final OnPrinterCall onPrinterCall){
        if(netService!=null&&isConnectPrinter){
            netService.writeDataByUSB(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    if(onPrinterCall!=null){
                        onPrinterCall.onCallBack(print_success,"网络打印机打印成功!");
                    }
                }

                @Override
                public void OnFailed() {
                    if(onPrinterCall!=null){
                        onPrinterCall.onCallBack(print_failure,"网络打印机打印失败!");
                    }
                }
            }, processData);
        }
    }







    public interface OnPrinterCall{
        void onCallBack(int code,String msg);
    }

}
