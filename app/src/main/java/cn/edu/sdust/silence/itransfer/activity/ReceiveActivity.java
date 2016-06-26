package cn.edu.sdust.silence.itransfer.activity;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.handler.ReceiveActivityHandler;
import cn.edu.sdust.silence.itransfer.reciever.WifiP2PBroadcastReceiver;
import cn.edu.sdust.silence.itransfer.thread.ReceiveManager;
import cn.edu.sdust.silence.itransfer.thread.ReceiveManager2;
import cn.edu.sdust.silence.itransfer.ui.actionbutton.FloatingActionButton;
import cn.edu.sdust.silence.itransfer.ui.loading.RotateLoading;
import cn.edu.sdust.silence.itransfer.ui.progress.NumberProgressBar;
import cn.edu.sdust.silence.itransfer.ui.scan.been.Info;
import cn.edu.sdust.silence.itransfer.ui.scan.custom.RadarViewGroup;

/**
 * create by shifeiqi
 */
public class ReceiveActivity extends AppCompatActivity implements RadarViewGroup.IRadarClickListener {

    private RadarViewGroup radarViewGroup;
    private SparseArray<Info> mDatas = new SparseArray<>();


    //broadcastReceive
    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;

    //wifi p2p
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pInfo info;

    //this is a flag about connection
    private boolean isConnect = false;
    private boolean isConnectServer = false;
    private ReceiveActivityHandler handler;

    //设备列表
    private List<WifiP2pDevice> peers = new ArrayList();

    //view
    private NumberProgressBar progress;
    private RotateLoading loading;
    private TextView tv_point;
    private View layout_point_container;

    private FloatingActionButton receiveFormComputer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initView();
        initIntentFilter();
        initWifiP2p();
        DiscoverPeers();
    }


    private void initIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initWifiP2p() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, Looper.myLooper(), null);

        WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                peers.clear();
                peers.addAll(peerList.getDeviceList());

                mDatas.clear();
                for (int i = 0; i < peers.size(); i++) {
                    WifiP2pDevice d = peers.get(i);
                    Info info = new Info();
                    info.setName(d.deviceName);
                    info.setPortraitId(R.drawable.icon_receice);
                    info.setAge(((int) Math.random() * 25 + 16) + "岁");
                    info.setSex(true);
                    info.setDistance(Math.round((Math.random() * 10) * 100) / 100);
                    mDatas.put(i, info);
                }
                radarViewGroup.setDatas(mDatas);
            }
        };

        WifiP2pManager.ConnectionInfoListener cInfo = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo minfo) {

                tv_point.setText("连接成功，正在准备数据");
                info = minfo;
                if (info.groupFormed && info.isGroupOwner && !isConnectServer) {
                    ReceiveManager manager = new ReceiveManager(handler);
                    manager.start();
                    isConnectServer = true;

                    Log.i("xyz", "create receive manage sucess");
                } else if (info.groupFormed && !info.isGroupOwner && !isConnectServer) {
                    ReceiveManager2 manager = new ReceiveManager2(handler, info.groupOwnerAddress.getHostAddress());
                    manager.start();
                    isConnectServer = true;
                    Log.i("xyz", "create receive manage sucess");
                }
            }
        };

        mReceiver = new WifiP2PBroadcastReceiver(mManager, mChannel, this, mPeerListListener, cInfo);
    }

    private void CancelConnect(final String address) {
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                CreateConnect(address);
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    private void CreateConnect(String address) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.groupOwnerIntent = 0;
        config.wps.setup = WpsInfo.PBC;

        Log.i("xyz", "other address : " + address);
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                if (info != null) {
                    tv_point.setText("连接成功，正在准备数据");
                    Log.d("mac connect", "sucess");
                    isConnect = true;
                    Log.d("xyz", "isGroupOwner " + info.isGroupOwner + "");
                    Log.d("xyz", "groupOwner ip " + info.groupOwnerAddress.getHostAddress());
                }
            }

            @Override
            public void onFailure(int reason) {
                isConnect = false;
                Log.d("mac connect", "fail");
            }
        });
    }

    private void initView() {
        radarViewGroup = (RadarViewGroup) findViewById(R.id.radar);
        radarViewGroup.setiRadarClickListener(this);
        progress = (NumberProgressBar) findViewById(R.id.progress);
        progress.setProgressTextColor(Color.WHITE);
        progress.setReachedBarColor(Color.WHITE);
        progress.setUnreachedBarColor(Color.GRAY);
        progress.setProgressTextSize(40f);

        loading = (RotateLoading) findViewById(R.id.loading);
        tv_point = (TextView) findViewById(R.id.tv_point);
        layout_point_container = findViewById(R.id.layout_point_container);
        layout_point_container.setVisibility(View.VISIBLE);
        loading.setLoadingColor(Color.WHITE);
        loading.start();
        tv_point.setText("点击对方头像进行文件接受");
        progress.setMax(100);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定要取消接收文件吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        handler = new ReceiveActivityHandler(ReceiveActivity.this);

        receiveFormComputer = (FloatingActionButton) findViewById(R.id.action_to_computer);
        receiveFormComputer.setIcon(R.drawable.icon_pc);
        receiveFormComputer.setColorNormal(getResources().getColor(R.color.button_normal));
        receiveFormComputer.setColorPressed(getResources().getColor(R.color.button_pressed));
        receiveFormComputer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveActivity.this);
                builder.setTitle("请选择文件接收模式");
                LayoutInflater inflater = LayoutInflater.from(ReceiveActivity.this);
                View view = inflater.inflate(R.layout.chose_to_computer_dialog, null);
                view.findViewById(R.id.have).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ReceiveActivity.this, CaptureActivity.class);
                        intent.putExtra("type", CaptureActivity.TYPE_INTENT_RECEIVE);
                        startActivity(intent);
                        finish();
                    }
                });

                view.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ReceiveActivity.this, FtpManagerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setView(view);
                builder.show();
            }
        });
    }

    /**
     * 开启发现节点
     */
    private void DiscoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }


    @Override
    public void onRadarItemClick(final int position) {
        if(!isConnect){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("确定要从 " + mDatas.get(position).getName() + " 接受文件吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    tv_point.setText("正在连接");
                    if (info == null)
                        CreateConnect((peers.get(position)).deviceAddress);
                    else
                        Toast.makeText(ReceiveActivity.this, "设备已连接,正在启用接受文件", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }else{
            Toast.makeText(ReceiveActivity.this,"已经连接至某台设备",Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(mReceiver, mFilter);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mManager.cancelConnect(mChannel, null);
        mManager.removeGroup(mChannel, null);
        super.onDestroy();
    }


    /**
     * 设置进度
     *
     * @param p
     */
    public void setProcess(int p) {
        layout_point_container.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progress.setProgress(p);
        if (p >= 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("文件接收成功！");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("确定要取消接受文件吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        return super.onKeyDown(keyCode, event);
    }
}
